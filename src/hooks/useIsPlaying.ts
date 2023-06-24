import TrackPlayer from '..';
import { State } from '../constants';
import { usePlayWhenReady } from './usePlayWhenReady';
import { usePlaybackState } from './usePlaybackState';

/**
 * Tells whether the TrackPlayer is in a mode that most people would describe
 * as "playing." Great for UI to decide whether to show a Play or Pause button.
 * @returns playing - whether UI should likely show as Playing
 * @returns bufferingDuringPlay - whether UI should show as Buffering
 */
export function useIsPlaying() {
  const state = usePlaybackState().state;
  const playWhenReady = usePlayWhenReady();

  return determineIsPlaying(playWhenReady, state);
}

function determineIsPlaying(playWhenReady?: boolean, state?: State) {
  if (!state) {
    return { playing: false, bufferingDuringPlay: false };
  }

  const isLoading = state === State.Loading || state === State.Buffering;
  const isErrored = state === State.Error;
  const isEnded = state === State.Ended;

  return {
    playing: !!playWhenReady && !(isErrored || isEnded),
    bufferingDuringPlay: !!playWhenReady && isLoading,
  };
}

/**
 * This exists if you need realtime status on whether the TrackPlayer is
 * playing, whereas the hooks all have a delay because they depend on responding
 * to events before their state is updated.
 *
 * It also exists whenever you need to know the play state outside of a React
 * component, since hooks only work in components.
 */
export async function isPlaying() {
  const [playbackState, playWhenReady] = await Promise.all([
    TrackPlayer.getPlaybackState(),
    TrackPlayer.getPlayWhenReady(),
  ]);
  const { playing } = determineIsPlaying(playWhenReady, playbackState.state);

  return playing;
}
