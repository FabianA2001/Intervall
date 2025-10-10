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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.intervall.ui.theme.IntervallTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

//A class to save and share the running state of the timer
class TimerViewModel(initSeconds: Int = 0) : ViewModel() {
    private val _isRunning = MutableStateFlow(true)
    private val _seconds = MutableStateFlow(initSeconds)
    val isRunning = _isRunning.asStateFlow()
    val seconds = _seconds.asStateFlow()

    fun toggleRunning() {
        _isRunning.value = !_isRunning.value
    }

    fun reduceSeconds(by: Int) {
        _seconds.value = (_seconds.value - by).coerceAtLeast(0)
    }
}

// Factory zum Übergeben von Argumenten an das ViewModel
class TimerViewModelFactory(private val initSeconds: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(initSeconds) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class Timer : ComponentActivity() {
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
        // Init-Argument aus dem Intent (Fallback 0 Sekunden)
        val initSeconds = intent?.getIntExtra("timeInSeconds", 0) ?: 0
        val viewModel: TimerViewModel = ViewModelProvider(
            this,
            TimerViewModelFactory(initSeconds)
        )[TimerViewModel::class.java]
        TimerStateHolder.viewModel = viewModel
        setContent {
            IntervallTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(viewModel)
//                    intent.getIntExtra("timeInSeconds", -1)
                }
            }
        }

        // init PiP with current state
        if (isPipSupported) {
            val initialParams = buildPipParams(viewModel.isRunning.value)
            enterPictureInPictureMode(initialParams)
        }

        // update Icon in PiP when state changes
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.isRunning.collect { running ->
                    if (isPipSupported) {
                        setPictureInPictureParams(buildPipParams(running))
                    }
                }
            }
        }
    }
}

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val isRunning by viewModel.isRunning.collectAsState()
    val seconds by viewModel.seconds.collectAsState()

    // Timer countdown logic
    LaunchedEffect(isRunning, seconds) {
        while (isRunning && seconds > 0) {
            delay(1000L)
            viewModel.reduceSeconds(1)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = formatTime(seconds),
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }
}