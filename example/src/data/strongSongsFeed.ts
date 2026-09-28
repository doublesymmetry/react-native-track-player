import { XMLParser } from 'fast-xml-parser';
import type { MediaItem } from '@rntp/player';
import { parseItunesDuration } from '../lib/parseItunesDuration';

const STRONG_SONGS_RSS = 'https://strongsongs.libsyn.com/rss';

export interface PodcastShowFromFeed {
  title: string;
  author: string;
  artwork: string;
  description: string;
  episodes: MediaItem[];
}

function first<T>(x: T | T[] | undefined): T | undefined {
  if (x == null) return undefined;
  return Array.isArray(x) ? x[0] : x;
}

function attr(obj: unknown, key: string): string | undefined {
  if (obj == null || typeof obj !== 'object') return undefined;
  const v = (obj as Record<string, unknown>)[key];
  return typeof v === 'string' ? v : undefined;
}

export async function fetchStrongSongsFeed(
  signal?: AbortSignal,
): Promise<PodcastShowFromFeed> {
  const res = await fetch(STRONG_SONGS_RSS, { signal });
  if (!res.ok) throw new Error(`Feed failed: ${res.status}`);
  const xml = await res.text();

  const parser = new XMLParser({
    ignoreAttributes: false,
    attributeNamePrefix: '@_',
  });
  const doc = parser.parse(xml) as Record<string, unknown>;
  const channel = first((doc?.rss as Record<string, unknown>)?.channel);
  if (!channel || typeof channel !== 'object') {
    throw new Error('Invalid feed: no channel');
  }

  const ch = channel as Record<string, unknown>;
  const imageEl = first(ch.image);
  const imageUrl =
    (imageEl &&
    typeof imageEl === 'object' &&
    typeof (imageEl as Record<string, unknown>).url === 'string'
      ? (imageEl as Record<string, string>).url
      : undefined) ??
    attr(ch['itunes:image'] ?? first(ch['itunes:image']), '@_href') ??
    '';

  const rawItems = ch.item;
  const items = Array.isArray(rawItems)
    ? rawItems
    : rawItems != null
      ? [rawItems]
      : [];

  const episodeList: MediaItem[] = [];

  for (const item of items) {
    if (!item || typeof item !== 'object') continue;
    const it = item as Record<string, unknown>;
    const enc = first(it.enclosure);
    if (!enc || typeof enc !== 'object') continue;
    const url = attr(enc, '@_url');
    const type = attr(enc, '@_type') ?? '';
    if (!url || !type.startsWith('audio/')) continue;

    const title = typeof it.title === 'string' ? it.title : 'Unknown';
    const guid =
      typeof it.guid === 'string'
        ? it.guid
        : (attr(it.guid as Record<string, unknown>, '#text') ?? url);
    const duration = parseItunesDuration(
      (typeof it['itunes:duration'] === 'string'
        ? it['itunes:duration']
        : undefined) ?? (it as Record<string, string>)?.itunesDuration,
    );
    const itunesImage = it['itunes:image'];
    const artworkUrl =
      (typeof itunesImage === 'string'
        ? itunesImage
        : attr(itunesImage as Record<string, unknown>, '@_href')) ??
      (imageUrl || undefined);
    const artist =
      (typeof it.creator === 'string' ? it.creator : undefined) ??
      (typeof (it as Record<string, string>).itunesAuthor === 'string'
        ? (it as Record<string, string>).itunesAuthor
        : undefined) ??
      'Strong Songs';

    episodeList.push({
      mediaId: guid,
      title,
      artist,
      albumTitle: 'Strong Songs',
      artworkUrl: artworkUrl || undefined,
      url,
      duration: duration > 0 ? duration : undefined,
    });
  }

  const authorRaw = ch.creator ?? ch['itunes:author'];
  const author = typeof authorRaw === 'string' ? authorRaw : 'Kirk Hamilton';

  return {
    title:
      (typeof ch.title === 'string' ? ch.title : undefined) ?? 'Strong Songs',
    author,
    artwork: imageUrl || '',
    description:
      (typeof ch.description === 'string' ? ch.description : undefined) ??
      "Music: it's good. On each episode of Strong Songs, host Kirk Hamilton takes listeners inside a piece of music.",
    episodes: episodeList,
  };
}
