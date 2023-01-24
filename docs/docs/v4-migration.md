---
sidebar_position: 8
---

# Migrating from v3.2 to v4


### `alwaysPauseOnInterruption` has been moved to `AndroidOptions`

```diff
await TrackPlayer.updateOptions({
+      android: {
+        alwaysPauseOnInterruption: true,
+      },
-      alwaysPauseOnInterruption: true,
}
```

### `usePlaybackState` initially returns `undefined`

Have the `usePlaybackState()` hook will return `{ state: undefined}` initially
before it has finished retrieving the current state. Before it was incorrectly
returning `State.None` which means no track is loaded.

### `getTrack` return type

`getTrack()` now returns  `undefined` instead of `null`

### Player `State` Updates

1. `State.Error` - Emitted when an error state is encountered.
1. `State.Ended` - State indicates playback stopped due to the end of the queue being reached.
1. `State.Loading` - State indicating the initial loading phase of a track.
1. `State.Buffering` - Now emitted no matter whether playback is paused or not.
1. `State.Connecting` -  Deprecated. Please use `State.Loading` instead.

### General Deprecations

1. `getState()` - Please use the `state` property returned by `getPlaybackState()`.
1. `getDuration()` -  Please use the `duration` property returned by `getPlaybackState()`.
1. `getPosition()` -  Please use the `position` property returned by `getPlaybackState()`.
1. `getBufferedPosition()` -  Please use the `buffered` property returned by `getProgress()`.
1. `Event.PlaybackTrackChanged` - Please use `Event.PlaybackActiveTrackChanged`.

### Typescript Deep Imports

If you were using deep imports from RNTP, the `src` has been completely
reorganized, and so you may need to adjust your imports accordingly. If you've
been importing everything directly (ex. `import ... from 'react-native-track-player';`)
then you don't need to do anything.
