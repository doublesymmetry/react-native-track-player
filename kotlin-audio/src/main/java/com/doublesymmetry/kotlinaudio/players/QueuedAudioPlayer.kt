package com.doublesymmetry.kotlinaudio.players

import android.content.Context
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.doublesymmetry.kotlinaudio.models.BufferConfig
import com.doublesymmetry.kotlinaudio.models.QueuePlayerOptionsImpl
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.IllegalSeekPositionException
import com.google.android.exoplayer2.source.MediaSource
import java.util.*

class QueuedAudioPlayer(context: Context, bufferConfig: BufferConfig? = null) : BaseAudioPlayer(context, bufferConfig) {
    private val queue = LinkedList<MediaSource>()
    override val playerOptions = QueuePlayerOptionsImpl(exoPlayer)

    val currentIndex
        get() = exoPlayer.currentWindowIndex

    val nextIndex: Int?
        get() {
            return if (exoPlayer.nextWindowIndex == C.INDEX_UNSET) null
            else exoPlayer.nextWindowIndex
        }

    val previousIndex: Int?
        get() {
            return if (exoPlayer.previousWindowIndex == C.INDEX_UNSET) null
            else exoPlayer.previousWindowIndex
        }

    val items: List<AudioItem>
        get() = queue.map { it.mediaItem.playbackProperties?.tag as AudioItem }

    val previousItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(0, exoPlayer.currentWindowIndex)
                .map { it.mediaItem.playbackProperties?.tag as AudioItem }
        }

    val nextItems: List<AudioItem>
        get() {
            return if (queue.isEmpty()) emptyList()
            else queue
                .subList(exoPlayer.currentWindowIndex, queue.lastIndex)
                .map { it.mediaItem.playbackProperties?.tag as AudioItem }
        }

    val currentItem: AudioItem?
        get() = exoPlayer.currentMediaItem?.playbackProperties?.tag as? AudioItem

    val nextItem: AudioItem?
        get() = items.getOrNull(currentIndex + 1)

    val previousItem: AudioItem?
        get() = items.getOrNull(currentIndex - 1)

    override fun load(item: AudioItem, playWhenReady: Boolean) {
        val currentIndex = exoPlayer.currentWindowIndex
        val mediaSource = getMediaSourceFromAudioItem(item)

        queue[currentIndex] = mediaSource
        exoPlayer.removeMediaItem(currentIndex)
        exoPlayer.addMediaSource(currentIndex, mediaSource)

        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()

        previous()
    }

    /**
     * Add a single item to the queue. If the AudioPlayer has no item loaded, it will load the `item`.
     * @param item The [AudioItem] to add.
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(item: AudioItem, playWhenReady: Boolean = true) {
        val mediaSource = getMediaSourceFromAudioItem(item)
        queue.add(mediaSource)
        exoPlayer.addMediaSource(mediaSource)

        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReady
    }

    /**
     * Add multiple items to the queue. If the AudioPlayer has no item loaded, it will load the first item in the list.
     * @param items The [AudioItem]s to add.
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        val mediaSources = items.map { getMediaSourceFromAudioItem(it) }
        queue.addAll(mediaSources)
        exoPlayer.addMediaSources(mediaSources)

        exoPlayer.prepare()
        exoPlayer.playWhenReady = playWhenReady
    }

    /**
     * Add multiple items to the queue.
     * @param items The [AudioItem]s to add.
     * @param atIndex  Index to insert items at, if no items loaded this will not automatically start playback.
     */
    fun add(items: List<AudioItem>, atIndex: Int) {
        val mediaSources = items.map { getMediaSourceFromAudioItem(it) }
        queue.addAll(atIndex, mediaSources)
        exoPlayer.addMediaSources(atIndex, mediaSources)

        exoPlayer.prepare()
    }

    /**
     * Remove an item from the queue.
     * @param index The index of the item to remove.
     */
    fun remove(index: Int) {
        queue.removeAt(index)
        exoPlayer.removeMediaItem(index)
    }

    /**
     * Remove items from the queue.
     * @param indexes The indexes of the items to remove.
     */
    fun remove(indexes: List<Int>) {
        indexes.forEach {
            remove(it)
        }
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
     * Jump to an item in the queue.
     */
    fun jumpToItem(index: Int, playWhenReady: Boolean = true) {
        exoPlayer.playWhenReady = playWhenReady
        try {
            exoPlayer.seekTo(index, C.INDEX_UNSET.toLong())
        } catch (e: IllegalSeekPositionException) {
            throw Error("This item index $index does not exist. The size of the queue is ${queue.size} items.")
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
    override fun destroy() {
        queue.clear()
        super.destroy()
    }
}

