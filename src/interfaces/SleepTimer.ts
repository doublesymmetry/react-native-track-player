export type SleepTimer =
  | { time: number }
  | { sleepWhenPlayedToEnd: boolean }
  | null;
