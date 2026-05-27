import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import TrackPlayer, { type BrowseItem } from '@rntp/player';
import type { PodcastShowFromFeed } from '../data/strongSongsFeed';
import { fetchStrongSongsFeed } from '../data/strongSongsFeed';
import { buildAlbum } from '../data/music';
import { useDownloadedTrackStore } from '../stores/downloadedTrack';
import { radioStations } from '../data/radio';

export type StrongSongsFeedValue = {
  show: PodcastShowFromFeed | null;
  loading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
};

const StrongSongsFeedContext = createContext<StrongSongsFeedValue | null>(null);

export function StrongSongsFeedProvider({ children }: { children: ReactNode }) {
  const [show, setShow] = useState<PodcastShowFromFeed | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    const album = buildAlbum(useDownloadedTrackStore.getState().fileUri);
    try {
      const data = await fetchStrongSongsFeed();
      setShow(data);
      // Group music tracks by albumTitle into nested browse items
      const albumGroups = new Map<string, typeof album.tracks>();
      for (const track of album.tracks) {
        const key = track.albumTitle ?? 'Other';
        const group = albumGroups.get(key) ?? [];
        group.push(track);
        albumGroups.set(key, group);
      }
      const musicItems = Array.from(albumGroups.entries()).map(
        ([albumTitle, tracks]) => ({
          mediaId: `album-${albumTitle.toLowerCase().replace(/\s+/g, '-')}`,
          title: albumTitle,
          artist: tracks[0]?.artist ?? '',
          artworkUrl: tracks[0]?.artworkUrl,
          children: tracks as BrowseItem[],
        }),
      );

      TrackPlayer.setBrowseTree([
        {
          mediaId: 'music',
          title: 'Music',
          items: musicItems,
        },
        {
          mediaId: 'podcasts',
          title: 'Podcasts',
          items: data.episodes as BrowseItem[],
        },
        {
          mediaId: 'radio',
          title: 'Radio',
          items: radioStations.map(s => s.mediaItem) as BrowseItem[],
        },
      ]);
    } catch (e) {
      setError(e instanceof Error ? e : new Error(String(e)));
      // Still set the browse tree with available static content
      const fallbackGroups = new Map<string, typeof album.tracks>();
      for (const track of album.tracks) {
        const key = track.albumTitle ?? 'Other';
        const group = fallbackGroups.get(key) ?? [];
        group.push(track);
        fallbackGroups.set(key, group);
      }
      const fallbackMusic = Array.from(fallbackGroups.entries()).map(
        ([albumTitle, tracks]) => ({
          mediaId: `album-${albumTitle.toLowerCase().replace(/\s+/g, '-')}`,
          title: albumTitle,
          artist: tracks[0]?.artist ?? '',
          artworkUrl: tracks[0]?.artworkUrl,
          children: tracks as BrowseItem[],
        }),
      );

      TrackPlayer.setBrowseTree([
        {
          mediaId: 'music',
          title: 'Music',
          items: fallbackMusic,
        },
        {
          mediaId: 'radio',
          title: 'Radio',
          items: radioStations.map(s => s.mediaItem) as BrowseItem[],
        },
      ]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const downloadedFileUri = useDownloadedTrackStore(s => s.fileUri);
  useEffect(() => {
    if (downloadedFileUri) load();
  }, [downloadedFileUri, load]);

  const value: StrongSongsFeedValue = {
    show,
    loading,
    error,
    refetch: load,
  };

  return (
    <StrongSongsFeedContext.Provider value={value}>
      {children}
    </StrongSongsFeedContext.Provider>
  );
}

export function useStrongSongsFeed(): StrongSongsFeedValue {
  const ctx = useContext(StrongSongsFeedContext);
  if (ctx == null) {
    throw new Error(
      'useStrongSongsFeed must be used within StrongSongsFeedProvider',
    );
  }
  return ctx;
}
