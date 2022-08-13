---
sidebar_position: 6
---

# Troubleshooting

## iOS: (Enable Swift) `library not found for -lswiftCoreAudio for architecture x86_64`
Because the iOS module uses Swift, if the user is using a standard react-native application they'll need to add support for Swift in the project. This can easily be done by adding a swift file to the Xcode project -- could be called `dummy.swift` and saying yes when prompted if you'd like to generate a bridging header.

![Importing Swift](https://i.imgur.com/CBqBcWs.png)

## Android: `CIRCULAR REFERENCE:com.android.tools.r8.ApiLevelException: Default interface methods are only supported starting with Android N (--min-api 24)`
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

## Android: `com.facebook.react.common.JavascriptException: No task registered for key TrackPlayer`
The playback service requires a headless task to be registered. You have to register it with `registerPlaybackService`.

## Android: `Error: Attribute XXX from [androidx.core:core:XXX] is also present at [com.android.support:support-compat:XXX]`
This error occurs when you're mixing both AndroidX and the Support Library in the same project.

You have to either upgrade everything to AndroidX or downgrade everything to the support library.


* For react-native-track-player, the last version to run the support library is **1.1.4** and the first version to run AndroidX is **1.2.0**.
* For react-native, the last version to run the support library is **0.59** and the first version to run AndroidX is **0.60**.

You can also use [jetifier](https://github.com/mikehardy/jetifier#usage-for-source-files) to convert all of the native code to use only one of them.

## Android: Cleartext HTTP traffic not permitted

Since API 28, Android disables traffic without TLS. To fix the issue you have to use `https` or [enable clear text traffic](https://stackoverflow.com/a/50834600).

## Android: Duplicate package names

Currently React Native Track Player uses a fork of Googles exoplayer which causes conflicts with other packages also using the original exoplayer.
Until trackplayer moves away from the work you can resolve conflicts with [gradle](https://docs.gradle.org/current/userguide/resolution_rules.html#sec:dependency_resolve_rules) by doing the following:

```diff
// Add this to the dependencies object in app/build.gradle
    modules {
        module("com.google.android.exoplayer:exoplayer") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-core") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-core", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-ui") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-ui", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-common") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-common", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-smoothstreaming") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-smoothstreaming", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-hls") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-hls", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        module("com.google.android.exoplayer:exoplayer-dash") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-dash", "replace exoplayer with doublesymmetry.Exoplayer")
        }
        
    }
```

### Still having conflicts

The example above is specific to expo-av and your package might be using additional modules of exoplayer. Here's an example on how to resolve any additional conflicts you might have

#### Example of Error Log

```log
Duplicate class com.google.android.exoplayer2.upstream.crypto.AesCipherDataSink found in modules jetified-exoplayer-datasource-2.17.1-runtime (com.google.android.exoplayer:exoplayer-datasource:2.17.1) and jetified-exoplayer-datasource-r2.17.2-runtime (com.github.doublesymmetry.Exoplayer:exoplayer-datasource:r2.17.2)
```

The conflict is with what is in the brackets (): `(com.google.android.exoplayer:exoplayer-datasource:2.17.1)`

So the conflicting module is: `exoplayer-datasource`

Resolve the conflict by adding another module to replace:

```diff
module("com.google.android.exoplayer:exoplayer-datasource") {
            replacedBy("com.github.doublesymmetry.Exoplayer:exoplayer-datasource", "replace exoplayer with doublesymmetry.Exoplayer")
}
```

