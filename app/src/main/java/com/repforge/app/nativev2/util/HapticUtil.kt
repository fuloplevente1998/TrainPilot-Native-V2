package com.repforge.app.nativev2.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberHapticTrigger(enabled: Boolean = true): () -> Unit {
    val view = LocalView.current
    return {
        if (enabled) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            } catch (e: Exception) {
                // Ignore if haptics unavailable
            }
        }
    }
}
