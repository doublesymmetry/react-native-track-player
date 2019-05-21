
/**
 * This is the code that will run tied to the player.
 *
 * The code here might keep running in the background.
 *
 * You should put everything here that should be tied to the playback but not the UI
 * such as processing media buttons or analytics
 */

import TrackPlayer from 'react-native-track-player';

module.exports = async () => {

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
  const DUCKED_VOLUME = 0.2;
  TrackPlayer.addEventListener(
    'remote-duck',
    async ({ paused, permanent, ducking }) => {
      if (permanent) {
        TrackPlayer.stop();
        return;
      }

      if (paused) {
        const playerState = await TrackPlayer.getState();
        playingBeforeDuck = playerState === TrackPlayer.STATE_PLAYING;
        TrackPlayer.pause();
        return;
      }

      if (ducking) {
        const volume = await TrackPlayer.getVolume();
        if (volume > DUCKED_VOLUME) {
          volumeBeforeDuck = volume;
          TrackPlayer.setVolume(DUCKED_VOLUME);
        }
        return;
      }

      if (playingBeforeDuck) {
        TrackPlayer.play();
      }

      const playerVolume = await TrackPlayer.getVolume();
      if (volumeBeforeDuck > playerVolume) {
        TrackPlayer.setVolume(volumeBeforeDuck || 1);
      }

      volumeBeforeDuck = playingBeforeDuck = null;
    }
  );
};