export default class MediaSession {
    constructor(capabilities) {
        if ("mediaSession" in navigator) {
            navigator.mediaSession.playbackState = "none";
            for (const [action, handler] of capabilities) {
                try { navigator.mediaSession.setActionHandler(action, handler); }
                catch{ console.log(action + " is not supported yet"); }
            }
        }
    }

    setPlaying = () => {
        if ("mediaSession" in navigator)
            navigator.mediaSession.playbackState = "playing";
    }

    setPaused = () => {
        if ("mediaSession" in navigator)
            navigator.mediaSession.playbackState = "paused";
    }

    setMetadata = (title, artist, artwork) => {
        if ("mediaSession" in navigator) {
            navigator.mediaSession.metadata = new MediaMetadata({
                title: title,
                artist: artist,
                artwork: [
                    { src: artwork, type: 'image/png' },
                ]
            });
        }
    }
}