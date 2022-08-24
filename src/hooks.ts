import { useEffect, useRef, useState } from 'react'

import { Event, EventsPayloadByEvent, Progress, State } from './interfaces'
import TrackPlayer from './trackPlayer'

/** Get current playback state and subsequent updates  */
export const usePlaybackState = () => {
  const [state, setState] = useState(State.None)
  const isUnmountedRef = useRef(true)

  useEffect(() => {
    isUnmountedRef.current = false
    return () => {
      isUnmountedRef.current = true
    }
  }, [])

  useEffect(() => {
    async function setPlayerState() {
      try {
        const playerState = await TrackPlayer.getState()

        // If the component has been unmounted, exit
        if (isUnmountedRef.current) return

        setState(playerState)
      } catch {} // getState only throw while you haven't yet setup, ignore failure.
    }

    // Set initial state
    setPlayerState()

    const sub = TrackPlayer.addEventListener<Event.PlaybackState>(Event.PlaybackState, data => {
      setState(data.state)
    })

    return () => sub.remove()
  }, [])

  return state
}

/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
export const useTrackPlayerEvents = <T extends Event[], H extends (data: EventsPayloadByEvent[T[number]]) => void>(
  events: T,
  handler: H,
) => {
  const savedHandler = useRef(handler)
  savedHandler.current = handler

  useEffect(() => {
    if (__DEV__) {
      const allowedTypes = Object.values(Event)
      const invalidTypes = events.filter(type => !allowedTypes.includes(type))
      if (invalidTypes.length) {
        console.warn(
          'One or more of the events provided to useTrackPlayerEvents is ' +
            `not a valid TrackPlayer event: ${invalidTypes.join("', '")}. ` +
            'A list of available events can be found at ' +
            'https://react-native-track-player.js.org/docs/api/events',
        )
      }
    }

    const subs = events.map(type =>
      TrackPlayer.addEventListener(type, payload => {
        // @ts-expect-error
        savedHandler.current({ ...payload, type })
      }),
    )

    return () => subs.forEach(sub => sub.remove())
  }, [events])
}

/**
 * A hook that returns the current playback progress
 * @param updateInterval The interval in ms at which the progress is updated
 */
export function useProgress(updateInterval = 1000) {
  useEffect(() => {
    if (updateInterval) {
      TrackPlayer.updateOptions({ progressUpdateEventInterval: updateInterval * 0.001 })
    }
  }, [updateInterval])

  const [state, setState] = useState<Progress>({ position: 0, duration: 0, buffered: 0 })
  useTrackPlayerEvents([Event.PlaybackProgressUpdated], setState)

  return state
}
