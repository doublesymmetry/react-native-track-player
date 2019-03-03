---
title: Cast Integration
permalink: /cast/
---

## Introduction
This tutorial is for apps that want to implement support for casting, and requires you to know at least what Chromecast is. This is only required for Google Cast devices, bluetooth support should work out-of-the-box.

* "Google Cast" is the name of the casting technology behind Chromecast, Chromecast built-in, Google Home, etc.
* "Sender apps" are applications that connect to a Google Cast device and send commands to it. In this case, your React Native app is a sender.
* "Receiver apps" are web applications (HTML, CSS and Javascript) that run in the Google Cast device. 

There are three types of receiver apps: Default Media Receiver (a default receiver that supports audio and video), Styled Media Receiver (based on the default one, but supports custom CSS) and Custom Receiver (your own HTML, CSS and Javascript code)

## Custom Application ID
If you are using the default media receiver (not a custom or styled one), you can skip this step.

If you want to create a new receiver application but don't know where to start, check out the official documentation [here](https://developers.google.com/cast/docs/receiver_apps).

The Cast SDK v3 for Android might need the application ID before the app finishes the initialization, because of that, you'll have to set the application ID in a JSON file.

Create a `track-player.json` file in the root of your app (same folder as `index.android.js` or `node_modules`), with the following JSON attribute:

```json
{
  "castApplicationId": "Your Application ID here"
}
```

See more about the `track-player.json` structure in [Build Preferences](https://react-native-kit.github.io/react-native-track-player/build-preferences/)

## Adding the Cast Button
The Cast Button is the most important component for Google Cast integration, as it lets the user select and connect to a cast device.

The component manages its visibility automatically depending whether it has found cast devices, its icon is also updated based on the state.

```javascript
import {CastButton} from 'react-native-track-player'

...

render() {
   return <CastButton />
}
```

The button also supports a function that shows the dialog:

```javascript
render() {
    return <CastButton ref="myCastButton" />
}

openDialog() {
    this.refs['myCastButton'].showDialog();
}
```

Even though the Cast Button should be enough for almost everyone, you might want to fully customize it. For that, you can create a new button, listen for the `cast-state` event to change its icon and visibility, hide the original button with `display: 'none'` and trigger the `showDialog` function when it is clicked. Make sure your button is hidden by default, and wait for the `cast-state` event to change its visibility.

## Events
### `cast-state`
Triggered when the state changes. It can send four `state` values:

* `CAST_NO_DEVICES_AVAILABLE`: When no devices were found
* `CAST_NOT_CONNECTED`: When one or more devices were found
* `CAST_CONNECTING`: When it's connecting to a device
* `CAST_CONNECTED`: When the device was successfully connected

Example:
```javascript
if(event.type == 'cast-state') {
    if(event.type != TrackPlayer.CAST_NO_DEVICES_AVAILABLE) {
        // Show our custom button
    }
}
```

### `cast-connecting`
Triggered when the user selects a device and it starts connecting to it. This event also sends information about the device. You can use this event to show a text in your UI.

Example:
```javascript
if(event.type == 'cast-connecting') {
    var id = event.id; // The device ID
    var version = event.version; // The device version
    var name = event.name; // The display name
    var model = event.model; // The device model
    var ip = event.ip; // The device IP address
    var port = event.port; // The device port

    // Do something, such as storing the display name and showing it to the user
}
```

### `cast-connected`
Triggered when the device is fully connected and all playback commands (such as `play`, `pause`, etc) will be sent directly to the device.

### `cast-disconnecting`
Triggered when the device starts disconnecting.

### `cast-disconnected`
Triggered when the device is fully disconnected and playback commands (such as `play`, `pause`, etc) will be sent to a local player.

## Notes
* In Android, the Cast Button only works in a `FragmentActivity`, but I've made a little hack that works in a regular `Activity`. If you're having problems or want the native behavior of the button, change your activity from `ReactActivity` to `ReactFragmentActivity`.