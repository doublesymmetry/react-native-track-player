# RNTP Example App

This app is useful to simply try out the RNTP features or as a basis for
implementing new features and/or bugfixes.

## Running The Example App

```sh
git clone git@github.com:DoubleSymmetry/react-native-track-player.git
cd react-native-track-player
yarn
yarn build
cd example
yarn
cd ios && pod install && cd ..
```

## Library Development

If you want to use the example project to work on features or bug fixes in
the core library then there are a few things to keep in mind. The most important
is that for changes to be reflected in the example app in the simulator code
changes need to be made in the version of `react-native-track-player` which is
installed at `./example/node_modules/react-native-track-player`. There
are a couple of approaches that you use to accomplish this:

1. `yarn add react-native-track-player@file:..`
2. `yarn sync`

In all cases keep the following in mind:

- If you're making changes to `ts` you'll need to re-run `yarn build`.
- If you're making changes to native code (e.g. anything `ios` or `android`)
  you'll need to rebuild the app in order to see those changes. (e.g. `yarn ios`
  or `yarn android`)

#### `yarn add react-native-track-player@file:..`

This command will effectively reinstall the `react-native-track-player` in the
example project based on the current state of the git repository.

#### `yarn sync`

This command works in the opposite direction of the `add` approach. It copies
the files from the `example/node_modules/react-native-track-player` directory
up to the top level project. You would want to use this approach if you're
making your changes directly to the code in the `example/node_modules/react-native-track-player`
folder.
