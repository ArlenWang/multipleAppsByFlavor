package com.smarola.main

import android.view.View
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.main.DemoUi.action
import com.smarola.main.DemoUi.infoCard

class RnDemoActivity : BaseBottomTabActivity() {
    override val selectedTab = AppTab.RN

    override fun createPageContent(): View = DemoUi.page(this, "React Native", "RN 0.79.6 离线 Bundle 容器") {
        infoCard("共享 Native Module", "支持页面跳转、WebView、系统分享、App 配置、剪贴板、本地缓存和 Native 事件。")
        action("进入 React Native 页面") { AppNavigator.openReactNative(this@RnDemoActivity) }
    }
}
