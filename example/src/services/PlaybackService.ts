import TrackPlayer, { Event } from 'react-native-track-player';

import { FadeEvent } from '../constant';

export async function PlaybackService() {
  TrackPlayer.addEventListener(Event.RemotePause, () => {
    console.log('Event.RemotePause');
    TrackPlayer.setAnimatedVolume({
      volume: 0,
      msg: FadeEvent.FadePause,
    });
  });

  TrackPlayer.addEventListener(Event.RemotePlay, () => {
    console.log('Event.RemotePlay');
    TrackPlayer.play();
    TrackPlayer.setAnimatedVolume({
      volume: 1,
    });
  });

  TrackPlayer.addEventListener(Event.RemoteNext, () => {
    console.log('Event.RemoteNext');
    TrackPlayer.setAnimatedVolume({
      volume: 0,
      msg: FadeEvent.FadeNext,
    });
  });

  TrackPlayer.addEventListener(Event.RemotePrevious, () => {
    console.log('Event.RemotePrevious');
    TrackPlayer.setAnimatedVolume({
      volume: 0,
      msg: FadeEvent.FadePrevious,
    });
  });

  TrackPlayer.addEventListener(Event.RemoteJumpForward, async (event) => {
    console.log('Event.RemoteJumpForward', event);
    TrackPlayer.seekBy(event.interval);
  });

  TrackPlayer.addEventListener(Event.RemoteJumpBackward, async (event) => {
    console.log('Event.RemoteJumpBackward', event);
    TrackPlayer.seekBy(-event.interval);
  });

  TrackPlayer.addEventListener(Event.RemoteSeek, (event) => {
    console.log('Event.RemoteSeek', event);
    TrackPlayer.seekTo(event.position);
  });

  TrackPlayer.addEventListener(Event.RemoteDuck, async (event) => {
    console.log('Event.RemoteDuck', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackQueueEnded, (event) => {
    console.log('Event.PlaybackQueueEnded', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackActiveTrackChanged, (event) => {
    console.log('Event.PlaybackActiveTrackChanged', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackPlayWhenReadyChanged, (event) => {
    console.log('Event.PlaybackPlayWhenReadyChanged', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackState, (event) => {
    console.log('Event.PlaybackState', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackMetadataReceived, (event) => {
    console.log('Event.PlaybackMetadataReceived', event);
  });

  TrackPlayer.addEventListener(Event.PlaybackAnimatedVolumeChanged, (event) => {
    switch (event.data) {
      case FadeEvent.FadePause:
        TrackPlayer.pause();
        break;
      case FadeEvent.FadeNext:
        TrackPlayer.skipToNext();
        TrackPlayer.setAnimatedVolume({
          volume: 1,
        });
        break;
      case FadeEvent.FadePrevious:
        TrackPlayer.skipToPrevious();
        TrackPlayer.setAnimatedVolume({
          volume: 1,
        });
        break;
      default:
        console.log('Event.PlaybackAnimatedVolumeChanged', event.data);
    }
  });

  TrackPlayer.addEventListener(
    Event.PlaybackMetadataReceived,
    async ({ title, artist }) => {
      const activeTrack = await TrackPlayer.getActiveTrack();
      TrackPlayer.updateNowPlayingMetadata({
        artist: [title, artist].filter(Boolean).join(' - '),
        title: activeTrack?.title,
        artwork: activeTrack?.artwork,
      });
    }
  );
}
