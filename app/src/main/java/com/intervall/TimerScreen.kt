package com.intervall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.intervall.Timer.TimerStateHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale


//A class to save and share the running state of the timer
class TimerViewModel(_secondsList: ArrayList<Int>) : ViewModel() {
    private val secondsList = _secondsList.ifEmpty { listOf(-1) }
    private val _isRunning = MutableStateFlow(true)
    val isRunning = _isRunning.asStateFlow()
    private val _indexSeconds = MutableStateFlow(0)
    val indexSeconds = _indexSeconds.asStateFlow()

    private val _seconds = MutableStateFlow(secondsList[_indexSeconds.value])
    val seconds = _seconds.asStateFlow()

    fun toggleRunning() {
        _isRunning.value = !_isRunning.value
    }

    fun nextSeconds() {
        _seconds.value = secondsList.getOrElse(++_indexSeconds.value) { -1 }
    }

    fun reduceSeconds(by: Int) {
        _seconds.value = (_seconds.value - by).coerceAtLeast(0)
    }

    fun getSecondsByIndex(index: Int): Int {
        return secondsList.getOrElse(index) { -1 }
    }
}

// Factory to pass arguments to the ViewModel
class TimerViewModelFactory(private val secondsList: ArrayList<Int>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(secondsList) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun TimerScreen(navController: NavController, secondsListString: String?) {
    val context = LocalContext.current

    // Parse den String zurück zu einer Liste
    val secondsList = (secondsListString?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?: emptyList()).toCollection(ArrayList())


    val viewModel: TimerViewModel = viewModel(
        factory = TimerViewModelFactory(secondsList)
    )

    TimerStateHolder.viewModel = viewModel
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Timer(viewModel)
        Button(
            onClick = {
                viewModel.toggleRunning()
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Text("Stop Timer")
        }
    }
}

@Composable
fun Timer(
    viewModel: TimerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val isRunning by viewModel.isRunning.collectAsState()
    val seconds by viewModel.seconds.collectAsState()
    val indexSeconds by viewModel.indexSeconds.collectAsState()

    // Timer countdown logic
    LaunchedEffect(isRunning, seconds) {
        while (isRunning && seconds > 0) {
            delay(1000L)
            viewModel.reduceSeconds(1)
            if (seconds <= 1) {
                viewModel.nextSeconds()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val mainText = if (seconds >= 0) formatTime(seconds) else "Fertig"
        Text(
            text = mainText,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        val nextSeconds = viewModel.getSecondsByIndex(indexSeconds + 1)
        if (nextSeconds >= 0) {
            Text(
                text = formatTime(nextSeconds),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
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
