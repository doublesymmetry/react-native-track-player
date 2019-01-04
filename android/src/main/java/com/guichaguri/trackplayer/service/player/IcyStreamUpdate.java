package com.guichaguri.trackplayer.service.player;

import android.util.Log;
import android.os.Bundle;

import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.MusicService;
import com.guichaguri.trackplayer.service.Utils;

import saschpe.exoplayer2.ext.icy.*;


public class IcyStreamUpdate {

    private static MusicService service = null;

    public static void setService(MusicService service) {
        IcyStreamUpdate.service = service;
    }
    
    public static IcyHttpDataSource.IcyHeadersListener HeaderUpdate = new IcyHttpDataSource.IcyHeadersListener() {
        @Override
        public void onIcyHeaders(IcyHttpDataSource.IcyHeaders icyHeaders) {
            Log.d(Utils.LOG, "HeaderUpdate: " + icyHeaders.toString());  
            if(IcyStreamUpdate.service != null) {
                
                Bundle bundle = new Bundle();
                bundle.putInt("bitRate", icyHeaders.getBitRate());
                bundle.putString("genre", icyHeaders.getGenre());
                bundle.putString("name", icyHeaders.getName());
                bundle.putString("url", icyHeaders.getUrl());
                bundle.putBoolean("public", icyHeaders.isPublic());
                IcyStreamUpdate.service.emit(MusicEvents.ICY_HEADER_UPDATE, bundle);   
            }  
        }
    };
    
    public static IcyHttpDataSource.IcyMetadataListener MetaDataUpdate = new IcyHttpDataSource.IcyMetadataListener() {
        @Override
        public void onIcyMetaData(IcyHttpDataSource.IcyMetadata icyMetadata) {
            Log.d(Utils.LOG, "MetaData Update! " + icyMetadata.toString());  
            if(IcyStreamUpdate.service != null) {
                
                Bundle bundle = new Bundle();
                bundle.putString("streamTitle", icyMetadata.getStreamTitle());
                bundle.putString("streamUrl", icyMetadata.getStreamUrl());
                IcyStreamUpdate.service.emit(MusicEvents.ICY_MATADATA_UPDATE, bundle);      
            }    
        }
    };
}