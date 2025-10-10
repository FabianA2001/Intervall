package com.intervall

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Rect
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.intervall.ui.theme.IntervallTheme
import kotlinx.coroutines.delay
import java.util.Locale

class Timer : ComponentActivity() {
    private val isPipSupported by lazy {
        packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    private var videoViewBounds = Rect()

    private fun updatedPipParams(): PictureInPictureParams? {
        return PictureInPictureParams.Builder()
            .setSourceRectHint(videoViewBounds)
            .setAspectRatio(Rational(4, 3))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntervallTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            videoViewBounds = it.boundsInWindow().toAndroidRect()
                        },
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(intent.getIntExtra("timeInSeconds", -1))
                }
            }
        }
        if (isPipSupported) {
            updatedPipParams()?.let { params ->
                enterPictureInPictureMode(params)
            }
        }
    }
}

@Composable
fun TimerScreen(paraTimeInSeconds: Int = 0) {
    var timeInSeconds by rememberSaveable { mutableStateOf(paraTimeInSeconds) }
    var isRunning by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            timeInSeconds--
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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