import { useEffect, useRef, useState } from 'react';

import { Event, EventsPayloadByEvent, Progress, State } from './interfaces';
import TrackPlayer from './trackPlayer';

/** Get current playback state and subsequent updates  */
export const usePlaybackState = () => {
  const [state, setState] = useState(State.None);
  useEffect(() => {
    let mounted = true;

    TrackPlayer.getState()
      .then((initialState) => {
        if (!mounted) return;
        // Only set the state if it wasn't already set by the Event.PlaybackState listener below:
        setState((state) => state ?? initialState);
      })
      .catch(() => {
        /** getState only throw while you haven't yet setup, ignore failure. */
      });

    const sub = TrackPlayer.addEventListener<Event.PlaybackState>(
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

/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
export const useTrackPlayerEvents = <
  T extends Event[],
  H extends (data: EventsPayloadByEvent[T[number]]) => void
>(
  events: T,
  handler: H
) => {
  const savedHandler = useRef(handler);
  savedHandler.current = handler;

  /* eslint-disable react-hooks/exhaustive-deps */
  useEffect(() => {
    if (__DEV__) {
      const allowedTypes = Object.values(Event);
      const invalidTypes = events.filter(
        (type) => !allowedTypes.includes(type)
      );
      if (invalidTypes.length) {
        console.warn(
          'One or more of the events provided to useTrackPlayerEvents is ' +
            `not a valid TrackPlayer event: ${invalidTypes.join("', '")}. ` +
            'A list of available events can be found at ' +
            'https://react-native-track-player.js.org/docs/api/events'
        );
      }
    }

    const subs = events.map((type) =>
      TrackPlayer.addEventListener(type, (payload) => {
        // @ts-expect-error - we know the type is correct
        savedHandler.current({ ...payload, type });
      })
    );

    return () => subs.forEach((sub) => sub.remove());
  }, events);
};

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
  const playerState = usePlaybackState();

  useEffect(() => {
    let mounted = true;
    if (playerState === State.None) {
      setState({ position: 0, duration: 0, buffered: 0 });
      return;
    }

    const update = async () => {
      try {
        const [position, duration, buffered] = await Promise.all([
          TrackPlayer.getPosition(),
          TrackPlayer.getDuration(),
          TrackPlayer.getBufferedPosition(),
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
  }, [playerState, updateInterval]);

  return state;
}
