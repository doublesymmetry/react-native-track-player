import { DeviceEventEmitter } from 'react-native';

import type { Spec } from '../src/NativeTrackPlayer';
import type { Track, UpdateOptions, PlaybackState } from '../src/interfaces';
import { Event } from '../src/constants/Event';
import { State } from '../src/constants/State';
import { PlaylistPlayer, RepeatMode } from './TrackPlayer';
import { SetupNotCalledError } from './TrackPlayer/SetupNotCalledError';

export class TrackPlayerModule extends PlaylistPlayer implements Spec {
  protected emitter = DeviceEventEmitter;
  protected progressUpdateEventInterval: NodeJS.Timeout | undefined;

  public getConstants() {
    return {
      // Capabilities
      CAPABILITY_JUMP_BACKWARD: 0,
      CAPABILITY_JUMP_FORWARD: 1,
      CAPABILITY_PAUSE: 2,
      CAPABILITY_PLAY: 3,
      CAPABILITY_PLAY_FROM_ID: 4,
      CAPABILITY_PLAY_FROM_SEARCH: 5,
      CAPABILITY_SEEK_TO: 6,
      CAPABILITY_SET_RATING: 7,
      CAPABILITY_SKIP: 8,
      CAPABILITY_SKIP_TO_NEXT: 9,
      CAPABILITY_SKIP_TO_PREVIOUS: 10,
      CAPABILITY_STOP: 11,

      // Rating Types
      RATING_HEART: 0,
      RATING_THUMBS_UP_DOWN: 1,
      RATING_3_STARS: 2,
      RATING_4_STARS: 3,
      RATING_5_STARS: 4,
      RATING_PERCENTAGE: 5,

      // Pitch Algorithms
      PITCH_ALGORITHM_LINEAR: 0,
      PITCH_ALGORITHM_MUSIC: 1,
      PITCH_ALGORITHM_VOICE: 2,

      // States
      STATE_BUFFERING: 'STATE_BUFFERING',
      STATE_LOADING: 'STATE_LOADING',
      STATE_NONE: 'STATE_NONE',
      STATE_PAUSED: 'STATE_PAUSED',
      STATE_PLAYING: 'STATE_PLAYING',
      STATE_READY: 'STATE_READY',
      STATE_STOPPED: 'STATE_STOPPED',

      // Repeat Modes
      REPEAT_OFF: RepeatMode.Off,
      REPEAT_TRACK: RepeatMode.Track,
      REPEAT_QUEUE: RepeatMode.Playlist,
    };
  }

  // observe and emit state changes
  public get state(): PlaybackState {
    return super.state;
  }
  public set state(newState: PlaybackState) {
    super.state = newState;
    this.emitter.emit(Event.PlaybackState, newState);
  }

  public async updateOptions(options: UpdateOptions) {
    this.setupProgressUpdates(options.progressUpdateEventInterval);
  }

  protected setupProgressUpdates(interval?: number) {
    // clear and reset interval
    this.clearUpdateEventInterval();
    if (interval) {
      this.clearUpdateEventInterval();
      this.progressUpdateEventInterval = setInterval(async () => {
        if (this.state.state === State.Playing) {
          const progress = await this.getProgress();
          this.emitter.emit(Event.PlaybackProgressUpdated, {
            ...progress,
            track: this.currentIndex,
          });
        }
      }, interval * 1000);
    }
  }

  protected clearUpdateEventInterval() {
    if (this.progressUpdateEventInterval) {
      clearInterval(this.progressUpdateEventInterval);
    }
  }

  protected async onPlaylistEnded() {
    await super.onPlaylistEnded();
    this.emitter.emit(Event.PlaybackQueueEnded, {
      track: this.currentIndex,
      position: this.element!.currentTime,
    });
  }

  public get playWhenReady(): boolean {
    return super.playWhenReady;
  }

  public set playWhenReady(pwr: boolean) {
    const didChange = pwr !== this._playWhenReady;
    super.playWhenReady = pwr;

    if (didChange) {
      this.emitter.emit(Event.PlaybackPlayWhenReadyChanged, {
        playWhenReady: this._playWhenReady,
      });
    }
  }

  public async getPlayWhenReady(): Promise<boolean> {
    return this.playWhenReady;
  }

  public async setPlayWhenReady(pwr: boolean): Promise<boolean> {
    this.playWhenReady = pwr;
    return this.playWhenReady;
  }

  public async load(track: Track) {
    if (!this.element) throw new SetupNotCalledError();
    const lastTrack = this.current;
    const lastPosition = this.element.currentTime;
    await super.load(track);

    this.emitter.emit(Event.PlaybackActiveTrackChanged, {
      lastTrack,
      lastPosition,
      lastIndex: this.lastIndex,
      index: this.currentIndex,
      track,
    });
  }

  public async getQueue(): Promise<Track[]> {
    return this.playlist;
  }

  public async setQueue(queue: Track[]) {
    await this.stop();
    this.playlist = queue;
  }

  public async getActiveTrack(): Promise<Track | undefined> {
    return this.current;
  }

  public async getActiveTrackIndex(): Promise<number | undefined> {
    // per the existing spec, this should throw if setup hasn't been called
    if (!this.element || !this.player) throw new SetupNotCalledError();
    return this.currentIndex;
  }

  public async getPlaybackState(): Promise<PlaybackState> {
    return this.state;
  }

  /**
   * overrides to match interface definition
   *
   * NOTE: these can be removed once we migrate to a sync API
   */
  public async pause() {
    return super.pause();
  }
  public async seekBy(seconds: number) {
    return super.seekBy(seconds);
  }
  public async seekTo(seconds: number) {
    return super.seekTo(seconds);
  }
  public async setVolume(volume: number) {
    return super.setVolume(volume);
  }
  // @ts-expect-error - promise return
  public async getVolume() {
    return super.getVolume();
  }
  // @ts-expect-error - promise return
  public async setRate(rate: number) {
    return super.setRate(rate);
  }

  /**
   * stubbed methods for cross-platform compat
   */
  public addListener() {}
  public removeListeners() {}
  public async acquireWakeLock() {}
  public async abandonWakeLock() {}
  public async validateOnStartCommandIntent() {
    return true;
  }
}
