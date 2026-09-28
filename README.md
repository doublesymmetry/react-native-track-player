# react-native-track-player

**The premium audio player for React Native.**

Built on the New Architecture with synchronous native calls — background playback, Android Auto, audio caching, preloading, and more.

> [!IMPORTANT]
> **Starting with V5, react-native-track-player is commercially licensed.** Personal and educational use remains free; commercial use requires a paid license — see [rntp.dev/pricing](https://rntp.dev/pricing). V4 and earlier remain available under Apache-2.0 on the [`v4` branch](https://github.com/doublesymmetry/react-native-track-player/tree/v4).

[![npm version](https://img.shields.io/npm/v/@rntp/player?color=violet)](https://www.npmjs.com/package/@rntp/player)
[![npm downloads](https://img.shields.io/npm/dm/@rntp/player)](https://www.npmjs.com/package/@rntp/player)
[![License](https://img.shields.io/badge/license-Commercial-violet)](./license.txt)

---

## Features

- **Simple TypeScript API** — Clean, minimal API fully typed for React Native developers
- **New Architecture Native** — Built on JSI with TurboModule support, no bridge overhead, no jitter
- **Background Playback** — Audio continues when the app is backgrounded or screen is off
- **Android Auto** — Full support for native car dashboard integration
- **Preloading** — Background buffering of upcoming tracks for gapless-like playback
- **Audio Caching** — Built-in caching to reduce bandwidth and enable offline playback
- **Remote Controls** — Lock screen, notification controls, and headset support out of the box
- **React Hooks** — `usePlaybackState`, `useIsPlaying`, `useProgress`, `useActiveMediaItem`, and more

## Requirements

- React Native **0.76** or later
- **New Architecture** enabled (Fabric + TurboModules), with **bridgeless enabled**

## Installation

```sh
npm install @rntp/player
```

Then for iOS:

```sh
cd ios && pod install
```

Android links automatically.

## Quick Start

```ts
import TrackPlayer from '@rntp/player';

// 1. Set up the player (call once at app startup, in the foreground on Android)
await TrackPlayer.setupPlayer({
  contentType: 'music',
  handleAudioBecomingNoisy: true,
  android: { wakeMode: 'network' },
});

// 2. Add tracks and play
await TrackPlayer.setMediaItems([
  {
    url: 'https://example.com/track.mp3',
    title: 'Track Title',
    artist: 'Artist Name',
    artwork: 'https://example.com/artwork.jpg',
  },
]);

TrackPlayer.play();
```

```tsx
// 3. Use hooks in your UI
import { useIsPlaying, useProgress, useActiveMediaItem } from '@rntp/player';

function PlayerUI() {
  const { playing } = useIsPlaying();
  const { position, duration } = useProgress();
  const track = useActiveMediaItem();

  return (/* your player UI */);
}
```

## Documentation

Full documentation, API reference, and guides are available at **[rntp.dev](https://rntp.dev)**.

## V5

V5 is a complete rewrite and is **not backwards-compatible with V4**.

Key improvements in V5:
- Built on JSI — `getProgress()`, `getQueue()`, `isPlaying()` return synchronously
- Full TurboModule and Fabric support
- Rewritten Android and iOS native layers

## License

Free for non-commercial use. **Commercial use requires a license.**

See [license.txt](./license.txt) for details or visit [rntp.dev/pricing](https://rntp.dev/pricing) to purchase a commercial license.

For questions: [team@doublesymmetry.com](mailto:team@doublesymmetry.com)
