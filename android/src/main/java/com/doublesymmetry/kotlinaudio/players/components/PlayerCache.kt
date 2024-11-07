package com.doublesymmetry.kotlinaudio.players.components

import android.content.Context
import com.doublesymmetry.kotlinaudio.models.CacheConfig
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object PlayerCache {
    @Volatile
    private var instance: SimpleCache? = null

    fun getInstance(context: Context, cacheConfig: CacheConfig): SimpleCache? {
        val cacheDir = File(context.cacheDir, cacheConfig.identifier)
        val db: DatabaseProvider = StandaloneDatabaseProvider(context)

        instance ?: synchronized(this) {
            instance ?: SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(cacheConfig.maxCacheSize ?: 0), db)
                .also { instance = it }
        }

        return instance
    }
}