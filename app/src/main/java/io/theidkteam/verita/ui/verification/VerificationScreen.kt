package io.theidkteam.verita.ui.verification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import org.matrix.android.sdk.api.session.crypto.verification.EmojiRepresentation

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun VerificationScreen(
    onBack: () -> Unit,
    viewModel: VerificationViewModel = hiltViewModel()
) {
    val state by viewModel.verificationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startDeviceVerification()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Verification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val s = state) {
                is VerificationViewModel.VerificationUIState.Idle -> {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Waiting for verification request...", textAlign = TextAlign.Center)
                }
                is VerificationViewModel.VerificationUIState.Started -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Starting verification...")
                }
                is VerificationViewModel.VerificationUIState.EmojiReady -> {
                    Text("Compare Emojis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Make sure the following emojis appear on both devices:", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(s.emojis) { emoji ->
                            EmojiItem(emoji)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = { viewModel.declineEmoji(); onBack() }, modifier = Modifier.weight(1f)) {
                            Text("No Match")
                        }
                        Button(onClick = { viewModel.approveEmoji() }, modifier = Modifier.weight(1f)) {
                            Text("They Match")
                        }
                    }
                }
                is VerificationViewModel.VerificationUIState.Verified -> {
                    Text("✅ Device Verified!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack) { Text("Done") }
                }
                is VerificationViewModel.VerificationUIState.Error -> {
                    Text("❌ Verification Failed", color = MaterialTheme.colorScheme.error)
                    Text(s.message, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onBack) { Text("Go Back") }
                }
            }
        }
    }
}

@Composable
fun EmojiItem(emoji: EmojiRepresentation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji.emoji, fontSize = 40.sp)
            Text(emoji.nameResId.toString(), style = MaterialTheme.typography.labelSmall) // SDK usually provides a name
        }
    }
}
