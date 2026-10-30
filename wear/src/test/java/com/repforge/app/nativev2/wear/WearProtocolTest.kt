package com.repforge.app.nativev2.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearProtocolTest {

    @Test
    fun `uncertain handover pauses timer and requires explicit ACK epoch increment`() {
        val initial = WearOwnershipState(
            workoutId = "w-100",
            owner = SessionOwner.PHONE,
            ownerEpoch = 2L,
            timerRunning = true
        )

        val pending = WearOwnershipProtocol.requestHandOver(initial, SessionOwner.WATCH)
        assertEquals(SessionOwner.PHONE, pending.owner)
        assertEquals(SessionOwner.WATCH, pending.pendingTargetOwner)
        assertFalse("Timer must pause while handover is unacknowledged", pending.timerRunning)

        // Reconnect before ACK must not auto-start parallel timer or change owner
        val reconnected = WearOwnershipProtocol.onReconnect(pending.copy(isOffline = true))
        assertFalse(reconnected.isOffline)
        assertFalse(reconnected.timerRunning)
        assertEquals(SessionOwner.PHONE, reconnected.owner)

        // Acknowledge handover with incremented epoch
        val acked = WearOwnershipProtocol.acknowledgeHandOver(reconnected, 3L)
        assertEquals(SessionOwner.WATCH, acked.owner)
        assertEquals(3L, acked.ownerEpoch)
        assertNull(acked.pendingTargetOwner)
    }

    @Test
    fun `touch lock requires continuous 3000ms hold and resets if released early`() {
        var state = WearTouchLock189.engageLock()
        assertTrue(state.isLocked)

        state = WearTouchLock189.onHoldStart(state, 1000L)
        state = WearTouchLock189.onHoldTick(state, 2500L)
        assertTrue(state.isLocked)
        assertEquals(0.5f, state.progressFraction, 0.01f)

        // Release at 1.5s -> resets progress, remains locked
        state = WearTouchLock189.onHoldRelease(state, 2500L)
        assertTrue(state.isLocked)
        assertEquals(0f, state.progressFraction, 0.001f)

        // Continuous 3.0s hold -> unlocks
        state = WearTouchLock189.onHoldStart(state, 5000L)
        state = WearTouchLock189.onHoldRelease(state, 8000L)
        assertFalse(state.isLocked)
        assertEquals(1f, state.progressFraction, 0.001f)
    }

    @Test
    fun `rotary editor adjusts weight in 0_5kg increments and clamps bounds`() {
        assertEquals(60.5, WearRotaryEditor212.adjustWeightKg(60.0, 12f), 0.001)
        assertEquals(59.5, WearRotaryEditor212.adjustWeightKg(60.0, -8f), 0.001)
        assertEquals(0.0, WearRotaryEditor212.adjustWeightKg(0.0, -10f), 0.001)
        assertEquals(11, WearRotaryEditor212.adjustReps(10, 5f))
    }
}
