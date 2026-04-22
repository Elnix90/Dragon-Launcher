<div align="center">

# [![Keep Android Open](https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fmedia.cybernews.com%2Fimages%2Ffeatured-big%2F2025%2F10%2Fkeep-android-open.jpg&f=1&nofb=1&ipt=6735a2941b603358cc56f61e6fc5996b512212d6b43a4b4b3004f95e2a6e7315)](https://keepandroidopen.org/)

# [KEEP ANDROID OPEN](https://keepandroidopen.org/)

---

<img src="https://github.com/Elnix90/Dragon-Launcher/blob/60e6a8a50312d18eb9342fcd9a1e9fd12eead6a1/core/common/src/main/res/drawable/dragon_launcher_foreground.png" width="22%" alt="App Icon"/>

# Dragon Launcher - Fast Gesture based android launcher

### Dragon Launcher is a highly customizable, *gestures based* Android launcher focused on speed and efficiency.

[![Kotlin](https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label=)](https://developer.android.com/compose)

[<img src="https://hosted.weblate.org/widgets/dragon-launcher/-/287x66-grey.png" alt="Translation Status">](https://hosted.weblate.org/engage/dragon-launcher/)

[![Website](https://img.shields.io/badge/website-dragonlauncher.lthb.fr-blue?style=for-the-badge)](https://dragonlauncher.lthb.fr/)

[![GitHub release](https://img.shields.io/github/v/release/Elnix90/Dragon-Launcher.svg?include_prereleases&style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/releases/latest)
[![GitHub stars](https://img.shields.io/github/stars/Elnix90/Dragon-Launcher?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/Elnix90/Dragon-Launcher?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/issues)
[![GitHub forks](https://img.shields.io/github/forks/Elnix90/Dragon-Launcher?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/forks)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/Elnix90/Dragon-Launcher?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/pull)
---
[![GitHub downloads](https://img.shields.io/github/downloads/Elnix90/Dragon-Launcher/total.svg?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/releases)
[![GitHub license](https://img.shields.io/github/license/Elnix90/Dragon-Launcher?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher/blob/dev/LICENSE)
[![Discord](https://img.shields.io/discord/1327996079786168441?color=blue&label=Discord&logo=discord&style=for-the-badge)](https://discord.gg/6UyuP8EBWS)
[![Offline First](https://img.shields.io/badge/Offline-First-orange.svg?style=for-the-badge&logo=rss&logoColor=white)]()
[![Privacy](https://img.shields.io/badge/Privacy-100%25-green.svg?style=for-the-badge&logo=shield&logoColor=white)]()
---
[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/en/packages/org.elnix.dragonlauncher/)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://apt.izzysoft.de/fdroid/index/apk/org.elnix.dragonlauncher)
---
[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt='Get it on GitHub' height="80">](https://github.com/Elnix90/Dragon-Launcher/releases/latest)
[<img src="https://www.openapk.net/images/openapk-badge.png" alt="Get it on OpenAPK" height="80">](https://www.openapk.net/dragon-launcher/org.elnix.dragonlauncher/)
---
[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60">](http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/Elnix90/Dragon-Launcher)
---
</div>

## ⚠️ Due to Android restrictions, you have to disable Android Play protect during installation, to avoid Dragon being blocked by Google

The reason is likely that Dragon asks for _sensitive permissions_ - normal, that's a launcher


---
# 🎉 Thank you very much to the stargazers !
---

## Features

### The main Idea of this launcher is to launch your apps via gestures.

* You can customize your list of fast access apps easily in the settings
* Use the app drawer to pick an app and browse apps

### Gestures & Actions

* Configurable swipe actions
* Drawer enter key actions
* Fast access app launching
* Gesture-first navigation with minimal UI clutter

### App Drawer

* Swipe-down to close drawer
* Configurable enter key behavior
* App shortcuts support (when provided by apps)
* Tap or long-press for quick actions

### Status Bar

* **Fully customizable**
* Optional visibility per user preference
* Custom time and date formatting
* Next alarm display
* Visual integration with launcher theme

### Appearance

* Deep customization features
* Customize each colors separately for every little action / surface
* Change wallpaper, apply blur, separate wallpaper on drawer / main screen
* Icon packs support
* Widgets support, arrange freely your widgets as well as floating apps / actions from Dragon

### Backup

* Manual backups to phone's storage
* Auto backup feature : auto backups on every app focus change

## Privacy & security

* **No** data collection
* Dragon Launcher has not even access to internet -> it cannot steal your data
* No intrusive permissions requested for the app to work
  - `android.permission.SYSTEM_ALERT_WINDOW` is used by the Wellbeing Feature, to display over other apps a popup telling the user when to stop using the app (social media or other)
  - `android.permission.READ_PHONE_STATE` is used by the status bar to get info about the connectivity states, wifi, bluetooth, data... even hotspot.
* Uses Accessibility permissions (optionally) to:
    1. Expand notifications panel (needed by android)
        - Uses accessibility to expand the notifications panel
        - Expanding the quick actions / notifications isn't required on some Android version, there is a hidden settings
          in the debug tab, to change the way it opens the quick actions, cause on my phone, the notifications action
          opens the quick actions
    2. Lock screen (needed by android)
        - It uses accessibility to lock the screen, as you pressed the lock button
    3. Open recent apps (needed by Android)
        - You can choose to open the recent apps, just like when you click on the Square button, or do the recent apps
          gesture
    4. Auto launch Dragon on system launcher detected (used by some Xiaomi users that cannot set another default
       launcher without loosing the gesture navigation)
        - It will launch Dragon Launcher when the accessibility detects that you entered your system launcher, for a
          default launcher-like experience when it cannot be set as android default (configurable in debug settings)
* Can set the Launcher as device admin
    * used for some OEMs to prevent it from killing the app, especially on Xiaomi/HyperOS devices that have really
      strong battery optimization features
    * used also for users that need the previous auto launch on system launcher, to prevent the accessibility services
      to be killed
* All data stored locally (you can backup manually or use the auto backup feature)

---

## Screenshots

<p align="center">
    <img src="assets/images/demo1.gif" alt="App demo animation 1" width="30%">
    <img src="assets/images/demo2.gif" alt="App demo animation 2" width="30%">
    <img src="assets/images/demo3.gif" alt="App demo animation 3" width="30%">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="22%" alt="App Screenshot 1"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="22%" alt="App Screenshot 2"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" width="22%" alt="App Screenshot 3"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="22%" alt="App Screenshot 4"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="22%" alt="App Screenshot 5"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="22%" alt="App Screenshot 6"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/7.jpg" width="22%" alt="App Screenshot 7"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" width="22%" alt="App Screenshot 8"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" width="22%" alt="App Screenshot 9"/>
  <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/fastlane/metadata/android/en-US/images/phoneScreenshots/10.jpg" width="22%" alt="App Screenshot 10"/>
</p>


## User Screenshots

<p align="center">
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/elnix.webp" width="22%" alt="Elnix Screenshot"/>
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/elnix2.jpg" width="22%" alt="Elnix Screenshot 2"/>
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/speedhog.jpg" width="22%" alt="speedhog Screenshot"/>
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/oniyon.jpg" width="22%" alt="oniyon Screenshot"/>
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/alyon.jpg" width="22%" alt="alyon Screenshot"/>
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/prizewhisper.webp" width="22%" alt="prizewhisper Screenshot"/> 
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/opaline.webp" width="22%" alt="opaline Screenshot"/> 
    <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/fede1.webp" width="22%" alt="fede1 Screenshot"/> 
</p>

 <details>
    <summary>More</summary>
    <p align="center">
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/infamous.webp" width="22%" alt="infamous Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/m0n0lith.webp" width="22%" alt="m0n0lith Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/sandsalamand.webp" width="22%" alt="sandsalamand Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/buck_fpv.jpg" width="22%" alt="buck_fpv Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/drBrigth.jpg" width="22%" alt="drBrigth Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/acress1.webp" width="22%" alt="acress1 Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/yerminator.webp" width="22%" alt="yerminator Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/yprum.webp" width="22%" alt="yprum Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/buck_fpv.webp" width="22%" alt="buck fpv Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/elryantheexplorer.webp" width="22%" alt="elryantheexplorer Screenshot"/>
        <img src="https://raw.githubusercontent.com/Elnix90/Dragon-Launcher/main/assets/user-screenshots/federico.webp" width="22%" alt="federico Screenshot"/>
    </p>
</details>



## Usage

* **Long click 3 seconds to access settings**
* Tap or long-press apps to quickly launch, view options, or uninstall on drawer
* Customize gestures and visual settings via the Settings menu.
* Change background for main screen / drawer, add blur to it

---

### What's this icon ?

| Icons                                  | Meaning                                                                                                           |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| <img src="/assets/1.png" height="80"/> | Enter / Exit the nest (click before on the nest you created on one circle)                                        |
| <img src="/assets/3.png" height="60"/> | Toggle points snapping (if not enabled, you can move freely the points, else they snap in rounded position - 15°) |
| <img src="/assets/2.png" height="60"/> | Toggle auto-separate points when you drag them                                                                    |
| <img src="/assets/4.png" height="60"/> | Enter the nest management dialog, where you can view, add and remove the nests                                    |

## Signing

Releases / F-Droid signing key (SHA-256):

```text
63068d94e01eeae50efcb2a0c43dfa8ac503a421cdeaf353d45b69ab933c0a06
```

Verify:

```bash 
apksigner verify --print-certs DragonLauncher-*.apk
```

## Contributing

* Contributions are welcome! Feel free to submit pull requests or open issues.
* Check [CONTRIBUTING.md](https://github.com/Elnix90/Dragon-Launcher/blob/main/CONTRIBUTING.MD) for contributions
  guidelines

* You may join the [Discord server](https://discord.gg/6UyuP8EBWS) to discuss more easily about changes in the project.


If you want to help translating, check out the project on [Weblate](https://hosted.weblate.org/engage/dragon-launcher).

---

## License

* This project is open-source under the **GPL 3 Licence**.
