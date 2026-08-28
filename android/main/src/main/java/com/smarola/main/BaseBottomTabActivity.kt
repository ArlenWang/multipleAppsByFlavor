package com.smarola.main

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.smarola.core.AppNavigator
import com.smarola.core.AppTab
import com.smarola.core.dp
import com.smarola.core.setDebouncedClickListener

abstract class BaseBottomTabActivity : AppCompatActivity() {
    protected abstract val selectedTab: AppTab
    protected abstract fun createPageContent(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 247, 250))
            addView(FrameLayout(this@BaseBottomTabActivity).apply {
                addView(
                    createPageContent(),
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(createBottomBar())
        })
    }

    protected open fun onTabReselected() = Unit

    private fun createBottomBar(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        elevation = dp(10).toFloat()
        setPadding(dp(4), dp(6), dp(4), dp(6))
        setBackgroundColor(Color.WHITE)
        val labels = linkedMapOf(
            AppTab.HOME to "首页",
            AppTab.WEB to "WebView",
            AppTab.RN to "RN",
            AppTab.PROFILE to "我的"
        )
        labels.forEach { (tab, label) ->
            addView(TextView(this@BaseBottomTabActivity).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = if (tab == selectedTab) 15f else 14f
                setTextColor(if (tab == selectedTab) themePrimaryColor() else Color.DKGRAY)
                setPadding(dp(4), dp(10), dp(4), dp(10))
                setDebouncedClickListener {
                    if (tab == selectedTab) onTabReselected() else {
                        AppNavigator.openTab(this@BaseBottomTabActivity, tab)
                        overridePendingTransition(0, 0)
                    }
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    @ColorInt
    private fun themePrimaryColor(): Int {
        val out = android.util.TypedValue()
        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, out, true)
        return out.data
    }
}
