---
sidebar_position: 1
---

# Events

All event types are made available through the named export `Event`:

```ts
import { Event } from 'react-native-track-player';
```

## Player

### `PlaybackState`
Fired when the state of the player changes.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| state | [State](./constants/state.md) | The new state |

### `PlaybackTrackChanged`
Fired when a track is changed.

| Param     | Type     | Description                            |
| --------- | -------- | -------------------------------------- |
| track     | `number` | The previous track index. Might be null   |
| position  | `number` | The previous track position in seconds |
| nextTrack | `number` | The next track index. Might be null       |

### `PlaybackQueueEnded`
Fired when the queue reaches the end.

| Param    | Type     | Description                               |
| -------- | -------- | ----------------------------------------- |
| track    | `number` | The previous track index. Might be null      |
| position | `number` | The previous track position in seconds    |

### `PlaybackMetadataReceived`
Fired when the current track receives metadata encoded in. (e.g. ID3 tags, Icy Metadata, Vorbis Comments or QuickTime metadata).

| Param    | Type     | Description                                         |
| -------- | -------- | --------------------------------------------------- |
| source   | `string` | The metadata source (`id3`, `icy`, `icy-headers`, `vorbis-comment`, `quicktime`) |
| title    | `string` | The track title. Might be null                      |
| url      | `string` | The track url. Might be null                        |
| artist   | `string` | The track artist. Might be null                     |
| album    | `string` | The track album. Might be null                      |
| date     | `string` | The track date. Might be null                       |
| genre    | `string` | The track genre. Might be null                      |

### `PlaybackProgressUpdated`

:warning: Note: This event is only emitted if you specify a non-zero `progressUpdateEventInterval` value in your player options.

Fired at the `progressUpdateEventInterval` if the player is playing _and_ if a `progressUpdateEventInterval` has been specified.

| Param       | Type     | Description       |
| ----------- | -------- | ----------------- |
| position    | `number` | See [`getPosition`](./functions/player.md#getposition)                 |
| duration    | `number` | See [`getDuration`](./functions/player.md#getduration)                 |
| buffer      | `number` | See [`getBufferedPosition`](./functions/player.md#getbufferedpostion)  |
| track       | `number` | The current index in the queue of the track.                           |

### `PlaybackError`
Fired when an error occurs.

| Param   | Type     | Description       |
| ------- | -------- | ----------------- |
| code    | `string` | The error code    |
| message | `string` | The error message |
---

## Media Controls

### `RemotePlay`
Fired when the user presses the play button. Only fired if the [`Capability.Play`](./constants/capability.md) is allowed.

### `RemotePlayId`
Fired when the user selects a track from an external device. Required for Android Auto support. Only fired if the [`Capability.PlayFromId`](./constants/capability.md) is allowed.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| id    | `string` | The track id  |

### `RemotePlaySearch`
Fired when the user searches for a track (usually voice search). Required for Android Auto support. Only fired if the [`Capability.PlayFromSearch`](./constants/capability.md) is allowed.

Every parameter except `query` is optional and may not be provided.
In the case where `query` is empty, feel free to select any track to play.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| query    | `string` | The search query |
| focus    | `string` | The focus of the search. One of `artist`, `album`, `playlist` or `genre` |
| title    | `string` | The track title |
| artist   | `string` | The track artist |
| album    | `string` | The track album |
| genre    | `string` | The track genre |
| playlist | `string` | The track playlist |

### `RemotePause`
Fired when the user presses the pause button. Only fired if the [`Capability.Pause`](./constants/capability.md) is allowed or if there's a change in outputs (e.g.: headphone disconnected).

### `RemoteStop`
Fired when the user presses the stop button. Only fired if the [`Capability.Stop`](./constants/capability.md) is allowed.

### `RemoteSkip`
Fired when the user skips to a track in the queue. Only fired if the [`Capability.Skip`](./constants/capability.md) is allowed.

| Param | Type     | Description   |
| ----- | -------- | ------------- |
| index | `number` | The track index  |

### `RemoteNext`
Fired when the user presses the next track button. Only fired if the [`Capability.SkipToNext`](./constants/capability.md) is allowed.

### `RemotePrevious`
Fired when the user presses the previous track button. Only fired if the [`Capability.SkipToPrevious`](./constants/capability.md) is allowed.

### `RemoteSeek`
Fired when the user changes the position of the timeline. Only fired if the [`Capability.SeekTo`](./constants/capability.md) is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| position | `number` | The position to seek to in seconds |

### `RemoteSetRating`
Fired when the user changes the rating for the track. Only fired if the [`Capability.SetRating`](./constants/capability.md) is allowed.

| Param  | Type     | Description   |
| ------ | -------- | ------------- |
| rating | Depends on the [Rating Type](./constants/rating.md) | The rating that was set |

### `RemoteJumpForward`
Fired when the user presses the jump forward button. Only fired if the [`Capability.JumpForward`](./constants/capability.md) is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| interval | `number` | The number of seconds to jump forward. It's usually the `forwardJumpInterval` set in the options. |

### `RemoteJumpBackward`
Fired when the user presses the jump backward button. Only fired if the [`Capability.JumpBackward`](./constants/capability.md) is allowed.

| Param    | Type     | Description   |
| -------- | -------- | ------------- |
| interval | `number` | The number of seconds to jump backward. It's usually the `backwardJumpInterval` set in the options. |

### `RemoteLike` (iOS only)
Fired when the user presses the like button in the now playing center. Only fired if the `likeOptions` is set in `updateOptions`.

### `RemoteDislike` (iOS only)
Fired when the user presses the dislike button in the now playing center. Only fired if the `dislikeOptions` is set in `updateOptions`.

### `RemoteBookmark` (iOS only)
Fired when the user presses the bookmark button in the now playing center. Only fired if the `bookmarkOptions` is set in `updateOptions`.

### `RemoteDuck`
Subscribing to this event to handle interruptions ensures that your app’s audio continues behaving gracefully when a phone call arrives, a clock or calendar alarm sounds, or another app plays audio.

On Android, this event is fired when the device needs the player to pause or stop for an interruption and again when the interruption has passed and playback may resume. On iOS this event is fired after playback was already interrupted (meaning pausing playback is unnecessary) and again when playback may resume or to notify that the interruption was permanent.

On Android, the volume may also be lowered on an transient interruption without triggering this event. If you want to receive those interruptions, set the `alwaysPauseOnInterruption` option to `true`.

- When the event is triggered with `paused` set to `true`, on Android the player should pause playback. When `permanent` is also set to `true`, on Android the player should stop playback.
- When the event is triggered and `paused` is not set to `true`, the player may resume playback.

| Param     | Type      | Description                                  |
| --------- | --------- | -------------------------------------------- |
| paused    | `boolean` | On Android when `true` the player should pause playback, when `false` the player may resume playback. On iOS when `true` the playback was paused and when `false` the player may resume playback. |
| permanent | `boolean` | Whether the interruption is permanent. On Android the player should stop playback.  |

Implementation examples can be found in the [example project](https://github.com/doublesymmetry/react-native-track-player/blob/main/example/src/services/PlaybackService.ts).