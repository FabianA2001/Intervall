package com.intervall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intervall.ui.theme.IntervallTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntervallTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        "Test", modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(this@MainActivity, Timer::class.java)
                            val zahlenListe = listOf(5, 6)
                            intent.putIntegerArrayListExtra(
                                "secondsList",
                                ArrayList(zahlenListe)
                            )
                            startActivity(intent)

                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Start Timer")
                    }
                }
            }
        }

    }


}