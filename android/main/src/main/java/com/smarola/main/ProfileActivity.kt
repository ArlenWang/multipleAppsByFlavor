package com.smarola.main

import android.view.View
import com.smarola.core.AppConfig
import com.smarola.core.AppTab
import com.smarola.core.ClipboardUtils
import com.smarola.core.Preferences
import com.smarola.core.ShareManager
import com.smarola.core.toast
import com.smarola.main.DemoUi.action
import com.smarola.main.DemoUi.infoCard

class ProfileActivity : BaseBottomTabActivity() {
    override val selectedTab = AppTab.PROFILE

    override fun createPageContent(): View {
        val config = AppConfig.from(this)
        val preferences = Preferences(this)
        return DemoUi.page(this, "我的", "无网络 API 的公共能力示例") {
            infoCard("App 信息", "${config.appName} ${config.versionName} (${config.versionCode})\n${config.packageName}")
            action("保存并读取本地缓存") {
                preferences.putString("demo_key", "来自 ${config.flavor} 的缓存")
                toast(preferences.getString("demo_key"))
            }
            action("复制 Scheme 到剪贴板") {
                ClipboardUtils.copy(this@ProfileActivity, "scheme", "${config.scheme}://home")
                toast("已复制")
            }
            action("调用系统分享") {
                ShareManager.shareText(this@ProfileActivity, "${config.appName}: ${config.scheme}://home")
                    .onFailure { toast(it.message ?: "分享失败") }
            }
        }
    }
}
