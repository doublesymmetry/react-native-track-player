export type SleepTimer =
| { time: number }
| { sleepWhenPlayedToEnd: boolean }
| null

export type SleepTimerChangedEvent = SleepTimer;
