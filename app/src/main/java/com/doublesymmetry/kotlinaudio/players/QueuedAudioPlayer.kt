package com.doublesymmetry.kotlinaudio.players

import android.content.Context
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import java.util.*

class QueuedAudioPlayer(private val context: Context) : AudioPlayer(context) {
    private val queue = LinkedList<MediaItem>()

    val currentIndex
        get() = exoPlayer.currentWindowIndex

    val items: List<MediaItem>
        get() = queue

    val previousItems: List<MediaItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue.subList(0, exoPlayer.currentWindowIndex)
        }

    val nextItems: List<MediaItem>
        get() {
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

    /**
     * Will replace the current item with a new one and load it into the player.
     * @param item The [AudioItem] to replace the current one
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    override fun load(item: AudioItem, playWhenReady: Boolean) {
        exoPlayer.playWhenReady = playWhenReady

        val currentIndex = exoPlayer.currentWindowIndex
        val mediaItem = getMediaItemFromAudioItem(item)

        queue[currentIndex] = mediaItem
        exoPlayer.removeMediaItem(currentIndex)
        exoPlayer.addMediaItem(currentIndex, mediaItem)

        previous()
    }

    /**
     * Add a single item to the queue
     * @param item The [AudioItem] to add
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(item: AudioItem, playWhenReady: Boolean = true) {
        val mediaItem = getMediaItemFromAudioItem(item)
        queue.add(mediaItem)
        exoPlayer.addMediaItem(mediaItem)
    }

    /**
     * Add multiple items to the queue
     * @param items The [AudioItem]s to add
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        val mediaItems = items.map { getMediaItemFromAudioItem(it) }
        queue.addAll(mediaItems)
        exoPlayer.addMediaItems(mediaItems)
    }

    /**
     * Remove an item from the queue
     * @param item The [AudioItem] to remove, if it exists in the queue
     */
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

