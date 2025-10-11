package com.intervall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intervall.ui.theme.IntervallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntervallTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    TimeInput("Aufwärmen") { }
                    TimeInput("Intervall") { }
                    TimeInput("Pause") { }
                    AmountInput("Anzahl") { }
                    TimeInput("Auslaufen") { }
                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, Timer::class.java)
                            val zahlenListe =
                                listOf(600, 60, 120, 60, 120, 60, 120, 60, 120, 60, 120, 60, 600)
                            intent.putIntegerArrayListExtra(
                                "secondsList",
                                ArrayList(zahlenListe)
                            )
                            startActivity(intent)

                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Text("Start Timer")
                    }
                }
            }
        }

    }
}

@Composable
fun TimeInput(
    name: String = "TimeInput",
    onTimeChange: (totalSeconds: Int) -> Unit = {}
) {
    var minutes by remember { mutableStateOf("0") }
    var seconds by remember { mutableStateOf("0") }

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

@Composable
fun AmountInput(
    name: String = "TimeInput",
    onTimeChange: (totalSeconds: Int) -> Unit = {}
) {
    var amount by remember { mutableStateOf("0") }

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