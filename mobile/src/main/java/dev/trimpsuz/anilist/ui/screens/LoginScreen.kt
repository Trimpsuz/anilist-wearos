package dev.trimpsuz.anilist.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.trimpsuz.anilist.utils.AUTH_URL
import android.widget.Toast

@Composable
fun LoginScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Please log in",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                try{
                    Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL)).apply {
                        context.startActivity(this)
                    }
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app found for this action", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Login")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Settings", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}