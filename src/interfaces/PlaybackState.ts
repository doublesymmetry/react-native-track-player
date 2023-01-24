import { State } from '../constants';
import type { PlaybackErrorEvent } from './events';

export type PlaybackState =
  | {
      state: Exclude<State, State.Error>;
    }
  | {
      state: State.Error;
      error: PlaybackErrorEvent;
    };
