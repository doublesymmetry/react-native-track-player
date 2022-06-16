package com.doublesymmetry.trackplayer.service

import android.content.Intent
import android.os.PowerManager.WakeLock
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.ReactInstanceEventListener
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.jstasks.HeadlessJsTaskContext
import java.util.concurrent.CopyOnWriteArraySet

abstract class CustomHeadlessJsTaskService: HeadlessJsTaskService() {
    private val mActiveTasks = CopyOnWriteArraySet<Int?>()
    private var sWakeLock: WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskConfig = getTaskConfig(intent)
        if (taskConfig != null) {
            startTask(taskConfig)
            return START_REDELIVER_INTENT
        }
        return START_NOT_STICKY
    }

    /**
     * Called from [.onStartCommand] to create a [HeadlessJsTaskConfig] for this intent.
     *
     * @param intent the [Intent] received in [.onStartCommand].
     * @return a [HeadlessJsTaskConfig] to be used with [.startTask], or `null` to
     * ignore this command.
     */
    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig? {
        return null
    }
//
//    /**
//     * Acquire a wake lock to ensure the device doesn't go to sleep while processing background tasks.
//     */
//    @SuppressLint("WakelockTimeout")
//    open fun acquireWakeLockNow(context: Context) {
//        if (sWakeLock == null || !sWakeLock!!.isHeld) {
//            val powerManager = Assertions.assertNotNull(context.getSystemService(POWER_SERVICE) as PowerManager)
//            sWakeLock = powerManager.newWakeLock(
//                    PowerManager.PARTIAL_WAKE_LOCK, HeadlessJsTaskService::class.java.canonicalName)
//            sWakeLock.setReferenceCounted(false)
//            sWakeLock.acquire()
//        }
//    }

//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }

    /**
     * Start a task. This method handles starting a new React instance if required.
     *
     *
     * Has to be called on the UI thread.
     *
     * @param taskConfig describes what task to start and the parameters to pass to it
     */
    override fun startTask(taskConfig: HeadlessJsTaskConfig) {
        UiThreadUtil.assertOnUiThread()
        acquireWakeLockNow(this)
        val reactInstanceManager = reactNativeHost.reactInstanceManager
        val reactContext = reactInstanceManager.currentReactContext
        if (reactContext == null) {
            reactInstanceManager.addReactInstanceEventListener(
                    object : ReactInstanceEventListener {
                        override fun onReactContextInitialized(reactContext: ReactContext) {
                            invokeStartTask(reactContext, taskConfig)
                            reactInstanceManager.removeReactInstanceEventListener(this)
                        }
                    })
            reactInstanceManager.createReactContextInBackground()
        } else {
            invokeStartTask(reactContext, taskConfig)
        }
    }
//
    private fun invokeStartTask(reactContext: ReactContext, taskConfig: HeadlessJsTaskConfig) {
        val headlessJsTaskContext = HeadlessJsTaskContext.getInstance(reactContext)
        headlessJsTaskContext.addTaskEventListener(this)
        UiThreadUtil.runOnUiThread {
            val taskId = headlessJsTaskContext.startTask(taskConfig)
            mActiveTasks.add(taskId)
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if (reactNativeHost.hasInstance()) {
//            val reactInstanceManager = reactNativeHost.reactInstanceManager
//            val reactContext = reactInstanceManager.currentReactContext
//            if (reactContext != null) {
//                val headlessJsTaskContext = HeadlessJsTaskContext.getInstance(reactContext)
//                headlessJsTaskContext.removeTaskEventListener(this)
//            }
//        }
//        if (sWakeLock != null) {
//            sWakeLock!!.release()
//        }
//    }

//    override fun onHeadlessJsTaskStart(taskId: Int) {}

    override fun onHeadlessJsTaskFinish(taskId: Int) {
//        mActiveTasks.remove(taskId) // can we make the task last forever??
//        if (mActiveTasks.size == 0) {
//            stopSelf()  // stopSelf only works here smh
//        }
    }

    fun stop() {
        mActiveTasks.clear()
        if (mActiveTasks.size == 0) {
            stopSelf()
        }
    }

//    /**
//     * Get the [ReactNativeHost] used by this app. By default, assumes [.getApplication]
//     * is an instance of [ReactApplication] and calls [ ][ReactApplication.getReactNativeHost]. Override this method if your application class does not
//     * implement `ReactApplication` or you simply have a different mechanism for storing a
//     * `ReactNativeHost`, e.g. as a static field somewhere.
//     */
//    override fun getReactNativeHost(): ReactNativeHost {
//        return (application as ReactApplication).reactNativeHost
//    }
}