---
---

[![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player) [![Chat](https://badges.gitter.im/react-native-track-player/gitter.png)](https://gitter.im/react-native-track-player/Support)

A fully fledged audio module created for music apps. Provides audio playback, external media controls, chromecast support, background mode and more!

<ul>
    {% for page in site.links %}
        <li><a href="{{ page.url }}">{{ page.title }}</a></li>
    {% endfor %}
</ul>

## Features

* **Lightweight** - Optimized to use the least amount of resources according to your needs
* **Feels native** - As everything is built together, it follows the same design principles as real music apps do
* **Multi-platform** - Supports Android, iOS and Windows
* **Media Controls support** - Provides events for controlling the app from a bluetooth device, the lockscreen, a notification, a smartwatch or even a car
* **Local or network, files or streams** - It doesn't matter where the media belongs, we've got you covered
* **[Casting support](https://github.com/react-native-kit/react-native-track-casting)** - Seamlessly switch to any Google Cast compatible device, supporting custom media receivers
* **Adaptive bitrate streaming support** - Support for DASH, HLS or SmoothStreaming
* **Caching support** - Cache media files to play them again without an internet connection
* **Background support** - Keep playing audio even after the app is in background
* **Fully Customizable** - Even the notification icons are customizable!
* **Supports React Hooks 🎣** - Includes React Hooks for common use-cases so you don't have to write them

## Example

If you want to get started with this module, check the [API](https://react-native-kit.github.io/react-native-track-player/api/) page.
If you want detailed information about the API, check the [Documentation](https://react-native-kit.github.io/react-native-track-player/documentation/).
You can also look at our example project [here](https://github.com/react-native-kit/react-native-track-player/tree/master/example).

```javascript
import TrackPlayer from 'react-native-track-player';

// Creates the player
TrackPlayer.setupPlayer().then(async () => {

    // Adds a track to the queue
    await TrackPlayer.add({
        id: 'trackId',
        url: require('track.mp3'),
        title: 'Track Title',
        artist: 'Track Artist',
        artwork: require('track.png')
    });

    // Starts playing it
    TrackPlayer.play();

});
```

## Maintainers
[David Chavez](https://github.com/dcvz) & [Guilherme Chaguri](https://github.com/Guichaguri)
