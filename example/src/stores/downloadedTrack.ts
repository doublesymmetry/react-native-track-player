import { create } from 'zustand';

interface DownloadedTrackState {
  fileUri: string | null;
  setFileUri: (uri: string | null) => void;
}

export const useDownloadedTrackStore = create<DownloadedTrackState>(set => ({
  fileUri: null,
  setFileUri: fileUri => set({ fileUri }),
}));
