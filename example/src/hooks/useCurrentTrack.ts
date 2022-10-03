import { useEffect, useState } from 'react';
import type { Track } from 'react-native-track-player';
import TrackPlayer, {
  Event,
  useTrackPlayerEvents,
} from 'react-native-track-player';

export const useCurrentTrack = (): Track | undefined => {
  const [index, setIndex] = useState<number | undefined>();
  const [track, setTrack] = useState<Track | undefined>();

  useEffect(() => {
    let unmounted = false;
    TrackPlayer.getCurrentTrack().then((currentTrack) => {
      if (unmounted) return;
      setIndex((index) => index ?? currentTrack ?? undefined);
    });
    return () => {
      unmounted = true;
    };
  });

  useTrackPlayerEvents([Event.PlaybackTrackChanged], async ({ nextTrack }) => {
    setIndex(nextTrack);
  });

  useEffect(() => {
    if (index === undefined) return;

    let unmounted = false;
    TrackPlayer.getTrack(index).then((track) => {
      if (unmounted) return;
      setTrack(track ?? undefined);
    });
    return () => {
      unmounted = true;
    };
  }, [index]);

  return track;
};
