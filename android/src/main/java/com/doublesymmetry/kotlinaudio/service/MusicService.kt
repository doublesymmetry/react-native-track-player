@file: OptIn(UnstableApi::class) package com.doublesymmetry.kotlinaudio.service

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.doublesymmetry.kotlinaudio.models.CustomButton
import com.doublesymmetry.kotlinaudio.models.PlayerOptions
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer

class MusicService : MediaLibraryService() {
    private val binder = MusicBinder()
    lateinit var player: QueuedAudioPlayer
    private lateinit var mediaSession: MediaLibrarySession

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    // Create your player and media session in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        setupService()
    }

    private fun setupService(customActions: List<CustomButton> = arrayListOf()) {

        player = QueuedAudioPlayer(this, PlayerOptions(nativeExample = true))
        mediaSession = MediaLibrarySession
            .Builder(this, player.player, CustomMediaSessionCallback(customActions))
            .setCustomLayout(customActions.filter { v -> v.onLayout }.map{ v -> v.commandButton})
            .setId("APM")
            .build()
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession.player
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
            mediaSession.release()
        }
        super.onDestroy()
    }

    @UnstableApi private inner class CustomMediaSessionCallback(
        val customActions: List<CustomButton>
    ) : MediaLibrarySession.Callback {

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d("APM", "custom command triggered: ${customCommand.customAction}")
//            when (customCommand.customAction) {
//                CROSSFADE_PREV_PREPARE -> { player.crossFadePrepare(true) }
//                CROSSFADE_PREV -> { player.switchExoPlayer({ player.previous() }) }
//                CROSSFADE_NEXT_PREPARE -> { player.crossFadePrepare() }
//                CROSSFADE_NEXT -> {
//                    player.switchExoPlayer()
//                    mediaSession.player = player.player
//                    this@MusicService.onUpdateNotification(mediaSession, true)
//                }
//            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }

        // Configure commands available to the controller in onConnect()
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller);
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
//            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()

            customActions.forEach{
                v -> v.commandButton.sessionCommand?.let(availableSessionCommands::add)
            }
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }
    }

    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        val intentAction = intent?.action
        return if (intentAction != null) {
            super.onBind(intent)
        } else {
            binder
        }
    }
}
