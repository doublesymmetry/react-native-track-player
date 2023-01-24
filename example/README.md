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
the core library then there are a few things to keep in mind.

#### TS/JS

If you want to work on the typescript files located in `src` (in the root
project) you should run

```
yarn dev
```

The above command will automatically watch for changes int the `src` folder
and recompile them while you work. Then they'll get automatically reloaded
in a running instance of the `example` app so you can see your changes.

## iOS Native

It's recommended that you make your changes directly in XCode. Which you can
open quickly by running one of the following commands:

From inside the `example` directory:

```sh
yarn ios:ide
```

From the root directory:

```sh
yarn example ios:ide
```

Once opened you can simply navigate to the native dependencies, open their
source files, modify them, or add breakpoints. See the screenshots below for
specifically how to navigate to react-native-track-player and SwiftAudioEx
dependencies (see screenshots below).

![Xcode RNTP](https://react-native-track-player.js.org/img/debugging/debug-ios-rntp.png)
![Xcode SwiftAudioEx](https://react-native-track-player.js.org/img/debugging/debug-ios-swift-audio-ex.png)

## Android Native

You can modify any android native code for RNTP by simply opening the example
android project in Android Studio and modifying the source:

**macOS Ex**

From inside the `example` directory:

```sh
yarn android:ide
```

From the root directory:

```sh
yarn example android:ide
```

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
