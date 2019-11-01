![logo](Images/original-horizontal.png)

# SwiftAudio

[![Build Status](https://app.bitrise.io/app/3d3ac2ba8d817235/status.svg?token=PHIPu3oMde5GdQEOZ1Ilww&branch=master)](https://app.bitrise.io/app/3d3ac2ba8d817235)
[![Version](https://img.shields.io/cocoapods/v/SwiftAudio.svg?style=flat)](http://cocoapods.org/pods/SwiftAudio)
[![codecov](https://codecov.io/gh/jorgenhenrichsen/SwiftAudio/branch/master/graph/badge.svg)](https://codecov.io/gh/jorgenhenrichsen/SwiftAudio)
[![License](https://img.shields.io/cocoapods/l/SwiftAudio.svg?style=flat)](http://cocoapods.org/pods/SwiftAudio)
[![Platform](https://img.shields.io/cocoapods/p/SwiftAudio.svg?style=flat)](http://cocoapods.org/pods/SwiftAudio)

SwiftAudio is an audio player written in Swift, making it simpler to work with audio playback from streams and files.

## Example

To see the audio player in action, run the example project!
To run the example project, clone the repo, and run `pod install` from the Example directory first.

## Requirements
iOS 10.0+

## Installation

### CocoaPods
SwiftAudio is available through [CocoaPods](http://cocoapods.org). To install
it, simply add the following line to your Podfile:

```ruby
pod 'SwiftAudio', '~> 0.9.2'
```

### Carthage
SwiftAudio supports [Carthage](https://github.com/Carthage/Carthage). Add this to your Cartfile:
```ruby
github "jorgenhenrichsen/SwiftAudio" ~> 0.9.2
```
Then follow the rest of Carthage instructions on [adding a framework](https://github.com/Carthage/Carthage#adding-frameworks-to-an-application).

## Usage

### AudioPlayer
To get started playing some audio:
```swift
let player = AudioPlayer()
let audioItem = DefaultAudioItem(audioUrl: "someUrl", sourceType: .stream)
player.load(item: audioItem, playWhenReady: true) // Load the item and start playing when the player is ready.
```

To listen for events in the `AudioPlayer`, subscribe to events found in the `event` property of the `AudioPlayer`.
To subscribe to an event:
```swift
class MyCustomViewController: UIViewController {

    let audioPlayer = AudioPlayer()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        audioPlayer.event.stateChange.addListener(self, handleAudioPlayerStateChange)
    }
    
    func handleAudioPlayerStateChange(state: AudioPlayerState) {
        // Handle the event
    }
}
```

#### QueuedAudioPlayer
The `QueuedAudioPlayer` is a subclass of `AudioPlayer` that maintains a queue of audio tracks.
```swift
let player = QueuedAudioPlayer()
let audioItem = DefaultAudioItem(audioUrl: "someUrl", sourceType: .stream)
player.add(item: audioItem, playWhenReady: true) // Since this is the first item, we can supply playWhenReady: true to immedietaly start playing when the item is loaded.
```

When a track is done playing, the player will load the next track and update the queue, as long as `automaticallyPlayNextSong` is `true` (default).

##### Navigating the queue
All `AudioItem`s are stored in either `previousItems` or `nextItems`, which refers to items that come prior to the `currentItem` and after, respectively. The queue is navigated with:
```swift
player.next() // Increments the queue, and loads the next item.
player.previous() // Decrements the queue, and loads the previous item.
player.jumpToItem(atIndex:) // Jumps to a certain item and loads that item.
```

##### Manipulating the queue
```swift
 player.removeItem(at:) // Remove a specific item from the queue.
 player.removeUpcomingItems() // Remove all items in nextItems.
```

### Configuring the AudioPlayer
Current options for configuring the `AudioPlayer`:
- `bufferDuration`: The amount of seconds to be buffered by the player.
- `timeEventFrequency`: How often the player should call the delegate with time progress events.
- `automaticallyWaitsToMinimizeStalling`: Indicates whether the player should automatically delay playback in order to minimize stalling.
- `volume`
- `isMuted`
- `rate`
- `audioTimePitchAlgorithm`: This value decides the `AVAudioTimePitchAlgorithm` used for each `AudioItem`. Implement `TimePitching` in your `AudioItem`-subclass to override individually for each `AudioItem`.

### Audio Session
Remember to activate an audio session with an appropriate category for your app. This can be done with `AudioSessionController`:
```swift
try? AudioSessionController.shared.set(category: .playback)
//...
// You should wait with activating the session until you actually start playback of audio.
// This is to avoid interrupting other audio without the need to do it.
try? AudioSessionController.shared.activateSession()
```

**Important**: If you want audio to continue playing when the app is inactive, remember to activate background audio:
App Settings -> Capabilities -> Background Modes -> Check 'Audio, AirPlay, and Picture in Picture'.

#### Interruptions
If you are using the `AudioSessionController` for setting up the audio session, you can use it to handle interruptions too.
Implement `AudioSessionControllerDelegate` and you will be notified by `handleInterruption(type: AVAudioSessionInterruptionType)`.
If you are storing progress for playback time on items when the app quits, it can be a good idea to do it on interruptions as well.
To disable interruption notifcations set `isObservingForInterruptions` to `false`.

### Now Playing Info
The `AudioPlayer` can automatically update `nowPlayingInfo` for you. This requires `automaticallyUpdateNowPlayingInfo` to be true (default), and that the `AudioItem` that is passed in return values for the getters. The `AudioPlayer` will update: artist, title, album, artwork, elapsed time, duration and rate.

Additional properties for items can be set by accessing the setter of the `nowPlayingInforController`:
```swift
    let player = AudioPlayer()
    player.load(item: someItem)
    player.nowPlayingInfoController.set(keyValue: NowPlayingInfoProperty.isLiveStream(true))
```
The set(keyValue:) and set(keyValues:) accept both `MediaItemProperty` and `NowPlayingInfoProperty`.

The info can be forced to reload/update from the `AudioPlayer`.
```swift
    audioPlayer.loadNowPlayingMetaValues()
    audioPlayer.updateNowPlayingPlaybackValues()
```
The current info can be cleared with:
```swift
    audioPlayer.nowPlayingInfoController.clear()
```

### Remote Commands
**First** go to App Settings -> Capabilites -> Background Modes -> Check 'Remote notifications'

To enable remote commands for the player you need to populate the RemoteCommands array for the player:
```swift
audioPlayer.remoteCommands = [
    .play,
    .pause,
    .skipForward(intervals: [30]),
    .skipBackward(intervals: [30]),
  ]
```
These commands will be activated for each `AudioItem`. If you need some audio items to have different commands, implement `RemoteCommandable` in a custom `AudioItem`-subclass. These commands will override the commands found in `AudioPlayer.remoteCommands` so make sure to supply all commands you need for that particular `AudioItem`.

#### Custom handlers for remote commands
To supply custom handlers for your remote commands, just override the handlers contained in the player's `RemoteCommandController`:
```swift
let player = QueuedAudioPlayer()
player.remoteCommandController.handlePlayCommand = { (event) in
    // Handle remote command here.
}
```
All available overrides can be found by looking at `RemoteCommandController`.

### Start playback from a certain point in time
Make your `AudioItem`-subclass conform to `InitialTiming` to be able to start playback from a certain time.

## Author

Jørgen Henrichsen

## License

SwiftAudio is available under the MIT license. See the LICENSE file for more info.
