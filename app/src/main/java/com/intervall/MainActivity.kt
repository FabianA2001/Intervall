package com.intervall

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_TOGGLE_TIMER = "com.intervall.ACTION_TOGGLE_TIMER"
        private var currentTimerViewModel: TimerViewModel? = null
        private var pipModeCallback: ((Boolean) -> Unit)? = null

        fun setTimerViewModel(viewModel: TimerViewModel?) {
            currentTimerViewModel = viewModel
        }

        fun getTimerViewModel(): TimerViewModel? = currentTimerViewModel

        fun setPipModeCallback(callback: ((Boolean) -> Unit)?) {
            pipModeCallback = callback
        }
    }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == ACTION_TOGGLE_TIMER) {
                currentTimerViewModel?.toggleRunning()
            }
        }
    }

    private val isPipSupported by lazy {
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Receiver registrieren
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                this,
                timerReceiver,
                IntentFilter(ACTION_TOGGLE_TIMER),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(timerReceiver, IntentFilter(ACTION_TOGGLE_TIMER))
        }

        setContent {
            MainScreen()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("HalloEnde")
        unregisterReceiver(timerReceiver)
        currentTimerViewModel = null
        pipModeCallback = null
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        println("ist in pip mode: $isInPictureInPictureMode")
        pipModeCallback?.invoke(isInPictureInPictureMode)
    }

    fun updatePipParams(isRunning: Boolean) {
        if (!isPipSupported) return

        val params = buildPipParams(isRunning)
        setPictureInPictureParams(params)
    }

    private fun buildPipParams(isRunning: Boolean): PictureInPictureParams {
        val iconRes = if (isRunning) {
            R.drawable.baseline_pause_24
        } else {
            R.drawable.baseline_play_arrow_24
        }
        val title = if (isRunning) "Pause" else "Play"

        val toggleIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_TOGGLE_TIMER),
            PendingIntent.FLAG_IMMUTABLE
        )

        val remoteAction = RemoteAction(
            Icon.createWithResource(this, iconRes),
            title,
            title,
            toggleIntent
        )

        return PictureInPictureParams.Builder()
            .setAspectRatio(Rational(4, 3))
            .setActions(listOf(remoteAction))
            .build()
    }

    fun enterPip() {
        if (isPipSupported) {
            val viewModel = currentTimerViewModel
            if (viewModel != null) {
                val params = buildPipParams(viewModel.isRunning.value)
                enterPictureInPictureMode(params)
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { Home(navController) }
        composable(
            route = "timer/{secondsList}",
            arguments = listOf(
                navArgument("secondsList") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val secondsListString = backStackEntry.arguments?.getString("secondsList")
            TimerScreen(navController, secondsListString)
        }
    }
}
