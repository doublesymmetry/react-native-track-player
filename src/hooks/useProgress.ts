import { useEffect, useState } from 'react';

import { State } from '../constants';
import type { Progress } from '../interfaces';
import { getPosition, getDuration, getBufferedPosition } from '../trackPlayer';
import { usePlaybackStateWithoutInitialValue } from './usePlaybackStateWithoutInitialValue';

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param updateInterval - ms interval
 */
export function useProgress(updateInterval = 1000) {
  const [state, setState] = useState<Progress>({
    position: 0,
    duration: 0,
    buffered: 0,
  });
  const playerState = usePlaybackStateWithoutInitialValue();
  const isNone = playerState === State.None;
  useEffect(() => {
    let mounted = true;
    if (isNone) {
      setState({ position: 0, duration: 0, buffered: 0 });
      return;
    }

    const update = async () => {
      try {
        const [position, duration, buffered] = await Promise.all([
          getPosition(),
          getDuration(),
          getBufferedPosition(),
        ]);
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
  }, [isNone, updateInterval]);

  return state;
}
