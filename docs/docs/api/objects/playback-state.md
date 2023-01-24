# PlaybackState

An object to represent the full state of the player.

| Property       | Type                        | Description  |
| -------------- | --------------------------- | ------------ |
| state          | [`State`](../constants/state.md) | The current state of the player. |
| error          | [`PlaybackErrorEvent`](./playback-error-event.md) \| `undefined` | If the `state` is type `Error` a [`PlaybackErrorEvent`](./playback-error-event.md) will be present. Else `undefined`.|
