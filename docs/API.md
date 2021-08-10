---
title: Getting Started
description: "Instructions to get started with react-native-track-player"
nav_order: 3
permalink: /getting-started/
redirect_from:
  - /api/
---

# Getting Started

## Starting off
First of all, you need to set up the player. This usually takes less than a second:
```typescript
import TrackPlayer from 'react-native-track-player';

await TrackPlayer.setupPlayer({})
// The player is ready to be used
```

You also need to register a [playback service](#playback-service) right after registering the main component of your app:
```typescript
// AppRegistry.registerComponent(...);
TrackPlayer.registerPlaybackService(() => require('./service'));
```

```typescript
// service.js
module.exports = async function() {
    // This service needs to be registered for the module to work
    // but it will be used later in the "Receiving Events" section
}
```

## Controlling the Player

### Adding Tracks to the Playback Queue
You can add a track to the player using a url or by requiring a file in the app bundle or on the file system.

First of all, you need to create a [track object](https://react-native-track-player.js.org/react-native-track-player/documentation/#track-object), which is a plain javascript object with a number of properties describing the track. Then [add](https://react-native-track-player.js.org/react-native-track-player/documentation/#addtracks-insertbeforeid) the track to the queue:

```typescript
var track = {
    url: 'http://example.com/avaritia.mp3', // Load media from the network
    title: 'Avaritia',
    artist: 'deadmau5',
    album: 'while(1<2)',
    genre: 'Progressive House, Electro House',
    date: '2014-05-20T07:00:00+00:00', // RFC 3339
    artwork: 'http://example.com/cover.png', // Load artwork from the network
    duration: 402 // Duration in seconds
};

const track2 = {
    url: require('./coelacanth.ogg'), // Load media from the app bundle
    title: 'Coelacanth I',
    artist: 'deadmau5',
    artwork: require('./cover.jpg'), // Load artwork from the app bundle
    duration: 166
};

const track3 = {
    url: 'file:///storage/sdcard0/Downloads/artwork.png', // Load media from the file system
    title: 'Ice Age',
    artist: 'deadmau5',
     // Load artwork from the file system:
    artwork: 'file:///storage/sdcard0/Downloads/cover.png',
    duration: 411
};

// You can then [add](https://react-native-track-player.js.org/react-native-track-player/documentation/#addtracks-insertbeforeindex) the items to the queue
await TrackPlayer.add([track1, track2, track3]);
```

### Player Information

```typescript

import TrackPlayer, { State } from 'react-native-track-player';

const state = await TrackPlayer.getState();
if (state === State.Playing) {
    console.log('The player is playing');
};

let trackIndex = await TrackPlayer.getCurrentTrack();
let trackObject = await TrackPlayer.getTrack(trackIndex);
console.log(`Title: ${trackObject.title}`);

const position = await TrackPlayer.getPosition();
const duration = await TrackPlayer.getDuration();
console.log(`${duration - position} seconds left.`);
```

### Changing Playback State

```typescript
TrackPlayer.play();
TrackPlayer.pause();
TrackPlayer.stop();
TrackPlayer.reset();

// Seek to 12.5 seconds:
TrackPlayer.seekTo(12.5);

// Set volume to 50%:
TrackPlayer.setVolume(0.5);
```

### Controlling the Queue
```typescript
// Skip to a specific track index:
await TrackPlayer.skip(trackIndex);

// Skip to the next track in the queue:
await TrackPlayer.skipToNext();

// Skip to the previous track in the queue:
await TrackPlayer.skipToPrevious();

// Remove two tracks from the queue:
await TrackPlayer.remove([trackIndex1, trackIndex2]);

// Retrieve the track objects in the queue:
const tracks = await TrackPlayer.getQueue();
console.log(`First title: ${tracks[0].title}`);
```
#### Playback Events

You can subscribe to [player events](https://react-native-track-player.js.org/react-native-track-player/documentation/#player), which describe the changing nature of the playback state. For example, subscribe to the `Event.PlaybackTrackChanged` event to be notified when the track has changed or subscribe to the `Event.PlaybackState` event to be notified when the player buffers, plays, pauses and stops.

#### Example
```tsx
import TrackPlayer, { Event } from 'react-native-track-player';

const PlayerInfo = () => {
    const [trackTitle, setTrackTitle] = useState<string>();

    // do initial setup, set initial trackTitle..

    useTrackPlayerEvents([Event.PlaybackTrackChanged], async event => {
        if (event.type === Event.PlaybackTrackChanged && event.nextTrack != null) {
            const track = await TrackPlayer.getTrack(event.nextTrack);
            const {title} = track || {};
            setTrackTitle(title);
        }
    });

    return (
        <Text>{trackTitle}</Text>
    );
}
```

### Playback Service

The playback service keeps running even when the app is in the background. It will start when the player is set up and will only stop when the player is destroyed. It is a good idea to put any code in there that needs to be directly tied to the player state. For example, if you want to be able to track what is being played for analytics purposes, the playback service would be the place to do so.

#### Remote Events

[Remote events](https://react-native-track-player.js.org/react-native-track-player/documentation/#media-controls) are sent from places outside of our user interface that we can react to. For example if the user presses the pause media control in the IOS lockscreen / Android notification or from their Bluetooth headset, we want to have TrackPlayer pause the audio.

If you create a listener to a remote event like `remote-pause` in the context of a React component, there is a chance the UI will be unmounted automatically when the app is in the background, causing it to be missed. For this reason it is best to place remote listeners in the playback service, since it will keep running even when the app is in the background.

#### Example
```typescript
// This needs to go right after you register the main component of your app
// AppRegistry.registerComponent(...)
TrackPlayer.registerPlaybackService(() => require('./service'));
```

```javascript
// service.js
module.exports = async function() {

    TrackPlayer.addEventListener('remote-play', () => TrackPlayer.play());

    TrackPlayer.addEventListener('remote-pause', () => TrackPlayer.pause());

    TrackPlayer.addEventListener('remote-stop', () => TrackPlayer.destroy());

    // ...

};
```

## Progress Updates
Music apps often need an automated way to show the progress of a playing track. For this purpose, we created the hook: `useProgress` which updates itself automatically.

#### Example

```tsx
import TrackPlayer, { useProgress } from 'react-native-track-player';

const MyPlayerBar = () => {
    const progress = useProgress();

    return (
            // Note: formatTime and ProgressBar are just examples:
            <View>
                <Text>{formatTime(progress.position)}</Text>
                <ProgressBar
                    progress={progress.position}
                    buffered={progress.buffered}
                />
            </View>
        );

}
```

## Track Player Options

Track Player can be configured using a number of options. Some of these options pertain to the media controls available in the lockscreen / notification and how they behave, others describe the availability of capabilities needed for platform specific functionalities like Android Auto.

You can change options multiple times. You do not need to specify all the options, just the ones you want to change.

For more information about the properties you can set, [check the documentation](https://react-native-track-player.js.org/react-native-track-player/documentation/#updateoptionsdata).

#### Example

```typescript
import TrackPlayer, { Capability } from 'react-native-track-player';

TrackPlayer.updateOptions({
    // Media controls capabilities
    capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.Stop,
    ],

    // Capabilities that will show up when the notification is in the compact form on Android
    compactCapabilities: [Capability.Play, Capability.Pause],

    // Icons for the notification on Android (if you don't like the default ones)
    playIcon: require('./play-icon.png'),
    pauseIcon: require('./pause-icon.png'),
    stopIcon: require('./stop-icon.png'),
    previousIcon: require('./previous-icon.png'),
    nextIcon: require('./next-icon.png'),
    icon: require('./notification-icon.png')
});
```

### Notes
* In order to keep playing audio in the background, the Android player service requires a notification in order to be in the "foreground" state (allowing it to use more system resources without being killed) which is forced to be "ongoing" (not swipable). Because you can not stop the player service by swiping the Android notification, we highly recommend you to have a stop button in the notification. The button should `destroy()` the player.
