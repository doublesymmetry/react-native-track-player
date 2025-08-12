# Changelog

## [5.0.0-alpha0](https://github.com/doublesymmetry/react-native-track-player/compare/v4.1.1...v5.0.0-alpha0) (2025-08-12)

### Features

* **android, ios:** migrate the library to the new architecture ([5ce8412](https://github.com/doublesymmetry/react-native-track-player/commit/5ce841270943a97b529ac1540c5c028413e0475b))
* initial media3 functionality ([67220ad](https://github.com/doublesymmetry/react-native-track-player/commit/67220adf37ac55cc27f8ef9233721137b995fd77))
* onStartCommandIntentValid ([fdeb546](https://github.com/doublesymmetry/react-native-track-player/commit/fdeb54647c7536d37dbaac9e4500399d80b6e84d))
* onStartCommandIntentValid ([26a5826](https://github.com/doublesymmetry/react-native-track-player/commit/26a5826c2214083b7106a44403e9fa29f9aed643))
* **web:** add setQueue method ([#2394](https://github.com/doublesymmetry/react-native-track-player/issues/2394)) ([2765d09](https://github.com/doublesymmetry/react-native-track-player/commit/2765d09cb93541d89be94a4809320e4df33c0d47))
* **web:** migrate web to turbomodule architecture ([f3fc4d5](https://github.com/doublesymmetry/react-native-track-player/commit/f3fc4d560154987dd7d341648b3ee6ac01972e15))

### Bug Fixes

* **android:** correct onBind method signature in MusicService.kt ([#2451](https://github.com/doublesymmetry/react-native-track-player/issues/2451)) ([e8701be](https://github.com/doublesymmetry/react-native-track-player/commit/e8701be9f7876ae3c80d7cfdfca7e90c40d6c389))
* **android:** fix an issue causing remote events to not fire ([0001895](https://github.com/doublesymmetry/react-native-track-player/commit/00018950f64f6a1bec814351d9bac20eccf22b28))
* **android:** fix an issue with maxCacheSize option on android causing crashes ([349eff0](https://github.com/doublesymmetry/react-native-track-player/commit/349eff03b930bc85da2a21c7e40bd16764989ef0))
* **android:** prevent livestream failures by assigning a default user-agent for Media3 requests ([#2496](https://github.com/doublesymmetry/react-native-track-player/issues/2496)) ([492d349](https://github.com/doublesymmetry/react-native-track-player/commit/492d349095ddec9e55444b37f2e87ea42915d6aa))
* **android:** resolve build errors in react-native@0.80.0 ([f40969b](https://github.com/doublesymmetry/react-native-track-player/commit/f40969bccc138821a4cff312b15ea27692fdbd81))
* broken events in bridgeless mode (React Native New Architecture) ([d918b8e](https://github.com/doublesymmetry/react-native-track-player/commit/d918b8e11d4dd8e66ecae0f50baebb6d68d327d5))
* catch currentTrack null ([2dc637f](https://github.com/doublesymmetry/react-native-track-player/commit/2dc637fd06ac7ef2e0d507c567549341f4cfb7bd))
* **component:** fixed example slider to include tap events and error styling ([#2376](https://github.com/doublesymmetry/react-native-track-player/issues/2376)) ([a486121](https://github.com/doublesymmetry/react-native-track-player/commit/a486121912eda10981c0c80a307fa7fb40998d0c))
* **docs:** typo ([#2322](https://github.com/doublesymmetry/react-native-track-player/issues/2322)) ([e049abb](https://github.com/doublesymmetry/react-native-track-player/commit/e049abbd935493bc40cd0a4d9edc25a804dd94ab))
* **ios:** add back errantly removed MetadataTimedReceived event ([6107f65](https://github.com/doublesymmetry/react-native-track-player/commit/6107f659cbea0a347f18d857fd45a924f31a8b60))
* **ios:** fix a build error by removing deprecated event listener ([b11736b](https://github.com/doublesymmetry/react-native-track-player/commit/b11736b7f249698730845339c086b7dc722ca1ee))
* **ios:** fix errors that occur when subscribing to the remote-play-pause event ([262d1a1](https://github.com/doublesymmetry/react-native-track-player/commit/262d1a144188d337a666e08b1b3c9d5203ad2ec4))
* **web:** Fix error message when calling setupPlayer twice ([#2364](https://github.com/doublesymmetry/react-native-track-player/issues/2364)) ([bc55ca1](https://github.com/doublesymmetry/react-native-track-player/commit/bc55ca13b9136225b148449fa60e341b0c090be8))
* **web:** fix issues with certain bundlers only containing `default` … ([#2299](https://github.com/doublesymmetry/react-native-track-player/issues/2299)) ([a89d785](https://github.com/doublesymmetry/react-native-track-player/commit/a89d7856c25e2cf5a440e996dd4976292b67feda))
* **web:** fix issues with repeat mode and track add logic ([#2291](https://github.com/doublesymmetry/react-native-track-player/issues/2291)) ([6c4a3bd](https://github.com/doublesymmetry/react-native-track-player/commit/6c4a3bd7d47c6ba8c85db4c1d031dc904bb0cd1a))
* **web:** preserve playback rate across tracks ([#2475](https://github.com/doublesymmetry/react-native-track-player/issues/2475)) ([eb32a97](https://github.com/doublesymmetry/react-native-track-player/commit/eb32a9781f16ee4912b98f7317e3d9cd188a020a))

## [4.1.2](https://github.com/doublesymmetry/react-native-track-player/compare/v4.1.1...v4.1.2) (2025-08-12)


### Bug Fixes

* **android:** correct onBind method signature in MusicService.kt ([#2451](https://github.com/doublesymmetry/react-native-track-player/issues/2451)) ([e8701be](https://github.com/doublesymmetry/react-native-track-player/commit/e8701be9f7876ae3c80d7cfdfca7e90c40d6c389))
* **component:** fixed example slider to include tap events and error styling ([#2376](https://github.com/doublesymmetry/react-native-track-player/issues/2376)) ([a486121](https://github.com/doublesymmetry/react-native-track-player/commit/a486121912eda10981c0c80a307fa7fb40998d0c))
* **docs:** typo ([#2322](https://github.com/doublesymmetry/react-native-track-player/issues/2322)) ([e049abb](https://github.com/doublesymmetry/react-native-track-player/commit/e049abbd935493bc40cd0a4d9edc25a804dd94ab))
* **web:** Fix error message when calling setupPlayer twice ([#2364](https://github.com/doublesymmetry/react-native-track-player/issues/2364)) ([bc55ca1](https://github.com/doublesymmetry/react-native-track-player/commit/bc55ca13b9136225b148449fa60e341b0c090be8))
* **web:** fix issues with certain bundlers only containing `default` … ([#2299](https://github.com/doublesymmetry/react-native-track-player/issues/2299)) ([a89d785](https://github.com/doublesymmetry/react-native-track-player/commit/a89d7856c25e2cf5a440e996dd4976292b67feda))
* **web:** fix issues with repeat mode and track add logic ([#2291](https://github.com/doublesymmetry/react-native-track-player/issues/2291)) ([6c4a3bd](https://github.com/doublesymmetry/react-native-track-player/commit/6c4a3bd7d47c6ba8c85db4c1d031dc904bb0cd1a))
* **web:** preserve playback rate across tracks ([#2475](https://github.com/doublesymmetry/react-native-track-player/issues/2475)) ([eb32a97](https://github.com/doublesymmetry/react-native-track-player/commit/eb32a9781f16ee4912b98f7317e3d9cd188a020a))


### Features

* **web:** add setQueue method ([#2394](https://github.com/doublesymmetry/react-native-track-player/issues/2394)) ([2765d09](https://github.com/doublesymmetry/react-native-track-player/commit/2765d09cb93541d89be94a4809320e4df33c0d47))



# [4.1.1](https://github.com/doublesymmetry/react-native-track-player/compare/v4.1.0...v4.1.1) (2024-03-26)

- **RN:** Fixes an issue when using local assets in release builds

# [4.1.0](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.1...v4.1.0) (2024-03-25)

- **web:** First beta version of RNTP for web
- **RN:** Massively reduce npm package size
- **android:** Fix: add namespace to support gradle 8
- **android:** Improvement: add http headers to notification artwork request
- **android:** Improvement: use http data source for local urls
- **android:** Improvement: use SVGs for notification icons
- **android:** Improvement: specify key when returning key error
- **ios:** Improvement: improve internal use of playWhenReady to avoid issue where we'd load two tracks at once
- **ios:** Fix: return correct already intialized error code
- **ios:** Fix: avoid emitting empty common metadata
- **ios:** Fix: occasional crash due to attaching metadata output
- **ios:** Fix: sometimes progress bar would be broken after repeat

# [4.0.1](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0...v4.0.1) (2023-10-31)

- **android:** Fix: notification dissapearing in background
- **android:** Allow overriding notification channel name and description

# [4.0.0](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc09...v4.0.0) (2023-10-20)

- **RN:** New metadata events have a new `metadata` property that contains the metadata that was received
- **android:** Fix: allow updating duration in notification metadata
- **ios:** Avoid prematurely activating audio session
- **android:** Fix: don't emit both PlaybackTrackChanged when queue ends (parity with iOS)
- **android:** Fix: allow progressUpdateEventInterval to be set to a decimal value (partial seconds)
- **android:** Support for setting grace period before stopForeground (defaults to 5 seconds)
- **ios:** Fix: updating rate will immediately reflect in control center
- **android** Fix: issue where loading a new track after end required seek to start
- **ios:** Fix: crash adding output when load is called too fast

# [4.0.0-rc09](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc08...v4.0.0-rc09) (2023-09-22)

- **RN:** useIsPlaying hook now takes into account `none` state
- **ios:** Fixes issue where rate was being reset to 1 on play
- **ios:** Deactivates session before activating on play to avoid issues when losing focus in some scenarios
- **RN:** Deprecated `Event.PlaybackMetadataReceived` and introduces three new events: [`Event.AudioChapterMetadataReceived`, `Event.AudioTimedMetadataReceived`, `Event.AudioCommonMetadataReceived`]
- **ios:** Change default pitch algorithm to `timeDomain` instead of `lowQualityZeroLatency`
- **android:** Fixes progress in notification for HLS audio

# [4.0.0-rc08](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc07...v4.0.0-rc08) (2023-09-07)

- **RN:** Undeprecate updateNowPlayingMetadata
- **android:** Restore notification image caching
- **RN:** Fix issue with updateOptions and local images
- **ios:** Activate session on play to avoid issues with background audio
- **ios:** Second fix for repeat mode
- **ios:** Correctly update control center progress when pausing/playing

# [4.0.0-rc07](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc06...v4.0.0-rc07) (2023-08-11)

- **ios:** Fix firing of `EventType.PlaybackQueueEnded` (fixes #2038)
- **android:** Avoid emitting track changed when replaying the same track
- **android:** Fixed a regression where `reset()` wasn't clearing notification properly
- **android:** Resolved a where the update metadata method was not working

# [4.0.0-rc06](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc05...v4.0.0-rc06) (2023-07-25)

- **ios:** Fix iOS not repeating track in RepeatMode.Track
- **RN:** Improve types on asset types
- **android:** Fix foreground issues and notification item
- **ios** Fix race conditions in player property setting
- **android:** Improve notification updates when spamming notification buttons
- **android** Fix AudioPlayerState.IDLE when queue emptied
- **android** Improve metadata handling

# [4.0.0-rc05](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc04...v4.0.0-rc05) (2023-06-26)

- **ios:** Fix crash on getting current item
- **android:** Improve preciseness of seeking
- **android:** Improve handling of service foregrounding

# [4.0.0-rc03](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc02...v4.0.0-rc03) (2023-03-28)

- **android:** Fixes compilation issue due to uses of Lifecycle (updates kotlin gradle plugin)

# [4.0.0-rc02](https://github.com/doublesymmetry/react-native-track-player/compare/v4.0.0-rc01...v4.0.0-rc02) (2023-03-27)

### Bug Fixes

- **android:** Fix ANR crashes ([#1974](https://github.com/doublesymmetry/react-native-track-player/issues/1974)) ([b70fbd7](https://github.com/doublesymmetry/react-native-track-player/commit/b70fbd7663921855f799a60083ca4522e913e6f8)
- **android:** Fix removeUpcomingItems ([#67](https://github.com/doublesymmetry/KotlinAudior/issues/67))
- **android:** Notification fixes

- **ios:** Fix removing items from queue other than the current track ([#41](https://github.com/doublesymmetry/SwiftAudioEx/issues/41))
- **ios:** Fix player state not becoming paused after loading ([#46](https://github.com/doublesymmetry/SwiftAudioEx/issues/46))
- **ios:** Fix current item not being updated when removing items from queue ([#45](https://github.com/doublesymmetry/SwiftAudioEx/issues/45))
- **ios:** Avoid calling onSkippedToSameCurrentItem when track before is removed ([#45](https://github.com/doublesymmetry/SwiftAudioEx/issues/45))

# [4.0.0-rc01](https://github.com/doublesymmetry/react-native-track-player/compare/v3.2.0...v4.0.0-rc01) (2023-03-14)

### Bug Fixes

- **android:** add deep link back ([#1872](https://github.com/doublesymmetry/react-native-track-player/issues/1872)) ([9c227fa](https://github.com/doublesymmetry/react-native-track-player/commit/9c227fa7e0748d3d43dba435ee284e01e18de9bc))
- **android:** fix handling of seek capability ([#1938](https://github.com/doublesymmetry/react-native-track-player/issues/1938)) ([166aa0d](https://github.com/doublesymmetry/react-native-track-player/commit/166aa0d2d4ee7d213d4a41e496592f20e4bbbede))
- **android:** resolve problem with StopPlaybackAndRemoveNotification not working on subsequent exists ([#1762](https://github.com/doublesymmetry/react-native-track-player/issues/1762)) ([e742959](https://github.com/doublesymmetry/react-native-track-player/commit/e742959cc1d697d69daeade39830155bce2ccde7))
- **android:** use “none” instead of “idle” for none state ([#1924](https://github.com/doublesymmetry/react-native-track-player/issues/1924)) ([e125045](https://github.com/doublesymmetry/react-native-track-player/commit/e125045106ad3db125a09dc60dab2acc3334f01c))
- clears queue on iOS when you call reset() ([#1900](https://github.com/doublesymmetry/react-native-track-player/issues/1900)) ([e3c670a](https://github.com/doublesymmetry/react-native-track-player/commit/e3c670a822488565b923300be76e70a8c492fec4))
- **hooks:** updates setting initial playback state in usePlaybackState hook ([417f3c4](https://github.com/doublesymmetry/react-native-track-player/commit/417f3c4cdf26c2db8551eeae6f84eed69f532e77)), closes [#1931](https://github.com/doublesymmetry/react-native-track-player/issues/1931)
- **ios:** emit state passed to handleAudioPlayerStateChange ([#1928](https://github.com/doublesymmetry/react-native-track-player/issues/1928)) ([a65fdcd](https://github.com/doublesymmetry/react-native-track-player/commit/a65fdcd5f913e20ffcdf45bb1d814d51ffe255ae))
- **ios:** prevents overwriting of forward/backward secs ([#1855](https://github.com/doublesymmetry/react-native-track-player/issues/1855)) ([fb594c7](https://github.com/doublesymmetry/react-native-track-player/commit/fb594c77b2f80ecf90995893c0158cc8cd11baa0)), closes [#1853](https://github.com/doublesymmetry/react-native-track-player/issues/1853)

# [3.2.0](https://github.com/doublesymmetry/react-native-track-player/compare/v3.1.0...v3.2.0) (2022-09-23)

### Bug Fixes

- **android, events:** properly intercept and fire remote playback events ([#1668](https://github.com/doublesymmetry/react-native-track-player/issues/1668)) ([9ed308c](https://github.com/doublesymmetry/react-native-track-player/commit/9ed308c2a8f5bbd4ab69df01a7cfb86047960538))
- **android:** fix state constants ([#1751](https://github.com/doublesymmetry/react-native-track-player/issues/1751)) ([7215e64](https://github.com/doublesymmetry/react-native-track-player/commit/7215e641615a1526e536d5f0e7bb6b0bb2b6d0f7))
- **example, ios:** remove Capability.Stop ([#1671](https://github.com/doublesymmetry/react-native-track-player/issues/1671)) ([49800ab](https://github.com/doublesymmetry/react-native-track-player/commit/49800ab81f9dbc34a136632a3c7623666e1ae95d))
- **hooks:** fix issues with excessive number of pending callbacks ([#1686](https://github.com/doublesymmetry/react-native-track-player/issues/1686)) ([1b5bb02](https://github.com/doublesymmetry/react-native-track-player/commit/1b5bb02bbe1457902949ebd9c29829ba4998eb07))
- **hooks:** fix useTrackPlayerEvents dependencies ([#1672](https://github.com/doublesymmetry/react-native-track-player/issues/1672)) ([f6229d6](https://github.com/doublesymmetry/react-native-track-player/commit/f6229d68c2cdb650b90c38fa47f7e40d0028dfee))
- **hooks:** useProgress & usePlayback hooks ([#1723](https://github.com/doublesymmetry/react-native-track-player/issues/1723)) ([31fa40a](https://github.com/doublesymmetry/react-native-track-player/commit/31fa40acafed2d4bb09a718e4c51879fb1c7e747))
- **ios, events:** fix an issue with PlaybackQueueEnded resulting from a race condition ([#1750](https://github.com/doublesymmetry/react-native-track-player/issues/1750)) ([e938c68](https://github.com/doublesymmetry/react-native-track-player/commit/e938c68a1968c8a0fa4aa1019d0343c04a427d60))
- **ios:** fix various issues in iOS by upgrading SwiftAudioEx ([#1738](https://github.com/doublesymmetry/react-native-track-player/issues/1738)) ([224c491](https://github.com/doublesymmetry/react-native-track-player/commit/224c4910e4c3fe2164ac1cbf0bb3f61810062d48))
- **ts:** add `null` to getCurrentTrack return type ([#1681](https://github.com/doublesymmetry/react-native-track-player/issues/1681)) ([096ec68](https://github.com/doublesymmetry/react-native-track-player/commit/096ec68bc0c3150f02c068ed20dd07f0ddf53e35))

### Features

- **android:** add back option to remove notification ([#1730](https://github.com/doublesymmetry/react-native-track-player/issues/1730)) ([82a5df9](https://github.com/doublesymmetry/react-native-track-player/commit/82a5df9ec62476b05bbef6f5d18ca9b0b801d298))
- **android:** add string values to State enum ([#1734](https://github.com/doublesymmetry/react-native-track-player/issues/1734)) ([bd48c2d](https://github.com/doublesymmetry/react-native-track-player/commit/bd48c2d6de2a56e0a96be8dd47bc316ea0dcd8cf)), closes [#1688](https://github.com/doublesymmetry/react-native-track-player/issues/1688)
- **android:** default the behavior to handle audio becoming noisy ([#1732](https://github.com/doublesymmetry/react-native-track-player/issues/1732)) ([dabf715](https://github.com/doublesymmetry/react-native-track-player/commit/dabf71566cba1cc9fba48899ad6be58e59a90212))
- **ios:** deprecate waitForBuffer ([#1695](https://github.com/doublesymmetry/react-native-track-player/issues/1695)) ([d277182](https://github.com/doublesymmetry/react-native-track-player/commit/d27718295cecc88886db2cbe96666c9c50d34b34))
- **ios:** improve disabling of playback-progress-updated ([#1706](https://github.com/doublesymmetry/react-native-track-player/issues/1706)) ([57de8b5](https://github.com/doublesymmetry/react-native-track-player/commit/57de8b5e12c05bed093a754474b2e4e0e8597578))

## 3.1.0 (18.08.22)

##### Enhancements

- Uses latest KotlinAudio which does not use ExoPlayer fork.
- Adds back support for bluetooth playback control.

##### Bug Fixes

- Fixes crash with `reset()` on Android.
- Removes `destroy()` on iOS - this was missed.
- Removes the `stop()` method -- use `pause()` instead.

## 3.0.0 (11.08.22)

We are changing how the audio service handles its lifecycle. Previously we had the stopWithApp bool that would try and stop the service when you remove the app from recents, but due to how Android handles foreground services the OS never stops the app process, as it's waiting for the foreground service to come back. We are embracing this and going with what other audio apps (Spotify, Soundcloud, Google Podcast etc.) are doing.

##### Enhancements

- Rewrite Android module in Kotlin and using KotlinAudio.
  [mpivchev](

##### Breaking

- stopWithApp turns into stoppingAppPausesPlayback
- `destroy()` is no longer available
-

##### Bug Fixes

- Fix crash with `reset()` on Android.
  [dcvz](https://github.com/dcvz)

## 2.1.3 (30.03.22)

##### Enhancements

- Add property `isLiveStream` to `Track` for correct display in iOS control center.
  [dcvz](https://github.com/dcvz)

- [iOS] Improve method documentation
  [alpha0010](https://github.com/alpha0010)

- [Android] Add isServiceRunning method
  [biomancer](https://github.com/biomancer)

##### Bug Fixes

- [iOS] Fix track loop crash in certain cases
  [mmmoussa](https://github.com/mmmoussa)

- [iOS] Fix seek after play
  [jspizziri](https://github.com/jspizziri)

- [Android] Support Android 12 devices
  [abhaydee](https://github.com/abhaydee)

- [iOS] Add method resolves promise with index
  [formula1](https://github.com/formula1)

- Fix getTrack return type
  [puckey](https://github.com/puckey)

- [iOS] Fix ambient session not working
  [grubicv](https://github.com/grubicv)

- [Android] Android 12 and higher bug fix
  [martin-richter-uk](https://github.com/martin-richter-uk)

- [iOS] Update SwiftAudioEx to 0.14.6 to avoid LICENSE warning
  [dcvz](https://github.com/dcvz)

- Make react-native-windows and optional peer dependency (#1324).
  [jspizziri](https://github.com/jspizziri)

## 2.1.2 (25.10.21)

##### Enhancements

- None.

##### Bug Fixes

- Update SwiftAudioEx - Fixes issues with flickering notifications + pause between loads

- Fix cyclic require warning regression
  [#1057](https://github.com/DoubleSymmetry/react-native-track-player/issues/1057)

- [ios] Fix `PlaybackQueueEnded` event to be called only when the track ends
  [#1243](https://github.com/DoubleSymmetry/react-native-track-player/issues/1243)

## 2.1.1 (25.09.21)

##### Enhancements

- [ios] Fix getCurrentTrack returns undefined instead of null
- [ios] Fix getTrack returning undefined instead of nil
- Fix an issue with next/previous in the control center stopping playing on iOS15

##### Bug Fixes

- None.

## 2.1.0 (16.09.21)

##### Enhancements

- None.

##### Bug Fixes

- Remove Support for iOS 10 & Support Xcode 13
  [dcvz](https://github.com/dcvz)
  [#1186](https://github.com/DoubleSymmetry/react-native-track-player/issues/1186)

  - **NOTE: Requires minimum deployment target to be updated to iOS 11.**

- Reset initialization on destroy
  [sreten-bild](https://github.com/sreten-bild)

- Fix `onTaskRemoved` NullPointerException
  [Markario](https://github.com/Markario)

## 2.0.3 (19.08.21)

##### Enhancements

- None.

##### Bug Fixes

- Fix `Event.PlaybackQueueEnded` firing on initialization on Android
  [dcvz](https://github.com/dcvz)
  [#1229](https://github.com/DoubleSymmetry/react-native-track-player/issues/1229)

- Make `useProgress` unmount aware.
  [lyswhut](https://github.com/lyswhut)

- Make `usePlaybackState` unmount aware.
  [dcvz](https://github.com/dcvz)

## 2.0.2 (15.08.21)

##### Enhancements

- Import SwiftAudioEx through podspec
  [dcvz](https://github.com/dcvz)

##### Bug Fixes

- `useProgress` hook should update while paused
  [dcvz](https://github.com/dcvz)

## 2.0.1 (11.08.21)

##### Enhancements

- None.

##### Bug Fixes

- Add `startForeground` to `onCreate`.
  [Bang9](https://github.com/Bang9)
  [#620](https://github.com/DoubleSymmetry/react-native-track-player/issues/620)
  [#524](https://github.com/DoubleSymmetry/react-native-track-player/issues/524)
  [#473](https://github.com/DoubleSymmetry/react-native-track-player/issues/473)
  [#391](https://github.com/DoubleSymmetry/react-native-track-player/issues/391)

- Fix compilation of Windows module.
  [dcvz](https://github.com/dcvz)

- Fix regression in updating artwork on `updateMetadata` and `updateNowPlayingMetadata`
  [dcvz](https://github.com/dcvz)
  [#662](https://github.com/DoubleSymmetry/react-native-track-player/issues/662#issuecomment-896370375)
