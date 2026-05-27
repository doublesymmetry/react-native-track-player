import type { BackgroundEvent } from '@rntp/player';
import { useEventLogStore } from '../stores/eventLog';

export async function TaskService(event: BackgroundEvent) {
  const { type, ...payload } = event;
  useEventLogStore
    .getState()
    .addLog(type, Object.keys(payload).length > 0 ? payload : undefined);
}
