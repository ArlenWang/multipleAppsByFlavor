package com.smarola.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

object ShareManager {
    fun shareText(context: Context, text: String, title: String? = null): Result<Unit> = runCatching {
        require(text.isNotBlank()) { "Share text cannot be blank" }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            title?.takeIf { it.isNotBlank() }?.let { putExtra(Intent.EXTRA_TITLE, it) }
        }
        context.startActivity(Intent.createChooser(sendIntent, title).withNewTask(context))
    }

    fun shareImage(context: Context, uriValue: String, text: String? = null): Result<Unit> = runCatching {
        require(uriValue.isNotBlank()) { "Image uri cannot be blank" }
        val parsed = uriValue.toUri()
        val shareUri = when (parsed.scheme) {
            "content" -> parsed
            "file", null -> FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(parsed.path ?: uriValue)
            )
            else -> error("Only content:// and local file paths can be shared as images")
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            text?.takeIf { it.isNotBlank() }?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, null).withNewTask(context))
    }

    private fun Intent.withNewTask(context: Context): Intent = apply {
        if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
