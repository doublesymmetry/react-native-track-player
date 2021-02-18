Fork of https://github.com/react-native-kit/react-native-track-player

### Changes from main v1.2.3 branch

There are several PRs that are up on the main project but have not been merged yet there. They are merged here.

- Added support for `initialTime` and `iosInitialTime`
- Fixed warning for circular references
- Added support for `isServiceRunning`
- Bug fixes

A few of my own fixes:

- Fixed a bug where the very first track would not honor initialTime on Android side
- Fixed a react component state update on an unmounted component in useTrackPlayerProgress
- Streamlined event emission and upgraded to use ExoPlayer 2.13.1
