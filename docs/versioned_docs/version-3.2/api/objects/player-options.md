# PlayerOptions

All parameters are optional. You also only need to specify the ones you want to update.

| Param | Type | Description | Android | iOS |
|-------|------|-------------|---------|-----|
| `minBuffer` | `number` | Minimum time in seconds that needs to be buffered. | ✅ | ✅ |
| `maxBuffer` | `number` | Maximum time in seconds that needs to be buffered. | ✅ | ✅ |
| `backBuffer` | `number` | Time in seconds that should be kept in the buffer behind the current playhead time. | ✅ | ✅ |
| `playBuffer` | `number` | Minimum time in seconds that needs to be buffered to start playing. | ✅ | ✅ |
| `maxCacheSize` | `number` | Maximum cache size in kilobytes. | ✅ | ✅ |
| `iosCategory` | [`IOSCategory`](../constants/ios-category.md) | An [`IOSCategory`](../constants/ios-category.md). Sets on `play()`. | ❌ | ✅  |
| `iosCategoryMode` | [`IOSCategoryMode`](../constants/ios-category-mode.md) | The audio session mode, together with the audio session category, indicates to the system how you intend to use audio in your app. You can use a mode to configure the audio system for specific use cases such as video recording, voice or video chat, or audio analysis. Sets on `play()`. | ❌ | ✅  |
| `iosCategoryOptions` | [`IOSCategoryOptions[]`](../constants/ios-category-options.md) | An array of [`IOSCategoryOptions`](../constants/ios-category-options.md). Sets on `play()`. | ❌ | ✅  |
| `waitForBuffer` | `boolean` | Indicates whether the player should automatically delay playback in order to minimize stalling. Defaults to `true`. @deprecated This option has been nominated for removal in a future version of RNTP. If you have this set to `true`, you can safely remove this from the options. If you are setting this to `false` and have a reason for that, please post a comment in the following discussion: https://github.com/doublesymmetry/react-native-track-player/pull/1695 and describe why you are doing so. | ✅ | ✅ |
| `autoUpdateMetadata` | `boolean` | Indicates whether the player should automatically update now playing metadata data in control center / notification. Defaults to `true`. | ✅ | ✅ |
