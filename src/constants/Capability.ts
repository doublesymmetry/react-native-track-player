import { NativeModules } from 'react-native';
import {RatingType} from "./RatingType";
import {ResourceObject} from "../interfaces";
const { TrackPlayerModule: TrackPlayer } = NativeModules;

enum CapabilityConstant {
  Play = TrackPlayer.CAPABILITY_PLAY,
  PlayFromId = TrackPlayer.CAPABILITY_PLAY_FROM_ID,
  PlayFromSearch = TrackPlayer.CAPABILITY_PLAY_FROM_SEARCH,
  Pause = TrackPlayer.CAPABILITY_PAUSE,
  Stop = TrackPlayer.CAPABILITY_STOP,
  SeekTo = TrackPlayer.CAPABILITY_SEEK_TO,
  Skip = TrackPlayer.CAPABILITY_SKIP,
  SkipToNext = TrackPlayer.CAPABILITY_SKIP_TO_NEXT,
  SkipToPrevious = TrackPlayer.CAPABILITY_SKIP_TO_PREVIOUS,
  JumpForward = TrackPlayer.CAPABILITY_JUMP_FORWARD,
  JumpBackward = TrackPlayer.CAPABILITY_JUMP_BACKWARD,
  SetRating = TrackPlayer.CAPABILITY_SET_RATING,
  Like = TrackPlayer.CAPABILITY_LIKE,
  Dislike = TrackPlayer.CAPABILITY_DISLIKE,
  Bookmark = TrackPlayer.CAPABILITY_BOOKMARK,

  // TODO: Android can support set repeat mode, shuffle mode, and playback speed.
}

/** The options for a notification capability. */
export interface NotificationCapabilityOptions {
  /** A custom icon to use in the notification. */
  icon?: ResourceObject,
  /** Wether this capability should be enabled in the compact notification. */
  compact: boolean;
}

export interface NotificationCapability {
  showInNotification: boolean;
  notificationOptions?: NotificationCapabilityOptions;
  constant: CapabilityConstant;
}

interface SimpleCapability {
  constant: CapabilityConstant;
}

interface JumpDirectionallyCapability {
  showInNotification: boolean;
  notificationOptions?: NotificationCapabilityOptions;
  jumpInterval: number;
  constant: CapabilityConstant;
}

interface AndroidRatingCapability {
  ratingType: RatingType;
  constant: CapabilityConstant;
}

interface IOSRatingCapability {
  /** The title given to the rating button. */
  title: string;
  /** Whether the rating button should be marked as active or "done". */
  isActive: boolean;
}

/** A type collection of the available capabilities. */
export type CapabilityImpl = NotificationCapability | JumpDirectionallyCapability | SimpleCapability | AndroidRatingCapability | IOSRatingCapability;

// MARK: - Capability Builders

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Capability {
  /**
   * Generates a capability that allows the user to play media.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function Play(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.Play,
    };
  }

  /**
   * ANDROID ONLY.
   * Capability that allows the user to play media from a given ID.
   * This is used for Android's media session (Android Auto).
   */
  export const PlayFromId: CapabilityImpl = {
    constant: CapabilityConstant.PlayFromId,
  }

  /**
   * ANDROID ONLY.
   * Capability that allows the user to play media from a given search query.
   * This is used for Android's media session (Android Auto).
   */
  export const PlayFromSearch: CapabilityImpl = {
    constant: CapabilityConstant.PlayFromSearch,
  }

  /**
   * Generates a capability that allows the user to pause media.
   * By default, this is enabled in the notification and in the compact notification.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function Pause(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.Pause,
    };
  }

  /**
   * Generates a capability that allows the user to stop media.
   * By default, this is enabled in the notification and in the compact notification.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function Stop(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.Stop,
    };
  }

  /**
   * Generates a capability that allows the user to seek to a given position.
   * By default, this is enabled in the notification and in the compact notification.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function SeekTo(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.SeekTo,
    };
  }

  /**
   * ANDROID ONLY.
   * Generates a capability that allows the user to skip to any song in the queue.
   * This is used for Android's media session.
   */
  export const Skip: CapabilityImpl = {
    constant: CapabilityConstant.SkipToNext,
  }

  /**
   * Generates a capability that allows the user to skip to the next song.
   * By default, this is enabled in the notification and in the compact notification.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function SkipToNext(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.SkipToNext,
    };
  }

  /**
   * Generates a capability that allows the user to skip to the previous song.
   * By default, this is enabled in the notification and in the compact notification.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   */
  export function SkipToPrevious(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      constant: CapabilityConstant.SkipToPrevious,
    };
  }

  /**
   * Generates a capability that allows the user to jump forward by a given interval.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   * @param jumpInterval The interval to jump forward by.
   */
  export function JumpForward(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
    jumpInterval = 15,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      jumpInterval,
      constant: CapabilityConstant.JumpForward,
    };
  }

  /**
   * Generates a capability that allows the user to jump backward by a given interval.
   * @param showInNotification Whether this capability should be shown in the notification.
   * @param notificationOptions The notification options for this capability.
   * @param jumpInterval The interval to jump backward by.
   */
  export function JumpBackward(
    showInNotification = true,
    notificationOptions?: NotificationCapabilityOptions,
    jumpInterval = 15,
  ): CapabilityImpl {
    return {
      showInNotification,
      notificationOptions,
      jumpInterval,
      constant: CapabilityConstant.JumpBackward,
    };
  }

  /**
   * ANDROID ONLY.
   * Generates a capability that allows the user to set a rating for the current track.
   * This is used for Android's media session.
   * @param ratingType The type of rating to set.
   */
  export function Rating(ratingType: RatingType): CapabilityImpl {
    return {
      ratingType,
      constant: CapabilityConstant.SetRating,
    };
  }

  /**
   * IOS ONLY.
   * Generates a capability that allows the user to like the current track.
   */
  export function Like(title: string, isActive: boolean): CapabilityImpl {
    return {
      title,
      isActive,
    };
  }

  /**
   * IOS ONLY.
   * Generates a capability that allows the user to dislike the current track.
   */
  export function Dislike(title: string, isActive: boolean): CapabilityImpl {
    return {
      title,
      isActive,
    };
  }

  /**
   * IOS ONLY.
   * Generates a capability that allows the user to bookmark the current track.
   */
  export function Bookmark(title: string, isActive: boolean): CapabilityImpl {
    return {
      title,
      isActive,
    };
  }
}
