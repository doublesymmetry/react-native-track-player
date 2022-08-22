# Player

## `updateOptions(options)`
Updates the configuration for the components.

All parameters are optional. You also only need to specify the ones you want to update.

These parameters are different than the ones set using `setupPlayer()`. Parameters other than those listed below will not be applied.

Some parameters are unused depending on platform.

| Param     | Type       | Description          | Android | iOS | Windows |
| --------- | ---------- | -------------------- | :-----: | :-: | :-----: |
| options      | `MetadataOptions`   | The options |
| options.ratingType | [RatingType](../constants/rating.md) | The rating type | ✅ | ❌ | ❌ |
| options.forwardJumpInterval | `number` | The interval in seconds for the jump forward buttons (if only one is given then we use that value for both) | ✅ | ✅ | ❌ |
| options.backwardJumpInterval | `number` | The interval in seconds for the jump backward buttons (if only one is given then we use that value for both) | ✅ | ✅ | ✅ |
| options.stoppingAppPausesPlayback | `boolean` | Whether the player will pause playback when the app closes | ✅ | ❌ | ❌ |
| options.alwaysPauseOnInterruption | `boolean` | Whether the `remote-duck` event will be triggered on every interruption | ✅ | ❌ | ❌ |
| options.likeOptions | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| options.dislikeOptions | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| options.bookmarkOptions | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| options.capabilities | [Capability[]](../constants/capability.md) | The media controls that will be enabled | ✅ | ✅ | ✅ |
| options.notificationCapabilities | [Capability[]](../constants/capability.md) | The buttons that it will show in the notification. Defaults to `data.capabilities`  | ✅ | ❌ | ❌ |
| options.compactCapabilities | [Capability[]](../constants/capability.md) | The buttons that it will show in the compact notification | ✅ | ❌ | ❌ |
| options.icon | [Resource Object](../objects/resource.md) | The notification icon¹ | ✅ | ❌ | ❌ |
| options.playIcon | [Resource Object](../objects/resource.md) | The play icon¹ | ✅ | ❌ | ❌ |
| options.pauseIcon | [Resource Object](../objects/resource.md) | The pause icon¹ | ✅ | ❌ | ❌ |
| options.stopIcon | [Resource Object](../objects/resource.md) | The stop icon¹ | ✅ | ❌ | ❌ |
| options.previousIcon | [Resource Object](../objects/resource.md) | The previous icon¹ | ✅ | ❌ | ❌ |
| options.nextIcon | [Resource Object](../objects/resource.md) | The next icon¹ | ✅ | ❌ | ❌ |
| options.rewindIcon | [Resource Object](../objects/resource.md) | The jump backward icon¹ | ✅ | ❌ | ❌ |
| options.forwardIcon | [Resource Object](../objects/resource.md) | The jump forward icon¹ | ✅ | ❌ | ❌ |
| options.color | `number` | The notification color in an ARGB hex | ✅ | ❌ | ❌ |
| options.progressUpdateEventInterval | `number` | The interval (in seconds) that the [`Event.PlaybackProgressUpdated`](../events.md#playbackprogressupdated) will be fired. `undefined` by default. | ✅ | ✅ | ❌ |

*¹ - The custom icons will only work in release builds*

## `play()`
Plays or resumes the current track.

## `pause()`
Pauses the current track.

## `seekTo(seconds)`
Seeks to a specified time position in the current track.

| Param   | Type     | Description             |
| ------- | -------- | ----------------------- |
| seconds | `number` | The position in seconds |

## `setVolume(volume)`
Sets the volume of the player.

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| volume | `number` | The volume in a range from 0 to 1 |

## `getVolume()`
Gets the volume of the player (a number between 0 and 1).

**Returns:** `Promise<number>`

## `setRate(rate)`
Sets the playback rate

| Param  | Type     | Description                       |
| ------ | -------- | --------------------------------- |
| rate   | `number` | The playback rate where 1 is the regular speed |

## `getRate()`
Gets the playback rate, where 1 is the regular speed.

**Returns:** `Promise<number>`

## `getDuration()`
Gets the duration of the current track in seconds.

Note: `react-native-track-player` is a streaming library, which means it slowly buffers the track and doesn't know exactly when it ends.
The duration returned by this function is determined through various tricks and *may not be exact or may not be available at all*.

You should only trust the result of this function if you included the `duration` property in the [Track Object](../objects/track.md).

**Returns:** `Promise<number>`

## `getPosition()`
Gets the position of the player in seconds.

**Returns:** `Promise<number>`

## `getBufferedPosition()`
Gets the buffered position of the player in seconds.

**Returns:** `Promise<number>`

## `getState()`
Gets the state of the player.

**Returns:** `Promise<`[State](../constants/state.md)`>`
