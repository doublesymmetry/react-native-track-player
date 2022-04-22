<img src="docs/assets/optimized-logo.svg" width="300" />

[![downloads](https://img.shields.io/npm/dw/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![discord](https://img.shields.io/discord/567636850513018880.svg)](https://discordapp.com/invite/ya2XDCR)

### 📢 The Android side of RN track player is currently being rewritten with Kotlin and [KotlinAudio](https://github.com/DoubleSymmetry/KotlinAudio). Help us test! [More information here.](https://github.com/DoubleSymmetry/react-native-track-player/discussions/1264)

----

A fully-fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!

- [Documentation](https://react-native-track-player.js.org)
  * [Installation](https://react-native-track-player.js.org/install/)
  * [Getting Started](https://react-native-track-player.js.org/getting-started/)
  * [API Docs](https://react-native-track-player.js.org/documentation/)
  * [Platform Support](https://react-native-track-player.js.org/platform-support/)
  * [Background Mode](https://react-native-track-player.js.org/background/)
  * [Build Preferences](https://react-native-track-player.js.org/build-preferences/)
  * [v2 Migration Guide](https://react-native-track-player.js.org/v2-migration/)
- [Sponsors](#sponsors)
- [Features](#features)
- [Why another music module?](#why-another-music-module)
- [Example Setup](#example-setup)
- [Core Team ✨](#core-team-)
- [Special Thanks ✨](#special-thanks-)
- [Community](#Community)

Not sure where to start?

1. Try [Getting Started](https://react-native-track-player.js.org/getting-started/).
2. Peruse the [API Docs](https://react-native-track-player.js.org/documentation/).
3. Run the [Example Project](/example).

## Sponsors

react-native-track-player is made possible by the generosity of the sponsors below, and many other [individual backers](docs/backers-sponsors.md#backers). Sponsoring directly impacts the longevity of this project.

#### 🥇 Gold sponsors (\$2000+ total contributions)

<table>
  <tr>
    <td align="center">
      <a href="http://radio.garden/">
        <img src="https://avatars.githubusercontent.com/u/271885?v=4" align="center" width="100" title="Radio Garden" alt="Radio Garden">
        <br /><sub><b>Radio Garden</b></sub>
      </a>
    </td>
  </tr>
</table>

#### 🥈 Silver sponsors (\$200+ per month)

<table>
  <tr>
    <td align="center">
      <a href="http://www.voxist.com/">
        <img src="https://avatars.githubusercontent.com/u/18028734?s=200&v=4" align="center" width="100" title="Voxist" alt="Voxist">
        <br /><sub><b>Voxist</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://evergrace.co">
        <img src="https://avatars.githubusercontent.com/u/1085976?v=4" align="center" width="100" title="Evergrace" alt="Evergrace">
        <br /><sub><b>Evergrace</b></sub>
      </a>
    </td>
  </tr>
</table>


#### 🥉 Bronze sponsors (\$500+ total contributions)
[Become the first bronze sponsor!](https://github.com/sponsors/DoubleSymmetry)

#### ✨ Contributing sponsors (\$25+ per month)

<table>
  <tr>
    <td align="center">
      <a href="https://podverse.fm"><img src="https://avatars.githubusercontent.com/u/11860029?s=200&v=4" align="center" width="100" title="Podverse" alt="Podverse"></a>
      <br /><sub><b>Podverse</b></sub>
    </td>
  </tr>
</table>

---

## Features

* **Lightweight** - Optimized to use the least amount of resources according to your needs
* **Feels native** - As everything is built together, it follows the same design principles as real music apps do
* **Multi-platform** - Supports Android, iOS and Windows
* **Media Controls support** - Provides events for controlling the app from a Bluetooth device, the lock screen, a notification, a smartwatch or even a car
* **Local or network, files or streams** - It doesn't matter where the media belongs, we've got you covered
* **Adaptive bitrate streaming support** - Support for DASH, HLS or SmoothStreaming
* **Caching support** - Cache media files to play them again without an internet connection
* **Background support** - Keep playing audio even after the app is in background
* **Fully Customizable** - Even the notification icons are customizable!
* **Supports React Hooks 🎣** - Includes React Hooks for common use-cases so you don't have to write them

## Why another music module?
After trying to team up modules like `react-native-sound`, `react-native-music-controls` and `react-native-google-cast`, I've noticed, that their structure and the way should be tied together can cause a lot of problems (mainly on Android). Those can heavily affect the app stability and user experience.

All audio modules (like `react-native-sound`) don't play in a separated service on Android, which should **only** be used for simple audio tracks in the foreground (such as sound effects, voice messages, etc.)

`react-native-music-controls` is meant for apps using those audio modules, but it has a few problems: the audio isn't tied directly to the controls. It can be pretty useful for casting (such as Chromecast).

`react-native-google-cast` works pretty well and also supports custom receivers, but it has fewer player controls, it's harder to integrate and still uses the Cast SDK v2.

## Example Setup

First please take a look at the [Getting Started](https://react-native-track-player.js.org/getting-started/) guide, but a basic example of how to play a track:

```javascript
import TrackPlayer from 'react-native-track-player';

const start = async () => {
    // Set up the player
    await TrackPlayer.setupPlayer();

    // Add a track to the queue
    await TrackPlayer.add({
        id: 'trackId',
        url: require('track.mp3'),
        title: 'Track Title',
        artist: 'Track Artist',
        artwork: require('track.png')
    });

    // Start playing it
    await TrackPlayer.play();
};
start();
```

## Core Team ✨

<table>
  <tr>
    <td align="center"><a href="https://github.com/dcvz"><img src="https://avatars.githubusercontent.com/u/2475932?v=4" width="100px;" alt=""/><br /><sub><b>David Chavez</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/mpivchev"><img src="https://avatars.githubusercontent.com/u/6960329?v=4" width="100px;" alt=""/><br /><sub><b>Milen Pivchev</b></sub></a><br /></td>
    https://react-native-track-player.js.org/build-preferences/
  </tr>
</table>

## Special Thanks ✨

<table>
  <tr>
    <td align="center"><a href="https://github.com/Guichaguri"><img src="https://avatars.githubusercontent.com/u/1813032?v=4" width="100px;" alt=""/><br /><sub><b>Guilherme Chaguri</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/curiousdustin"><img src="https://avatars.githubusercontent.com/u/1706540?v=4" width="100px;" alt=""/><br /><sub><b>Dustin Bahr</b></sub></a><br /></td>
  </tr>
</table>

## Community

You can find us as part of the [React Native Folks](https://discordapp.com/invite/ya2XDCR) Discord in the `#react-native-track-player` channel.
