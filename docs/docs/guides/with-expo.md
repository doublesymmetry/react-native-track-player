---
sidebar_position: 4
---

# Developing with Expo

Expo is a popular development platform in the react-native ecosystem.

## Custom Entrypoint

By default, Expo points to an entrypoint in the `node_modules` package. The path for this is configured in the `"main"` field of `package.json`. To configure the Playback Service in this entrypoint file:

1. Copy the file

```
cp node_modules/expo/AppEntry.js AppEntry.js 
```

2. Update the `"main"` field of `package.json` to the new location e.g. `"AppEntry.js"`
3. Edit the file to have it import your app code, and configure the Playback Service.


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
