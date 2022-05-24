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

## iOS Native

First opening the Xcode Project (`open ios/example.xcworkspace`). Then you can
simply navigate to the native dependencies, open their source files, modify
them, or add breakpoints. See the screenshots below for specifically how to
navigate to react-native-track-player and SwiftAudioEx dependencies (see
screenshots below).

![Xcode RNTP](https://react-native-track-player.js.org/img/debugging/debug-ios-rntp.png)
![Xcode SwiftAudioEx](https://react-native-track-player.js.org/img/debugging/debug-ios-swift-audio-ex.png)

## Android Native

You can modify any android native code for RNTP by simply opening the example
android project in Android Studio and modifying the source:

**macOS Ex**

```sh
cd react-native-track-player/example
open -a /Applications/Android\ Studio.app ./android
```

**NOTE:** remember to run `yarn sync` when you're done making and testing your
changes to copy them up from `node_modules` to the main repo so they can be
committed.

## KotlinAudio

If you need to resolve a bug that exists in `KotlinAudio` you'll need to build
and install a local version of `KotlinAudio` in order to do so. Here's how:

#### 1. Clone the `KotlinAudio` project:

```sh
git clone git@github.com:doublesymmetry/KotlinAudio.git
```

#### 2. Build and export to maven local which is the local dependency repository:

```sh
cd KotlinAudio
./gradlew -x test  build publishToMavenLocal
```

Make a note of the `versionNumber` configured in the `kotlin-audio/build.gradle`
file as you'll need this in the next step.

**NOTES:**
- The result of this is a local version of the build published here:

  ```
  Windows: C:\Users\<user_name>\.m2
  Linux: /home/<user_name>/.m2
  macOS: /Users/<user_name>/.m2
  ```
- The `-x test` skips tests for faster build. Make sure you run the test
  before submitting a PR to the `KotlinAudio` project.

#### 3. Point your RNTP dependency at the local build:

Please note that `<version_number>` below will need to be replaced with the
`versionNumber` you got from `KotlinAudio/kotlin-audio/build.gradle`.

```groovy
// react-native-track-player/android/build.gradle
...

dependencies {
    // implementation 'com.github.DoubleSymmetry:KotlinAudio:v0.1.33' // this is remote
    implementation 'com.github.doublesymmetry:kotlin-audio:<version_number>' // this is local

    ...
}
```

**NOTE:** there are small differences in the package naming.

#### 4. Add `mavenLocal()` to `example/android/build.gradle`:

```groovy
// example/android/build.gradle

...

allprojects {
    repositories {
        mavenLocal()
        maven {
...
```


#### 5. Install the new version of RNTP in the example app and build android:

```sh
cd ./example
yarn add file:..
yarn android
```

:confetti_ball: You've done it. :confetti_ball:
