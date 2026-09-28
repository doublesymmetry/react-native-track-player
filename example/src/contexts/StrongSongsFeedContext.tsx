import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import type { PodcastShowFromFeed } from '../data/strongSongsFeed';
import { useDownloadedTrackStore } from '../stores/downloadedTrack';
import { refreshBrowseTree } from '../lib/browseTree';

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

    try {
      const result = await refreshBrowseTree();
      if (result) setShow(result.show);
    } catch (e) {
      setError(e instanceof Error ? e : new Error(String(e)));
    } finally {
      setLoading(false);
    }
  }, []);

  const downloadedFileUri = useDownloadedTrackStore(s => s.fileUri);
  useEffect(() => {
    load();
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
