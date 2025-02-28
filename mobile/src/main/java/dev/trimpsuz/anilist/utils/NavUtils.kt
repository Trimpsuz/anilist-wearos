package dev.trimpsuz.anilist.utils

import androidx.navigation.NavController

fun NavController.isOnBackStack(route: String): Boolean = try { getBackStackEntry(route); true } catch(e: Throwable) { false }
fun NavController.navigateOrPopBackStack(route: String) = run {
    if(isOnBackStack(route)) popBackStack(route, false)
    else navigate(route)
}