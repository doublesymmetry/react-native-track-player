import { useEffect, useRef, useState } from 'react';

import { Event, EventsPayloadByEvent, Progress, State } from './interfaces';
import TrackPlayer from './trackPlayer';

/** Get current playback state and subsequent updates  */
export const usePlaybackState = () => {
  const [state, setState] = useState(State.None);
  const isUnmountedRef = useRef(true);

  useEffect(() => {
    isUnmountedRef.current = false;
    return () => {
      isUnmountedRef.current = true;
    };
  }, []);

  useEffect(() => {
    async function setPlayerState() {
      try {
        const playerState = await TrackPlayer.getState();

        // If the component has been unmounted, exit
        if (isUnmountedRef.current) return;

        setState(playerState);
      } catch {
        // getState only throw while you haven't yet setup, ignore failure.
      }
    }

    // Set initial state
    setPlayerState();

    const sub = TrackPlayer.addEventListener<Event.PlaybackState>(
      Event.PlaybackState,
      (data) => {
        setState(data.state);
      }
    );

    return () => sub.remove();
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
 * @param interval - ms interval
 */
export function useProgress(updateInterval?: number) {
  const [state, setState] = useState<Progress>({
    position: 0,
    duration: 0,
    buffered: 0,
  });
  const playerState = usePlaybackState();
  const stateRef = useRef(state);
  const isUnmountedRef = useRef(true);

  useEffect(() => {
    isUnmountedRef.current = false;
    return () => {
      isUnmountedRef.current = true;
    };
  }, []);

  const getProgress = async () => {
    try {
      const [position, duration, buffered] = await Promise.all([
        TrackPlayer.getPosition(),
        TrackPlayer.getDuration(),
        TrackPlayer.getBufferedPosition(),
      ]);

      // If the component has been unmounted, exit
      if (isUnmountedRef.current) return;

      // If there is no change in properties, exit
      if (
        position === stateRef.current.position &&
        duration === stateRef.current.duration &&
        buffered === stateRef.current.buffered
      )
        return;

      const state = { position, duration, buffered };
      stateRef.current = state;
      setState(state);
    } catch {
      // these method only throw while you haven't yet setup, ignore failure.
    }
  };

  useEffect(() => {
    if (playerState === State.None) {
      setState({ position: 0, duration: 0, buffered: 0 });
      return;
    }

    // Set initial state
    getProgress();

    // Create interval to update state periodically
    const poll = setInterval(getProgress, updateInterval || 1000);
    return () => clearInterval(poll);
  }, [playerState, updateInterval]);

  return state;
}
