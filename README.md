<img src="https:/rntp.dev/img/optimized-logo.svg" width="300" />

[![downloads](https://img.shields.io/npm/dw/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![npm](https://img.shields.io/npm/v/react-native-track-player.svg)](https://www.npmjs.com/package/react-native-track-player)
[![discord](https://img.shields.io/discord/567636850513018880.svg)](https://discordapp.com/invite/ya2XDCR)
[![Commitizen friendly](https://img.shields.io/badge/commitizen-friendly-brightgreen.svg)](http://commitizen.github.io/cz-cli/)

----

A fully-fledged audio module created for music apps. Provides audio playback, external media controls, background mode and more!

<div align="left" valign="middle">
<a href="https://runblaze.dev">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://www.runblaze.dev/logo_dark.png">
   <img align="right" src="https://www.runblaze.dev/logo_light.png" height="102px"/>
 </picture>
</a>

<br style="display: none;"/>

_[Blaze](https://runblaze.dev) sponsors RNTP by providing super fast Apple Silicon based macOS Github Action Runners. Use the discount code `RNTP50` at checkout to get 50% off your first year._

</div>

## Overview

- [Documentation](https://rntp.dev)
  * [Installation](https://rntp.dev/docs/basics/installation/)
  * [Getting Started](https://rntp.dev/docs/basics/getting-started/)
  * [API Docs](https://rntp.dev/docs/api/events)
  * [Platform Support](https://rntp.dev/docs/basics/platform-support)
  * [Background Mode](https://rntp.dev/docs/basics/background-mode)
  * [Build Preferences](https://rntp.dev/docs/basics/build-preferences)
  * [v2 Migration Guide](https://rntp.dev/docs/v2-migration)
- [Sponsors](#sponsors)
- [Features](#features)
- [Why another music module?](#why-another-music-module)
- [Example Setup](#example-setup)
- [Core Team ✨](#core-team-)
- [Special Thanks ✨](#special-thanks-)
- [I Have A Bug/Feature Request](#contributing)
- [Community](#Community)

Not sure where to start?

1. Try [Getting Started](https://rntp.dev/docs/basics/getting-started).
2. Peruse the [API Docs](https://rntp.dev/docs/api/events).
3. Run the [Example Project](/example).

## Commerical Usage

**Are you using RNTP for a personal project?** Great! Sponsor us if you're able but otherwise you can support us by reporting any bugs you find, sending us screenshots of your projects and starring us on Github 🌟

**Are you using RNTP at your organisation and/or making money from it?** Awesome! We rely on your support to keep RNTP developed and maintained under the Apache-2.0 license. You can do that through [Github Sponsors](https://github.com/sponsors/doublesymmetry).

## Features

* **Lightweight** - Optimized to use the least amount of resources according to your needs
* **Feels native** - As everything is built together, it follows the same design principles as real music apps do
* **Multi-platform** - Supports Android, iOS and Web
* **Media Controls support** - Provides events for controlling the app from a Bluetooth device, the lock screen, a notification, a smartwatch or even a car
* **Local or network, files or streams** - It doesn't matter where the media belongs, we've got you covered
* **Adaptive bitrate streaming support** - Support for DASH, HLS or SmoothStreaming
* **Caching support** - Cache media files to play them again without an internet connection
* **Background support** - Keep playing audio even after the app is in background
* **Fully Customizable** - Even the notification icons are customizable!
* **Supports React Hooks 🎣** - Includes React Hooks for common use-cases so you don't have to write them

## Why another music module?
After trying to team up modules like `react-native-sound`, `react-native-music-controls` and `react-native-google-cast`, I've noticed that their structure and the way they should be tied together can cause a lot of problems (mainly on Android). Those can heavily affect the app stability and user experience.

All audio modules (like `react-native-sound`) don't play in a separated service on Android, which should **only** be used for simple audio tracks in the foreground (such as sound effects, voice messages, etc.)

`react-native-music-controls` is meant for apps using those audio modules, but it has a few problems: the audio isn't tied directly to the controls. It can be pretty useful for casting (such as Chromecast).

`react-native-google-cast` works pretty well and also supports custom receivers, but it has fewer player controls, it's harder to integrate and still uses the Cast SDK v2.

## Example Setup

First please take a look at the [Getting Started](https://rntp.dev/docs/basics/getting-started/) guide, but a basic example of how to play a track:

```javascript
import TrackPlayer from 'react-native-track-player';

const start = async () => {
    // Set up the player
    await TrackPlayer.setupPlayer();

    // Add a track to the queue
    await TrackPlayer.add({
        id: 'trackId',
        url: require('track.mp3'),
        title: 'Track Title',
        artist: 'Track Artist',
        artwork: require('track.png')
    });

    // Start playing it
    await TrackPlayer.play();
};
start();
```

## Core Team ✨

<table>
  <tr>
    <td align="center"><a href="https://github.com/dcvz"><img src="https://avatars.githubusercontent.com/u/2475932?v=4" width="100px;" alt=""/><br /><sub><b>David Chavez</b></sub></a><br /></td>
  </tr>
</table>

## External Core Contributors ✨

<table>
  <tr>
    <td align="center"><a href="https://github.com/jspizziri"><img src="https://avatars.githubusercontent.com/u/1452066?v=4" width="100px;" alt=""/><br /><sub><b>Jacob Spizziri</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/puckey"><img src="https://avatars.githubusercontent.com/u/271885?v=4" width="100px;" alt=""/><br /><sub><b>Jonathan Puckey</b></sub></a><br /></td>
  </tr>
</table>

## Special Thanks ✨

<table>
  <tr>
    <td align="center"><a href="https://github.com/Guichaguri"><img src="https://avatars.githubusercontent.com/u/1813032?v=4" width="100px;" alt=""/><br /><sub><b>Guilherme Chaguri</b></sub></a><br /></td>
    <td align="center"><a href="https://github.com/curiousdustin"><img src="https://avatars.githubusercontent.com/u/1706540?v=4" width="100px;" alt=""/><br /><sub><b>Dustin Bahr</b></sub></a><br /></td>
  </tr>
</table>

## Contributing

You want this package to be awesome and we want to deliver on that. As you know
already you can just [File A Ticket](#file-a-ticket), but thats not actually the
best way for you to get what you need (read on to see why). The best way is for
you to [Be A Champion](#be-a-champion) and [dive into the code](#where-do-you-start).

#### File A Ticket

The reality is that filing a ticket isn't always enough. **This is probably only
going to work if your issue aligns with both the interests _and_ the resources available** to the core team. Here are the things that align with our _interests_
in order of priority.

1. Fixing **_widespread, common, and critical Bugs_**.
2. Fixing **_uncommon but necessary Bugs_**.
3. Introducing new  **_Features that have broad value_**.

Now keep in mind available resources. Long story short, the thing you care about
needs to be cared about by either a lot of other people, or by us.

**BUT!** There's another and, arguably even **_better way_** that helps you get what
you need faster: [Be A Champion](#be-a-champion).

#### Be A Champion

Being a _champion_ makes it easy for us to help you. Which is what we all want!
So how can you be a champion? [Sponsor the Project](https://github.com/sponsors/DoubleSymmetry) or _be willing to write some code_.

**If _you're willing_** to write some code **_we're willing_** to:

- Open a design discussion, give feedback, and approve something that works.
- Provide guidance in the implementation journey.

So, in a nutshell, let us know you're willing to do the work and ask for a little
guidance, and watch the things you care about get done faster than anyone else.
The best help will be given to those who are willing to help themselves.

###### You don't have experience you say? It's OK!

You may be thinking that you can't help because you know nothing about native
iOS or Android or maybe even React code. But we're willing to help guide you.

If you're up for that task then we can help you understand native code and how
React Native works.

The only way you go from _not-knowing_ to _knowing_ is by learning. Learning isn't
something you should be ashamed of nor is it something you should be scared of.

#### Where Do You Start?

Our goal is to make it as easy as possible for you to make changes to the library.
All the documentation on how to work on the library and it's dependencies is
[located in this Guide](./example/README.md)

## Release

The standard release command for this project is [`yarn version`](https://classic.yarnpkg.com/lang/en/docs/cli/version/).

```
yarn version [--major | --minor | --patch | --new-version <version>]
```

Ex.

```
yarn version --new-version 1.2.17
yarn version --patch // 1.2.17 -> 1.2.18
yarn version --minor // 1.2.18 -> 1.3.0
yarn version --major // 2.0.0
```

This command will:

1. Generate/update the Changelog
2. Bump the package version
3. Tag & pushing the commit
4. Build & publish the package


## Community

You can find us as part of the [React Native Track Player](https://discordapp.com/invite/ya2XDCR):

- `#introduce-yourself` - Come greet the newest members of this group!
- `#troubleshooting-forum` - Ask members of the community to trouble shoot issues with your app and make recommendations.
- `#show-and-tell` - Tell the community about the app you made with this project!
- `#news-and-releases` - Stay updated about the latest releases and dev efforts on the project.
