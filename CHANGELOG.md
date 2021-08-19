# Installation

To install `react-native-track-player` see this [guide](https://react-native-track-player.js.org/install/).

To install release candidates run `yarn add react-native-track-player@next`

## Main

##### Enhancements

* None.

##### Bug Fixes

* None.

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
