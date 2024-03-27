---
sidebar_position: 11
---

# Doing Local Development of Swift Code

RNTP internally relies on a Swift audio library we maintain called `AudioPlayerSwiftEx`. This library lives in the same repository as RNTP, but is normally fetched from the cocoapods repository. In order to use it as a local dependency that can be modified and tested, you'll need to update your Podfile with the following:

1. At the top of your file import our custom script

```ruby
require_relative '../node_modules/react-native-track-player/scripts/rntp_pods'
```

2. Below `use_native_modules!` add the following line

```ruby
use_rntp_local_audio!
```
