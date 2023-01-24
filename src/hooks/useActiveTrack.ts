import { useState, useEffect } from 'react';

import { getActiveTrack } from '../trackPlayer';
import { Event } from '../constants';
import type { Track } from '../interfaces/Track';
import { useTrackPlayerEvents } from './useTrackPlayerEvents';

export const useActiveTrack = (): Track | undefined => {
  const [track, setTrack] = useState<Track | undefined>();

  // Sets the initial index (if still undefined)
  useEffect(() => {
    let unmounted = false;
    getActiveTrack()
      .then((initialTrack) => {
        if (unmounted) return;
        setTrack((track) => track ?? initialTrack ?? undefined);
      })
      .catch(() => {
        // throws when you haven't yet setup, which is fine because it also
        // means there's no active track
      });
    return () => {
      unmounted = true;
    };
  }, []);

  useTrackPlayerEvents(
    [Event.PlaybackActiveTrackChanged],
    async ({ track }) => {
      setTrack(track ?? undefined);
    }
  );

  return track;
};
