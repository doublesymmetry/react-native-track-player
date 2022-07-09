import TrackPlayer, {Event, State} from 'react-native-track-player';
import type {ProgressUpdateEvent} from 'react-native-track-player';

let wasPausedByDuck = false;

export async function PlaybackService() {
  TrackPlayer.addEventListener(Event.RemotePause, () => {
    console.log('Event.RemotePause');
    TrackPlayer.pause();
  });

  TrackPlayer.addEventListener(Event.RemotePlay, () => {
    console.log('Event.RemotePlay');
    TrackPlayer.play();
  });

  TrackPlayer.addEventListener(Event.RemoteNext, () => {
    console.log('Event.RemoteNext');
    TrackPlayer.skipToNext();
  });

  TrackPlayer.addEventListener(Event.RemotePrevious, () => {
    console.log('Event.RemotePrevious');
    TrackPlayer.skipToPrevious();
  });

  TrackPlayer.addEventListener(Event.RemoteSeek, data => {
    console.log('Event.RemoteSeek', data);
    TrackPlayer.seekTo(data.position);
  });

  TrackPlayer.addEventListener(Event.RemoteJumpForward, data => {
    console.log('Event.RemoteJumpForward', data);
  });

  TrackPlayer.addEventListener(Event.RemoteJumpBackward, data => {
    console.log('Event.RemoteJumpBackward', data);
  });

  TrackPlayer.addEventListener(Event.RemoteDuck, async e => {
    console.log('Event.RemoteJumpBackward', e);

    if (e.permanent === true) {
      TrackPlayer.stop();
    } else {
      if (e.paused === true) {
        const playerState = await TrackPlayer.getState();
        wasPausedByDuck = playerState !== State.Paused;
        TrackPlayer.pause();
      } else {
        if (wasPausedByDuck === true) {
          TrackPlayer.play();
          wasPausedByDuck = false;
        }
      }
    }
  });

  TrackPlayer.addEventListener(Event.PlaybackQueueEnded, data => {
    console.log('Event.PlaybackQueueEnded', data);
  });

  TrackPlayer.addEventListener(Event.PlaybackTrackChanged, data => {
    console.log('Event.PlaybackTrackChanged', data);
  });

  TrackPlayer.addEventListener(
    Event.PlaybackProgressUpdated,
    (data: ProgressUpdateEvent) => {
      console.log('Event.PlaybackProgressUpdated', data);
    },
  );
}
