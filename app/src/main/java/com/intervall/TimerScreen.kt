package com.intervall

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TimerScreen(navController: NavController, secondsListString: String?) {
    val context = LocalContext.current

    // Parse den String zurück zu einer Liste
    val secondsList = secondsListString?.split(",")
        ?.mapNotNull { it.toIntOrNull() }
        ?: emptyList()

    Button(
        onClick = {
            val intent = Intent(context, Timer::class.java)
            intent.putIntegerArrayListExtra(
                "secondsList",
                ArrayList(secondsList)
            )
            context.startActivity(intent)
        },

        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text("Start Timer")
    }
}