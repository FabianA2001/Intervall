package com.intervall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intervall.ui.theme.IntervallTheme
import kotlinx.coroutines.launch


class TimeData {
    private var aufwaermen: Int = 0
    private var interval: Int = 0
    private var pause: Int = 0
    private var anzahl: Int = 0
    private var auslaufen: Int = 0

    fun setAufwaermen(seconds: Int) {
        aufwaermen = seconds
        return
    }

    fun setInterval(seconds: Int) {
        interval = seconds
    }

    fun setPause(seconds: Int) {
        pause = seconds
    }

    fun setAnzahl(amount: Int) {
        anzahl = amount
    }

    fun setAuslaufen(seconds: Int) {
        auslaufen = seconds
    }

    fun getSecondsList(): List<Int> {
        val list = mutableListOf<Int>()
        if (aufwaermen > 0) {
            list.add(aufwaermen)
        }
        for (i in 1..anzahl) {
            if (interval > 0) {
                list.add(interval)
            }
            if (pause > 0) {
                list.add(pause)
            }
        }
        if (auslaufen > 0) {
            list.add(auslaufen)
        }
        return list
    }
}


@Composable
fun Home(navController: NavController) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val aufwaermen by settingsRepository.aufwaermen.collectAsState(initial = 5)
    val interval by settingsRepository.interval.collectAsState(initial = 2)
    val pause by settingsRepository.pause.collectAsState(initial = 3)
    val anzahl by settingsRepository.anzahl.collectAsState(initial = 2)
    val auslaufen by settingsRepository.auslaufen.collectAsState(initial = 4)

    val timeData = TimeData()

    IntervallTheme {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            TimeInput("Aufwärmen", defaultSec = aufwaermen) {
                timeData.setAufwaermen(it)
                scope.launch { settingsRepository.saveAufwaermen(it) }
            }
            TimeInput("Intervall", defaultSec = interval) {
                timeData.setInterval(it)
                scope.launch { settingsRepository.saveInterval(it) }
            }
            TimeInput("Pause", defaultSec = pause) {
                timeData.setPause(it)
                scope.launch { settingsRepository.savePause(it) }
            }
            AmountInput("Anzahl", defaultAmount = anzahl) {
                timeData.setAnzahl(it)
                scope.launch { settingsRepository.saveAnzahl(it) }
            }
            TimeInput("Auslaufen", defaultSec = auslaufen) {
                timeData.setAuslaufen(it)
                scope.launch { settingsRepository.saveAuslaufen(it) }
            }
            Button(
                onClick = {
                    val secondsList = timeData.getSecondsList()
                    val listString = secondsList.joinToString(",")
                    navController.navigate("timer/$listString")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text("Starte Timer")
            }
        }
    }
}

@Composable
fun AmountInput(
    name: String = "TimeInput",
    defaultAmount: Int = 0,
    onTimeChange: (totalSeconds: Int) -> Unit = {}
) {
    var amount by remember { mutableStateOf(defaultAmount.toString()) }

    // Update amount when defaultAmount changes
    LaunchedEffect(defaultAmount) {
        amount = defaultAmount.toString()
    }

    // Gesamtzeit in Sekunden berechnen und zurückgeben
    LaunchedEffect(amount) {
        onTimeChange(amount.toIntOrNull() ?: 0)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { new: String ->
                if (new.length <= 3 && new.all { it.isDigit() }) {
                    amount = new
                }
            },
            label = { Text("Anzahl") },
            modifier = Modifier.width(100.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(23.dp))
    }
}

@Composable
fun TimeInput(
    name: String = "TimeInput",
    defaultSec: Int = 0,
    defaultMin: Int = 0,
    onTimeChange: (totalSeconds: Int) -> Unit = {}
) {
    var minutes by remember { mutableStateOf(defaultMin.toString()) }
    var seconds by remember { mutableStateOf(defaultSec.toString()) }

    // Update seconds when defaultSec changes
    LaunchedEffect(defaultSec) {
        val totalSeconds = defaultSec
        minutes = (totalSeconds / 60).toString()
        seconds = (totalSeconds % 60).toString()
    }

    // Gesamtzeit in Sekunden berechnen und zurückgeben
    LaunchedEffect(minutes, seconds) {
        val min = minutes.toIntOrNull() ?: 0
        val sec = seconds.toIntOrNull() ?: 0
        onTimeChange(min * 60 + sec)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minutes,
                onValueChange = { new: String ->
                    if (new.length <= 3 && new.all { it.isDigit() }) {
                        minutes = new
                    }
                },
                label = { Text("Minuten") },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text(":")

            OutlinedTextField(
                value = seconds,
                onValueChange = { new ->
                    if (new.length <= 2 && new.all { it.isDigit() }) {
                        val intVal = new.toIntOrNull() ?: 0
                        // Sekunden maximal 59
                        if (intVal in 0..59) seconds = new
                    }
                },
                label = { Text("Sekunden") },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(modifier = Modifier.height(23.dp))
    }
}