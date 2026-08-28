package com.smarola.webview

import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity
import com.smarola.core.AppConfig
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.core.ShareManager
import com.smarola.core.toast
import org.json.JSONObject

class SmarolaJsBridge(private val activity: AppCompatActivity) {
    @JavascriptInterface
    fun close() = activity.runOnUiThread { activity.finish() }

    @JavascriptInterface
    fun showToast(message: String) = activity.runOnUiThread { activity.toast(message.take(200)) }

    @JavascriptInterface
    fun share(text: String) = activity.runOnUiThread {
        ShareManager.shareText(activity, text)
            .onFailure { activity.toast(it.message ?: "分享失败") }
    }

    @JavascriptInterface
    fun openNativePage(tab: String) = activity.runOnUiThread {
        val target = runCatching { AppTab.valueOf(tab.uppercase()) }.getOrDefault(AppTab.HOME)
        AppNavigator.openTab(activity, target)
    }

    @JavascriptInterface
    fun getAppInfo(): String = AppConfig.from(activity).let { config ->
        JSONObject()
            .put("appName", config.appName)
            .put("flavor", config.flavor)
            .put("packageName", config.packageName)
            .put("versionName", config.versionName)
            .put("versionCode", config.versionCode)
            .put("scheme", config.scheme)
            .toString()
    }
}
