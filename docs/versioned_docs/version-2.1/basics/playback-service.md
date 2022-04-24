---
sidebar_position: 3
---

# Playback Service

The playback service keeps running even when the app is in the background. It will start when the player is set up and will only stop when the player is destroyed. It is a good idea to put any code in there that needs to be directly tied to the player state. For example, if you want to be able to track what is being played for analytics purposes, the playback service would be the place to do so.

## Remote Events

[Remote events](../api/events.md#media-controls) are sent from places outside of our user interface that we can react to. For example if the user presses the pause media control in the IOS lockscreen / Android notification or from their Bluetooth headset, we want to have TrackPlayer pause the audio.

If you create a listener to a remote event like `remote-pause` in the context of a React component, there is a chance the UI will be unmounted automatically when the app is in the background, causing it to be missed. For this reason it is best to place remote listeners in the playback service, since it will keep running even when the app is in the background.

## Example
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
