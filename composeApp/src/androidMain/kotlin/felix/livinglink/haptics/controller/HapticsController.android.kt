package felix.livinglink.haptics.controller

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import felix.livinglink.AppContext
import felix.livinglink.haptics.store.HapticsSettingsStore

actual class HapticsDefaultController actual constructor(
    hapticsSettingsStore: HapticsSettingsStore
) : HapticsController {

    private val hapticsSettingsStore = hapticsSettingsStore

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppContext.applicationContext.getSystemService<VibratorManager>()
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            AppContext.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun performSuccess() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else {
            vibrateFallback(duration = 40, amplitude = 180)
        }
    }

    override fun performError() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            )
        } else {
            vibrateFallback(
                pattern = longArrayOf(0, 100, 50, 100),
                amplitudes = intArrayOf(255, 0, 255, 0)
            )
        }
    }

    override fun performLightImpact() {
        if (hapticsSettingsStore.updates.value?.equals(HapticsSettingsStore.Options.OFF) == true) {
            return
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            )
        } else {
            vibrateFallback(duration = 20, amplitude = 100)
        }
    }

    private fun vibrateFallback(duration: Long, amplitude: Int) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }

    private fun vibrateFallback(pattern: LongArray, amplitudes: IntArray) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }
}