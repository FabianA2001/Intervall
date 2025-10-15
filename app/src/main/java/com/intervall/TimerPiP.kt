package com.intervall

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.intervall.ui.theme.IntervallTheme


class TimerPiP : ComponentActivity() {
    // Receiver für die PiP-Action:
    // Toggle running state im ViewModel based on button
    class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            TimerStateHolder.viewModel?.toggleRunning()
        }
    }

    // einfache Singleton-Hilfe:
    object TimerStateHolder {
        var viewModel: TimerViewModel? = null
    }

    private val isPipSupported by lazy {
        packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    private fun buildPipParams(isRunning: Boolean): PictureInPictureParams {
        val iconRes =
            if (isRunning) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
        val title = if (isRunning) "Pause" else "Play"
        val description = title

        val toggleIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, MyReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return PictureInPictureParams.Builder()
            .setAspectRatio(Rational(4, 3))
            .setActions(
                listOf(
                    RemoteAction(
                        Icon.createWithResource(applicationContext, iconRes),
                        title,
                        description,
                        toggleIntent
                    )
                )
            )
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TimerViewModel = TimerStateHolder.viewModel ?: run {
            // Kein ViewModel verfügbar, Activity beenden
            finish()
            return
        }
        setContent {
            IntervallTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Timer(viewModel)
                    }
                }
            }
        }

        // init PiP with current state
        if (isPipSupported) {
            val initialParams = buildPipParams(true)
            enterPictureInPictureMode(initialParams)
        }

//        // update Icon in PiP when state changes
//        lifecycleScope.launch {
//            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
//                viewModel.isRunning.collect { running ->
//                    if (isPipSupported) {
//                        setPictureInPictureParams(buildPipParams(running))
//                    }
//                }
//            }
//        }
    }
}
