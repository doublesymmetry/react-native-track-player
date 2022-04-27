---
sidebar_position: 1
---

# State

All State types are made available through the named export `State`:

```ts
import { State } from 'react-native-track-player';
```

| Name | Description |
|------|-------------|
| `None`        | State indicating that no media is currently loaded |
| `Ready`       | State indicating that the player is ready to start playing |
| `Playing`     | State indicating that the player is currently playing |
| `Paused`      | State indicating that the player is currently paused |
| `Stopped`     | State indicating that the player is currently stopped |
| `Buffering`   | State indicating that the player is currently buffering (in "play" state) |
| `Connecting`  | State indicating that the player is currently buffering (in "pause" state) |
