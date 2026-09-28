import TrackPlayer, { Event } from '@rntp/player';
import { useEventLogStore } from '../stores/eventLog';

/**
 * Process-scoped listeners registered from the playback session so they survive UI
 * unmounts and receive background deliveries through the built-in forwarder.
 */
export function registerEventLogListeners() {
  const addLog = useEventLogStore.getState().addLog;
  Object.values(Event).forEach(event => {
    TrackPlayer.addEventListener(event as any, (payload: any) => {
      addLog(event, payload ?? {});
    });
  });
}
