package com.repforge.app.nativev2.wear

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class SessionOwner {
    PHONE,
    WATCH
}

data class WearOwnershipState(
    val workoutId: String,
    val owner: SessionOwner = SessionOwner.PHONE,
    val ownerEpoch: Long = 1L,
    val pendingTargetOwner: SessionOwner? = null,
    val isOffline: Boolean = false,
    val timerRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val lastAckEpoch: Long = 1L,
    val pendingEvents: List<String> = emptyList()
)

object WearOwnershipProtocol {
    fun requestHandOver(
        current: WearOwnershipState,
        targetOwner: SessionOwner
    ): WearOwnershipState {
        if (current.owner == targetOwner && current.pendingTargetOwner == null) return current
        return current.copy(
            pendingTargetOwner = targetOwner,
            timerRunning = false, // Uncertain handoff pauses active timer to prevent dual timers
            pendingEvents = current.pendingEvents + "HANDOVER_REQ:${targetOwner.name}:epoch=${current.ownerEpoch + 1}"
        )
    }

    fun acknowledgeHandOver(
        current: WearOwnershipState,
        acknowledgedEpoch: Long
    ): WearOwnershipState {
        val target = current.pendingTargetOwner ?: return current
        if (acknowledgedEpoch <= current.ownerEpoch) return current
        return current.copy(
            owner = target,
            ownerEpoch = acknowledgedEpoch,
            lastAckEpoch = acknowledgedEpoch,
            pendingTargetOwner = null,
            pendingEvents = current.pendingEvents + "HANDOVER_ACK:${target.name}:epoch=$acknowledgedEpoch"
        )
    }

    fun onReconnect(current: WearOwnershipState): WearOwnershipState {
        // Reconnecting never starts a parallel timer or silently flips owner without ACK
        return current.copy(
            isOffline = false,
            timerRunning = if (current.pendingTargetOwner != null) false else current.timerRunning
        )
    }
}

object WearTouchLock189 {
    const val REQUIRED_HOLD_MS = 3000L

    data class LockState(
        val isLocked: Boolean = false,
        val holdStartedAtMs: Long? = null,
        val progressFraction: Float = 0f
    )

    fun engageLock(): LockState = LockState(isLocked = true, holdStartedAtMs = null, progressFraction = 0f)

    fun onHoldStart(state: LockState, nowMs: Long): LockState {
        if (!state.isLocked) return state
        return state.copy(holdStartedAtMs = nowMs, progressFraction = 0f)
    }

    fun onHoldTick(state: LockState, nowMs: Long): LockState {
        if (!state.isLocked) return state
        val start = state.holdStartedAtMs ?: return state.copy(progressFraction = 0f)
        val elapsed = (nowMs - start).coerceAtLeast(0L)
        if (elapsed >= REQUIRED_HOLD_MS) {
            return LockState(isLocked = false, holdStartedAtMs = null, progressFraction = 1f)
        }
        return state.copy(progressFraction = (elapsed.toFloat() / REQUIRED_HOLD_MS.toFloat()).coerceIn(0f, 1f))
    }

    fun onHoldRelease(state: LockState, nowMs: Long): LockState {
        val ticked = onHoldTick(state, nowMs)
        return if (!ticked.isLocked) {
            ticked
        } else {
            // Interrupted hold resets progress to 0; must be a continuous 3-second hold
            state.copy(holdStartedAtMs = null, progressFraction = 0f)
        }
    }
}

object WearRotaryEditor212 {
    fun adjustWeightKg(currentKg: Double, scrollDelta: Float, stepKg: Double = 0.5): Double {
        if (scrollDelta == 0f) return currentKg
        val direction = if (scrollDelta > 0f) 1 else -1
        val next = currentKg + direction * stepKg
        val clamped = max(0.0, min(500.0, next))
        return (clamped * 10.0).roundToInt() / 10.0
    }

    fun adjustReps(currentReps: Int, scrollDelta: Float): Int {
        if (scrollDelta == 0f) return currentReps
        val direction = if (scrollDelta > 0f) 1 else -1
        return (currentReps + direction).coerceIn(0, 200)
    }
}
