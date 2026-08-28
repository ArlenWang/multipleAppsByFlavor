package com.rnkotlin.rnhome

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.smarola.core.AppConfig
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.core.ClipboardUtils
import com.smarola.core.Preferences
import com.smarola.core.ShareManager

class NativeModule internal constructor(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext){
    private val preferences by lazy { Preferences(reactContext) }

    override fun getName(): String {
        return "ConnectNativeModule"
    }

    @ReactMethod
    fun sendMessageToNative(rnMessage: String?) {
        Log.d("This log is from java", rnMessage ?: "")
        reactApplicationContext?.runOnUiQueueThread {
            Toast.makeText(reactContext,rnMessage,Toast.LENGTH_SHORT).show()
        }
    }

    @ReactMethod
    fun sendCallbackToNative(rnCallback: Callback) {
        rnCallback.invoke("A greeting from java")
    }

    @ReactMethod
    fun finishActivity() {
        currentActivity?.finish()
    }

    @ReactMethod
    fun showToast(message: String) {
        reactContext.runOnUiQueueThread {
            Toast.makeText(reactContext, message.take(200), Toast.LENGTH_SHORT).show()
        }
    }

    @ReactMethod
    fun openWebView(url: String, title: String?, promise: Promise) {
        reactContext.runOnUiQueueThread {
            runCatching { AppNavigator.openWebView(currentActivity ?: reactContext, url = url, title = title) }
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("OPEN_WEB_FAILED", it) }
        }
    }

    @ReactMethod
    fun openNativePage(tab: String, promise: Promise) {
        reactContext.runOnUiQueueThread {
            runCatching {
                val target = AppTab.valueOf(tab.uppercase())
                AppNavigator.openTab(currentActivity ?: reactContext, target)
            }.onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("OPEN_PAGE_FAILED", it) }
        }
    }

    @ReactMethod
    fun shareText(text: String, title: String?, promise: Promise) {
        reactContext.runOnUiQueueThread {
            ShareManager.shareText(currentActivity ?: reactContext, text, title)
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("SHARE_FAILED", it) }
        }
    }

    @ReactMethod
    fun shareImage(uri: String, text: String?, promise: Promise) {
        reactContext.runOnUiQueueThread {
            ShareManager.shareImage(currentActivity ?: reactContext, uri, text)
                .onSuccess { promise.resolve(null) }
                .onFailure { promise.reject("SHARE_IMAGE_FAILED", it) }
        }
    }

    @ReactMethod
    fun getAppInfo(promise: Promise) {
        runCatching {
            val config = AppConfig.from(reactContext)
            Arguments.createMap().apply {
                putString("appName", config.appName)
                putString("flavor", config.flavor)
                putString("packageName", config.packageName)
                putString("versionName", config.versionName)
                putDouble("versionCode", config.versionCode.toDouble())
                putString("scheme", config.scheme)
                putString("defaultWebUrl", config.defaultWebUrl)
            }
        }.onSuccess(promise::resolve)
            .onFailure { promise.reject("APP_INFO_FAILED", it) }
    }

    @ReactMethod
    fun copyToClipboard(value: String) = ClipboardUtils.copy(reactContext, "React Native", value)

    @ReactMethod
    fun getClipboardText(promise: Promise) = promise.resolve(ClipboardUtils.read(reactContext))

    @ReactMethod
    fun putString(key: String, value: String) = preferences.putString(key, value)

    @ReactMethod
    fun getString(key: String, fallback: String?, promise: Promise) =
        promise.resolve(preferences.getString(key, fallback.orEmpty()))

    @ReactMethod
    fun putBoolean(key: String, value: Boolean) = preferences.putBoolean(key, value)

    @ReactMethod
    fun getBoolean(key: String, fallback: Boolean, promise: Promise) =
        promise.resolve(preferences.getBoolean(key, fallback))

    @ReactMethod
    fun putInt(key: String, value: Int) = preferences.putInt(key, value)

    @ReactMethod
    fun getInt(key: String, fallback: Int, promise: Promise) =
        promise.resolve(preferences.getInt(key, fallback))

    @ReactMethod
    fun removeCache(key: String) = preferences.remove(key)

    @ReactMethod
    fun emitNativeEvent(name: String, payload: String) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(name, payload)
    }

}
