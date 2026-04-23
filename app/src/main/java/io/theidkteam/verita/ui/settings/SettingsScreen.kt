package io.theidkteam.verita.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import io.theidkteam.verita.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            ) 
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Text("Proxy Settings", style = MaterialTheme.typography.titleMedium)
                // ... (the rest of the proxy code is unchanged for brevity)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Proxy")
                    Switch(
                        checked = settingsManager.useProxy,
                        onCheckedChange = { settingsManager.useProxy = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Theme Color (RGB)", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = settingsManager.primaryR.toFloat(),
                    onValueChange = { settingsManager.primaryR = it.toInt() },
                    valueRange = 0f..255f
                )
                Slider(
                    value = settingsManager.primaryG.toFloat(),
                    onValueChange = { settingsManager.primaryG = it.toInt() },
                    valueRange = 0f..255f
                )
                Slider(
                    value = settingsManager.primaryB.toFloat(),
                    onValueChange = { settingsManager.primaryB = it.toInt() },
                    valueRange = 0f..255f
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(settingsManager.primaryR, settingsManager.primaryG, settingsManager.primaryB))
                )
                Button(onClick = {
                    settingsManager.saveColors(
                        settingsManager.primaryR,
                        settingsManager.primaryG,
                        settingsManager.primaryB
                    )
                }) {
                    Text("Save Colors")
                }
            }
        }
    }
}
