package com.doublesymmetry.kotlinaudio.players.components

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object Cache {
    @Volatile
    private var instance: SimpleCache? = null

    fun initCache(context: Context, sizeKb: Long): SimpleCache {
        val db: DatabaseProvider = StandaloneDatabaseProvider(context)

        instance ?: synchronized(this) {
            instance ?: SimpleCache(
                File(context.cacheDir, "RNTP"),
                LeastRecentlyUsedCacheEvictor(
                  sizeKb * 1000 // kb to bytes
                ),
                db
            ).also { instance = it }
        }

        return instance!!
    }
}
