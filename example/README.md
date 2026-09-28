# Track Player example

A React Native app using `@rntp/player` for music, radio, and podcasts, with an
event log and a browse catalog for Android Auto and CarPlay.

Run these commands from this directory:

```sh
yarn install
yarn start
```

In another terminal, run `yarn android`. For iOS, install the pods first:

```sh
bundle install
cd ios
bundle exec pod install
cd ..
yarn ios
```

`index.js` registers the playback session before loading the UI. The session in
`src/services/playbackSession.ts` configures the player and registers listeners
that live for the JS runtime's lifetime. Android's `autoStartJs` option lets a
media-browser connection start that session. Transport controls use the player's
native command handling.

`src/lib/browseTree.ts` checks `getBrowseTreeInfo()` before publishing the initial
catalog: an existing catalog stays available while fresh content loads. Both the
UI and `BrowseRefreshRequested` events refresh the podcast feed; only the latest
request can publish, and the session cancels its request after ten seconds.

The UI starts in `src/App.tsx`. The screens, sample media, and state stores are in
`src/screens`, `src/data`, and `src/stores`. The local-asset helper copies a bundled
track to the app's documents directory to demonstrate playback from a `file://`
URL.

Run `yarn test` for the app's startup test and `yarn tsc --noEmit` to check its
TypeScript.
