package com.rnkotlin.rnhome
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class SimpleNativeText : SimpleViewManager<TextView>() {

    override fun getName(): String {
        return "SimpleText"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): TextView {
        return TextView(reactContext).apply {
            text = "默认文本"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(32, 16, 32, 16)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.LTGRAY)
        }
    }

    // 文本内容属性
    @ReactProp(name = "text")
    fun setText(view: TextView, text: String?) {
        view.text = text ?: "默认文本"
    }

    // 文本大小属性
    @ReactProp(name = "textSize", defaultFloat = 16f)
    fun setTextSize(view: TextView, size: Float) {
        view.textSize = size
    }

    // 文本颜色属性
    @ReactProp(name = "textColor", customType = "Color")
    fun setTextColor(view: TextView, color: Int?) {
        view.setTextColor(color ?: Color.BLACK)
    }

    // 背景颜色属性
    @ReactProp(name = "backgroundColor", customType = "Color")
    fun setBackgroundColor(view: TextView, color: Int?) {
        view.setBackgroundColor(color ?: Color.LTGRAY)
    }

    // 是否显示边框
    @ReactProp(name = "showBorder", defaultBoolean = false)
    fun setShowBorder(view: TextView, showBorder: Boolean) {
        if (showBorder) {
            view.setBackgroundResource(android.R.drawable.btn_default)
        } else {
            view.setBackgroundColor(Color.LTGRAY)
        }
    }

    // 对齐方式
    @ReactProp(name = "textAlign")
    fun setTextAlign(view: TextView, align: String?) {
        val gravity = when (align) {
            "left" -> Gravity.START or Gravity.CENTER_VERTICAL
            "right" -> Gravity.END or Gravity.CENTER_VERTICAL
            "center" -> Gravity.CENTER
            else -> Gravity.CENTER
        }
        view.gravity = gravity
    }
}