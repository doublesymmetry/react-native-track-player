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
