# AniList WearOS

An AniList client to track anime and manga from your WearOS device.

## Features

- **Synchronized Tracking**: Update your anime/manga progress directly from your watch
- **Customizable Tile**: Track up to 3 entries simultaneously on your WearOS device
- **One-tap Progress**: Increment episode/chapter count with a simple tap
- **Companion Mobile App**: Easily manage your tracked entries from your phone

## Getting Started

### Installation
1. Download the apks from the [**latest release**](https://github.com/Trimpsuz/AniList-wearos/releases/latest).
2. Open the mobile app and sign in with your AniList credentials
3. Select entries to be displayed on your WearOS device
4. Add the AniList tile to your WearOS device

## Using the Mobile App

### Login and Setup
1. Launch the app and sign in with your AniList account
2. Authorize the app to access your AniList data

### Selecting Entries for Your Tile
- Browse your current lists
- Select up to 3 entries to display on your WearOS tile

### Managing Your Selections
- Tap on a selected entry to remove it

## Using the WearOS Tile

### Adding the Tile
1. From your watch face, swipe left or right to access tiles
2. Long press on the tile and press the `+` button toward the bottom
3. Select "AniList tile" from the available tiles

### Using the Tile
- **View Progress**: Your selected entries appear with their current progress
- **Update Progress**: Tap on the `+` button next to an entry to increment the episode/chapter count
- **Refreshing**: The tile automatically refreshes every 15 minutes. You can tap anywhere on the tile to force a refresh

## Libraries used

* [AniList GraphQL API](https://github.com/AniList/ApiV2-GraphQL-Docs)
* [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin)
* [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
* [Glide Compose](https://bumptech.github.io/glide/int/compose.html)
* [okhttp](https://github.com/square/okhttp)
* [Jetpack Compose](https://developer.android.com/jetpack/compose)
* [Hilt](https://dagger.dev/hilt)
* [Horologist](https://github.com/google/horologist)
* [protolayout](https://developer.android.com/jetpack/androidx/releases/wear-protolayout)

---

*AniList WearOS is not officially affiliated with AniList. All anime and manga data is provided through the public AniList API.*