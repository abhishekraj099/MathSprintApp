package com.example.mathsprint.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    showBackButton: Boolean = false
) {
    val user by viewModel.user.collectAsState()
    val loggedOut by viewModel.loggedOut.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }

    LaunchedEffect(loggedOut) {
        if (loggedOut) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LimeGreen)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("Profile", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = SurfaceWhite)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LimeGreen)
                .padding(vertical = 24.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(SurfaceWhite),
                    contentAlignment = Alignment.Center
                ) { Text("🦊", fontSize = 40.sp) }
                Spacer(Modifier.height(12.dp))
                Text(user?.name ?: "Mathlete", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = SurfaceWhite)
                Text("Level ${user?.rankLevel ?: 1} • ${user?.xp ?: 0} XP",
                    style = MaterialTheme.typography.bodyMedium, color = SurfaceWhite.copy(alpha = 0.85f))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple("🔥", "Streak", "${user?.streak ?: 0}d"),
                Triple("🪙", "Coins", "${user?.coins ?: 0}"),
                Triple("💎", "Gems", "${user?.gems ?: 0}")
            ).forEach { (emoji, label, value) ->
                Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 24.sp)
                        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(label, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Settings", modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        listOf(
            Triple(Icons.Default.Email, "Change Email") { showChangeEmailDialog = true },
            Triple(Icons.Default.Palette, "Theme Colors") { },
            Triple(Icons.Default.Email, "Email Developer") { },
            Triple(Icons.Default.Feedback, "Send Feedback") { }
        ).forEach { (icon, label, onClick) ->
            SettingsRow(icon, label, onClick = onClick)
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

        SettingsRow(Icons.AutoMirrored.Filled.Logout, "Logout", onClick = {
            viewModel.logout()
        })
        SettingsRow(Icons.Default.DeleteForever, "Delete All Data", textColor = CoralRed, onClick = {
            showDeleteDialog = true
        })

        Spacer(Modifier.height(40.dp))
    }

    if (showDeleteDialog) {
        val onDismissRequest = { showDeleteDialog = false }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed) },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your account, all progress, and data from our servers. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount()
                    showDeleteDialog = false
                }) {
                    Text("Delete Everything", color = CoralRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangeEmailDialog) {
        AlertDialog(
            onDismissRequest = { showChangeEmailDialog = false },
            title = { Text("Change Email Address", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Current Email: ${user?.email ?: "Not set"}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it.lowercase().trim() },
                        label = { Text("New Email") },
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newEmail.isNotEmpty() && newEmail.matches("[a-zA-Z0-9._-]+@gmail\\.com".toRegex())) {
                        viewModel.changeEmail(newEmail)
                        newEmail = ""
                        showChangeEmailDialog = false
                    }
                }) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newEmail = ""
                    showChangeEmailDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

