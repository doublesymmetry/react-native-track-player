---
title: Overview
description: "A fully fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!"
nav_order: 1
nav_exclude: true
permalink: /
---

[![downloads](https://img.shields.io/npm/dw/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![discord](https://img.shields.io/discord/567636850513018880.svg)](https://discordapp.com/invite/ya2XDCR)

# react-native-track-player

A fully fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!

[Getting Started](./API.md){: .btn .btn-blue }
[API Documentation](./Documentation.md){: .btn }

## Features

* **Lightweight** - Optimized to use the least amount of resources according to your needs
* **Feels native** - As everything is built together, it follows the same design principles as real music apps do
* **Multi-platform** - Supports Android, iOS and Windows
* **Media Controls support** - Provides events for controlling the app from a bluetooth device, the lockscreen, a notification, a smartwatch or even a car
* **Local or network, files or streams** - It doesn't matter where the media belongs, we've got you covered
* **Adaptive bitrate streaming support** - Support for DASH, HLS or SmoothStreaming
* **Caching support** - Cache media files to play them again without an internet connection
* **Background support** - Keep playing audio even after the app is in background
* **Fully Customizable** - Even the notification icons are customizable!
* **Supports React Hooks 🎣** - Includes React Hooks for common use-cases so you don't have to write them
* **Casting support** - Use in combination with [react-native-track-casting (WIP)](https://github.com/react-native-kit/react-native-track-casting) to seamlessly switch to any Google Cast compatible device that supports custom media receivers

## Example

If you want to get started with this module, check the [Getting Started](https://react-native-track-player.js.org/react-native-track-player/api/) page.
If you want detailed information about the API, check the [Documentation](https://react-native-track-player.js.org/react-native-track-player/documentation/).
You can also look at our example project [here](https://github.com/react-native-kit/react-native-track-player/tree/master/example).

```javascript
import TrackPlayer, { RepeatMode } from 'react-native-track-player';

// Creates the player
const setup = async () => {
  await TrackPlayer.setupPlayer({});

  await TrackPlayer.add({
    url: require('track.mp3'),
    title: 'Track Title',
    artist: 'Track Artist',
    artwork: require('track.png')
  });

  TrackPlayer.setRepeatMode(RepeatMode.Queue);
};
```

## Maintainers

[David Chavez](https://github.com/dcvz) under [Double Symmetry](https://doublesymmetry.com)

## Other/Previous Maintainers

[Guilherme Chaguri](https://github.com/Guichaguri), [Dustin Bahr](https://github.com/curiousdustin)
