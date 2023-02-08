---
sidebar_position: 8
---

# Migrating from v3.2 to v4


### `alwaysPauseOnInterruption` has been moved to [`AndroidOptions`](./api/objects/android-options.md)

```diff
await TrackPlayer.updateOptions({
+      android: {
+        alwaysPauseOnInterruption: true,
+      },
-      alwaysPauseOnInterruption: true,
}
```

### `usePlaybackState` initially returns `undefined`

Have the [`usePlaybackState()`](./api/hooks.md##useplaybackstate) hook will
return `{ state: undefined}` initially before it has finished retrieving the
current state. Before it was incorrectly returning
[`State.None`](./api/constants/state.md) which means no track is loaded.

### `getTrack` return type

[`getTrack()`](./api/functions/queue.md#gettrack) now returns  `undefined`
instead of `null`

### Player `State` Updates

1. [`State.Error`](./api/constants/state.md) - Emitted when an error state is encountered.
1. [`State.Ended`](./api/constants/state.md) - State indicates playback stopped due to the end of the queue being reached.
1. [`State.Loading`](./api/constants/state.md) - State indicating the initial loading phase of a track.
1. [`State.Buffering`](./api/constants/state.md) - Now emitted no matter whether playback is paused or not.
1. [`State.Connecting`](./api/constants/state.md) -  Deprecated. Please use `State.Loading` instead.

### General Deprecations

1. `getState()` - Please use the `state` property returned by [`getPlaybackState()`](./api/functions/player.md#getplaybackstate).
1. `getDuration()` -  Please use the `duration` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
1. `getPosition()` -  Please use the `position` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
1. `getBufferedPosition()` -  Please use the `buffered` property returned by [`getProgress()`](./api/functions/player.md#getprogress).
1. `Event.PlaybackTrackChanged` - Please use [`Event.PlaybackActiveTrackChanged`](./api/events.md#playbackactivetrackchanged).

### Typescript Deep Imports

If you were using deep imports from RNTP, the `src` has been completely
reorganized, and so you may need to adjust your imports accordingly. If you've
been importing everything directly (ex. `import ... from 'react-native-track-player';`)
then you don't need to do anything.
