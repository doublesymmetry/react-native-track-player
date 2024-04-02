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
| `compactCapabilities` | [Capability[]](../constants/capability.md) | The buttons that it will show in the compact notification | ✅ | ❌ | ❌ |
| `icon` | [Resource Object](../objects/resource.md) | The notification icon¹ | ✅ | ❌ | ❌ |
| `playIcon` | [Resource Object](../objects/resource.md) | The play icon¹ | ✅ | ❌ | ❌ |
| `pauseIcon` | [Resource Object](../objects/resource.md) | The pause icon¹ | ✅ | ❌ | ❌ |
| `stopIcon` | [Resource Object](../objects/resource.md) | The stop icon¹ | ✅ | ❌ | ❌ |
| `previousIcon` | [Resource Object](../objects/resource.md) | The previous icon¹ | ✅ | ❌ | ❌ |
| `nextIcon` | [Resource Object](../objects/resource.md) | The next icon¹ | ✅ | ❌ | ❌ |
| `rewindIcon` | [Resource Object](../objects/resource.md) | The jump backward icon¹ | ✅ | ❌ | ❌ |
| `forwardIcon` | [Resource Object](../objects/resource.md) | The jump forward icon¹ | ✅ | ❌ | ❌ |
| `color` | `number` | The notification color in an ARGB hex | ✅ | ❌ | ❌ |
| `progressUpdateEventInterval` | `number` | The interval (in seconds) that the [`Event.PlaybackProgressUpdated`](../events.md#playbackprogressupdated) will be fired. `undefined` by default. | ✅ | ✅ | ✅ |
| `iosCategory` | [`IOSCategory`](../constants/ios-category.md) | An [`IOSCategory`](../constants/ios-category.md). | ❌ | ✅ | ❌ |
| `iosCategoryMode` | [`IOSCategoryMode`](../constants/ios-category-mode.md) | The audio session mode, together with the audio session category, indicates to the system how you intend to use audio in your app. You can use a mode to configure the audio system for specific use cases such as video recording, voice or video chat, or audio analysis. | ❌ | ✅ | ❌ |
| `iosCategoryOptions` | [`IOSCategoryOptions[]`](../constants/ios-category-options.md) | An array of [`IOSCategoryOptions`](../constants/ios-category-options.md). | ❌ | ✅  | ❌ |
| `androidAudioUsageType` | `boolean` | (Android only) Specifies why the source is playing and controls routing, focus, and volume decisions. With `androidAudioUsageType` set to VoiceCommunication the audio will come from the earpiece speaker | ✅ | ❌ |
| `androidAudioContentType` | `boolean` | The audio content type indicates to the android system how you intend to use audio in your app. With `autoHandleInterruptions: true` and `androidAudioContentType: AndroidAudioContentType.Speech`, the audio will be paused during short interruptions, such as when a message arrives. Otherwise the playback volume is reduced while the notification is playing. Defaults to `AndroidAudioContentType.Music` | ✅ | ❌ |

*¹ - The custom icons will only work in release builds*
