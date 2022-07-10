import TrackPlayer, {Event, State} from 'react-native-track-player';

let wasPausedByDuck = false;

export async function PlaybackService() {
  TrackPlayer.addEventListener<Event.RemotePause>(Event.RemotePause, () => {
    TrackPlayer.pause();
  });

  TrackPlayer.addEventListener<Event.RemotePlay>(Event.RemotePlay, () => {
    TrackPlayer.play();
  });

  TrackPlayer.addEventListener<Event.RemoteNext>(Event.RemoteNext, () => {
    TrackPlayer.skipToNext();
  });

  TrackPlayer.addEventListener<Event.RemotePrevious>(
    Event.RemotePrevious,
    () => {
      TrackPlayer.skipToPrevious();
    },
  );

  TrackPlayer.addEventListener<Event.RemoteDuck>(Event.RemoteDuck, async e => {
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

  TrackPlayer.addEventListener<Event.PlaybackQueueEnded>(
    Event.PlaybackQueueEnded,
    data => {
      console.log('Event.PlaybackQueueEnded', data);
    },
  );

  TrackPlayer.addEventListener<Event.PlaybackTrackChanged>(
    Event.PlaybackTrackChanged,
    data => {
      console.log('Event.PlaybackTrackChanged', data);
    },
  );

  TrackPlayer.addEventListener<Event.PlaybackProgressUpdated>(
    Event.PlaybackProgressUpdated,
    data => {
      console.log('Event.PlaybackProgressUpdated', data);
    },
  );
}
