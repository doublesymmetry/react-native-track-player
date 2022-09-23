import { State } from '../constants';
import { usePlaybackStateWithoutInitialValue } from './usePlaybackStateWithoutInitialValue';

/** Get current playback state and subsequent updates  */
export const usePlaybackState = () => {
  const state = usePlaybackStateWithoutInitialValue();
  return state ?? State.None;
};
