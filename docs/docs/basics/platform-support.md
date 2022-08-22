---
sidebar_position: 5
---

# Platform Support

## Audio Sources

| Feature | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| App bundle¹ | ✅ | ✅ | ✅ |
| Network | ✅ | ✅ | ✅ |
| File System² | ✅ | ✅ | ✅ |

¹: Use `require` or `import`

²: Prefix the file path with `file:///`

## Stream Types

| Feature | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| Regular Streams | ✅ | ✅ | ✅ |
| DASH | ✅ | ❌ | ✅ |
| HLS | ✅ | ✅ | ✅ |
| SmoothStreaming | ✅ | ❌ | ❌ |

## Casting

| Feature | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| Google Cast¹ | ✅ | ❌ | ❌ |
| Miracast/DLNA | ❌ | ❌ | ❌ |
| AirPlay | ❌ | ❌ | ❌ |

¹: Google Cast support has been moved to [react-native-track-casting (WIP)](https://github.com/react-native-kit/react-native-track-casting) which can be used in combination with `react-native-track-player`.

## Miscellaneous

| Feature | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| Media Controls | ✅ | ✅ | ✅ |
| Caching | ✅ | ❌ | ❌ |
| Background Mode¹ | ✅ | ✅ | ✅ |

¹: Read more in [Background Mode](./background-mode.md)

## Functions

| Function | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| `setupPlayer` | ✅ | ✅ | ✅ |
| `updateOptions` | ✅ | ✅ | ✅ |
| `registerPlaybackService` | ✅ | ✅ | ✅ |
| `addEventListener` | ✅ | ✅ | ✅ |
| `play` | ✅ | ✅ | ✅ |
| `pause` | ✅ | ✅ | ✅ |
| `reset` | ✅ | ✅ | ✅ |
| `setVolume` | ✅ | ✅ | ✅ |
| `getVolume` | ✅ | ✅ | ✅ |
| `setRate` | ✅ | ✅ | ✅ |
| `getRate` | ✅ | ✅ | ✅ |
| `seekTo` | ✅ | ✅ | ✅ |
| `getPosition` | ✅ | ✅ | ✅ |
| `getBufferedPosition` | ✅ | ✅ | ✅ |
| `getDuration` | ✅ | ✅ | ✅ |
| `getState` | ✅ | ✅ | ✅ |
| `getQueue` | ✅ | ✅ | ✅ |
| `getCurrentTrack` | ✅ | ✅ | ✅ |
| `getTrack` | ✅ | ✅ | ✅ |
| `add` | ✅ | ✅ | ✅ |
| `remove` | ✅ | ✅ | ✅ |
| `skip` | ✅ | ✅ | ✅ |
| `skipToPrevious` | ✅ | ✅ | ✅ |
| `skipToNext` | ✅ | ✅ | ✅ |
| `removeUpcomingTracks` | ✅ | ✅ | ✅ |

## Events

| Event | Android | iOS | Windows |
| ------- | :-----: | :-: | :-----: |
| `remote-play` | ✅ | ✅ | ✅ |
| `remote-play-id` | ✅ | ❌ | ❌ |
| `remote-play-search` | ✅ | ❌ | ❌ |
| `remote-pause` | ✅ | ✅ | ✅ |
| `remote-stop` | ✅ | ✅ | ✅ |
| `remote-skip` | ✅ | ❌ | ❌ |
| `remote-next` | ✅ | ✅ | ✅ |
| `remote-previous` | ✅ | ✅ | ✅ |
| `remote-seek` | ✅ | ✅ | ✅ |
| `remote-set-rating` | ✅ | ❌ | ❌ |
| `remote-jump-forward` | ✅ | ✅ | ✅ |
| `remote-jump-backward` | ✅ | ✅ | ✅ |
| `remote-duck` | ✅ | ✅ | ❌ |
| `playback-state` | ✅ | ✅ | ✅ |
| `playback-track-changed` | ✅ | ✅ | ✅ |
| `playback-queue-ended` | ✅ | ✅ | ✅ |
| `playback-error` | ✅ | ✅ | ✅ |
| `playback-metadata-received` | ✅ | ✅ | ❌ |
