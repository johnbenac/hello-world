package com.example.hello_world

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(
    onSessionsClicked: () -> Unit,
    onConfigPacksClicked: () -> Unit
) {
    Column {
        Button(onClick = onSessionsClicked) {
            Text("Sessions")
        }
        Button(onClick = onConfigPacksClicked) {
            Text("Config Packs")
        }
    }
}