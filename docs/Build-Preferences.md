---
title: Build Preferences
description: "Custom compile-time options"
nav_order: 5
permalink: /build-preferences/
redirect_from:
  - /build-options/
---

# Build Preferences

You can change a few properties for building the module in your app, those properties can be set in a JSON file in the root folder of your app (the same folder where `index.android.js` and `node_modules` are stored). The file should be named `track-player.json`, and it should look like this:

```json
{
  "dash": false,
  "hls": false,
  "smoothstreaming": false
}
```

#### `dash`
{: .d-inline-block }
Android
{: .label .label-green }

Whether it will add support for DASH streams. This option adds an extension for handling DASH streams, which changes the app size.

#### `hls`
{: .d-inline-block }
Android
{: .label .label-green }

Whether it will add support for HLS streams. This option adds an extension for handling HLS streams, which changes the app size.

#### `smoothstreaming`
{: .d-inline-block }
Android
{: .label .label-green }

Whether it will add support for SmoothStreaming streams. This option adds an extension for handling SmoothStreaming streams, which changes the app size.
