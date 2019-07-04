---
title: Installation
permalink: /install/
---

## Installing the packages
**1. Install the module from npm or yarn**
```
npm install --save react-native-track-player
```

```
yarn add react-native-track-player
```

**2. Install react-native-swift to configure your iOS project to use the module correctly**
```
npm install --save react-native-swift
```

```
yarn add react-native-swift
```

After installing it, you will need to link it. **Requires project to use Swift 4.2**

## Automatic Link
Run the command below and the module will be automatically linked
```
react-native link
```

This is the easiest way to link it, but if it doesn't work, follow the manual instructions below

## Manual Link
### Android
Edit the following files:

**android/app/build.gradle**
```diff
dependencies {
    ...
    compile "com.facebook.react:react-native:+"  // From node_modules
+   compile project(':react-native-track-player')
}
```

**android/settings.gradle**
```diff
...
include ':app'
+include ':react-native-track-player'
+project(':react-native-track-player').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-track-player/android')
```

**android/app/src/main/java/** *your app's package* **/MainApplication.java**
```diff
// ...

+import com.guichaguri.trackplayer.TrackPlayer;

public class MainApplication extends Application implements ReactApplication {
    // ...

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
+           new TrackPlayer(),
            new MainReactPackage()
        );
    }

    // ...
  }
```

### iOS
In Xcode:
1. Add RNTrackPlayer.xcodeproj to Libraries.
2. Add libRNTrackPlayer.a to Link Binary With Libraries under Build Phases.
3. Enable swift in the project following [these instructions](#troubleshooting)

For more details (and screenshots), follow the [official linking guide](http://facebook.github.io/react-native/docs/linking-libraries-ios.html#content).


### Windows

1. Open your app in Visual Studio
2. Add `node_modules/react-native-track-player/windows/RNTrackPlayer/RNTrackPlayer.csproj` as an "existing project" in Visual Studio
3. Add it as a reference in your app
4. Update `MainPage.cs` with the following additions:

```diff
// ...
+using TrackPlayer;

// ...
    class MainPage : ReactPage
    {
        // ...

        public override List<IReactPackage> Packages
        {
            get
            {
                return new List<IReactPackage>
                {
                    new MainReactPackage(),
+                   new TrackPlayerPackage(),
                };
            }
        }

        // ...
    }
}
```

For more details (and screenshots), follow the [official linking guide](https://github.com/Microsoft/react-native-windows/blob/master/docs/LinkingLibrariesWindows.md).

## Unstable
If you want to try the latest features, you can install the module directly from GitHub using:

```
npm install --save react-native-kit/react-native-track-player#dev
```

```
yarn add react-native-kit/react-native-track-player#dev
```

To update it, run the same command again.

Note: You don't need to link the module after updating it.

## Troubleshooting

#### Expo and Expokit support
Currently react-native-track-player does not support projects with Expo or Expokit.

#### iOS: (Enable Swift) `library not found for -lswiftCoreAudio for architecture x86_64`
Because the iOS module uses Swift, if the user is using a standard react-native application they'll need to add support for Swift in the project. This can be easily by adding a swift file to the Xcode project -- could be called `dummy.swift` and saying yes when prompted if you'd like to generate a bridging header.

![Importing Swift](https://i.imgur.com/CBqBcWs.png)

#### Android: `CIRCULAR REFERENCE:com.android.tools.r8.ApiLevelException: Default interface methods are only supported starting with Android N (--min-api 24)`
Since version 1.0.0, we began using a few Java 8 features in the project to reduce the code size.

To fix the issue, add the following options to your `android/app/build.gradle` file:
```diff
android {
    ...
+   compileOptions {
+       sourceCompatibility JavaVersion.VERSION_1_8
+       targetCompatibility JavaVersion.VERSION_1_8
+   }
    ...
}
```

#### Android: `com.facebook.react.common.JavascriptException: No task registered for key TrackPlayer`
The playback service requires a headless task to be registered. You have to register it with `registerPlaybackService`.

#### Android: `Error: Attribute XXX from [androidx.core:core:XXX] is also present at [com.android.support:support-compat:XXX]`
This error occurs when you're mixing both AndroidX and the Support Library in the same project.

You have to either upgrade everything to AndroidX or downgrade everything to the support library.


* For react-native-track-player, the last version to run the support library is **1.1.4** and the first version to run AndroidX is **1.2.0**.
* For react-native, the last version to run the support library is **0.59** and the first version to run AndroidX is **0.60**.

You can also use [jetifier](https://github.com/mikehardy/jetifier#usage-for-source-files) to convert all of the native code to use only one of them.

## Next
You can choose the build preferences for your app using `track-player.json`. See more [here](https://react-native-kit.github.io/react-native-track-player/build-preferences/).
