# UpdateOptions

All parameters are optional. You also only need to specify the ones you want to update.


| Param     | Type       | Description          | Android | iOS | Web |
| --------- | ---------- | -------------------- | ------- | --- | ------- |
| `ratingType` | [RatingType](../constants/rating.md) | The rating type | ✅ | ❌ | ❌ |
| `forwardJumpInterval` | `number` | The interval in seconds for the jump forward buttons (if only one is given then we use that value for both) | ✅ | ✅ | ✅ |
| `backwardJumpInterval` | `number` | The interval in seconds for the jump backward buttons (if only one is given then we use that value for both) | ✅ | ✅ | ✅ |
| `android` | [`AndroidOptions`](./android-options.md) | Whether the player will pause playback when the app closes | ✅ | ❌ | ❌ |
| `likeOptions` | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| `dislikeOptions` | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| `bookmarkOptions` | [FeedbackOptions](../objects/feedback.md) | The media controls that will be enabled | ❌ | ✅ | ❌ |
| `capabilities` | [Capability[]](../constants/capability.md) | The media controls that will be enabled | ✅ | ✅ | ❌ |
| `notificationCapabilities` | [Capability[]](../constants/capability.md) | The buttons that it will show in the notification. Defaults to `data.capabilities`  | ✅ | ❌ | ❌ |
| `color` | `number` | The notification color in an ARGB hex | ✅ | ❌ | ❌ |
| `progressUpdateEventInterval` | `number` | The interval (in seconds) that the [`Event.PlaybackProgressUpdated`](../events.md#playbackprogressupdated) will be fired. `undefined` by default. | ✅ | ✅ | ✅ |

*¹ - The custom icons will only work in release builds*
