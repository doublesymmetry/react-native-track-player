package za.co.digitalwaterfall.reactnativemediasuite.mediadownloader.downloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.offline.*;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.guichaguri.trackplayer.service.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

public class DownloadTracker {

    public interface Listener {
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final ReactApplicationContext context;
    private final HttpDataSource.Factory httpDataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<String, Download> downloads;
    private final HashMap<String, DownloadCred> downloadCreds;
    private final DownloadIndex downloadIndex;


    public DownloadTracker(ReactApplicationContext context, HttpDataSource.Factory httpDataSourceFactory, DownloadManager downloadManager) {
        this.context = context;
        this.httpDataSourceFactory = httpDataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadCreds = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        checkNotNull(listener);
        listeners.add(listener);
    }

    public void setDownloadCred(String downloadID, String queryParam) {
        downloadCreds.put(downloadID, new DownloadCred(queryParam));
    }

    public DownloadCred getDownloadCred(String downloadID) {
        return downloadCreds.get(downloadID);
    }

    private void emit(String event, Bundle data) {
        Intent intent = new Intent(Utils.EVENT_INTENT);
        intent.putExtra("event", event);
        if(data != null) intent.putExtra("data", data);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void onDownloadProgressEvent(String downloadID, float progress){
        Bundle params = new Bundle();
        params.putString("downloadID", downloadID);
        params.putDouble("percentComplete", progress);
        emit("onDownloadProgress", params);
    }

    public void onDownloadFinishedEvent(String downloadID, long downloadedBytes){
        Bundle params = new Bundle();
        params.putString("downloadID", downloadID);
        params.putDouble("size", downloadedBytes);
        //TODO: Add local path of downloaded file
        params.putString("downloadLocation", "N/A");
        emit("onDownloadFinished", params);
    }

    public void onDownloadCancelledEvent(String downloadID){
        Bundle params = new Bundle();
        params.putString("downloadID", downloadID);
        emit("onDownloadCancelled", params);
    }

    public void onDownloadStartedEvent(String downloadID){
        Bundle params = new Bundle();
        params.putString("downloadID", downloadID);
        emit("onDownloadStarted", params);
    }

    public void onDownloadErrorEvent(String downloadID, String errorType, String error){
        Bundle params = new Bundle();
        params.putString("error", error);
        params.putString("errorType", errorType);
        params.putString("downloadID", downloadID);
        emit("onDownloadError", params);
    }

    public Download getCurrentDownload() {
        for (Download download : DownloadUtil.getDownloadManager(context).getCurrentDownloads()) {
            if(download.state == Download.STATE_DOWNLOADING) {
                return download;
            }
        }
        return null;
    }

    public List<Download> getCurrentDownloads() {
        return DownloadUtil.getDownloadManager(context).getCurrentDownloads();
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    public long getDowloadDirectorySize() {
        File folder = DownloadUtil.getDownloadDirectory(context).getAbsoluteFile();
        return getFolderSize(folder);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(String downloadId) {
        @Nullable Download download = downloads.get(downloadId);
        return download != null && download.state != Download.STATE_FAILED;
    }


    public Download getDownload(String downloadId) {
        try {
            return downloadIndex.getDownload(downloadId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private DownloadHelper getDownloadHelper(Uri uri, String queryParams) {
        return DownloadHelper.forMediaItem(context, MediaItem.fromUri(uri), new DefaultRenderersFactory(context), DownloadUtil.getResolvingFactory(httpDataSourceFactory, queryParams));
    }


    public void toggleDownload(String downloadId, Uri uri) {
        @Nullable Download download = downloads.get(uri);

        Log.i(TAG, Integer.toString(DownloadUtil.getDownloadManager(context).getCurrentDownloads().size()));
        if(download != null && download.state != Download.STATE_FAILED){

        } else {
            String queryParams = downloadCreds.get(downloadId).queryParams;
            DownloadHelper downloadHelper = getDownloadHelper(uri, queryParams);
            new StartDownloadHelper(downloadHelper, downloadId);
        }
    }

    public void pauseDownload(String downloadId) {
        DownloadService.sendSetStopReason(context, NativeDownloadService.class, downloadId, Download.STATE_STOPPED, false);
    }

    public void resumeDownload(String downloadId) {
        DownloadService.sendSetStopReason(context, NativeDownloadService.class, downloadId, Download.STOP_REASON_NONE,/* foreground= */ false);
    }

    public void deleteDownload(String downloadId) {
        DownloadService.sendRemoveDownload(context, NativeDownloadService.class, downloadId, false);
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.id, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {

        @Override
        public void onDownloadChanged(
                @NonNull DownloadManager downloadManager,
                @NonNull Download download,
                @Nullable Exception finalException) {
            String downloadID = download.request.id;

            if(context.hasActiveCatalystInstance()){
                if(download.state == Download.STATE_COMPLETED){
                    if(downloadID != null){
                        onDownloadProgressEvent(downloadID, 100);
                        onDownloadFinishedEvent(downloadID, download.getBytesDownloaded());
                    }
                } else if (download.state == Download.STATE_DOWNLOADING){
                    if(downloadID != null) {
                        onDownloadStartedEvent(downloadID);
                    }
                } else if (download.state == Download.STATE_FAILED) {
                    if (downloadID != null) {
                        Log.e(TAG, "failed", finalException);
                        onDownloadErrorEvent(downloadID, "UNEXPECTEDLY_CANCELLED", finalException.toString());
                    }
                }
            }

            downloads.put(downloadID, download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(
                @NonNull DownloadManager downloadManager, @NonNull Download download) {
            downloads.remove(download.request.id);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onInitialized(DownloadManager downloadManager) {
            Log.i(TAG, "All downloads restored");
        }
    }


    private final class StartDownloadHelper implements DownloadHelper.Callback {

        private final DownloadHelper downloadHelper;
        private final String contentId;



        public StartDownloadHelper(DownloadHelper downloadHelper,String contentId) {
            this.downloadHelper = downloadHelper;
            this.contentId = contentId;
            downloadHelper.prepare(this);
        }

        public void release() {
            downloadHelper.release();
        }


        @Override
        public void onPrepared(DownloadHelper helper) {
            startDownload();
            release();
        }


        private void startDownload() {
            startDownload(buildDownloadRequest());
        }

        private void startDownload(DownloadRequest downloadRequest) {
            DownloadService.sendAddDownload(context, NativeDownloadService.class, downloadRequest, /* foreground= */ false);
        }

        private DownloadRequest buildDownloadRequest() {
            return downloadHelper.getDownloadRequest(contentId, null);
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Log.e(TAG,
                    e instanceof DownloadHelper.LiveContentUnsupportedException ? "Downloading live content unsupported"
                            : "Failed to start download",
                    e);
        }
    }
}