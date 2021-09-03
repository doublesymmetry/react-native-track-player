---
title: Installation
description: "Instructions to set up react-native-track-player"
nav_order: 2
permalink: /install/
redirect_from:
  - /installation/
---

# Installation

## Installing the packages
**1. Install the module from npm or yarn**
```
npm install --save react-native-track-player
```

```
yarn add react-native-track-player
```

**2. (iOS) Enable Swift Modules**

Because the iOS module uses Swift, if the user is using a standard react-native application they'll need to add support for Swift in the project. This can be easily by adding a swift file to the Xcode project -- could be called `dummy.swift` and saying yes when prompted if you'd like to generate a bridging header.

![Importing Swift](https://i.imgur.com/CBqBcWs.png)

## Automatic Link
Since `react-native-track-player` only support RN 0.60 and above, the module should be autolinked :tada:

## Unstable
If you want to try the latest features, you can install the module directly from GitHub using:

```
npm install --save react-native-kit/react-native-track-player#dev
```

```
yarn add react-native-kit/react-native-track-player#dev
```

To update it, run the same command again.

## Expo

You can now use React Native Track Player with Expo.

Start by creating a [custom development client](https://docs.expo.dev/clients/getting-started/) for your Expo app and then install React Native Track Player.

Here is the configuration required for audio playback in background:

- iOS: Enable audio playback in background via your app.json https://docs.expo.dev/versions/latest/sdk/audio/#playing-or-recording-audio-in-background-ios

- Android: Stop playback when the app is closed https://react-native-track-player.js.org/background/#android

And don't forget to register a [playback service](https://react-native-track-player.js.org/getting-started/#playback-service).

## Troubleshooting

### iOS: (Enable Swift) `library not found for -lswiftCoreAudio for architecture x86_64`
Because the iOS module uses Swift, if the user is using a standard react-native application they'll need to add support for Swift in the project. This can be easily by adding a swift file to the Xcode project -- could be called `dummy.swift` and saying yes when prompted if you'd like to generate a bridging header.

![Importing Swift](https://i.imgur.com/CBqBcWs.png)

### Android: `CIRCULAR REFERENCE:com.android.tools.r8.ApiLevelException: Default interface methods are only supported starting with Android N (--min-api 24)`
Since version 1.0.0, we began using a few Java 8 features in the project to reduce the code size.

To fix the issue, add the following options to your `android/app/build.gradle` file:
```diff
android {
    ...
+   compileOptions {
+       sourceCompatibility JavaVersion.VERSION_1_8
+       targetCompatibility JavaVersion.VERSION_1_8
+   }
    ...
}
```

### Android: `com.facebook.react.common.JavascriptException: No task registered for key TrackPlayer`
The playback service requires a headless task to be registered. You have to register it with `registerPlaybackService`.

### Android: `Error: Attribute XXX from [androidx.core:core:XXX] is also present at [com.android.support:support-compat:XXX]`
This error occurs when you're mixing both AndroidX and the Support Library in the same project.

You have to either upgrade everything to AndroidX or downgrade everything to the support library.


* For react-native-track-player, the last version to run the support library is **1.1.4** and the first version to run AndroidX is **1.2.0**.
* For react-native, the last version to run the support library is **0.59** and the first version to run AndroidX is **0.60**.

You can also use [jetifier](https://github.com/mikehardy/jetifier#usage-for-source-files) to convert all of the native code to use only one of them.

### Android: Cleartext HTTP traffic not permitted

Since API 28, Android disables traffic without TLS. To fix the issue you have to use `https` or [enable clear text traffic](https://stackoverflow.com/a/50834600).

## Next
You can choose the build preferences for your app using `track-player.json`. See more [here](Build-Preferences.md).
