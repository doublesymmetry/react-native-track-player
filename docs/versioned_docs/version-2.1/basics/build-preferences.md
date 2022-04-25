---
sidebar_position: 6
---

# Build Preferences

You can optionally configure a few options for how the Track Player module is built in your app. Add a JSON file named `track-player.json` to the root folder of your app (the same folder where `index.android.js` and `node_modules` are stored) and set one or more of the following properties:

```json
{
  "dash": false,
  "hls": false,
  "smoothstreaming": false
}
```

## `dash` (Android)

Whether it will add support for [DASH](https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP) streams. This option adds an extension for handling DASH streams, which changes the app size. Defaults to `false`.

## `hls` (Android)

Whether it will add support for [Smooth Streaming](https://en.wikipedia.org/wiki/Adaptive_bitrate_streaming#Microsoft_Smooth_Streaming) streams. This option adds an extension for handling SmoothStreaming streams, which changes the app size. Defaults to `false`.

## `smoothstreaming` (Android)

Whether it will add support for SmoothStreaming streams. This option adds an extension for handling SmoothStreaming streams, which changes the app size.
