export default class MediaSession {
    static capabilities;
    static actionsEnabled = false;

    static setCapabilities = capabilities => {
        this.capabilities = capabilities;
    }

    static enableCapabilities = () => {
        if (!this.actionsEnabled && this.capabilities) {
            this.actionsEnabled = true;
            if ("mediaSession" in navigator) {
                navigator.mediaSession.playbackState = "none";
                for (const [action, handler] of this.capabilities) {
                    try { navigator.mediaSession.setActionHandler(action, handler); }
                    catch{ console.log(action + " is not supported yet"); }
                }
            }
        }
    }

    static setPlaying = () => {
        if ("mediaSession" in navigator)
            navigator.mediaSession.playbackState = "playing";
    }

    static setPaused = () => {
        if ("mediaSession" in navigator)
            navigator.mediaSession.playbackState = "paused";
    }

    static setMetadata = (title, artist, artwork) => {
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