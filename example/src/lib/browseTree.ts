import TrackPlayer, {
  type BrowseCategory,
  type BrowseItem,
  type MediaItem,
} from '@rntp/player';
import {
  buildAlbum,
  FILE_TRACK_RESOURCE_NAME,
  FILE_TRACK_RESOURCE_EXT,
} from '../data/music';
import { radioStations } from '../data/radio';
import { fetchStrongSongsFeed } from '../data/strongSongsFeed';
import { useDownloadedTrackStore } from '../stores/downloadedTrack';
import { ensureDownloadedTrack } from './downloadedTracks';

let latestRefreshRequest = 0;

export function publishInitialBrowseTree() {
  const info = TrackPlayer.getBrowseTreeInfo();
  if (info.isEmpty) {
    TrackPlayer.setBrowseTree(
      buildStaticBrowseTree(useDownloadedTrackStore.getState().fileUri),
    );
  }
  return info;
}

export async function refreshBrowseTree(
  signal?: AbortSignal,
  shouldPublish = () => true,
) {
  const request = ++latestRefreshRequest;
  const canPublish = () =>
    request === latestRefreshRequest && !signal?.aborted && shouldPublish();
  const show = await fetchStrongSongsFeed(signal);
  if (!canPublish()) return { show, published: false };

  const recoveredUri =
    useDownloadedTrackStore.getState().fileUri ??
    (await ensureDownloadedTrack(
      FILE_TRACK_RESOURCE_NAME,
      FILE_TRACK_RESOURCE_EXT,
    ));
  if (!canPublish()) return { show, published: false };
  const fileTrackUri =
    useDownloadedTrackStore.getState().fileUri ?? recoveredUri;
  if (!fileTrackUri) return { show, published: false };

  TrackPlayer.setBrowseTree(
    mergePodcasts(buildStaticBrowseTree(fileTrackUri), show.episodes),
  );
  return { show, published: true };
}

export function buildStaticBrowseTree(
  fileTrackUri: string | null = null,
): BrowseCategory[] {
  const album = buildAlbum(fileTrackUri);
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

  return [
    {
      mediaId: 'music',
      title: 'Music',
      items: musicItems,
    },
    {
      mediaId: 'radio',
      title: 'Radio',
      items: radioStations.map(station => station.mediaItem) as BrowseItem[],
    },
  ];
}

export function mergePodcasts(
  tree: BrowseCategory[],
  episodes: MediaItem[],
): BrowseCategory[] {
  const podcasts: BrowseCategory = {
    mediaId: 'podcasts',
    title: 'Podcasts',
    items: episodes as BrowseItem[],
  };
  const radioIndex = tree.findIndex(category => category.mediaId === 'radio');

  if (radioIndex === -1) return [...tree, podcasts];

  return [...tree.slice(0, radioIndex), podcasts, ...tree.slice(radioIndex)];
}
