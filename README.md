<h1 style="display: flex; align-items: center; gap: 10px;">
  <img height="50px" src="https://github.com/Trimpsuz/anilist-wearos/blob/master/mobile/src/main/res/mipmap-hdpi/ic_launcher_round.webp" />
  AniList WearOS
</h1>

[![Downloads](https://img.shields.io/github/downloads/Trimpsuz/anilist-wearos/total.svg)](https://github.com/Trimpsuz/anilist-wearos/releases/latest)
[![Latest release](https://img.shields.io/github/v/release/Trimpsuz/anilist-wearos?label=latest)](https://github.com/Trimpsuz/anilist-wearos/releases/latest)
[![Latest release](https://img.shields.io/github/stars/Trimpsuz/anilist-wearos?style=flat)](https://img.shields.io/github/stars/Trimpsuz/anilist-wearos?style=flat)
[![License](https://img.shields.io/badge/License-GPLv3-green.svg?label=license)](LICENSE)

An unofficial [AniList](https://anilist.co/) client for WearOS devices.

##

<table style="width: 100%; table-layout: auto;">
    <tbody>
        <tr style="display: flex; flex-wrap: wrap; justify-content: center; align-items: center;">
            <td style="padding: 5px; display: flex; justify-content: center; align-items: center; height: 220px;">
                <img style="height: 180px; max-width: 100%;" src="https://img.trimpsuz.dev/i/tpaq6.png" />
            </td>
            <td style="padding: 5px; display: flex; justify-content: center; align-items: center; height: 220px;">
                <img style="height: 180px; max-width: 100%;" src="https://img.trimpsuz.dev/i/8ay5h.png" />
            </td>
            <td style="padding: 5px; display: flex; justify-content: center; align-items: center; height: 220px;">
                <img style="height: 210px; max-width: 100%;" src="https://img.trimpsuz.dev/i/z0fg5.png" />
            </td>
            <td style="padding: 5px; display: flex; justify-content: center; align-items: center; height: 220px;">
                <img style="height: 210px; max-width: 100%;" src="https://img.trimpsuz.dev/i/wdzvp.png" />
            </td>
            <td style="padding: 5px; display: flex; justify-content: center; align-items: center; height: 220px;">
                <img style="height: 210px; max-width: 100%;" src="https://img.trimpsuz.dev/i/tfr2p.png" />
            </td>
        </tr>
    </tbody>
</table>

## Features

- **Wear tile**

  - Select up to 3 entries to display on the tile.
  - The current and total episode/chapter counts are displayed next to each entry.
  - Easily increment the episode/chapter count from the tile by tapping the `+` button next to an entry.

- **Wear app**

  - Access more detailed information about your current, unfinished or all entries.
  - The current and total episode/chapter counts along with the status of the entry are displayed next to each entry.
  - Increment the episode/chapter count by tapping the `+` button next to an entry or tap on an entry to access a more detailed edit view.

- **Mobile app**

  - There is no real functionality in the mobile app, it is only used for logging in and selecting which entries you want to display on the tile as well as changing the tile update interval.

## Getting started

### Installation

1. Download the apks from the [**latest release**](https://github.com/Trimpsuz/AniList-wearos/releases/latest).

2. Install the mobile app from the **mobile-release** apk.

3. Install the WearOS app via one of the following methods:

> [!IMPORTANT]
> You must have **developer options** enabled on your WearOS device. Instructions on enabling them can be found [**here**](https://developer.android.com/training/wearables/get-started/debugging#enable-dev-options).

<details>
<summary>Installation via mobile</summary>

1. Make sure your WearOS device and phone are on the same **WiFi network**.

2. Install the [**Wear Installer 2**](https://play.google.com/store/apps/details?id=org.freepoc.wearinstaller2) app on your mobile device.

3. Enable **wireless debugging** in your WearOS device's **developer options**. To do so, navigate to `Settings` > `Developer options` > `Wireless debugging` > `Enable wireless debugging`.

4. Open the **Wear Installer 2** app and input the IP address of your WearOS device.

5. Press the three dots in the top right corner of the app and select `Pair with watch`, then press the `Enable` button.

6. In the `Wireless debugging` menu on your WearOS device, select `Pair new device`. Enter the pairing code and port into the dialog in the **Wear Installer 2** app.

7. Return to the `Wireless debugging` menu on your WearOS device and input the port into the the field next to the IP address in the **Wear Installer 2** app.

8. Press `Done` and select the `Custom APK` tab. Select the **wear-release** apk you downloaded and press `Continue` to install the app.

</details>

<details>
<summary>Installation via computer</summary>

1. Make sure your WearOS device and computer are on the same **WiFi network**.

2. Make sure you have [**adb**](https://developer.android.com/tools/adb) installed on your computer.

3. Enable **wireless debugging** in your WearOS device's **developer options**. To do so, navigate to `Settings` > `Developer options` > `Wireless debugging` > `Enable wireless debugging`.

4. In the `Wireless debugging` menu on your WearOS device, select `Pair new device`. On your computer, run `adb pair <ip:port>`, inputting the pairing IP and port from your WearOS device.

5. Return to the `Wireless debugging` menu on your WearOS device and run `adb connect <ip:port>` on your computer, inputting the IP and port from your WearOS device.

6. Run `adb install <apk>`, inputting the path to the **wear-release** apk you downloaded.

</details>

### Login and setup

1. Launch the mobile app and sign in with your AniList account.

2. Authorize the app to access your AniList data. After authorizing, the **Anilist WearOS** app can be found under the [**apps tab in your anilist settings**](https://anilist.co/settings/apps).

3. Once the login is successful, you will also be automatically logged in to the wear app if your WearOS device is connected and the app is installed.

### Selecting entries for the wear tile

- Browse your current entries in the mobile app.

- Tap on an entry to select it or deselect it if it is already selected.

- Select up to three entries to be displayed on the wear tile.

### Adding the wear tile

1. From your watch face, swipe left or right to access tiles.

2. Long press on a tile and press the `+` button toward the bottom.

3. Select **AniList tile** from the available tiles.

## Using the wear app and tile

### Using the wear tile

- Your selected entries appear with their current progress.

- Tap on the `+` button next to an entry to increment its episode/chapter count.

- The tile refreshes automatically every of 15 minutes by default. This can be changed in the settings of the mobile app, intervals of 1, 5, 10, 15 and 30 minutes as well as 1 hour are available. You can tap anywhere on the tile to force a refresh. Please note that a shorter interval will cause higher battery drain.

### Using the wear app

- View your entries and their progress on their corresponding tabs.

- Tap on the `+` button next to an entry to increment its episode/chapter count.

- Tap on an entry to access a more detailed **edit view** where you can edit the status of the entry, as well as the volume and repeat counts.

- Press the three dots next to the tab selector to access the **filter options**. You can select which statuses that entries must have in order to be displayed, and in which order the entries should be displayed.

- The default status option is `Current`, which includes entries in the **watching/reading** and **rewatching/rereading** statuses. The `Unfinished` status option includes all entries, except for those in the **dropped** or **completed** statuses. The default sort order is `Update date descending`.

## Libraries used

- [AniList GraphQL API](https://github.com/AniList/ApiV2-GraphQL-Docs)
- [Apollo Kotlin](https://github.com/apollographql/apollo-kotlin)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Glide Compose](https://bumptech.github.io/glide/int/compose.html)
- [okhttp](https://github.com/square/okhttp)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt](https://dagger.dev/hilt)
- [Horologist](https://github.com/google/horologist)
- [protolayout](https://developer.android.com/jetpack/androidx/releases/wear-protolayout)

## License

This project is licensed under the GPLv3 license. Please refer to the [LICENSE](LICENSE) file for more details.

---

_AniList WearOS is not officially affiliated with AniList. All data is provided through the public AniList API._
