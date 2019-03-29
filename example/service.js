
/**
 * This is the code that will run tied to the player.
 *
 * The code here might keep running in the background.
 *
 * You should put everything here that should be tied to the playback but not the UI
 * such as processing media buttons or analytics
 */

import TrackPlayer from 'react-native-track-player';

module.exports = async function() {

  TrackPlayer.addEventListener('remote-play', () => {
    TrackPlayer.play()
  })

  TrackPlayer.addEventListener('remote-pause', () => {
    TrackPlayer.pause()
  });

  TrackPlayer.addEventListener('remote-next', () => {
    TrackPlayer.skipToNext()
  });

  TrackPlayer.addEventListener('remote-previous', () => {
    TrackPlayer.skipToPrevious()
  });

  TrackPlayer.addEventListener('remote-stop', () => {
    TrackPlayer.destroy()
  });

  let playingBeforeDuck;
  let volumeBeforeDuck;
  TrackPlayer.addEventListener(
    'remote-duck',
    async ({ paused, permanent, ducking }) => {
      // When the event is triggered with permanent set to true,
      // you should stop the playback:
      if (permanent) {
        TrackPlayer.stop();
        return;
      }

      // When the event is triggered with ducking set to true,
      // you should either lower the volume or pause the app.
      // If it’s a music app, you probably want to lower the volume,
      // but if it’s a podcast app, you probably want to pause it for a moment.
      if (ducking) {
        volumeBeforeDuck = await TrackPlayer.getVolume();

        // Lower the volume by 80%:
        TrackPlayer.setVolume(volumeBeforeDuck * 0.2);
        return;
      }

      // When the event is triggered with paused set to true,
      // you should pause the playback. It will also be set to
      // true in both cases described above.
      if (paused) {
        const playerState = await TrackPlayer.getState();
        playingBeforeDuck = playerState === TrackPlayer.STATE_PLAYING;
        TrackPlayer.pause();
        return;
      }

      // Recover playback after duck:
      if (playingBeforeDuck) {
        TrackPlayer.play();
      }

      // Recover volume after duck:
      const playerVolume = await TrackPlayer.getVolume();
      TrackPlayer.setVolume(volumeBeforeDuck);

      volumeBeforeDuck = playingBeforeDuck = null;
    }
  );
};