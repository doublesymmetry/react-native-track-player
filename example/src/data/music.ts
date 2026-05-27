import type { MediaItem } from '@rntp/player';

// Three flavours of local audio, one song each:
//
// 1. require()   — Call the Spirit (Metro-bundled JS asset)
// 2. asset://    — One Love, Two Love (native resource: res/raw / Bundle.main)
// 3. file://     — The Sea Awaits (copied from native bundle to Documents at startup)
const localCallTheSpirit = require('../assets/CallTheSpirit.mp3');

export type AlbumTrack = MediaItem & { isLocal?: boolean };

export const LOCAL_MEDIA_IDS = new Set<string>([
  'call-the-spirit',
  'one-love-asset',
  'sea-awaits-file',
]);

/** Native resource name used for the file:// demo (copied to Documents). */
export const FILE_TRACK_RESOURCE_NAME = 'the_sea_awaits';
export const FILE_TRACK_RESOURCE_EXT = 'mp3';

export interface Album {
  title: string;
  artist: string;
  artwork: string;
  year: number;
  tracks: AlbumTrack[];
}

const baseTracks: AlbumTrack[] = [
  // 1) require() path
  {
    mediaId: 'call-the-spirit',
    title: 'Call the Spirit (require)',
    artist: 'dcvz',
    albumTitle: 'Synthetic Dreams',
    url: localCallTheSpirit,
    duration: undefined,
    isLocal: true,
  },
  // 2) asset:// path — bare name normalized to asset:// by the JS layer
  {
    mediaId: 'one-love-asset',
    title: 'One Love, Two Love (asset://)',
    artist: 'dcvz',
    albumTitle: 'Synthetic Dreams',
    url: 'one_love_two_love.mp3',
    isLocal: true,
  },
  {
    mediaId: 'lunar-serenade',
    title: 'Lunar Serenade',
    artist: 'dcvz',
    albumTitle: 'Synthetic Dreams',
    artworkUrl:
      'https://cdn2.suno.ai/image_479d721a-7fac-4f99-8f20-6e767b857784.jpeg',
    url: 'https://cdn1.suno.ai/479d721a-7fac-4f99-8f20-6e767b857784.mp3',
    duration: 212,
  },
  {
    mediaId: 'beware-french-men',
    title: 'Beware of the French Men',
    artist: 'KenSarMusic',
    albumTitle: 'Synthetic Dreams',
    artworkUrl:
      'https://cdn2.suno.ai/image_a73f6912-2c39-4c1d-a134-87af32fb5e9f.jpeg',
    url: 'https://cdn1.suno.ai/9cd7f8cc-d9e5-4d10-b1a0-3b5fad2418c5.mp3',
    duration: 186,
  },
  {
    mediaId: 'fairy-spring',
    title: 'Fairy Spring',
    artist: 'dcvz',
    albumTitle: 'LoZ',
    artworkUrl:
      'https://cdn2.suno.ai/image_3b11f85d-6efd-4667-b138-bdcf659331a7.jpeg',
    url: 'https://cdn1.suno.ai/3b11f85d-6efd-4667-b138-bdcf659331a7.mp3',
    duration: 198,
  },
  {
    mediaId: 'rain-storms',
    title: 'Rain Storms',
    artist: 'dcvz',
    albumTitle: 'LoZ',
    artworkUrl:
      'https://cdn2.suno.ai/image_8c4e4561-a919-4b4c-9a60-402ec7117dc7.jpeg',
    url: 'https://cdn1.suno.ai/8c4e4561-a919-4b4c-9a60-402ec7117dc7.m4a',
    duration: 224,
  },
  {
    mediaId: 'lullaby',
    title: 'Lullaby',
    artist: 'dcvz',
    albumTitle: 'LoZ',
    artworkUrl:
      'https://cdn2.suno.ai/image_2f6a521d-5f91-4da5-8785-06228904da79.jpeg',
    url: 'https://cdn1.suno.ai/2f6a521d-5f91-4da5-8785-06228904da79.mp3',
    duration: 195,
  },
];

const ALBUM_META = {
  title: 'Synthetic Dreams',
  artist: 'Various Artists',
  artwork:
    'https://cdn2.suno.ai/image_479d721a-7fac-4f99-8f20-6e767b857784.jpeg',
  year: 2025,
} as const;

/**
 * When `fileTrackUri` is provided, inserts the file:// demo track after
 * the asset:// entry.
 */
export function buildAlbum(fileTrackUri: string | null): Album {
  if (!fileTrackUri) return { ...ALBUM_META, tracks: baseTracks };

  const fileTrack: AlbumTrack = {
    mediaId: 'sea-awaits-file',
    title: 'The Sea Awaits (file://)',
    artist: 'dcvz',
    albumTitle: 'Synthetic Dreams',
    url: fileTrackUri,
    isLocal: true,
  };

  const assetIdx = baseTracks.findIndex(t => t.mediaId === 'one-love-asset');
  return {
    ...ALBUM_META,
    tracks: [
      ...baseTracks.slice(0, assetIdx + 1),
      fileTrack,
      ...baseTracks.slice(assetIdx + 1),
    ],
  };
}

export const album: Album = buildAlbum(null);
