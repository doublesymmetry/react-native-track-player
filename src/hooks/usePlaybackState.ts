import { useEffect, useState } from 'react';

import { addEventListener } from '../trackPlayer';
import { Event, State } from '../constants';
import type { PlaybackState } from '../interfaces';

class PlaybackStateSingleton {
  public state = State.None;

  constructor() {
    addEventListener(Event.PlaybackState, (e) => {
      this.state = e.state;
    });
  }
}

const PlaybackStateProvider = new PlaybackStateSingleton();

/**
 * Get current playback state and subsequent updates.
 * */
export const usePlaybackState = (): PlaybackState => {
  const [playbackState, setPlaybackState] = useState<PlaybackState>({
    state: PlaybackStateProvider.state,
  } as PlaybackState);
  useEffect(() => {
    const sub = addEventListener(Event.PlaybackState, (state) => {
      setPlaybackState(state);
    });

    return () => {
      sub.remove();
    };
  }, []);

  return playbackState;
};
