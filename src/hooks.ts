import { useEffect, useState, useRef } from 'react'
import TrackPlayer, { State, Event } from './index'

/** Get current playback state and subsequent updatates  */
export const usePlaybackState = () => {
  const [state, setState] = useState(State.None)

  useEffect(() => {
    async function setPlayerState() {
      const playerState = await TrackPlayer.getState()
      setState(playerState)
    }

    setPlayerState()

    const sub = TrackPlayer.addEventListener(Event.PlaybackState, data => {
      setState(data.state)
    })

    return () => {
      sub.remove()
    }
  }, [])

  return state
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Handler = (payload: { type: Event; [key: string]: any }) => void

/**
 * Attaches a handler to the given TrackPlayer events and performs cleanup on unmount
 * @param events - TrackPlayer events to subscribe to
 * @param handler - callback invoked when the event fires
 */
export const useTrackPlayerEvents = (events: Event[], handler: Handler) => {
  const savedHandler = useRef<Handler>()

  useEffect(() => {
    savedHandler.current = handler
  }, [handler])

  useEffect(() => {
    if (__DEV__) {
      const allowedTypes = Object.values(Event)
      const invalidTypes = events.filter(type => !allowedTypes.includes(type))
      if (invalidTypes.length) {
        console.warn(
          'One or more of the events provided to useTrackPlayerEvents is ' +
            `not a valid TrackPlayer event: ${invalidTypes.join("', '")}. ` +
            'A list of available events can be found at ' +
            'https://react-native-kit.github.io/react-native-track-player/documentation/#events',
        )
      }
    }

    const subs = events.map(event =>
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      TrackPlayer.addEventListener(event, payload => savedHandler.current!({ ...payload, type: event })),
    )

    return () => {
      subs.forEach(sub => sub.remove())
    }
  }, events)
}

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param interval - ms interval
 */
export function useProgress(updateInterval?: number) {
  const [state, setState] = useState({ position: 0, duration: 0, buffered: 0 })
  const playerState = usePlaybackState()

  const getProgress = async () => {
    const [position, duration, buffered] = await Promise.all([
      TrackPlayer.getPosition(),
      TrackPlayer.getDuration(),
      TrackPlayer.getBufferedPosition(),
    ])
    setState({ position, duration, buffered })
  }

  useEffect(() => {
    if (playerState !== State.Playing && playerState !== State.Buffering) return
    const poll = setInterval(getProgress, updateInterval || 1000)
    return () => clearInterval(poll)
  }, [playerState])

  return state
}
