using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Media;
using Windows.Media.Playback;
using TrackPlayer.Logic;

namespace TrackPlayer.Players {

    public abstract class Playback {

        protected MediaManager manager;

        protected List<Track> queue = new List<Track>();
        protected int currentTrack = -1;
        protected MediaPlaybackState prevState = MediaPlaybackState.None;

        public Playback(MediaManager manager) {
            this.manager = manager;
        }

        protected void UpdateState(MediaPlaybackState state) {
            if(prevState == state) return;

            manager.OnStateChange(state);
            prevState = state;
        }

        protected void UpdateCurrentTrack(int index, IPromise promise) {
            if(queue.Count == 0) {
                Reset();
                promise.Reject("queue", "The queue is empty");
                return;
            } else if(index < 0) {
                index = 0;
            } else if(index >= queue.Count) {
                index = queue.Count - 1;
            }

            Track previous = GetCurrentTrack();
            double position = GetPosition();
            MediaPlaybackState oldState = GetState();

            Debug.WriteLine("Updating current track...");

            Track track = queue[index];
            currentTrack = index;

            Load(track, promise);

            if(Utils.IsPlaying(oldState)) {
                Play();
            } else if(Utils.IsPaused(oldState)) {
                Pause();
            }

            manager.OnTrackUpdate(previous, position, track, true);
        }

        public Track GetCurrentTrack() {
            return currentTrack >= 0 && currentTrack < queue.Count ? queue[currentTrack] : null;
        }

        public Track GetTrack(string id) {
            return queue.Find(track => track.id == id);
        }

        public void Add(List<Track> tracks, string insertBeforeId, IPromise promise) {
            if(insertBeforeId == null) {
                bool empty = queue.Count == 0;
                queue.AddRange(tracks);

                // Tracks were added, we'll update the current track accordingly
                if(empty) UpdateCurrentTrack(0, null);
            } else {
                int index = queue.FindIndex(track => track.id == insertBeforeId);
                if(index == -1) index = queue.Count;

                queue.InsertRange(index, tracks);

                if(currentTrack >= index) {
                    currentTrack += tracks.Count;
                }
            }

            promise?.Resolve(null);
        }

        public void Remove(List<string> ids, IPromise promise) {
            int currTrack = currentTrack;

            foreach(string id in ids) {
                int index = queue.FindIndex(track => track.id == id);
                queue.RemoveAt(index);

                if(index == currTrack) currTrack += 1;
            }

            if(currTrack != currentTrack) {
                UpdateCurrentTrack(currTrack, null);
            }

            promise?.Resolve(null);
        }

        public abstract SystemMediaTransportControls GetTransportControls();

        protected abstract void Load(Track track, IPromise promise);

        public abstract void Play();

        public abstract void Pause();

        public abstract void Stop();

        public void Reset() {
            Track prev = GetCurrentTrack();
            double pos = GetPosition();

            Stop();

            currentTrack = -1;
            queue.Clear();

            manager.OnTrackUpdate(prev, pos, null, true);
        }

        public void Skip(string id, IPromise promise) {
            int index = queue.FindIndex(track => track.id == id);

            if(index >= 0) {
                UpdateCurrentTrack(index, promise);
            } else {
                promise?.Reject("skip", "The track was not found");
            }
        }

        protected bool HasNext() {
            return currentTrack < queue.Count - 1;
        }

        public void SkipToNext(IPromise promise) {
            if(HasNext()) {
                UpdateCurrentTrack(currentTrack + 1, promise);
            } else {
                promise?.Reject("skip", "There is no next tracks");
            }
        }

        public void SkipToPrevious(IPromise promise) {
            if(currentTrack > 0) {
                UpdateCurrentTrack(currentTrack - 1, promise);
            } else {
                promise?.Reject("skip", "There is no previous tracks");
            }
        }

        public abstract void SetVolume(double volume);

        public abstract double GetVolume();

        public abstract void SetRate(double rate);

        public abstract double GetRate();

        public abstract void SeekTo(double seconds);

        public abstract double GetPosition();

        public abstract double GetBufferedPosition();

        public abstract double GetDuration();

        public abstract MediaPlaybackState GetState();

        public abstract void Dispose();

    }

}
