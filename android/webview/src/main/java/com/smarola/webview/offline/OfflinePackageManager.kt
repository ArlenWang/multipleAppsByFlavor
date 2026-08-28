package com.smarola.webview.offline

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream

/** Installs trusted ZIPs into app-private storage and maps their HTTPS URLs to local files. */
object OfflinePackageManager {
    const val DEMO_URL = "https://offline.smarola.local/demo/index.html"

    private const val DEMO_MANIFEST = "offline_demo/manifest.json"
    private const val MAX_ENTRIES = 512
    private const val MAX_UNCOMPRESSED_BYTES = 20L * 1024L * 1024L
    private val packages = ConcurrentHashMap<String, InstalledPackage>()

    @Synchronized
    fun installBundledDemo(context: Context): Result<Unit> = runCatching {
        val json = context.assets.open(DEMO_MANIFEST).bufferedReader().use { it.readText() }
        val data = JSONObject(json)
        val spec = PackageSpec(
            packageId = data.getString("packageId"),
            version = data.getString("version"),
            archive = data.getString("archive"),
            origin = data.getString("origin").trimEnd('/'),
            basePath = normalizeBasePath(data.getString("basePath")),
            sha256 = data.getString("sha256").lowercase(Locale.US)
        )
        require(spec.packageId.matches(Regex("[A-Za-z0-9._-]+"))) { "非法 packageId" }
        require(spec.version.matches(Regex("[A-Za-z0-9._-]+"))) { "非法 version" }

        val archiveAsset = "offline_demo/${spec.archive}"
        val actualHash = context.assets.open(archiveAsset).use(::sha256)
        require(actualHash == spec.sha256) { "离线包 SHA-256 校验失败" }

        val root = File(context.filesDir, "offline-packages/${spec.packageId}")
        val installDir = File(root, spec.version)
        val marker = File(installDir, ".sha256")
        if (!installDir.isDirectory || marker.readTextOrNull() != spec.sha256) {
            val staging = File(root, ".${spec.version}.installing")
            staging.deleteRecursively()
            require(staging.mkdirs()) { "无法创建离线包暂存目录" }
            try {
                context.assets.open(archiveAsset).use { safeUnzip(it, staging) }
                File(staging, ".sha256").writeText(spec.sha256)
                installDir.deleteRecursively()
                require(staging.renameTo(installDir)) { "无法提交离线包安装" }
            } catch (error: Throwable) {
                staging.deleteRecursively()
                throw error
            }
        }
        packages[spec.key] = InstalledPackage(spec, installDir)
    }

    fun intercept(request: WebResourceRequest): WebResourceResponse? {
        if (!request.method.equals("GET", ignoreCase = true)) return null
        val uri = request.url ?: return null
        val installed = packages.values.firstOrNull { it.spec.matches(uri) } ?: return null
        val relativePath = installed.spec.relativePath(uri) ?: return null
        val resource = File(installed.directory, relativePath)
        val rootPath = installed.directory.canonicalPath + File.separator
        if (!resource.isFile || !resource.canonicalPath.startsWith(rootPath)) return null

        val mimeType = mimeType(resource.name)
        val encoding = if (mimeType.startsWith("text/") || mimeType.contains("json") || mimeType.contains("javascript")) "UTF-8" else null
        return WebResourceResponse(
            mimeType,
            encoding,
            200,
            "OK",
            mapOf("Cache-Control" to "no-cache", "X-Smarola-Offline" to installed.spec.version),
            resource.inputStream().buffered()
        )
    }

    private fun safeUnzip(input: InputStream, destination: File) {
        val destinationPath = destination.canonicalPath + File.separator
        var entries = 0
        var totalBytes = 0L
        ZipInputStream(input.buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries += 1
                require(entries <= MAX_ENTRIES) { "离线包文件数量超限" }
                val output = File(destination, entry.name)
                require(output.canonicalPath.startsWith(destinationPath)) { "离线包包含越界路径" }
                if (entry.isDirectory) {
                    require(output.mkdirs() || output.isDirectory) { "无法创建目录" }
                } else {
                    require(output.parentFile?.let { it.mkdirs() || it.isDirectory } == true) { "无法创建父目录" }
                    FileOutputStream(output).use { fileOutput ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val count = zip.read(buffer)
                            if (count < 0) break
                            totalBytes += count
                            require(totalBytes <= MAX_UNCOMPRESSED_BYTES) { "离线包解压大小超限" }
                            fileOutput.write(buffer, 0, count)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
        require(entries > 0) { "离线包为空" }
    }

    private fun sha256(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun normalizeBasePath(path: String): String = "/" + path.trim('/').let { if (it.isEmpty()) "" else "$it/" }

    private fun File.readTextOrNull(): String? = runCatching { if (isFile) readText().trim() else null }.getOrNull()

    private fun mimeType(name: String): String = when (name.substringAfterLast('.', "").lowercase(Locale.US)) {
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        "js", "mjs" -> "text/javascript"
        "json" -> "application/json"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        else -> "application/octet-stream"
    }

    private data class PackageSpec(
        val packageId: String,
        val version: String,
        val archive: String,
        val origin: String,
        val basePath: String,
        val sha256: String
    ) {
        val key = "$origin$basePath"
        private val originUri = Uri.parse(origin)

        fun matches(uri: Uri): Boolean =
            uri.scheme.equals(originUri.scheme, true) &&
                uri.host.equals(originUri.host, true) &&
                effectivePort(uri) == effectivePort(originUri) &&
                (uri.path == basePath.dropLast(1) || uri.path.orEmpty().startsWith(basePath))

        fun relativePath(uri: Uri): String? {
            val path = uri.path.orEmpty()
            val relative = if (path == basePath.dropLast(1) || path == basePath) "index.html" else path.removePrefix(basePath)
            if (relative.isBlank() || relative.split('/').any { it == ".." || it.contains('\\') || it.contains('\u0000') }) return null
            return relative
        }

        private fun effectivePort(uri: Uri): Int = if (uri.port >= 0) uri.port else if (uri.scheme.equals("https", true)) 443 else 80
    }

    private data class InstalledPackage(val spec: PackageSpec, val directory: File)
}
