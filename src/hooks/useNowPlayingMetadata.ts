import { useState, useEffect } from 'react';

import { getActiveTrack } from '../trackPlayer';
import { Event } from '../constants';
import { useTrackPlayerEvents } from './useTrackPlayerEvents';
import type { NowPlayingMetadata, Track } from '../interfaces';

const extractNowPlayingMetadata = (
  track: Track | undefined
): NowPlayingMetadata | undefined => {
  if (!track) return undefined;

  return {
    title: track.title,
    album: track.album,
    artist: track.artist,
    duration: track.duration,
    artwork: track.artwork,
    description: track.description,
    mediaId: track.mediaId,
    genre: track.genre,
    date: track.date,
    rating: track.rating,
    isLiveStream: track.isLiveStream,
    elapsedTime: track.elapsedTime,
  };
};

export const useNowPlayingMetadata = (): NowPlayingMetadata | undefined => {
  const [nowPlayingMetadata, setNowPlayingMetadata] = useState<
    NowPlayingMetadata | undefined
  >();

  // Sets the initial index (if still undefined)
  useEffect(() => {
    let unmounted = false;
    getActiveTrack()
      .then((initialTrack) => {
        if (unmounted) return;
        setNowPlayingMetadata(
          (currentTrack) =>
            currentTrack ?? extractNowPlayingMetadata(initialTrack) ?? undefined
        );
      })
      .catch(() => {
        // throws when you haven't yet setup, which is fine because it also
        // means there's no active track
      });
    return () => {
      unmounted = true;
    };
  }, []);

  useTrackPlayerEvents(
    [
      Event.PlaybackActiveTrackChanged,
      Event.NowPlayingMetadataUpdated,
      Event.TrackMetadataUpdated,
    ],
    async (event) =>
      setNowPlayingMetadata(extractNowPlayingMetadata(event.track) ?? undefined)
  );

  return nowPlayingMetadata;
};
