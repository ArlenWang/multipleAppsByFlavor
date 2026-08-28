package com.smarola.core

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.Toast

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun View.setDebouncedClickListener(intervalMillis: Long = 500, action: (View) -> Unit) {
    var lastClick = 0L
    setOnClickListener { view ->
        val now = SystemClock.elapsedRealtime()
        if (now - lastClick >= intervalMillis) {
            lastClick = now
            action(view)
        }
    }
}
