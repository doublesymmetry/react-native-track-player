package com.doublesymmetry.kotlinaudio

import android.content.Context
import android.util.Log
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import java.util.*

class QueuedAudioPlayer(context: Context) : AudioPlayer(context) {
    private val queue = LinkedList<MediaItem>()

    val currentIndex
        get() = exoPlayer.currentWindowIndex

    val items
        get() = queue.toList()

    val previousItems
        get() = queue.subList(0, exoPlayer.currentWindowIndex)

    val nextItems: List<MediaItem>
        get() {
            Log.d("TEST", queue.size.toString())
            return if (queue.isEmpty()) emptyList()
            else queue.subList(exoPlayer.currentWindowIndex, queue.lastIndex)
        }

    val currentItem
        get() = exoPlayer.currentMediaItem

    var repeatMode: RepeatMode
        get() {
            return when (exoPlayer.repeatMode) {
                REPEAT_MODE_ALL -> RepeatMode.ALL
                REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        }
        set(value) {
            when (value) {
                RepeatMode.ALL -> exoPlayer.repeatMode = REPEAT_MODE_ALL
                RepeatMode.ONE -> exoPlayer.repeatMode = REPEAT_MODE_ONE
                RepeatMode.OFF -> exoPlayer.repeatMode = REPEAT_MODE_OFF
            }
    }

    override fun load(item: AudioItem, playWhenReady: Boolean) {
//        val mediaItem = MediaItem.fromUri(item.audioUrl)
//        queue.add(mediaItem)

//        super.load(item, playWhenReady)

        //TODO: Replace current item
    }

    fun add(item: AudioItem, playWhenReady: Boolean = true) {
        Log.d("test", "wtf " + queue.size)
        val mediaItem = MediaItem.fromUri(item.audioUrl)
        queue.add(mediaItem)
        exoPlayer.addMediaItem(mediaItem)

        val notification = manager.createNotification(mediaSession, item)
        manager.refreshNotification(notification)
    }

    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        val mediaItems = items.map { MediaItem.fromUri(it.audioUrl) }
        queue.addAll(mediaItems)
        exoPlayer.addMediaItems(mediaItems)
    }

    fun remove(item: AudioItem) {
        val mediaItem = MediaItem.fromUri(item.audioUrl)
        val index = queue.indexOf(mediaItem)
        queue.removeAt(index)
        exoPlayer.removeMediaItem(index)
    }

    fun next() {
        exoPlayer.seekToNext()
    }

    fun previous() {
        exoPlayer.seekToPrevious()
    }

    fun jumpToItem(index: Int, playWhenReady: Boolean = true) {

    }

    fun removeUpcomingItems() {

    }

    fun removePreviousItems() {

    }

    override fun stop() {
        queue.clear()
        super.stop()
    }

    enum class RepeatMode {
        ALL, ONE, OFF
    }
}

