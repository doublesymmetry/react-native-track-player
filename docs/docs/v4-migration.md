---
sidebar_position: 8
---

# Migrating from v3.2 to v4

### `Capabilities` have been updated to include inline data about its configuration
Before capabilities were split into a combination of `capabilities`, `notificationCapabilities` and `compactCapabilities`. These controlled different aspects of an audio player's configuration and were not clear nor consistent. Furthermore icons for notification actions were separately set by individual properties (i.e. `playIcon`, `pauseIcon`, etc). Now all of this information is contained in the `Capabilities` object.

For more information on Capabilities see the [documentation](./api/objects/capabilities.md).

```diff
await TrackPlayer.updateOptions({
+      capabilities: [
+        Capability.Play(),
+        Capability.Pause(),
+        Capability.SkipToNext(),
+        Capability.SkipToPrevious(true, { compact: false, icon: require('./previous.png') }),
+        Capability.SeekTo,
+      ],
-      capabilities: [
-        Capability.Play,
-        Capability.Pause,
-        Capability.SkipToNext,
-        Capability.SkipToPrevious,
-        Capability.SeekTo,
-      ],
-      compactCapabilities: [
-        Capability.Play,
-        Capability.Pause,
-        Capability.SkipToNext,
-      ],
-      previousIcon: require('./previous.png'),
```

### `alwaysPauseOnInterruption` has been moved to [`AndroidOptions`](./api/objects/android-options.md)

```diff
await TrackPlayer.updateOptions({
+      android: {
+        alwaysPauseOnInterruption: true,
+      },
-      alwaysPauseOnInterruption: true,
}
```

### `icon` and `color` have been moved to [`NotificationOptions`](./api/objects/notification-options.md) under [`AndroidOptions`](./api/objects/android-options.md)

```diff
await TrackPlayer.updateOptions({
+      android: {
+        notificationOptions: {
+          color: '#ff0000',
+          icon: require('./icon.png'),
+        },
+      },
-      color: '#ff0000',
-      icon: require('./icon.png'),
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
1. `getCurrentTrack()` - Please use [`getActiveTrackIndex()`](./api/functions/queue.md#getactivetrackindex).
1. `Event.PlaybackTrackChanged` - Please use [`Event.PlaybackActiveTrackChanged`](./api/events.md#playbackactivetrackchanged). Also note that in 4.0 `Event.PlaybackTrackChanged` is no longer emitted when a track repeats.

### Typescript Imports

1. If you were using deep imports from RNTP, the `src` has been completely
reorganized, and so you may need to adjust your imports accordingly. If you've
been importing everything directly (ex. `import ... from 'react-native-track-player';`)
then you don't need to do anything.
1. The `PlaybackStateEvent` interface has been renamed to `PlaybackState`
