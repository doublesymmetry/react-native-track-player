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
```javascript
import TrackPlayer from 'react-native-track-player';

TrackPlayer.setupPlayer().then(() => {
    // The player is ready to be used
});
```

You also need to register a [playback service](#playback-service) right after registering the main component of your app:
```javascript
// AppRegistry.registerComponent(...);
TrackPlayer.registerPlaybackService(() => require('./service'));
```

```javascript
// service.js
module.exports = async function() {
    // This service needs to be registered for the module to work
    // but it will be used later in the "Receiving Events" section
}
```

## Controlling the Player

### Adding Tracks to the Playback Queue
You can add a track to the player using a url or by requiring a file in the app bundle or on the file system.

First of all, you need to create a [track object](https://react-native-kit.github.io/react-native-track-player/documentation/#track-object), which is a plain javascript object with a number of properties describing the track. Then [add](https://react-native-kit.github.io/react-native-track-player/documentation/#addtracks-insertbeforeid) the track to the queue:

```javascript
const track1 = {
    id: 'avaritia', // Must be a string, required
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
    id: 'coelacanth',
    url: require('./coelacanth.ogg'), // Load media from the app bundle
    title: 'Coelacanth I',
    artist: 'deadmau5',
    artwork: require('./cover.jpg'), // Load artwork from the app bundle
    duration: 166
};

const track3 = {
    id: 'ice_age',
    url: 'file:///storage/sdcard0/Downloads/artwork.png', // Load media from the file system
    title: 'Ice Age',
    artist: 'deadmau5',
     // Load artwork from the file system:
    artwork: 'file:///storage/sdcard0/Downloads/cover.png',
    duration: 411
};

// Add the tracks to the queue:
await TrackPlayer.add([track1, track2, track3]);
```

### Player Information

```javascript
const state = await TrackPlayer.getState();
if (state === TrackPlayer.STATE_PLAYING) {
    console.log('The player is playing');
};

const trackId = await TrackPlayer.getCurrentTrack();
const trackObject = await TrackPlayer.getTrack(trackId);
console.log(`Title: ${trackObject.title}`);

const position = await TrackPlayer.getPosition();
const duration = await TrackPlayer.getDuration();
console.log(`${duration - position} seconds left.`);
```

### Changing Playback State

```javascript
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
```javascript
// Skip to a specific track id:
await TrackPlayer.skip('the-track-id');

// Skip to the next track in the queue:
await TrackPlayer.skipToNext();

// Skip to the previous track in the queue:
await TrackPlayer.skipToPrevious();

// Remove two tracks from the queue:
await TrackPlayer.remove([trackId1, trackId2]);

// Retrieve the track objects in the queue:
const tracks = await TrackPlayer.getQueue();
console.log(`First title: ${tracks[0].title}`);
```
#### Playback Events

You can subscribe to [playback events](https://react-native-kit.github.io/react-native-track-player/documentation/#player), which describe the changing nature of the playback state. For example, subscribe to the `playback-track-changed` event to be notified when the track has changed or subscribe to the `playback-state` event to be notified when the player buffers, plays, pauses and stops.

#### Example
```jsx
const PlayerInfo = () => {
    const [trackTitle, setTrackTitle] = useState();
    useEffect(() => {
        let mounted = true;

        // Set the initial track title:
        (async() => {
            const trackId = await TrackPlayer.getCurrentTrack();
            if (!mounted || !trackId) return;
            const track = await TrackPlayer.getTrack(trackId);
            if (!mounted) return;
            setTrackTitle(track.title);
        })();

        // Set the track title whenever the track changes:
        const listener = TrackPlayer.addEventListener(
            'playback-track-changed',
            async (data) => {
                const track = await TrackPlayer.getTrack(data.nextTrack);
                if (!mounted) return;
                setTrackTitle(track.title);
            }
        );
        return () => {
            mounted = false;
            listener.remove();
        }
    }, []);

    return (
        <Text>{trackTitle}</Text>
    );
}
```

### Playback Service

The playback service keeps running even when the app is in the background. It will start when the player is set up and will only stop when the player is destroyed. It is a good idea to put any code in there that needs to be directly tied to the player state. For example, if you want to be able to track what is being played for analytics purposes, the playback service would be the place to do so.

#### Remote Events

[Remote events](https://react-native-kit.github.io/react-native-track-player/documentation/#media-controls) are sent from places outside of our user interface that we can react to. For example if the user presses the pause media control in the IOS lockscreen / Android notification or from their Bluetooth headset, we want to have TrackPlayer pause the audio.

If you create a listener to a remote event like `remote-pause` in the context of a React component, there is a chance the UI will be unmounted automatically when the app is in the background, causing it to be missed. For this reason it is best to place remote listeners in the playback service, since it will keep running even when the app is in the background.

#### Example
```javascript
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

## Progress Component
Music apps often need an automated way to show the progress of a playing track. For this purpose, we created [ProgressComponent](https://react-native-kit.github.io/react-native-track-player/documentation/#progresscomponent) which updates itself automatically. You can build your own player bar on top of it. Be careful, as the component will be re-rendered every progress update!

#### Example

```jsx
class MyPlayerBar extends TrackPlayer.ProgressComponent {

    render() {
        return (
            // Note: formatTime and ProgressBar are just examples:
            <View>
                <Text>{formatTime(this.state.position)}</Text>
                <ProgressBar
                    progress={this.getProgress()}
                    buffered={this.getBufferedProgress()}
                />
            </View>
        );
    }

}
```

## Track Player Options

Track Player can be configured using a number of options. Some of these options pertain to the media controls available in the lockscreen / notification and how they behave, others describe the availability of capabilities needed for platform specific functionalities like Android Auto.

You can change options multiple times. You do not need to specify all the options, just the ones you want to change.

For more information about the properties you can set, [check the documentation](https://react-native-kit.github.io/react-native-track-player/documentation/#updateoptionsdata).

#### Example

```javascript
TrackPlayer.updateOptions({
    // Media controls capabilities
    capabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE,
        TrackPlayer.CAPABILITY_STOP,
        TrackPlayer.CAPABILITY_NEXT,
        TrackPlayer.CAPABILITY_PREVIOUS,
    ],

    // Capabilities that will show up when the notification is in the compact form on Android
    compactCapabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE
    ]

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
