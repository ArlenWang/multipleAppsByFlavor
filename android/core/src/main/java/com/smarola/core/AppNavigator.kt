package com.smarola.core

import android.content.Context
import android.content.Intent

enum class AppTab(val className: String) {
    HOME("com.smarola.main.HomeActivity"),
    WEB("com.smarola.main.WebDemoActivity"),
    RN("com.smarola.main.RnDemoActivity"),
    PROFILE("com.smarola.main.ProfileActivity")
}

object AppNavigator {
    const val EXTRA_URL = "smarola.extra.URL"
    const val EXTRA_HTML = "smarola.extra.HTML"
    const val EXTRA_TITLE = "smarola.extra.TITLE"
    const val EXTRA_ENABLE_BRIDGE = "smarola.extra.ENABLE_BRIDGE"
    private const val WEBVIEW_CLASS = "com.smarola.webview.WebViewActivity"
    private const val RN_CLASS = "com.rnkotlin.rnhome.MainRnActivity"

    fun openTab(context: Context, tab: AppTab) {
        context.startActivity(componentIntent(context, tab.className).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
    }

    fun openWebView(
        context: Context,
        url: String? = null,
        html: String? = null,
        title: String? = null,
        enableBridge: Boolean = false
    ) {
        context.startActivity(componentIntent(context, WEBVIEW_CLASS).apply {
            putExtra(EXTRA_URL, url)
            putExtra(EXTRA_HTML, html)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_ENABLE_BRIDGE, enableBridge)
        })
    }

    fun openReactNative(context: Context) {
        context.startActivity(componentIntent(context, RN_CLASS))
    }

    private fun componentIntent(context: Context, className: String) =
        Intent().setClassName(context.packageName, className)
}
