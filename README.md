# react-native-track-player [![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player) [![Chat](https://badges.gitter.im/react-native-track-player/gitter.png)](https://gitter.im/react-native-track-player/Support)

An all-in-one module that provides audio playback, external media controls, chromecast support and background mode.

---

* [Installation](https://github.com/Guichaguri/react-native-track-player/wiki/Installation)
* [Getting Started](https://github.com/Guichaguri/react-native-track-player/wiki/API)
* [Documentation](https://github.com/Guichaguri/react-native-track-player/wiki/Documentation)
* [Platform Support](https://github.com/Guichaguri/react-native-track-player/wiki/Platform-Support)
* [Cast Integration](https://github.com/Guichaguri/react-native-track-player/wiki/Cast-Integration)
* [Car Integration](https://github.com/Guichaguri/react-native-track-player/wiki/Car-Integration)
* [Background Mode](https://github.com/Guichaguri/react-native-track-player/wiki/Background-Mode)
* [Build Preferences](https://github.com/Guichaguri/react-native-track-player/wiki/Build-Preferences)

## Features

* **Lightweight** - Optimized to use the least amount of resources according to your needs
* **Media Controls support** - Control the app from a bluetooth device, the lockscreen, a notification, a smartwatch or even a car
* **Local or network, files or streams** - It doesn't matter where the media belongs, we've got you covered
* **Chromecast support** - Seamlessly switch to any Google Cast compatible device, supporting custom media receivers
* **Adaptive bitrate streaming support** - Optional support for DASH, HLS or SmoothStreaming
* **Caching support** - Cache media files to play them again without an internet connection
* **Background support** - Keep playing audio even after the app is closed
* **Fully Customizable** - Even the notification icons are customizable!

## Platform Support

| Feature | Android | iOS | Windows |
| ------- | ------- | --- | ------- |
| Load from the app bundle | ✓ | ✓ | ✓ |
| Load from the network | ✓ | ✓ | ✓ |
| Load from the file system | ✗ | ✗ | ✗ |
| Adaptive Bitrate Streaming | ✓ | ✗ | ✓ |
| Play/Pause/Stop/Reset | ✓ | ✓ | ✓ |
| Seeking/Volume | ✓ | ✓ | ✓ |
| Remote Media Controls | ✓ | ✓ | ✓ |
| Caching | ✓ | ✗ | ✗ |
| Events | ✓ | ✓ | ✓ |
| Background Mode | ✓ | ✓ | ✓ |
| Google Cast | ✓ | ✗ | ✗ |

Check [Platform Support](https://github.com/Guichaguri/react-native-track-player/wiki/Platform-Support) for more information.

## Why another music module?
After trying to team up modules like `react-native-sound`, `react-native-music-controls` and `react-native-google-cast`, I've noticed that their structure and the way should be tied together can cause a lot problems (mainly on Android). Those can heavily affect the app stability and user experience.

All audio modules (like `react-native-sound`) don't play in a separated service on Android, which should **only** be used for simple audio tracks in foreground (such as sound effects, voice messages, etc)

`react-native-music-controls` is meant for apps using those audio modules, although it has a few problems due to how the audio is not directly tied to the controls, it can be pretty useful for casting (such as Chromecast)

`react-native-google-cast` works pretty well and also supports custom receivers, but it has fewer player controls, it's harder to integrate and still uses the Cast SDK v2

## Example

If you want to get started with this module, check the [API](https://github.com/Guichaguri/react-native-track-player/wiki/API) page.
If you want detailed information about the API, check the [Documentation](https://github.com/Guichaguri/react-native-track-player/wiki/Documentation).
```javascript
import TrackPlayer from 'react-native-track-player';

// Creates the player
TrackPlayer.setupPlayer().then(() => {

    // Adds a track to the queue
    await TrackPlayer.add({
        id: 'trackId',
        url: require('track.mp3'),
        title: 'Track Title',
        artist: 'Track Artist',
        artwork: require('track.png')
    });

    // Starts playing it
    TrackPlayer.play();

});
```
