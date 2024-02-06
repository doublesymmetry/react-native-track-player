import { useEffect, useState } from 'react';
import { Event } from '../constants';
import { useTrackPlayerEvents } from './useTrackPlayerEvents';
import { NowPlayingMetadata } from '../interfaces';
import TrackPlayer from '..';

export const useNowPlayingMetadata = (): NowPlayingMetadata | undefined => {
  const [metadata, setMetadata] = useState<NowPlayingMetadata | undefined>();

  useEffect(() => {
    let unmounted = false;

    if (unmounted) return;

    TrackPlayer.getNowPlayingMetadata()
    .then(setMetadata)
      .catch(() => {
        /** Only throws while you haven't yet setup, ignore failure. */
      });

      return () => {
        unmounted = true;
      };
  }, [])

  useTrackPlayerEvents(
    [Event.NowPlayingMetadataChanged],
    async (event) => {
      setMetadata(event.metadata);
    }
  );

  return metadata;
};
