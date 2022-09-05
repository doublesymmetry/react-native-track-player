# Queue

## `add(tracks, insertBeforeIndex)`
Adds one or more tracks to the queue.

**Returns:** `Promise<number | void>` - The promise resolves with the first
added track index. If no tracks were added it returns `void`.

| Param          | Type     | Description   |
| -------------- | -------- | ------------- |
| tracks         | `array` of [Track Object](../objects/track.md) or a single one | The tracks that will be added |
| insertBeforeIndex | `number` | The index of the track that will be located immediately after the inserted tracks. Set it to `null` to add it at the end of the queue |

## `remove(tracks)`
Removes one or more tracks from the queue.

**Returns:** `Promise<void>`

| Param  | Type     | Description   |
| ------ | -------- | ------------- |
| tracks | `array` of track indexes or a single one | The tracks that will be removed |

## `skip(index)`
Skips to a track in the queue.

**Returns:** `Promise<void>`

| Param  | Type     | Description     |
| ------ | -------- | --------------- |
| index  | `number` | The track index |

## `skipToNext()`
Skips to the next track in the queue.

**Returns:** `Promise<void>`

## `skipToPrevious()`
Skips to the previous track in the queue.

**Returns:** `Promise<void>`

## `reset()`
Resets the player stopping the current track and clearing the queue.

## `getTrack(index)`
Gets a track object from the queue.

**Returns:** `Promise<`[Track](../objects/track.md)`>`

| Param    | Type       | Description     |
| -------- | ---------- | --------------- |
| index    | `number`   | The track index |

## `getCurrentTrack()`
Gets the index of the current track

**Returns:** `Promise<number>`

## `getQueue()`
Gets the whole queue

**Returns:** `Promise<`[Track[]](../objects/track.md)`>`

## `removeUpcomingTracks()`
Clears any upcoming tracks from the queue.

## `updateMetadataForTrack(index, metadata)`
Updates the metadata of a track in the queue.
If the current track is updated, the notification and the Now Playing Center will be updated accordingly.

**Returns:** `Promise<void>`

| Param    | Type       | Description   |
| -------- | ---------- | ------------- |
| index    | `number`   | The track index  |
| metadata | `object`   | A subset of the [Track Object](../objects/track.md) with only the `artwork`, `title`, `artist`, `album`, `description`, `genre`, `date`, `rating` and `duration` properties. |

## `setRepeatMode(mode)`
Sets the repeat mode.

| Param    | Type       | Description     |
| -------- | ---------- | --------------- |
| mode     | [Repeat Mode](../constants/repeat-mode.md) | The repeat mode |

## `getRepeatMode()`
Gets the repeat mode.

**Returns:** [Repeat Mode](../constants/repeat-mode.md)
