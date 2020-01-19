package com.guichaguri.trackplayer.service.Tasks.CacheTasks;

import android.os.AsyncTask;

import com.facebook.react.bridge.Promise;



public class DropCacheTask extends AsyncTask<DropCacheParams, Void, Void> {

    Promise callback;

    @Override
    protected Void doInBackground(DropCacheParams... params) {
        callback = params[0].callback;
        try {
            params[0].cache.release();
        } catch (Exception e) {
            callback.reject(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void results) {
        callback.resolve(null);
    }

}

