---
sidebar_position: 2
---

# Capability

All Capability types are made available through the named export `Capability`. Some exposed capabilities are constants and some are builder functions which allow further configuration.

**Important:** Some of these builder functions expose a `showInNotification` and `notificationOptions` parameter. These are only really used for Android as the system differentiates between capabilities in the media session, enabled commands that could be triggered externally (i.e over headset or auto), from what you display in the notification as buttons. On iOS there's only one system for actions users can take which allows for both external actions and in the now playing section in the command center. As such, on iOS if the capability is present, it will be enabled.   



```ts
import { Capability } from 'react-native-track-player';
```

| Name                                                                | Description                                                                                          |
|---------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| `Play(showInNofication, notificationOptions)`                       | Capability indicating the ability to play                                                            |
| `PlayFromId`                                                        | Capability indicating the ability to play from a track id (Required for Android Auto)                |
| `PlayFromSearch`                                                    | Capability indicating the ability to play from a text/voice search (Required for Android Auto)       |
| `Pause(showInNofication, notificationOptions)`                      | Capability indicating the ability to pause                                                           |
| `Stop(showInNofication, notificationOptions)`                       | Capability indicating the ability to stop                                                            |
| `SeekTo(showInNofication, notificationOptions)`                     | Capability indicating the ability to seek to a position in the timeline                              |
| `Skip`                                                              | Capability indicating the ability to skip to any song in the queue                                   |
| `SkipToNext(showInNofication, notificationOptions)`                 | Capability indicating the ability to skip to the next track                                          |
| `SkipToPrevious(showInNofication, notificationOptions)`             | Capability indicating the ability to skip to the previous track                                      |
| `JumpForward(showInNofication, notificationOptions, jumpInterval)`  | Capability indicating the ability to jump forward by the amount of seconds specified in the options  |
| `JumpBackward(showInNofication, notificationOptions, jumpInterval)` | Capability indicating the ability to jump backward by the amount of seconds specified in the options |
| `SetRating(ratingType)`                                             | (android-only) Capability indicating the ability to set the rating value based on the rating type    |
| `Like(title, isActive)`                                             | (ios-only) Capability indicating the ability to like from control center                             |
| `Dislike(title, isActive)`                                          | (ios-only) Capability indicating the ability to dislike from control center                          |
| `Bookmark(title, isActive)`                                         | (ios-only) Capability indicating the ability to bookmark from control center                         |
