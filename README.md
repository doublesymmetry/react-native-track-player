# react-native-track-player

An all-in-one module, supporting multiple audio and video players, media metadata, media controls, chromecast, files streams, DASH/HLS/SmoothStreaming, and more!

Designed to be as lightweight as possible.

## WIP
This is a work-in-progress project not ready for production yet.

## Features

* **Lightweight** - Highly optimized to use the least amount of storage according to your needs and do the least amount of processing
* **Multi-player support** - Create how many players you want
* **Media Controls support** - Control the app from bluetooth, lockscreen, notification, smartwatch or even a car
* **Local files or Remote streams** - It doesn't matter where the media belongs, we've got you covered
* **Chromecast support** - Cast media to any Google Cast compatible device, supporting custom media receivers
* **Adaptive bitrate streaming support** - Optional support for DASH, HLS or SmoothStreaming
* **Video support** - A simple video component that can be bound to a player
* **Caching support** - Cache media files to play media without using data quota
* **Background support** - Keep playing media even when the app is closed
* **Fully Customizable** - Even the notification icons are customizable

## Example

If you want to get started with this module, check the [API](https://github.com/Guichaguri/react-native-track-player/wiki/API) page.
If you want detailed information about the API, check the [Documentation](https://github.com/Guichaguri/react-native-track-player/wiki/Documentation).
```javascript
import TrackPlayer from 'react-native-track-player';

// Waits for the module to get ready
TrackPlayer.onReady(async () => {
    
    // Creates a local player
    let id = await TrackPlayer.createPlayer();
    
    // Sets the player as the main one
    TrackPlayer.setMain(id);
    
    // Loads a local track
    await TrackPlayer.load(id, {
        id: 'trackId',
        url: require('track.mp3'),
        title: 'Track Title',
        artist: 'Track Artist',
        artwork: require('track.png')
    });
    
    // Starts playing it
    TrackPlayer.play(id);
    
});
```

## Installation
First of all, install the module from NPM with the following command:
```
npm install react-native-track-player --save
```

### Automatic

Link the module with the following command:
```
react-native link
```

### Manual
#### Android

**android/app/build.gradle**
```diff
dependencies {
    ...
    compile "com.facebook.react:react-native:+"  // From node_modules
+   compile project(':react-native-track-player')
}
```

**android/settings.gradle**
```diff
...
include ':app'
+include ':react-native-track-player'
+project(':react-native-track-player').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-track-player/android')
```

**MainApplication.java**
```diff
// ...

+import guichaguri.trackplayer.TrackPlayer;

public class MainApplication extends Application implements ReactApplication {
    // ...

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
+           new TrackPlayer(),
            new MainReactPackage()
        );
    }

    // ...
  }
```