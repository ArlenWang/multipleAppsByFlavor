package com.smarola.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.smarola.core.AppNavigator
import com.smarola.core.dp
import com.smarola.core.toast

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var titleView: TextView
    private lateinit var progress: ProgressBar
    private lateinit var content: FrameLayout
    private var errorView: View? = null
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        fileCallback?.onReceiveValue(uri?.let { arrayOf(it) })
        fileCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createLayout()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = handleBack()
        })

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = "$userAgentString Smarola/${packageManager.getPackageInfo(packageName, 0).versionName}"
        }
        CookieManager.getInstance().setAcceptCookie(true)
        WebView.setWebContentsDebuggingEnabled(applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0)
        webView.webViewClient = Client()
        webView.webChromeClient = ChromeClient()
        webView.setDownloadListener(createDownloadListener())

        val html = intent.getStringExtra(AppNavigator.EXTRA_HTML)
        val bridgeRequested = intent.getBooleanExtra(AppNavigator.EXTRA_ENABLE_BRIDGE, false)
        // The initial implementation only exposes native methods to caller-owned inline HTML.
        if (bridgeRequested && html != null) {
            webView.addJavascriptInterface(SmarolaJsBridge(this), "SmarolaBridge")
        }
        if (html != null) {
            webView.loadDataWithBaseURL("https://local.smarola/", html, "text/html", "UTF-8", null)
        } else {
            val url = intent.getStringExtra(AppNavigator.EXTRA_URL)
            if (url.isNullOrBlank()) showError("没有可加载的地址") else webView.loadUrl(url)
        }
    }

    private fun createLayout() {
        webView = WebView(this)
        titleView = TextView(this).apply {
            text = intent.getStringExtra(AppNavigator.EXTRA_TITLE) ?: "WebView"
            textSize = 18f
            setTextColor(Color.rgb(30, 30, 36))
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        content = FrameLayout(this).apply { addView(webView) }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
            setBackgroundColor(Color.WHITE)
            addView(Button(this@WebViewActivity).apply {
                text = "返回"
                isAllCaps = false
                setOnClickListener { handleBack() }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(52)))
            addView(titleView, LinearLayout.LayoutParams(0, dp(52), 1f))
            addView(Button(this@WebViewActivity).apply {
                text = "关闭"
                isAllCaps = false
                setOnClickListener { finish() }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(52)))
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(toolbar)
            addView(progress, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(3)))
            addView(content, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        })
    }

    private fun handleBack() {
        if (webView.canGoBack()) webView.goBack() else finish()
    }

    private fun showError(message: String) {
        errorView?.let(content::removeView)
        errorView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(247, 247, 250))
            addView(TextView(this@WebViewActivity).apply {
                text = message
                textSize = 16f
                setTextColor(Color.DKGRAY)
            })
            addView(Button(this@WebViewActivity).apply {
                text = "重新加载"
                setOnClickListener {
                    visibility = View.GONE
                    webView.reload()
                }
            })
        }.also { content.addView(it) }
    }

    private inner class Client : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            errorView?.let(content::removeView)
            errorView = null
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (intent.getStringExtra(AppNavigator.EXTRA_TITLE).isNullOrBlank()) {
                titleView.text = view?.title ?: "WebView"
            }
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (request?.isForMainFrame == true) showError(error?.description?.toString() ?: "页面加载失败")
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
            handleExternalUrl(request?.url)

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean = handleExternalUrl(url?.toUri())
    }

    private fun handleExternalUrl(uri: Uri?): Boolean {
        uri ?: return true
        if (uri.scheme == "http" || uri.scheme == "https") return false
        return runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        }.getOrElse {
            toast("没有可处理该链接的应用")
            true
        }
    }

    private inner class ChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            progress.progress = newProgress
            progress.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            if (intent.getStringExtra(AppNavigator.EXTRA_TITLE).isNullOrBlank()) titleView.text = title ?: "WebView"
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            fileCallback?.onReceiveValue(null)
            fileCallback = filePathCallback
            val type = fileChooserParams?.acceptTypes?.firstOrNull { it.isNotBlank() } ?: "*/*"
            filePicker.launch(type)
            return true
        }
    }

    private fun createDownloadListener() = DownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
        runCatching {
            val request = DownloadManager.Request(url.toUri()).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                setTitle(fileName)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
            toast("已加入下载任务")
        }.onFailure { toast(it.message ?: "下载失败") }
    }

    override fun onDestroy() {
        fileCallback?.onReceiveValue(null)
        fileCallback = null
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
}
