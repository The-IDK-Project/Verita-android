package io.theidkteam.verita.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
        ) {
            item {
                SettingsSectionTitle("Appearance")
                
                SettingsPreferenceItem(
                    title = "Theme",
                    subtitle = "Current: ${settingsManager.themeMode}",
                    icon = Icons.Default.Palette,
                    onClick = { /* Show dialog */ }
                )

                var showThemeDialog by remember { mutableStateOf(false) }
                if (showThemeDialog) {
                    ThemeSelectionDialog(
                        currentMode = settingsManager.themeMode,
                        onDismiss = { showThemeDialog = false },
                        onSelect = { 
                            settingsManager.saveThemeMode(it)
                            showThemeDialog = false
                        }
                    )
                }
                
                // Add the specific "Theme Preference" item with a switch if needed, 
                // but Element usually uses a selection. 
                // Let's add a "Sync with system" toggle as requested.
                SettingsSwitchItem(
                    title = "Follow system theme",
                    subtitle = "Automatically switch between light and dark themes",
                    icon = Icons.Default.Settings,
                    checked = settingsManager.themeMode == "System",
                    onCheckedChange = { checked ->
                        settingsManager.saveThemeMode(if (checked) "System" else "Light")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                SettingsSectionTitle("Theme Customization")
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Primary Color (RGB)", style = MaterialTheme.typography.labelLarge)
                    ColorSlider("Red", settingsManager.primaryR) { settingsManager.primaryR = it }
                    ColorSlider("Green", settingsManager.primaryG) { settingsManager.primaryG = it }
                    ColorSlider("Blue", settingsManager.primaryB) { settingsManager.primaryB = it }
                    
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(settingsManager.primaryR, settingsManager.primaryG, settingsManager.primaryB),
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = {
                            settingsManager.saveColors(
                                settingsManager.primaryR,
                                settingsManager.primaryG,
                                settingsManager.primaryB
                            )
                        }) {
                            Text("Apply Theme Color")
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                SettingsSectionTitle("Connectivity")
                
                SettingsSwitchItem(
                    title = "Use Proxy",
                    subtitle = "Enable if you are in a restricted network",
                    icon = Icons.Default.Settings,
                    checked = settingsManager.useProxy,
                    onCheckedChange = { settingsManager.useProxy = it }
                )
                
                if (settingsManager.useProxy) {
                    // Proxy details could go here or in a sub-screen
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsPreferenceItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
fun ColorSlider(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: $value", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf("Light", "Dark", "System")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == currentMode),
                                onClick = { onSelect(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == currentMode),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
