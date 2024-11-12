package com.doublesymmetry.kotlinaudio.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.OutputStream

fun isUriLocalFile(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme
    val host = uri.host
    if ((scheme == "http" || scheme == "https") &&
                    (host == "localhost" || host == "127.0.0.1" || host == "[::1]")
    ) {
        return false
    }
    return scheme == null ||
            scheme == ContentResolver.SCHEME_FILE ||
            scheme == ContentResolver.SCHEME_ANDROID_RESOURCE ||
            scheme == ContentResolver.SCHEME_CONTENT ||
            scheme == "res" ||
            host == null
}

const val bitmapCoverFileName = "APMCover.png"
val bitmapCoverDir = "${Environment.DIRECTORY_PICTURES}/APMCache/"

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("Range")
fun findAPMCacheApi(
        contentResolver: ContentResolver,
        contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
): Uri? {
    val selection = "${MediaStore.MediaColumns.RELATIVE_PATH}=?"
    val selectionArgs = arrayOf(bitmapCoverDir)
    val cursor = contentResolver.query(contentUri, null, selection, selectionArgs, null)
    if (cursor != null && cursor.count > 0) {
        while (cursor.moveToNext()) {
            val filename =
                    cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
            if (filename == bitmapCoverFileName) {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                cursor.close()
                return ContentUris.withAppendedId(contentUri, id)
            }
        }
        cursor.close()
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.R)
fun getAPMCacheBitmapUri(contentResolver: ContentResolver, contentValues: ContentValues): Uri? {
    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val mAPMUri = findAPMCacheApi(contentResolver, contentUri)
    if (mAPMUri != null) contentResolver.delete(mAPMUri, null)
    return contentResolver.insert(contentUri, contentValues)
}

fun getEmbeddedBitmapArray(path: String?): ByteArray? {
    if (path == null) return null
    val mmr = MediaMetadataRetriever()
    try {
        mmr.setDataSource(path)
        return mmr.embeddedPicture
    } catch (e: Exception) {
        return null
    }
}

fun getEmbeddedBitmap(path: String?): Bitmap? {
    val artworkData = getEmbeddedBitmapArray(path)
    return if (artworkData == null) null
    else BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size)
}

var cacheKeyG = ""
// HACK: this will insert a png file at pictures/APMCache/APMCover.png.
fun saveMediaCoverToPng(
        path: String?,
        contentResolver: ContentResolver,
        cacheKey: String
): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (cacheKey == cacheKeyG) {
            return findAPMCacheApi(contentResolver).toString()
        }
        val contentValues =
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, bitmapCoverFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, bitmapCoverDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
        val imageUri = getAPMCacheBitmapUri(contentResolver, contentValues)
        val bitmap = getEmbeddedBitmap(path) ?: return null
        cacheKeyG = cacheKey
        var fos: OutputStream?

        contentResolver.also { resolver -> fos = imageUri?.let { resolver.openOutputStream(it) } }
        // TODO: add error handling
        if (imageUri == null) return null
        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 70, it) }
        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        contentResolver.update(imageUri, contentValues, null, null)
        return imageUri.toString()
    }
    return null
    // TODO: does this insert without overwriting?
    // return MediaStore.Images.Media.insertImage(contentResolver, bitmap,
    //   bitmapCoverFileName,"APMCover");
}
