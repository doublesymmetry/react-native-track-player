package com.doublesymmetry.kotlinaudio.players

import android.content.Context
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.IllegalSeekPositionException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import java.util.*

open class QueuedAudioPlayer(context: Context) : AudioPlayer(context) {
    private val queue = LinkedList<MediaItem>()

    val currentIndex
        get() = exoPlayer.currentWindowIndex

    val items: List<AudioItem>
        get() = queue.map { it.playbackProperties?.tag as AudioItem }

    val previousItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(0, exoPlayer.currentWindowIndex)
                .map { it.playbackProperties?.tag as AudioItem }
        }

    val nextItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(exoPlayer.currentWindowIndex, queue.lastIndex)
                .map { it.playbackProperties?.tag as AudioItem }
        }

    val currentItem: AudioItem
        get() {
            return exoPlayer.currentMediaItem?.playbackProperties?.tag as AudioItem
        }

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
     * @param item The [AudioItem] to replace the current one.
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
     * Add a single item to the queue.
     * @param item The [AudioItem] to add.
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(item: AudioItem, playWhenReady: Boolean = true) {
        val mediaItem = getMediaItemFromAudioItem(item)
        queue.add(mediaItem)
        exoPlayer.addMediaItem(mediaItem)
    }

    /**
     * Add multiple items to the queue.
     * @param items The [AudioItem]s to add.
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        val mediaItems = items.map { getMediaItemFromAudioItem(it) }
        queue.addAll(mediaItems)
        exoPlayer.addMediaItems(mediaItems)
    }

    /**
     * Remove an item from the queue.
     * @param item The [AudioItem] to remove, if it exists in the queue.
     */
    fun remove(item: AudioItem) {
        val mediaItem = MediaItem.fromUri(item.audioUrl)
        val index = queue.indexOf(mediaItem)
        queue.removeAt(index)
        exoPlayer.removeMediaItem(index)
    }

    /**
     * Play the next item in the queue, if any.
     */
    fun next() {
        exoPlayer.seekToNext()
    }

    /**
     * Play the previous item in the queue, if any. Otherwise, starts the current item from the beginning.
     */
    fun previous() {
        exoPlayer.seekToPrevious()
    }

    /**
     * Move an item in the queue from one position to another.
     * @param fromIndex The index of the item ot move.
     * @param toIndex The index to move the item to. If the index is larger than the size of the queue, the item is moved to the end of the queue instead.
     */
    fun move(fromIndex: Int, toIndex: Int) {
        exoPlayer.moveMediaItem(fromIndex, toIndex)
    }

    /**
     * Jump to an item in the queue
     */
    fun jumpToItem(index: Int, playWhenReady: Boolean = true) {
        exoPlayer.playWhenReady = playWhenReady
        try {
            exoPlayer.seekTo(index, C.INDEX_UNSET.toLong())
        } catch (e: IllegalSeekPositionException) {
            throw Error("This item index does not exist. The size of the queue is ${queue.size} items")
        }
    }

    /**
     * Removes all the upcoming items, if any (the ones returned by [next]).
     */
    fun removeUpcomingItems() {
        val lastIndex = queue.lastIndex

        exoPlayer.removeMediaItems(currentIndex, lastIndex)
        queue.subList(currentIndex, lastIndex).clear()
    }

    /**
     * Removes all the previous items, if any (the ones returned by [previous]).
     */
    fun removePreviousItems() {
        exoPlayer.removeMediaItems(0, currentIndex)
        queue.subList(0, currentIndex).clear()
    }

    /**
     * Stops and resets the player, as well as clears the queue. Only call this when you are finished using the player, otherwise use [pause].
     */
    override fun stop() {
        queue.clear()
        super.stop()
    }

    enum class RepeatMode {
        ALL, ONE, OFF
    }
}

