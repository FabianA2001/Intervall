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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
class TimerViewModel : ViewModel() {
    private val _isRunning = MutableStateFlow(true)
    val isRunning = _isRunning.asStateFlow()

    fun toggleRunning() {
        _isRunning.value = !_isRunning.value
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
        val viewModel: TimerViewModel = ViewModelProvider(this)[TimerViewModel::class.java]
        TimerStateHolder.viewModel = viewModel
        setContent {
            IntervallTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(intent.getIntExtra("timeInSeconds", -1), viewModel)
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
    paraTimeInSeconds: Int = 0,
    viewModel: TimerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    var timeInSeconds by rememberSaveable { mutableStateOf(paraTimeInSeconds) }
    val isRunning by viewModel.isRunning.collectAsState()

    // Timer countdown logic
    LaunchedEffect(isRunning) {
        while (isRunning && timeInSeconds > 0) {
            delay(1000L)
            timeInSeconds--
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = formatTime(timeInSeconds),
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