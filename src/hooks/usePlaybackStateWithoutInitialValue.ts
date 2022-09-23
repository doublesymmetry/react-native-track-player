import { useEffect, useState } from 'react';

import { Event, State } from '../constants';
import { addEventListener, getState } from '../trackPlayer';

export const usePlaybackStateWithoutInitialValue = () => {
  const [state, setState] = useState<State | undefined>(undefined);
  useEffect(() => {
    let mounted = true;

    getState()
      .then((initialState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackState listener below:
        setState((state) => state ?? initialState);
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = addEventListener<Event.PlaybackState>(
      Event.PlaybackState,
      ({ state }) => {
        setState(state);
      }
    );

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return state;
};
