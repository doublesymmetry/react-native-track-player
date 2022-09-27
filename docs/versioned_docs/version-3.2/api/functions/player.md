# Player

## `setupPlayer(options)`

Accepts a [`PlayerOptions`](../objects/player-options.md) object.

## `updateOptions(options)`

Accepts a [`MetadataOptions`](../objects/metadata-options.md) object. Updates
the configuration for the components.


:warning: These parameters are different than the ones set using `setupPlayer()`.
Parameters other than those listed below will not be applied.

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
Gets the position of the current track in seconds.

**Returns:** `Promise<number>`

## `getBufferedPosition()`
Gets the buffered position of the current track in seconds.

**Returns:** `Promise<number>`

## `getState()`
Gets the playback [`State`](../constants/state.md) of the player.

**Returns:** `Promise<`[State](../constants/state.md)`>`
