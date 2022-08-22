---
sidebar_position: 6
---

# Migrating from v2 to v3

This version includes fundamental changes in how we handle the Android
foreground service, ensuring that the OS never stops the app process, and that
controls for audio playback are present even if the app is stopped. This mimics
how other popular apps (Spotify, Soundcloud, Google Podcasts, etc.) work.

- On Android, the audio service can't be manually stopped by the app anymore.
    The OS itself decides when to stop it.
- An audio control notification will *always* be present (depending on phone
    vendor, this would look and behave differently), which allows users to
    quickly go back to the app by tapping on it.
- The `destroy` function does not exist anymore. 
- The `stopWithApp` flag turns into `stoppingAppPausesPlayback` 
    - [More information here](https://github.com/doublesymmetry/react-native-track-player/pull/1447#issuecomment-1195246389)

The full changelog of added features and bug fixes [can be found here](https://github.com/doublesymmetry/react-native-track-player/releases/tag/v3.0).

When migrating from v2 to v3, the following has changed:

```diff
// Methods
await TrackPlayer.updateOptions({
-  stopWithApp: true,
+  stoppingAppPausesPlayback: true,
  ...
});

// remove all usages of `.destroy()` and `.stop()`
-  TrackPlayer.destroy();
-  TrackPlayer.stop();
```
