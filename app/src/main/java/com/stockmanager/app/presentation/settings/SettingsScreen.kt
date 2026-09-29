package com.stockmanager.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockmanager.app.R
import com.stockmanager.app.ui.theme.MaterialTheme
import com.stockmanager.app.ui.theme.StockManagerTheme
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.Backup
import androidx.compose.material3.icons.filled.DarkMode
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.material3.icons.filled.Download
import androidx.compose.material3.icons.filled.Info
import androidx.compose.material3.icons.filled.Lock
import androidx.compose.material3.icons.filled.Person
import androidx.compose.material3.icons.filled.Restore
import androidx.compose.material3.icons.filled.Security
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavController

@Composable
fun SettingsScreen(
    navController: NavController
) {
    var darkMode by remember { mutableStateOf(false) }
    var biometricAuth by remember { mutableStateOf(false) }
    var autoBackup by remember { mutableStateOf(true) }

    StockManagerTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Appearance Section
                    SettingsSection(title = "Appearance") {
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Mode",
                            subtitle = "Use dark theme",
                            trailing = {
                                Switch(
                                    checked = darkMode,
                                    onCheckedChange = { darkMode = it },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                    }

                    // Security Section
                    SettingsSection(title = "Security") {
                        SettingsRow(
                            icon = Icons.Default.Security,
                            title = "Biometric Authentication",
                            subtitle = "Use fingerprint/face to unlock",
                            trailing = {
                                Switch(
                                    checked = biometricAuth,
                                    onCheckedChange = { biometricAuth = it },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update your login password",
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { /* TODO: Navigate to change password */ }
                        )
                    }

                    // Data Section
                    SettingsSection(title = "Data & Backup") {
                        SettingsRow(
                            icon = Icons.Default.Backup,
                            title = "Auto Backup",
                            subtitle = "Automatically backup data to cloud",
                            trailing = {
                                Switch(
                                    checked = autoBackup,
                                    onCheckedChange = { autoBackup = it },
                                    colors = androidx.compose.material3.SwitchDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        SettingsRow(
                            icon = Icons.Default.Download,
                            title = "Export Data",
                            subtitle = "Export all data as CSV/JSON",
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { /* TODO: Export */ }
                        )
                        SettingsRow(
                            icon = Icons.Default.Restore,
                            title = "Import Data",
                            subtitle = "Import data from backup file",
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { /* TODO: Import */ }
                        )
                        SettingsRow(
                            icon = Icons.Default.Delete,
                            title = "Clear All Data",
                            subtitle = "Permanently delete all products and transactions",
                            titleColor = MaterialTheme.colorScheme.error,
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { /* TODO: Show confirmation dialog */ }
                        )
                    }

                    // About Section
                    SettingsSection(title = "About") {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Version",
                            subtitle = "1.0.0",
                            trailing = {}
                        )
                        SettingsRow(
                            icon = Icons.Default.Person,
                            title = "Privacy Policy",
                            subtitle = "Read our privacy policy",
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { /* TODO: Open privacy policy */ }
                        )
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Terms of Service",
                            subtitle = "Read our terms of service",
                            trailing = {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { /* TODO: Open terms */ }
                        )
                    }

                    // Logout Button
                    Button(
                        onClick = { /* TODO: Logout */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp).padding(end = 8.dp)
                            )
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Divider()
            content()
        }
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    titleColor: Color = androidx.compose.ui.graphics.Color.Unspecified,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveTitleColor = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface

    val row = Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = effectiveTitleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing()
    }

    if (onClick != null) {
        androidx.compose.foundation.gestures.clickable(
            onClick = onClick,
            role = androidx.compose.ui.semantics.Role.Button
        ) {
            row
        }
    } else {
        row
    }
}