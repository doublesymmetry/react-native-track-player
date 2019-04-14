---
title: Background Mode
permalink: /background/
---

## Android
The background support works right out of the box, and it will keep playing even after the app is closed. If that is not the desired behavior and you only want to play when the app is open, you can disable it with the `stopWithApp` property in `updateOptions`:

```js
TrackPlayer.updateOptions({
    stopWithApp: true
});
```

While your app is in background, the UI might unmount, but you can still handle the events through the playback service.

### Notification
The notification will be visible as long as the playback service runs. Your app will be opened when it is clicked, you can implement a custom initialization (e.g.: opening directly the player UI) by using the [Linking API](https://facebook.github.io/react-native/docs/linking) looking for the `trackplayer://notification.click` URI.

## iOS
The background support requires you to activate the background capability in Xcode. Without activating it, the audio will only play when the app is in foreground.

![Xcode Background Capability](https://developer.apple.com/library/content/documentation/Audio/Conceptual/AudioSessionProgrammingGuide/Art/background_modes_2x.png)

## Windows
The background support requires you to add the background capability in the app manifest, as [documented by Microsoft](https://docs.microsoft.com/windows/uwp/audio-video-camera/background-audio#background-media-playback-manifest-capability)

```xml
<Capabilities>
    <uap3:Capability Name="backgroundMediaPlayback"/>
</Capabilities>
```