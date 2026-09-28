import type { MediaItem } from '@rntp/player';

export interface RadioStation {
  mediaItem: MediaItem;
  genre: string;
  description: string;
  color: string;
}

export const radioStations: RadioStation[] = [
  {
    mediaItem: {
      mediaId: 'klubradio-live',
      title: 'Klubrádió',
      artist: 'Klubrádió',
      albumTitle: 'Klubrádió',
      url: 'https://stream.klubradio.hu:8443/',
      isLive: true,
      mimeType: 'audio/mpeg',
    },
    genre: 'News / Talk / Music',
    description:
      'Magyarország független közösségi rádiója. Independent community radio from Budapest.',
    color: '#E30613',
  },
  {
    mediaItem: {
      mediaId: 'dlf-kultur-live',
      title: 'Deutschlandfunk Kultur',
      artist: 'Deutschlandradio',
      albumTitle: 'Deutschlandfunk Kultur',
      url: 'https://st02.sslstream.dlf.de/dlf/02/128/mp3/stream.mp3',
      isLive: true,
      mimeType: 'audio/mpeg',
    },
    genre: 'Culture / Arts / Music',
    description:
      'German public radio for culture, arts and debate. Debatte, Feuilleton, Hörspiel, Konzert.',
    color: '#0A4B78',
  },
  {
    mediaItem: {
      mediaId: 'somafm-groovesalad',
      title: 'Groove Salad',
      artist: 'SomaFM',
      albumTitle: 'SomaFM',
      url: 'https://ice1.somafm.com/groovesalad-128-mp3',
      isLive: true,
      mimeType: 'audio/mpeg',
    },
    genre: 'Ambient / Downtempo',
    description:
      'SomaFM — sends ICY StreamTitle + StreamUrl with frequent track changes. Live relay path.',
    color: '#5A3E85',
  },
  {
    // Same stream on the failover host so its cache key differs from the entry
    // above (otherwise they collide). isLive:false exercises response-driven
    // endless-ICY detection instead of the explicit-live routing hint.
    mediaItem: {
      mediaId: 'somafm-groovesalad-cached',
      title: 'Groove Salad (cached-path)',
      artist: 'SomaFM',
      albumTitle: 'SomaFM',
      url: 'https://ice2.somafm.com/groovesalad-128-mp3',
      isLive: false,
      mimeType: 'audio/mpeg',
    },
    genre: 'Ambient / Downtempo',
    description: 'Same stream, isLive:false — exercises response-driven ICY relay detection.',
    color: '#3E855A',
  },
];
