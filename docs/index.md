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

If you want to get started with this module, check the [Getting Started](https://react-native-track-player.js.org/getting-started/) page.
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

## ⚠️ V2 Migration Guide

All queue methods have been updating to work on indexes instead of id's. We want this library to support all kinds of apps -- and moving to be index based will allow us to better support applications who have long/endless queues and in the future to allow us to build a performant API around queue management.

When migrating from v1 to v2, the following methods have changed:

```diff
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
```

## Core Team

[David Chavez](https://github.com/dcvz) under [Double Symmetry](https://doublesymmetry.com)

## Special Thanks

[Guilherme Chaguri](https://github.com/Guichaguri), [Dustin Bahr](https://github.com/curiousdustin)
