package dev.trimpsuz.anilist.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.trimpsuz.anilist.BuildConfig
import dev.trimpsuz.anilist.ui.viewModels.MainViewModel
import dev.trimpsuz.anilist.utils.firstBlocking
import dev.trimpsuz.anilist.utils.sendToWear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: MainViewModel = hiltViewModel()
    val selectedTheme by viewModel.theme.collectAsStateWithLifecycle(viewModel.theme.firstBlocking())
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(viewModel.isLoggedIn.firstBlocking())
    val selectedInterval by viewModel.updateInterval.collectAsStateWithLifecycle(viewModel.updateInterval.firstBlocking())

    val intervalOptions = listOf(
        "1 minute" to 60 * 1000L,
        "5 minutes" to 5 * 60 * 1000L,
        "10 minutes" to 10 * 60 * 1000L,
        "15 minutes" to 15 * 60 * 1000L,
        "30 minutes" to 30 * 60 * 1000L,
        "1 hour" to 60 * 60 * 1000L
    )

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    fun selectTheme(theme: String) {
        viewModel.setTheme(theme)
        showThemeDialog = false
    }

    fun selectInterval(interval: String) {
        viewModel.setUpdateInterval(interval)
        scope.launch(Dispatchers.IO) {
            sendToWear("interval", interval, context)
        }
        showIntervalDialog = false
    }

    Scaffold(
        topBar = {
            if(navController.graph.startDestinationRoute == "login") {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon =  {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }

    ) { innerPadding ->
        LazyColumn(
            modifier = when (navController.graph.startDestinationRoute) {
                "login" -> Modifier.padding(innerPadding)
                else -> Modifier
            }.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Display",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text(selectedTheme)},
                    modifier = Modifier.clickable { showThemeDialog = true },
                    leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) }
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Information",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Version") },
                    supportingContent = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")},
                    leadingContent = { Icon(Icons.Outlined.NewReleases, contentDescription = null) }
                )
            }
            if(isLoggedIn) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Wear",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 40.dp)
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("Tile update interval") },
                        supportingContent = { Text(intervalOptions.firstOrNull { it.second.toString() == selectedInterval}?.first ?: "15 minutes" )},
                        modifier = Modifier.clickable { showIntervalDialog = true },
                        leadingContent = { Icon(Icons.Outlined.Update, contentDescription = null) }
                    )
                }
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Account",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 40.dp)
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("Log out") },
                        modifier = Modifier.clickable { showAlert = true },
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Tile update interval") },
            text = {
                Column {
                    intervalOptions.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectInterval(value.toString()) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedInterval
                                    ?: (15 * 60 * 1000L).toString()) == value.toString(),
                                onClick = { selectInterval(value.toString()) }
                            )
                            Text(text = label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Close")
                }
            }
        )
    }


    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    listOf("System", "Light", "Dark").forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectTheme(theme) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = { selectTheme(theme) }
                            )
                            Text(text = theme)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showAlert = false
                        viewModel.saveToken("", context)
                        viewModel.setToken("")
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) { Text("Log out") }
            },
            dismissButton = { OutlinedButton(onClick = { showAlert = false }) { Text("Cancel") } }
        )
    }
}