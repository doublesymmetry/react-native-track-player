# PlaybackErrorEvent

This error event is the error emitted by the native device itself. Both
the code and value will correspond to error codes emitted by the underlying
native players.

| Property | Type     | Description |
|----------|----------|-------------|
| code     | `string` | The code values are strings prefixed with `android_` on Android and `ios_` on iOS. |
| message  | `string` | The error message emitted by the native player. |
