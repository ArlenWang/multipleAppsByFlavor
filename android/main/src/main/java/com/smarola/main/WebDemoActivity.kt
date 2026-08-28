package com.smarola.main

import android.view.View
import com.smarola.core.AppConfig
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.main.DemoUi.action
import com.smarola.main.DemoUi.infoCard
import com.smarola.webview.offline.OfflinePackageManager

class WebDemoActivity : BaseBottomTabActivity() {
    override val selectedTab = AppTab.WEB

    override fun createPageContent(): View = DemoUi.page(this, "WebView", "独立、可复用且带安全边界") {
        infoCard("基础功能", "标题和进度、错误重试、返回栈、文件选择、系统下载、外部 Scheme 与可选 JS Bridge。")
        action("打开当前 Flavor 默认网页") {
            AppNavigator.openWebView(this@WebDemoActivity, url = AppConfig.from(this@WebDemoActivity).defaultWebUrl)
        }
        action("打开离线 ZIP 示例") {
            AppNavigator.openWebView(
                this@WebDemoActivity,
                url = OfflinePackageManager.DEMO_URL,
                title = "离线包 Demo"
            )
        }
        action("打开本地 JS Bridge 演示") {
            AppNavigator.openWebView(
                this@WebDemoActivity,
                html = BRIDGE_HTML,
                title = "JS Bridge Demo",
                enableBridge = true
            )
        }
    }

    companion object {
        private val BRIDGE_HTML = """
            <!doctype html><html><meta name="viewport" content="width=device-width,initial-scale=1">
            <body style="font-family:sans-serif;padding:28px;background:#f7f7fa">
            <h2>Smarola JS Bridge</h2><p>Bridge 只在调用方明确开启时注入。</p>
            <button style="padding:12px" onclick="SmarolaBridge.showToast('Hello from Web')">Native Toast</button>
            <button style="padding:12px" onclick="SmarolaBridge.share('WebView 分享示例')">系统分享</button>
            <button style="padding:12px" onclick="document.getElementById('info').innerText=SmarolaBridge.getAppInfo()">读取 App 信息</button>
            <pre id="info" style="white-space:pre-wrap"></pre></body></html>
        """.trimIndent()
    }
}
