package com.guichaguri.trackplayer.service.player;

import com.google.android.exoplayer2.extractor.mp4.MdtaMetadataEntry;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.flac.VorbisComment;
import com.google.android.exoplayer2.metadata.icy.IcyHeaders;
import com.google.android.exoplayer2.metadata.icy.IcyInfo;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame;
import com.guichaguri.trackplayer.service.MusicManager;

public class SourceMetadata {

    /**
     * Reads metadata and triggers the metadata-received event
     */
    public static void handleMetadata(MusicManager manager, Metadata metadata) {
        handleId3Metadata(manager, metadata);
        handleIcyMetadata(manager, metadata);
        handleVorbisCommentMetadata(manager, metadata);
        handleQuickTimeMetadata(manager, metadata);
    }

    /**
     * ID3 Metadata (MP3)
     *
     * https://en.wikipedia.org/wiki/ID3
     */
    private static void handleId3Metadata(MusicManager manager, Metadata metadata) {
        String title = null, url = null, artist = null, album = null, date = null, genre = null;

        for(int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            if (entry instanceof TextInformationFrame) {
                // ID3 text tag
                TextInformationFrame id3 = (TextInformationFrame) entry;
                String id = id3.id.toUpperCase();

                if (id.equals("TIT2") || id.equals("TT2")) {
                    title = id3.value;
                } else if (id.equals("TALB") || id.equals("TOAL") || id.equals("TAL")) {
                    album = id3.value;
                } else if (id.equals("TOPE") || id.equals("TPE1") || id.equals("TP1")) {
                    artist = id3.value;
                } else if (id.equals("TDRC") || id.equals("TOR")) {
                    date = id3.value;
                } else if (id.equals("TCON") || id.equals("TCO")) {
                    genre = id3.value;
                }

            } else if (entry instanceof UrlLinkFrame) {
                // ID3 URL tag
                UrlLinkFrame id3 = (UrlLinkFrame) entry;
                String id = id3.id.toUpperCase();

                if (id.equals("WOAS") || id.equals("WOAF") || id.equals("WOAR") || id.equals("WAR")) {
                    url = id3.url;
                }

            }
        }

        if (title != null || url != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("id3", title, url, artist, album, date, genre);
        }
    }

    /**
     * Shoutcast / Icecast metadata (ICY protocol)
     *
     * https://cast.readme.io/docs/icy
     */
    private static void handleIcyMetadata(MusicManager manager, Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            if(entry instanceof IcyHeaders) {
                // ICY headers
                IcyHeaders icy = (IcyHeaders)entry;

                manager.onMetadataReceived("icy-headers", icy.name, icy.url, null, null, null, icy.genre);

            } else if(entry instanceof IcyInfo) {
                // ICY data
                IcyInfo icy = (IcyInfo)entry;

                String artist, title;
                int index = icy.title == null ? -1 : icy.title.indexOf(" - ");

                if (index != -1) {
                    artist = icy.title.substring(0, index);
                    title = icy.title.substring(index + 3);
                } else {
                    artist = null;
                    title = icy.title;
                }

                manager.onMetadataReceived("icy", title, icy.url, artist, null, null, null);

            }
        }
    }

    /**
     * Vorbis Comments (Vorbis, FLAC, Opus, Speex, Theora)
     *
     * https://xiph.org/vorbis/doc/v-comment.html
     */
    private static void handleVorbisCommentMetadata(MusicManager manager, Metadata metadata) {
        String title = null, url = null, artist = null, album = null, date = null, genre = null;

        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            if (!(entry instanceof VorbisComment)) continue;

            VorbisComment comment = (VorbisComment) entry;
            String key = comment.key;

            if (key.equals("TITLE")) {
                title = comment.value;
            } else if (key.equals("ARTIST")) {
                artist = comment.value;
            } else if (key.equals("ALBUM")) {
                album = comment.value;
            } else if (key.equals("DATE")) {
                date = comment.value;
            } else if (key.equals("GENRE")) {
                genre = comment.value;
            } else if (key.equals("URL")) {
                url = comment.value;
            }
        }

        if (title != null || url != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("vorbis-comment", title, url, artist, album, date, genre);
        }
    }

    /**
     * QuickTime MDTA metadata (mov, qt)
     *
     * https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/Metadata/Metadata.html
     */
    private static void handleQuickTimeMetadata(MusicManager manager, Metadata metadata) {
        String title = null, artist = null, album = null, date = null, genre = null;

        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            if (!(entry instanceof MdtaMetadataEntry)) continue;

            MdtaMetadataEntry mdta = (MdtaMetadataEntry) entry;
            String key = mdta.key;

            try {
                if (key.equals("com.apple.quicktime.title")) {
                    title = new String(mdta.value, "UTF-8");
                } else if (key.equals("com.apple.quicktime.artist")) {
                    artist = new String(mdta.value, "UTF-8");
                } else if (key.equals("com.apple.quicktime.album")) {
                    album = new String(mdta.value, "UTF-8");
                } else if (key.equals("com.apple.quicktime.creationdate")) {
                    date = new String(mdta.value, "UTF-8");
                } else if (key.equals("com.apple.quicktime.genre")) {
                    genre = new String(mdta.value, "UTF-8");
                }
            } catch(Exception ex) {
                // Ignored
            }
        }

        if (title != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("quicktime", title, null, artist, album, date, genre);
        }
    }

}
