---
sidebar_position: 4
---

# Background Mode

React Native Track Player supports playing audio while your app is in the background on all supported platforms.

## Android
Background audio playback works right out of the box. By default, the audio will continue to play, not only when the app is suspended in the background, but also after the app is closed by the user. If that is not the desired behavior and you want the audio to stop playing when the app is closed, you can disable it with the `stoppingAppPausesPlayback` property in `updateOptions`. In this case, the audio will still play while the app is open in the background.:

```js
TrackPlayer.updateOptions({
    stoppingAppPausesPlayback: true
});
```

Please note that while your app is in background, your UI might be unmounted by React Native. Event listeners added in the [playback service](./playback-service.md) will continue to receive events.

### Notification
The notification will be visible as long as the playback service runs. Your app will be opened when it is clicked. You can implement a custom initialization (e.g.: opening directly the player UI) by using the [Linking API](https://reactnative.dev/docs/linking) looking for the `trackplayer://notification.click` URI.

## iOS
To allow background audio playback on iOS, you need to activate the 'Audio, Airplay and Picture in Picture' background mode in Xcode. Without activating it, the audio will only play when the app is in the foreground.

![Xcode Background Capability](https://developer.apple.com/library/content/documentation/Audio/Conceptual/AudioSessionProgrammingGuide/Art/background_modes_2x.png)

### iOS Simulator
As of iOS Simulator version 11, Apple has removed support for Control Center and Now Playing Info from the simulator. You will not be able to test lock screen controls on recent versions of iOS Simulator. You can either test on real devices, or download older versions of the iOS Simulator.

## Windows
To allow background audio playback on Windows, you need to add the background capability in the app manifest, as [documented by Microsoft](https://docs.microsoft.com/windows/uwp/audio-video-camera/background-audio#background-media-playback-manifest-capability)

```xml
<Capabilities>
    <uap3:Capability Name="backgroundMediaPlayback"/>
</Capabilities>
```
