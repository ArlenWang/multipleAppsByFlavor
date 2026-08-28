package com.smarola.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {
    fun copy(context: Context, label: String, value: String) {
        manager(context).setPrimaryClip(ClipData.newPlainText(label, value))
    }

    fun read(context: Context): String = manager(context)
        .primaryClip
        ?.getItemAt(0)
        ?.coerceToText(context)
        ?.toString()
        .orEmpty()

    private fun manager(context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}
