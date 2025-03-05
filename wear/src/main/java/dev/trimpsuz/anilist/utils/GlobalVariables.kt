package dev.trimpsuz.anilist.utils
import dev.trimpsuz.anilist.GetMediaQuery

class GlobalVariables {
    var accessToken: String? = null
    var RESOURCES_VERSION: String = "0"
    var REFRESH_INTERVAL_TILE: Long? = null
    var mediaList: List<GetMediaQuery.Medium?>? = null
}