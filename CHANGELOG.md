# [3.2.0](https://github.com/doublesymmetry/react-native-track-player/compare/v3.1.0...v3.2.0) (2022-09-23)


### Bug Fixes

* **android, events:** properly intercept and fire remote playback events ([#1668](https://github.com/doublesymmetry/react-native-track-player/issues/1668)) ([9ed308c](https://github.com/doublesymmetry/react-native-track-player/commit/9ed308c2a8f5bbd4ab69df01a7cfb86047960538))
* **android:** fix state constants ([#1751](https://github.com/doublesymmetry/react-native-track-player/issues/1751)) ([7215e64](https://github.com/doublesymmetry/react-native-track-player/commit/7215e641615a1526e536d5f0e7bb6b0bb2b6d0f7))
* **example, ios:** remove Capability.Stop ([#1671](https://github.com/doublesymmetry/react-native-track-player/issues/1671)) ([49800ab](https://github.com/doublesymmetry/react-native-track-player/commit/49800ab81f9dbc34a136632a3c7623666e1ae95d))
* **hooks:**  fix issues with excessive number of pending callbacks  ([#1686](https://github.com/doublesymmetry/react-native-track-player/issues/1686)) ([1b5bb02](https://github.com/doublesymmetry/react-native-track-player/commit/1b5bb02bbe1457902949ebd9c29829ba4998eb07))
* **hooks:** fix useTrackPlayerEvents dependencies ([#1672](https://github.com/doublesymmetry/react-native-track-player/issues/1672)) ([f6229d6](https://github.com/doublesymmetry/react-native-track-player/commit/f6229d68c2cdb650b90c38fa47f7e40d0028dfee))
* **hooks:** useProgress & usePlayback hooks ([#1723](https://github.com/doublesymmetry/react-native-track-player/issues/1723)) ([31fa40a](https://github.com/doublesymmetry/react-native-track-player/commit/31fa40acafed2d4bb09a718e4c51879fb1c7e747))
* **ios, events:** fix an issue with PlaybackQueueEnded resulting from a race condition ([#1750](https://github.com/doublesymmetry/react-native-track-player/issues/1750)) ([e938c68](https://github.com/doublesymmetry/react-native-track-player/commit/e938c68a1968c8a0fa4aa1019d0343c04a427d60))
* **ios:** fix various issues in iOS by upgrading SwiftAudioEx ([#1738](https://github.com/doublesymmetry/react-native-track-player/issues/1738)) ([224c491](https://github.com/doublesymmetry/react-native-track-player/commit/224c4910e4c3fe2164ac1cbf0bb3f61810062d48))
* **ts:** add `null` to getCurrentTrack return type ([#1681](https://github.com/doublesymmetry/react-native-track-player/issues/1681)) ([096ec68](https://github.com/doublesymmetry/react-native-track-player/commit/096ec68bc0c3150f02c068ed20dd07f0ddf53e35))


### Features

* **android:** add back option to remove notification ([#1730](https://github.com/doublesymmetry/react-native-track-player/issues/1730)) ([82a5df9](https://github.com/doublesymmetry/react-native-track-player/commit/82a5df9ec62476b05bbef6f5d18ca9b0b801d298))
* **android:** add string values to State enum ([#1734](https://github.com/doublesymmetry/react-native-track-player/issues/1734)) ([bd48c2d](https://github.com/doublesymmetry/react-native-track-player/commit/bd48c2d6de2a56e0a96be8dd47bc316ea0dcd8cf)), closes [#1688](https://github.com/doublesymmetry/react-native-track-player/issues/1688)
* **android:** default the behavior to handle audio becoming noisy ([#1732](https://github.com/doublesymmetry/react-native-track-player/issues/1732)) ([dabf715](https://github.com/doublesymmetry/react-native-track-player/commit/dabf71566cba1cc9fba48899ad6be58e59a90212))
* **ios:** deprecate waitForBuffer ([#1695](https://github.com/doublesymmetry/react-native-track-player/issues/1695)) ([d277182](https://github.com/doublesymmetry/react-native-track-player/commit/d27718295cecc88886db2cbe96666c9c50d34b34))
* **ios:** improve disabling of playback-progress-updated ([#1706](https://github.com/doublesymmetry/react-native-track-player/issues/1706)) ([57de8b5](https://github.com/doublesymmetry/react-native-track-player/commit/57de8b5e12c05bed093a754474b2e4e0e8597578))



## 3.1.0 (18.08.22)

##### Enhancements

* Uses latest KotlinAudio which does not use ExoPlayer fork.
* Adds back support for bluetooth playback control.

##### Bug Fixes
* Fixes crash with `reset()` on Android.
* Removes `destroy()` on iOS - this was missed.
* Removes the `stop()` method -- use `pause()` instead.

## 3.0.0 (11.08.22)

We are changing how the audio service handles its lifecycle. Previously we had the stopWithApp bool that would try and stop the service when you remove the app from recents, but due to how Android handles foreground services the OS never stops the app process, as it's waiting for the foreground service to come back. We are embracing this and going with what other audio apps (Spotify, Soundcloud, Google Podcast etc.) are doing.

##### Enhancements

* Rewrite Android module in Kotlin and using KotlinAudio.
  [mpivchev](

##### Breaking

* stopWithApp turns into stoppingAppPausesPlayback
* `destroy()` is no longer available
* 

##### Bug Fixes
* Fix crash with `reset()` on Android.
  [dcvz](https://github.com/dcvz)

## 2.1.3 (30.03.22)

##### Enhancements

* Add property `isLiveStream` to `Track` for correct display in iOS control center.
  [dcvz](https://github.com/dcvz)

* [iOS] Improve method documentation
  [alpha0010](https://github.com/alpha0010)

* [Android] Add isServiceRunning method
  [biomancer](https://github.com/biomancer)

##### Bug Fixes

* [iOS] Fix track loop crash in certain cases
  [mmmoussa](https://github.com/mmmoussa)

* [iOS] Fix seek after play
  [jspizziri](https://github.com/jspizziri)

* [Android] Support Android 12 devices
  [abhaydee](https://github.com/abhaydee)

* [iOS] Add method resolves promise with index
  [formula1](https://github.com/formula1)

* Fix getTrack return type
  [puckey](https://github.com/puckey)

* [iOS] Fix ambient session not working
  [grubicv](https://github.com/grubicv)

* [Android] Android 12 and higher bug fix
  [martin-richter-uk](https://github.com/martin-richter-uk)

* [iOS] Update SwiftAudioEx to 0.14.6 to avoid LICENSE warning
  [dcvz](https://github.com/dcvz)

* Make react-native-windows and optional peer dependency (#1324).
  [jspizziri](https://github.com/jspizziri)

## 2.1.2 (25.10.21)

##### Enhancements

* None.

##### Bug Fixes

* Update SwiftAudioEx - Fixes issues with flickering notifications + pause between loads

* Fix cyclic require warning regression
  [#1057](https://github.com/DoubleSymmetry/react-native-track-player/issues/1057)

* [ios] Fix `PlaybackQueueEnded` event to be called only when the track ends
  [#1243](https://github.com/DoubleSymmetry/react-native-track-player/issues/1243)

## 2.1.1 (25.09.21)

##### Enhancements

* [ios] Fix getCurrentTrack returns undefined instead of null
* [ios] Fix getTrack returning undefined instead of nil
* Fix an issue with next/previous in the control center stopping playing on iOS15

##### Bug Fixes

* None.

## 2.1.0 (16.09.21)

##### Enhancements

* None.

##### Bug Fixes

* Remove Support for iOS 10 & Support Xcode 13
  [dcvz](https://github.com/dcvz)
  [#1186](https://github.com/DoubleSymmetry/react-native-track-player/issues/1186)
  - **NOTE: Requires minimum deployment target to be updated to iOS 11.**

* Reset initialization on destroy
  [sreten-bild](https://github.com/sreten-bild)

* Fix `onTaskRemoved` NullPointerException
  [Markario](https://github.com/Markario)

## 2.0.3 (19.08.21)

##### Enhancements

* None.

##### Bug Fixes

* Fix `Event.PlaybackQueueEnded` firing on initialization on Android
  [dcvz](https://github.com/dcvz)
  [#1229](https://github.com/DoubleSymmetry/react-native-track-player/issues/1229)

* Make `useProgress` unmount aware.
  [lyswhut](https://github.com/lyswhut)

* Make `usePlaybackState` unmount aware.
  [dcvz](https://github.com/dcvz)

## 2.0.2 (15.08.21)

##### Enhancements

* Import SwiftAudioEx through podspec
  [dcvz](https://github.com/dcvz)

##### Bug Fixes

* `useProgress` hook should update while paused
  [dcvz](https://github.com/dcvz)


## 2.0.1 (11.08.21)

##### Enhancements

* None.  

##### Bug Fixes

* Add `startForeground` to `onCreate`.
  [Bang9](https://github.com/Bang9)
  [#620](https://github.com/DoubleSymmetry/react-native-track-player/issues/620)
  [#524](https://github.com/DoubleSymmetry/react-native-track-player/issues/524)
  [#473](https://github.com/DoubleSymmetry/react-native-track-player/issues/473)
  [#391](https://github.com/DoubleSymmetry/react-native-track-player/issues/391)

* Fix compilation of Windows module.
  [dcvz](https://github.com/dcvz)

* Fix regression in updating artwork on `updateMetadata` and `updateNowPlayingMetadata`
  [dcvz](https://github.com/dcvz)
  [#662](https://github.com/DoubleSymmetry/react-native-track-player/issues/662#issuecomment-896370375)
