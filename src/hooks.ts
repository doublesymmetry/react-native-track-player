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

const useInterval = (callback: () => void, delay: number) => {
  const savedCallback = useRef<() => void>()

  useEffect(() => {
    savedCallback.current = callback
  })

  useEffect(() => {
    if (!delay) return
    const id = setInterval(savedCallback.current, delay)
    return () => clearInterval(id)
  }, [delay])
}

const useWhenPlaybackStateChanges = (callback: (state: State) => void) => {
  useTrackPlayerEvents([Event.PlaybackState], ({ state }) => {
    callback(state)
  })
  useEffect(() => {
    let didCancel = false
    const fetchPlaybackState = async () => {
      const playbackState = await TrackPlayer.getState()
      if (!didCancel) {
        callback(playbackState)
      }
    }
    fetchPlaybackState()
    return () => {
      didCancel = true
    }
  }, [])
}

const usePlaybackStateIs = (...states: State[]) => {
  const [is, setIs] = useState()
  useWhenPlaybackStateChanges(state => {
    setIs(states.includes(state))
  })

  return is
}

/**
 * Poll for track progress for the given interval (in miliseconds)
 * @param interval - ms interval
 */
export const useTrackPlayerProgress = (interval: number = 1000) => {
  const initialState = {
    position: 0,
    bufferedPosition: 0,
    duration: 0,
  }

  const [state, setState] = useState(initialState)
  const needsPoll = usePlaybackStateIs(State.Playing, State.Buffering)

  const getProgress = async () => {
    if (!needsPoll) return
    const [position, bufferedPosition, duration] = await Promise.all([
      TrackPlayer.getPosition(),
      TrackPlayer.getBufferedPosition(),
      TrackPlayer.getDuration(),
    ])
    setState({ position, bufferedPosition, duration })
  }

  useInterval(getProgress, interval)
  return state
}
