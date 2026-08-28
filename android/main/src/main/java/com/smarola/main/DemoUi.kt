package com.smarola.main

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.smarola.core.dp

internal object DemoUi {
    fun page(context: Context, title: String, subtitle: String, block: LinearLayout.() -> Unit): View =
        ScrollView(context).apply {
            isFillViewport = true
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(context.dp(24), context.dp(28), context.dp(24), context.dp(28))
                addView(TextView(context).apply {
                    text = title
                    textSize = 28f
                    setTextColor(Color.rgb(30, 30, 36))
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(context).apply {
                    text = subtitle
                    textSize = 15f
                    setTextColor(Color.GRAY)
                    setPadding(0, context.dp(8), 0, context.dp(20))
                })
                block()
            })
        }

    fun LinearLayout.infoCard(title: String, body: String) {
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(18), context.dp(16), context.dp(18), context.dp(16))
            background = android.graphics.drawable.GradientDrawable().apply {
                color = android.content.res.ColorStateList.valueOf(Color.WHITE)
                cornerRadius = context.dp(16).toFloat()
            }
            elevation = context.dp(2).toFloat()
            addView(TextView(context).apply {
                text = title
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.rgb(40, 40, 48))
            })
            addView(TextView(context).apply {
                text = body
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, context.dp(8), 0, 0)
                gravity = Gravity.START
            })
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = context.dp(14)
        })
    }

    fun LinearLayout.action(label: String, onClick: () -> Unit) {
        addView(Button(context).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = context.dp(10)
        })
    }
}
