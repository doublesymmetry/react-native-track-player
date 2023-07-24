import { useEffect, useState } from 'react';

import { getProgress } from '../trackPlayer';
import { Event } from '../constants';
import type { Progress } from '../interfaces';
import { useTrackPlayerEvents } from './useTrackPlayerEvents';

const INITIAL_STATE = {
  position: 0,
  duration: 0,
  buffered: 0,
};

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param updateInterval - ms interval
 */
export function useProgress(updateInterval = 1000) {
  const [state, setState] = useState<Progress>(INITIAL_STATE);

  useTrackPlayerEvents([Event.PlaybackActiveTrackChanged], () => {
    setState(INITIAL_STATE);
  });

  useEffect(() => {
    let mounted = true;

    const update = async () => {
      try {
        const { position, duration, buffered } = await getProgress();
        if (!mounted) return;

        setState((state) =>
          position === state.position &&
          duration === state.duration &&
          buffered === state.buffered
            ? state
            : { position, duration, buffered }
        );
      } catch {
        // these method only throw while you haven't yet setup, ignore failure.
      }
    };

    const poll = async () => {
      await update();
      if (!mounted) return;
      await new Promise<void>((resolve) => setTimeout(resolve, updateInterval));
      if (!mounted) return;
      poll();
    };

    poll();

    return () => {
      mounted = false;
    };
  }, [updateInterval]);

  return state;
}
