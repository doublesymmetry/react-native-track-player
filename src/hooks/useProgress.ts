import { useEffect, useState } from 'react';

import { getProgress } from '../trackPlayer';
import { Event } from '../constants';
import type { Progress } from '../interfaces';
import { useTrackPlayerEvents } from './useTrackPlayerEvents';

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param updateInterval - ms interval
 * @param useSetInterval - use setInterval instead of setTimeout
 */
export function useProgress(updateInterval = 1000, useSetInterval = false) {
  const INITIAL = {
    position: 0,
    duration: 0,
    buffered: 0,
  };
  const [state, setState] = useState<Progress>(INITIAL);

  useTrackPlayerEvents([Event.PlaybackActiveTrackChanged], () => {
    setState(INITIAL);
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

    if (useSetInterval) {
      const pollInterval = setInterval(update, updateInterval);

      return () => {
        mounted = false;
        clearInterval(pollInterval);
      };
    }

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
  }, [updateInterval, useSetInterval]);

  return state;
}
