---
sidebar_position: 98
---


# Developing with Expo

Expo is a popular development platform in the react-native ecosystem.

Please be aware that while many people are using React Native Track Player with Expo successfully, the current maintainers of this project do not use Expo and their ability to resolve issues involving Expo is limited.


## Development Build

A [Dev Client](https://docs.expo.dev/more/glossary-of-terms/#dev-clients) is required in order to use this package (Expo Go is not supported).

To get started, create a [development build](https://docs.expo.dev/clients/getting-started/) for your Expo app and then install React Native Track Player.


## Custom Entry Point

In order to configure the Playback Service within the [entry point](https://docs.expo.dev/more/glossary-of-terms/#entry-point), you will need to [create a custom entry point](https://docs.expo.dev/guides/monorepos/#change-default-entrypoint) and adjust the `package.json` configuration.

By default, Expo points to an entry point in the `node_modules` folder. The path for this is configured in the `"main"` field of `package.json`. To create a new entry point:

1. Copy the default one

```
cp node_modules/expo/AppEntry.js AppEntry.js 
```

2. Update the `"main"` field of `package.json` to reference the new file e.g. `"AppEntry.js"`
3. Edit the new file to have it import your app code, and configure the Playback Service.


## Streaming HTTP

On newer versions of Android, HTTP streaming is disallowed by default (HTTPS is OK). However, it is only disallowed in production builds which can make this issue hard to diagnose.

To allow HTTP, you must first install the [`expo-build-properties` package](https://docs.expo.dev/versions/latest/sdk/build-properties).

```
expo install expo-build-properties
```

Then add this configuration in `app.json`:

```json
...
    "plugins": [
      [
        "expo-build-properties",
        {
          "android": {
            "usesCleartextTraffic": true
          }
        }
      ]
    ]
...
```

## Background Audio

Here is the configuration required for audio playback in background:

- [iOS: Enable audio playback in background via your app.json](https://docs.expo.dev/versions/latest/sdk/audio/#playing-or-recording-audio-in-background)
- [Android: Stop playback when the app is closed](../basics/background-mode.md/#android)


