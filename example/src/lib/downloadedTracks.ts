import { NativeModules } from 'react-native';

const { LocalAssetHelper } = NativeModules;

export async function ensureDownloadedTrack(
  resourceName: string,
  ext: string,
): Promise<string | null> {
  try {
    const uri: string = await LocalAssetHelper.copyToDocuments(
      resourceName,
      ext,
    );
    return uri;
  } catch (err) {
    console.warn(
      `[downloadedTracks] failed to copy ${resourceName}.${ext}:`,
      err,
    );
    return null;
  }
}
