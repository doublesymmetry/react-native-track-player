package com.doublesymmetry.kotlin_audio_sample

import com.doublesymmetry.kotlinaudio.models.DefaultAudioItem
import com.doublesymmetry.kotlinaudio.models.SourceType


val firstItem = DefaultAudioItem(
    "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3", SourceType.FILE,
    title = "Dirty Computer",
    artwork = "https://upload.wikimedia.org/wikipedia/en/0/0b/DirtyComputer.png",
    artist = "Janelle Monáe"
)

val secondItem = DefaultAudioItem(
    "https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_1MG.mp3", SourceType.FILE,
    title = "Melodrama",
    artwork = "https://images-na.ssl-images-amazon.com/images/I/A18QUHExFgL._SL1500_.jpg",
    artist = "Lorde"
)