# Installation

To install `react-native-track-player` see this [guide](https://react-native-track-player.js.org/install/).

To install release candidates run `yarn add react-native-track-player@next`

## Main

##### Enhancements

* None.

##### Bug Fixes

* None.

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
