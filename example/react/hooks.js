import { useState, useEffect } from 'react';
import TrackPlayer from 'react-native-track-player';

const { TrackPlayerEvents } = TrackPlayer;
export function useQueue(initialQueue) {
  const [queue, setQueueState] = useState([]);

  useEffect(() => {
    let didCancel = false;
    const applyInitialQueue = async () => {
      await TrackPlayer.reset();
      if (didCancel) return;
      await TrackPlayer.add(initialQueue);
    }
    if (initialQueue) applyInitialQueue();
    return () => { didCancel = true };
  }, [initialQueue]);

  useEffect(() => {
    let didCancel = false;
    const fetchQueue = async () => {
      const fetched = await TrackPlayer.getQueue();
      if (!didCancel) {
        setQueueState(fetched);
      }
    }
    fetchQueue();
    return () => { didCancel = true };
  }, []);

  return queue;
}

export function useCurrentTrack() {
  const [trackId, setTrackId] = useState(null);
  const queue = useQueue();
  const [track, setTrack] = useState(null);
  TrackPlayer.useTrackPlayerEvents(
    [TrackPlayerEvents.PLAYBACK_TRACK_CHANGED],
    ({ nextTrack }) => {
      setTrackId(nextTrack);
    }
  );

  useEffect(() => {
    let didCancel = false;
    const fetchTrack = async () => {
      const trackId = await TrackPlayer.getCurrentTrack();
      if (!didCancel && trackId) {
        setTrackId(trackId);
      }
    }
    return () => { didCancel = true };
  }, []);

  useEffect(() => {
    setTrack(
      queue.find(
        ({ id }) => id === trackId
      ) || null
    );
  }, [trackId, queue]);

  return track;
}

const getQueueIndex = (track, queue) => queue.findIndex(({ id }) => id === track.id);

export function useNextTrack() {
  const queue = useQueue();
  const currentTrack = useCurrentTrack();
  const [nextTrack, setNextTrack] = useState(null);
  useEffect(() => {
    if (!currentTrack) return;
    const index = getQueueIndex(currentTrack, queue);
    const hasNextTrack = index < (queue.length - 1);
    setNextTrack(
      hasNextTrack
        ? queue[index + 1]
        : null
    );
  }, [queue, currentTrack]);
  return nextTrack;
}

export function usePreviousTrack() {
  const queue = useQueue();
  const currentTrack = useCurrentTrack();
  const [previousTrack, setPreviousTrack] = useState(null);
  useEffect(() => {
    if (!currentTrack) return;
    const index = getQueueIndex(currentTrack, queue);
    const hasPreviousTrack = index !== 0;
    setPreviousTrack(
      hasPreviousTrack
        ? queue[index - 1]
        : null
    );
  }, [queue, currentTrack]);
  return previousTrack;
}

function useWhenPlaybackStateChanges(callback) {
  TrackPlayer.useTrackPlayerEvents(
    [TrackPlayerEvents.PLAYBACK_STATE],
    ({ state }) => {
      callback(state);
    }
  );
  useEffect(() => {
    let didCancel = false;
    async function fetchPlaybackState() {
      const playbackState = await TrackPlayer.getState();
      if (!didCancel) {
        callback(playbackState);
      }
    }
    fetchPlaybackState();
    return () => { didCancel = true };
  }, []);
}

export function usePlaybackState() {
  const [playbackState, setPlaybackState] = useState();
  useWhenPlaybackStateChanges(setPlaybackState);
  return playbackState;
}

export const usePlaybackStateIs = (...states) => {
  const [is, setIs] = useState();
  useWhenPlaybackStateChanges(state => {
    setIs(states.includes(state));
  });

  return is;
}
