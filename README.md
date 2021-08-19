<img src="docs/assets/optimized-logo.svg" width="300" />

[![downloads](https://img.shields.io/npm/dw/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player) 
[![discord](https://img.shields.io/discord/567636850513018880.svg)](https://discordapp.com/invite/ya2XDCR)

A fully-fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!

---

## ⚠️ V2 Migration Guide

All queue methods have been updating to work on indexes instead of id's. We want this library to support all kinds of apps -- and moving to be index based will allow us to better support applications who have long/endless queues and in the future to allow us to build a performant API around queue management.

We recommend using Typescript to have the system alert you of issues.

When migrating from v1 to v2, the following has changed:

```diff
// Methods

- async function add(tracks: Track | Track[], insertBeforeId?: string): Promise<void> {
+ async function add(tracks: Track | Track[], insertBeforeIndex?: number): Promise<void> {

- async function remove(tracks: string | string[]): Promise<void> {
+ async function remove(tracks: number | number[]): Promise<void> {

- async function skip(trackId: string): Promise<void> {
+ function skip(trackIndex: number): Promise<void> {

- async function updateMetadataForTrack(trackId: string, metadata: TrackMetadataBase): Promise<void> {
+ async function updateMetadataForTrack(trackIndex: number, metadata: TrackMetadataBase): Promise<void> {

- async function getTrack(trackId: string): Promise<Track> {
+ async function getTrack(trackIndex: number): Promise<Track> {

- async function getCurrentTrack(): Promise<string> {
+ async function getCurrentTrack(): Promise<number> {

// Imports

import TrackPlayer, {
-  STATE_XXX,
-  CAPABILITY_XXX,
-  PITCH_ALGORITHM_XXX,
-  RATING_XXX,
+  State,
+  Capability,
+  PitchAlgorithm,
+  RatingType,
+  Event,
+  RepeatMode
} from 'react-native-track-player'

// Hooks

- useTrackPlayerProgress
+ useProgress

// Event Listeners
// Refrain from using: TrackPlayer.addEventListener() and instead use the provided hooks

+ usePlaybackState
+ useTrackPlayerEvents
+ useProgress
```

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

## Quick Guides

* [Installation](https://react-native-track-player.js.org/install/)
* [Getting Started](https://react-native-track-player.js.org/getting-started/)
* [Documentation](https://react-native-track-player.js.org/documentation/)
* [Platform Support](https://react-native-track-player.js.org/platform-support/)
* [Background Mode](https://react-native-track-player.js.org/background/)
* [Build Preferences](https://react-native-track-player.js.org/build-preferences/)

## Why another music module?
After trying to team up modules like `react-native-sound`, `react-native-music-controls` and `react-native-google-cast`, I've noticed, that their structure and the way should be tied together can cause a lot of problems (mainly on Android). Those can heavily affect the app stability and user experience.

All audio modules (like `react-native-sound`) don't play in a separated service on Android, which should **only** be used for simple audio tracks in the foreground (such as sound effects, voice messages, etc.)

`react-native-music-controls` is meant for apps using those audio modules, but it has a few problems: the audio isn't tied directly to the controls. It can be pretty useful for casting (such as Chromecast).

`react-native-google-cast` works pretty well and also supports custom receivers, but it has fewer player controls, it's harder to integrate and still uses the Cast SDK v2.

## First Steps

If you want to get started with this module, check the [Getting Started](https://react-native-track-player.js.org/getting-started/) page.

If you want detailed information about the API, check the [Documentation](https://react-native-track-player.js.org/documentation/). You can also look at our example project [here](/example).

## Example

A basic example of how to play a track:

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
    <td align="center"><a href="https://github.com/dchavezlive"><img src="https://avatars.githubusercontent.com/u/2475932?v=4" width="100px;" alt=""/><br /><sub><b>David Chavez</b></sub></a><br /></td>
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
