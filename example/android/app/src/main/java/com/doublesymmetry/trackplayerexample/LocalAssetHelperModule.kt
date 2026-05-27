package com.doublesymmetry.trackplayerexample

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.File

class LocalAssetHelperModule(
  reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "LocalAssetHelper"

  @ReactMethod
  fun copyToDocuments(resourceName: String, ext: String, promise: Promise) {
    try {
      val ctx = reactApplicationContext
      val resId = ctx.resources.getIdentifier(resourceName, "raw", ctx.packageName)
      if (resId == 0) {
        promise.reject("NOT_FOUND", "Raw resource '$resourceName' not found")
        return
      }

      val destFile = File(ctx.filesDir, "$resourceName.$ext")
      if (!destFile.exists()) {
        ctx.resources.openRawResource(resId).use { input ->
          destFile.outputStream().use { output -> input.copyTo(output) }
        }
      }

      promise.resolve("file://${destFile.absolutePath}")
    } catch (e: Exception) {
      promise.reject("COPY_FAILED", e.message, e)
    }
  }
}
