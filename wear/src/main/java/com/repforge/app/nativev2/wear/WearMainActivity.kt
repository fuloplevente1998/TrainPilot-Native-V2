package com.repforge.app.nativev2.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearAppScreen()
            }
        }
    }
}

@Composable
fun WearAppScreen() {
    var ownership by remember {
        mutableStateOf(
            WearOwnershipState(
                workoutId = "wear-standalone-1",
                owner = SessionOwner.WATCH,
                ownerEpoch = 1L
            )
        )
    }
    var lockState by remember { mutableStateOf(WearTouchLock189.LockState()) }
    var weightKg by remember { mutableDoubleStateOf(60.0) }
    var reps by remember { mutableIntStateOf(10) }
    var setsCompleted by remember { mutableIntStateOf(0) }
    var restRemainingSec by remember { mutableIntStateOf(0) }
    var holdingUnlock by remember { mutableStateOf(false) }

    LaunchedEffect(holdingUnlock, lockState.isLocked) {
        if (holdingUnlock && lockState.isLocked) {
            val start = System.currentTimeMillis()
            lockState = WearTouchLock189.onHoldStart(lockState, start)
            while (holdingUnlock && lockState.isLocked) {
                delay(100L)
                lockState = WearTouchLock189.onHoldTick(lockState, System.currentTimeMillis())
            }
        }
    }

    LaunchedEffect(restRemainingSec, ownership.owner, ownership.pendingTargetOwner) {
        if (restRemainingSec > 0 && ownership.owner == SessionOwner.WATCH && ownership.pendingTargetOwner == null) {
            delay(1000L)
            restRemainingSec = (restRemainingSec - 1).coerceAtLeast(0)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1015))
            .onPreRotaryScrollEvent { event ->
                if (!lockState.isLocked && ownership.owner == SessionOwner.WATCH) {
                    weightKg = WearRotaryEditor212.adjustWeightKg(weightKg, event.verticalScrollPixels)
                    true
                } else {
                    false
                }
            },
        color = Color(0xFF0E1015)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TrainPilot Wear V2",
                    color = Color(0xFFF5B841),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (ownership.pendingTargetOwner != null) {
                        "Átadás függőben (${ownership.pendingTargetOwner?.name})"
                    } else {
                        "Tulajdonos: ${ownership.owner.name} (epoch ${ownership.ownerEpoch})"
                    },
                    color = Color(0xFF9AA4B2),
                    fontSize = 12.sp
                )

                if (lockState.isLocked) {
                    Text(
                        text = "Érintésvédelem aktív (${(lockState.progressFraction * 100).toInt()}%)",
                        color = Color(0xFFFFB74D),
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = {
                            holdingUnlock = !holdingUnlock
                            if (!holdingUnlock) {
                                lockState = WearTouchLock189.onHoldRelease(lockState, System.currentTimeMillis())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wear_unlock_hold_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5B841))
                    ) {
                        Text(
                            text = if (holdingUnlock) "Feloldás folyamatban (3 mp)..." else "3 mp folyamatos feloldás",
                            color = Color(0xFF0E1015),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "${weightKg} kg × $reps ism. • Kész: $setsCompleted",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (restRemainingSec > 0) {
                        Text(
                            text = "Pihenő: ${restRemainingSec} mp",
                            color = Color(0xFFF5B841),
                            fontSize = 14.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { weightKg = WearRotaryEditor212.adjustWeightKg(weightKg, -1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wear_weight_minus")
                        ) {
                            Text("-0.5 kg", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { weightKg = WearRotaryEditor212.adjustWeightKg(weightKg, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wear_weight_plus")
                        ) {
                            Text("+0.5 kg", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = {
                            if (ownership.owner == SessionOwner.WATCH && ownership.pendingTargetOwner == null) {
                                setsCompleted += 1
                                restRemainingSec = 90
                            }
                        },
                        enabled = ownership.owner == SessionOwner.WATCH && ownership.pendingTargetOwner == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("wear_record_set_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5B841), contentColor = Color(0xFF0E1015)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sorozat kész (OK)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { lockState = WearTouchLock189.engageLock() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wear_lock_button")
                        ) {
                            Text("Zárolás", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                ownership = if (ownership.pendingTargetOwner != null) {
                                    WearOwnershipProtocol.acknowledgeHandOver(ownership, ownership.ownerEpoch + 1)
                                } else {
                                    val nextOwner = if (ownership.owner == SessionOwner.WATCH) SessionOwner.PHONE else SessionOwner.WATCH
                                    WearOwnershipProtocol.requestHandOver(ownership, nextOwner)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wear_handover_button")
                        ) {
                            Text(
                                text = if (ownership.pendingTargetOwner != null) "ACK" else "Átadás",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
