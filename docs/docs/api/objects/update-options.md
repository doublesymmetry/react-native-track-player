# UpdateOptions

All parameters are optional. You also only need to specify the ones you want to update.


| Param                         | Type                                       | Description                                                                                                                                       | Android | iOS | Windows |
|-------------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|---------|-----|---------|
| `android`                     | [`AndroidOptions`](./android-options.md)   | Android specific configuration options                                                                                                            | ✅       | ❌   | ❌       |
| `capabilities`                | [Capability[]](../constants/capability.md) | The media controls that will be enabled                                                                                                           | ✅       | ✅   | ✅       |
| `progressUpdateEventInterval` | `number`                                   | The interval (in seconds) that the [`Event.PlaybackProgressUpdated`](../events.md#playbackprogressupdated) will be fired. `undefined` by default. | ✅       | ✅   | ❌       |

*¹ - The custom icons will only work in release builds*
