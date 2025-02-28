package dev.trimpsuz.anilist.utils

import androidx.compose.ui.graphics.vector.ImageVector
import dev.trimpsuz.anilist.BuildConfig

const val ANILIST_API_URL = "https://anilist.co/api/v2"
const val ANILIST_AUTH_URL = "$ANILIST_API_URL/oauth/authorize"
const val AUTH_URL = "$ANILIST_AUTH_URL?client_id=${BuildConfig.CLIENT_ID}&response_type=token"
const val ANILIST_GRAPHQL_URL = "https://graphql.anilist.co/graphql"

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int? = null
)