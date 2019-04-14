---
title: Build Preferences
permalink: /build-preferences/
---

You can change a few properties for building the module in your app, those properties can be set in a JSON file in the root folder of your app (the same folder where `index.android.js` and `node_modules` are stored). The file should be named `track-player.json`, and it should look like this:

```json
{
  "dash": false,
  "hls": false,
  "smoothstreaming": false
}
```

#### `dash` (Android Only)
Whether it will add support for DASH streams. This option adds an extension for handling DASH streams, which changes the app size.

#### `hls` (Android Only)
Whether it will add support for HLS streams. This option adds an extension for handling HLS streams, which changes the app size.

#### `smoothstreaming` (Android Only)
Whether it will add support for SmoothStreaming streams. This option adds an extension for handling SmoothStreaming streams, which changes the app size.
