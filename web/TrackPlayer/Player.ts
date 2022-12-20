// @ts-ignore
import shaka from 'shaka-player/dist/shaka-player.ui';
import { State } from '../../src';

import type { Track, Progress } from '../../src/interfaces';

export class Player {
  protected element: HTMLMediaElement;
  protected player: shaka.Player;
  protected _current?: Track = undefined;
  protected _playWhenReady: boolean = false;

  public get current(): Track | undefined {
    return this._current;
  }

  public set current(cur: Track | undefined) {
    this._current = cur;
  }

  public get playWhenReady(): boolean {
    return this._playWhenReady;
  }

  public set playWhenReady(pwr: boolean) {
    this._playWhenReady = pwr;
  }

  constructor() {
    // Install built-in polyfills to patch browser incompatibilities.
    shaka.polyfill.installAll();
    // Check to see if the browser supports the basic APIs Shaka needs.
    if (!shaka.Player.isBrowserSupported()) {
      // This browser does not have the minimum set of APIs we need.
      throw new Error('Browser not supported!');
    }

    this.element = document.createElement('audio');
    this.element.setAttribute('id', 'react-native-track-player');
    this.player = new shaka.Player(this.element);

    // Attach player to the window to make it easy to access in the JS console.
    // @ts-ignore
    window.rntp = this.player;
    // Listen for error events.
    this.player.addEventListener('error', this.onErrorEvent);
    this.element.addEventListener('ended', () => this.onTrackEnded());
  }

  protected async onTrackEnded() {}

  protected onErrorEvent(event: any) {
    // Extract the shaka.util.Error object from the event.
    this.onError(event.detail);
  }

  protected onError(error: any) {
    // Log the error.
    console.error('Error code', error.code, 'object', error);
  }

  public async load(track: Track) {
    await this.player.load(track.url);
    this.current = track;
  }

  public async stop() {
    this.current = undefined;
    await this.player.unload()
  }

  public play() {
    this.playWhenReady = true;
    return this.element.play();
  }

  public pause() {
    this.playWhenReady = false;
    return this.element.pause();
  }

  public setRate(rate: number) {
    return this.element.playbackRate = rate;
  }

  public getRate() {
    return this.element.playbackRate;
  }

  public seekTo(seconds: number) {
    return this.element.currentTime = seconds;
  }

  public setVolume(volume: number) {
    return this.element.volume = volume;
  }

  public getVolume() {
    return this.element.volume;
  }

  public getDuration() {
    return this.element.duration
  }

  public getPosition() {
    return this.element.currentTime
  }

  public getProgress(): Progress {
    return {
      position: this.element.currentTime,
      duration: this.element.duration || 0,
      buffered: 0, // TODO: this.element.buffered.end,
    }
  }

  public getBufferedPosition() {
    return this.element.buffered.end;
  }

  public getActiveTrack() {
    return this.current;
  }

  public getState(): State {
    const stats = this.player.getStats();

    if (!stats) {
      return State.None;
    }

    const lastState = stats.stateHistory && stats.stateHistory[stats.stateHistory.length -1];

    if (!lastState) {
      return State.None;
    }

    switch (lastState.state) {
      case 'buffering':
        return State.Buffering;
      case 'playing':
        return State.Playing;
      case 'paused':
        return State.Paused;
      default:
        return State.None;
    }
  }
}
