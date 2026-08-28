package com.smarola.main

import android.view.View
import com.smarola.core.AppConfig
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.main.DemoUi.action
import com.smarola.main.DemoUi.infoCard

class HomeActivity : BaseBottomTabActivity() {
    override val selectedTab = AppTab.HOME

    override fun createPageContent(): View {
        val config = AppConfig.from(this)
        return DemoUi.page(this, config.appName, "多 App 架构演示底座") {
            infoCard("当前构建", "Flavor: ${config.flavor}\n包名: ${config.packageName}\nScheme: ${config.scheme}://home")
            infoCard("模块化能力", "Native Activity 底部导航、通用 WebView、React Native Bridge 与共享平台工具已解耦。")
            action("打开 WebView 示例") { AppNavigator.openTab(this@HomeActivity, AppTab.WEB) }
            action("打开 React Native 示例") { AppNavigator.openTab(this@HomeActivity, AppTab.RN) }
        }
    }
}
