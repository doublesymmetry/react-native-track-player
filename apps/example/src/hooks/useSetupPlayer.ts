import { SetupService } from '@/services';
import { useEffect, useState } from 'react';

export function useSetupPlayer() {
  const [playerReady, setPlayerReady] = useState<boolean>(false);

  useEffect(() => {
    let unmounted = false;
    (async () => {
      await SetupService();
      if (unmounted) return;
      setPlayerReady(true);
      if (unmounted) return;
    })();
    return () => {
      unmounted = true;
    };
  }, []);
  return playerReady;
}
