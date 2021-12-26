export default class MediaSession {
    static capabilities;
    static actionsEnabled = false;

    static setCapabilities = capabilities => {
        this.capabilities = capabilities;
    }

    static enableCapabilities = () => {
        if (!this.actionsEnabled && this.capabilities) {
            this.actionsEnabled = true;
            navigator.mediaSession.playbackState = "none";
            for (const [action, handler] of this.capabilities) {
                try { navigator.mediaSession.setActionHandler(action, handler); }
                catch{ console.log(action + " is not supported yet"); }
            }
        }
    }

    static setPlaying = () => {
        navigator.mediaSession.playbackState = "playing";
    }

    static setPaused = () => {
        navigator.mediaSession.playbackState = "paused";
    }

    static setNone = () => {
        navigator.mediaSession.playbackState = "none";
    }

    static setMetadata = (title, artist, artwork) => {
        navigator.mediaSession.metadata = new MediaMetadata({
            title: title,
            artist: artist,
            artwork: [
                { src: artwork, type: 'image/png' },
            ]
        });
    }

    static setPosition = (duration, position) => {
        navigator.mediaSession.setPositionState({
            duration: duration,
            playbackRate: 1,
            position: position
        });
    }
}