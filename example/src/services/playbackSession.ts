import TrackPlayer, {
  DEFAULT_CAST_RECEIVER_APP_ID,
  Event,
  type BrowseRefreshRequestedEvent,
} from '@rntp/player';
import { publishInitialBrowseTree, refreshBrowseTree } from '../lib/browseTree';
import { registerEventLogListeners } from './eventLogListeners';

async function refreshPodcasts(): Promise<boolean> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10_000);
  try {
    const result = await refreshBrowseTree(controller.signal);
    return result?.published ?? false;
  } catch (error) {
    if (controller.signal.aborted) {
      console.log('Podcast refresh aborted');
      return false;
    }
    console.warn('Podcast refresh failed', error);
    return false;
  } finally {
    clearTimeout(timeout);
  }
}

export async function startPlaybackSession(
  refresh: (
    event: BrowseRefreshRequestedEvent,
  ) => Promise<unknown> = refreshPodcasts,
): Promise<void> {
  TrackPlayer.addEventListener(Event.BrowseRefreshRequested, refresh);

  registerEventLogListeners();
  TrackPlayer.setupPlayer({
    contentType: 'music',
    handleAudioBecomingNoisy: true,
    cache: {},
    progressSync: {
      intervalSeconds: 5,
      http: {
        url: 'http://localhost:3333/progress',
      },
    },
    android: {
      wakeMode: 'network',
      skipSilenceEnabled: false,
      autoStartJs: true,
      cast: DEFAULT_CAST_RECEIVER_APP_ID,
    },
  });
  publishInitialBrowseTree();
}
