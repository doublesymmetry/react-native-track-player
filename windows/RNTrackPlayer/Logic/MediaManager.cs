using Microsoft.ReactNative.Managed;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using TrackPlayer.Players;

namespace TrackPlayer.Logic
{
    public class MediaManager
    {
        private TrackPlayerModule module;

        private Metadata metadata;

        private Playback player;

        public MediaManager(TrackPlayerModule module)
        {
            this.module = module;
            this.metadata = new Metadata(this, module);
        }

        public void SwitchPlayback(Playback pb)
        {
            if (player != null)
            {
                player.Stop();
                player.Dispose();
            }

            player = pb;
            metadata.SetTransportControls(pb?.GetTransportControls());
        }

        public LocalPlayback CreateLocalPlayback(JSValueObject options)
        {
            return new LocalPlayback(this, options);
        }

        public void UpdateOptions(JSValue options)
        {
            metadata.UpdateOptions(options);
        }

        public Playback GetPlayer()
        {
            return player;
        }

        public Metadata GetMetadata()
        {
            return metadata;
        }

        public void OnEnd(Track previous, double prevPos)
        {
            module.PlaybackQueueEnded(new JSValueObject{ {"track", previous?.Id },
                                                         {"position", prevPos } });
        }

        public void OnStateChange(PlaybackState state)
        {
            module.PlaybackStateAction(new JSValueObject { { "state", (int)state } });
        }

        public void OnTrackUpdate(Track previous, double prevPos, Track next, bool changed)
        {
            metadata.UpdateMetadata(next);

            if (changed)
            {
                var jvo = new JSValueObject{{ "track", previous?.Id },
                                            { "position", prevPos },
                                            { "nextTrack", next?.Id } };
                module.PlaybackTrackChanged(jvo);
            }
        }

        public void OnError(string code, string error)
        {
            JSValueObject jvo = new JSValueObject{{ "code", code },
                                                  { "message", error } };
            module.PlaybackError(jvo);
        }

        public void Dispose()
        {
            if (player != null)
            {
                player.Dispose();
                player = null;
            }

            metadata.Dispose();
        }
    }
}
