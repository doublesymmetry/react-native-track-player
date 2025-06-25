import { useState, useEffect } from 'react';
import { AppState, type AppStateStatus } from 'react-native';

export function useAppIsInBackground() {
  const [state, setState] = useState<AppStateStatus>('active');

  useEffect(() => {
    const onStateChange = (nextState: AppStateStatus) => {
      setState(nextState);
    };

    const subscription = AppState.addEventListener('change', onStateChange);

    return () => {
      subscription.remove();
    };
  }, []);
  return state === 'background';
}
