---
title: Getting Started
permalink: /api/
---

## Starting off
First of all, you need to initialize the player.
```javascript
import TrackPlayer from 'react-native-track-player';

TrackPlayer.setupPlayer().then(() => {
    // The player is ready to be used
});
```
This usually takes less than a second. If the player is already initialized, the promise is resolved immediately.

You also need to register a playback service right after you have registered the main component of your app:
```javascript
//AppRegistry.registerComponent(...);
TrackPlayer.registerPlaybackService(() => require('./service.js'));
```
```javascript
// service.js
module.exports = async function() {
    // This service needs to be registered for the module to work
	// but it will be used later in the "Receiving Events" section
}
```

## Controlling the Player

### Initializing a Track
Using the newly created player, you can add a media file from the network or from the app bundle.

First of all, you need to create a [Track Structure](https://react-native-kit.github.io/react-native-track-player/documentation/#track-structure) to provide all the information for the player

```javascript
var track = {
    id: 'unique track id', // Must be a string, required
    
    url: 'http://example.com/avaritia.mp3', // Load media from the network
    url: require('./avaritia.ogg'), // Load media from the app bundle
    url: 'file:///storage/sdcard0/Music/avaritia.wav' // Load media from the file system 

    title: 'Avaritia',
    artist: 'deadmau5',
    album: 'while(1<2)',
    genre: 'Progressive House, Electro House',
    date: '2014-05-20T07:00:00+00:00', // RFC 3339
    
    artwork: 'http://example.com/avaritia.png', // Load artwork from the network
    artwork: require('./avaritia.jpg'), // Load artwork from the app bundle
    artwork: 'file:///storage/sdcard0/Downloads/artwork.png' // Load artwork from the file system
};
```
[Check the documentation](https://react-native-kit.github.io/react-native-track-player/documentation/#track-structure) for more information about the properties you can have in a track structure.

After creating it, you can [add](https://react-native-kit.github.io/react-native-track-player/documentation/#addtracks-insertbeforeid) it to the queue:

```javascript
// Adding to the queue
// You can provide multiple items in one single call
TrackPlayer.add([track, track2]).then(function() {
    // The tracks were added
});
```

### Player Information
After a media file has been loaded, you can get information about it (such as retrieving the [state](https://react-native-kit.github.io/react-native-track-player/documentation/#state))

```javascript
// State is one of STATE_NONE, STATE_PLAYING, STATE_PAUSED, STATE_STOPPED, STATE_BUFFERING
let state = await TrackPlayer.getState();

let trackId = await TrackPlayer.getCurrentTrack();
let trackObject = await TrackPlayer.getTrack(trackId);

// Position, buffered position and duration return values in seconds
let position = await TrackPlayer.getPosition();
let buffered = await TrackPlayer.getBufferedPosition();
let duration = await TrackPlayer.getDuration();
```

### Changing the State

```javascript
TrackPlayer.play();
TrackPlayer.pause();
TrackPlayer.stop();
TrackPlayer.reset();
```

### Changing position and volume

```javascript
// Seeks to a position in seconds. Can only be called after the player has been loaded
TrackPlayer.seekTo(12.5);

// The volume must be a number between 0 to 1.
TrackPlayer.setVolume(0.5);
```

### Controlling the queue
```javascript
// Skipping
TrackPlayer.skip(trackId);
TrackPlayer.skipToNext();
TrackPlayer.skipToPrevious();

// Removing from the queue
TrackPlayer.remove([trackId1, trackId2]);

// Retrieving the queue
let tracks = await TrackPlayer.getQueue();
```
## Receiving Events
![Handling Events](https://i.imgur.com/gnsmHvU.png)

### React components
* Works as long as the UI is mounted
* Should only be used to update the UI state

Events inside React components can be used to:
* Update the state about the current playing track
* Update the state about the current playback state

#### Example
```jsx
class PlayerInfo extends Component {

    componentDidMount() {
        // Adds an event handler for the playback-track-changed event
        this.onTrackChange = TrackPlayer.addEventListener('playback-track-changed', async (data) => {
            
            const track = await TrackPlayer.getTrack(data.nextTrack);
            this.setState({trackTitle: track.title});
            
        });
    }
    
    componentWillUnmount() {
        // Removes the event handler
        this.onTrackChange.remove();
    }

    render() {
        return (
            <Text>{this.state.trackTitle}</Text>
        );
    }

}
```

### Playback Service
* Works as long as the player runs
* May run while the app is in background (without the UI mounted)
* Should be used to run any code that should be directly tied to the player

The player service can be used to:
* Process media buttons
* Add more tracks when the queue exhausts
* Playback analytics

#### Example
```javascript
// This needs to go right after you register the main component of your app
//AppRegistry.registerComponent(...)
TrackPlayer.registerPlaybackService(() => require('./service.js'));
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

You can find a full list of events (and its parameters) in the [full documentation reference](https://react-native-kit.github.io/react-native-track-player/documentation/#events).

## Progress
Music apps usually need an automated way to update its progress, for that, we created a React Component that updates itself. You can build your own player bar on top of it:

```jsx
class MyPlayerBar extends TrackPlayer.ProgressComponent {

    render() {
        return (
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

Note: `formatTime` and `ProgressBar` are just examples, replace them with your own implementation.

Be careful, as the component will be re-rendered every progress update!

You can find more information about the ProgressComponent in the [documentation](https://react-native-kit.github.io/react-native-track-player/documentation/#progresscomponent).

## Options
You can define the rating type, capabilities, icons, etc.
Every parameter is optional, you don't have to set all of them.
You can also call this function multiple times, you don't need to specify all the options, just the ones you want to change.

It's recommended to set the options right before using any other function (other than `setupPlayer`)

For more information about the properties you can set, [check the documentation](https://react-native-kit.github.io/react-native-track-player/documentation/#updateoptionsdata).
```javascript
TrackPlayer.updateOptions({
    // One of RATING_HEART, RATING_THUMBS_UP_DOWN, RATING_3_STARS, RATING_4_STARS, RATING_5_STARS, RATING_PERCENTAGE
    ratingType: TrackPlayer.RATING_5_STARS,
    
    // Whether the player should stop running when the app is closed on Android
    stopWithApp: false,

    // An array of media controls capabilities
    // Can contain CAPABILITY_PLAY, CAPABILITY_PAUSE, CAPABILITY_STOP, CAPABILITY_SEEK_TO,
    // CAPABILITY_SKIP_TO_NEXT, CAPABILITY_SKIP_TO_PREVIOUS, CAPABILITY_SET_RATING
    capabilities: [
        TrackPlayer.CAPABILITY_PLAY,
        TrackPlayer.CAPABILITY_PAUSE,
        TrackPlayer.CAPABILITY_STOP
    ],
    
    // An array of capabilities that will show up when the notification is in the compact form on Android
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
    icon: require('./notification-icon.png'), // The notification icon
});
```

### Notes
* Due to the notification not being swipable, we highly recommend you to have a stop button in the notification. The button should `destroy()` the player.