# SwiftAudio

React Native Track Player uses SwiftAudio to play audio and manage the track queue on iOS.

## Current Version

The included module is based on version [SwiftAudio 0.11.2](https://github.com/jorgenhenrichsen/SwiftAudio/releases/tag/0.11.2).

A fork is maintained with minimal changes: [curiousdustin/SwiftAudio#react-native-track-player](https://github.com/curiousdustin/SwiftAudio/tree/react-native-track-player)

## Dependency Integration

Attempts have been made in the past to add SwiftAudio as a depedency.

### Git Submodule

This approach caused issues when installing via `yarn`. Yarn does not automatically checkout git submodules.

Also at one point, the commit that the submodule was pointed at was deleted. This caused a large amount of confusion.

### Pod Dependency

Adding SwiftAudio to the podspec as a dependency seems like a nice solution. However, unless determined otherwise, this approach forces users to use CocoaPods for their main React Native project. This might be an ok thing to do in future versions, because React Native is starting to require CocoaPods. However, the current 1.2.x version of this library is still built with previous versions of React Native in mind.

Another downside is that this approach means the SwiftAudio files are actually built into their own module. This means they cannot be subclassed, unless the creator opens up the classes for other libraries to inherit from. (Swift `open` vs `public`)