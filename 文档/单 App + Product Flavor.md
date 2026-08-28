目前项目已经是“多 Application 模块”。下面分别说明两种方案落到当前工程时的具体结构和实现。

## 一、单 App + Product Flavor

这种方式只有一个 `com.android.application` 模块，例如：

```
android
├── app                    # 唯一可运行模块
├── core
├── main
├── webview
├── rnhome
└── rnlibrary
```

### 1. settings.gradle

只注册一个 App：

```
include ':app'
include ':core'
include ':main'
include ':webview'
include ':rnhome'
include ':rnlibrary'
```

### 2. app/build.gradle

在同一个 `app` 中配置多个 Flavor：

```
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace 'com.smarola.app'
    compileSdk 36

    defaultConfig {
        applicationId "com.smarola.demo"
        minSdk 24
        targetSdk 36
        versionCode 1
        versionName "1.0"
    }

    flavorDimensions += "brand"

    productFlavors {
        demoA {
            dimension "brand"

            applicationId "com.smarola.demo.a"
            versionNameSuffix "-a"

            resValue "string", "app_name", '"Smarola A"'
            resValue "color", "brand_primary", "#6750A4"

            manifestPlaceholders = [
                appScheme    : "smarolaa",
                appFlavor    : "demoA",
                defaultWebUrl: "https://example.com"
            ]

            buildConfigField "String", "APP_FLAVOR", '"demoA"'
        }

        demoB {
            dimension "brand"

            applicationId "com.smarola.demo.b"
            versionNameSuffix "-b"

            resValue "string", "app_name", '"Smarola B"'
            resValue "color", "brand_primary", "#006C4C"

            manifestPlaceholders = [
                appScheme    : "smarolab",
                appFlavor    : "demoB",
                defaultWebUrl: "https://www.example.org"
            ]

            buildConfigField "String", "APP_FLAVOR", '"demoB"'
        }
    }
}

dependencies {
    implementation project(':main')
    implementation project(':core')
}
```

### 3. 生成的 Build Variants

一个 `app` 模块下会出现：

```
demoADebug
demoARelease
demoBDebug
demoBRelease
```

编译命令：

```
./gradlew :app:assembleDemoADebug
./gradlew :app:assembleDemoBDebug
```

APK 路径：

```
app/build/outputs/apk/demoA/debug/app-demoA-debug.apk
app/build/outputs/apk/demoB/debug/app-demoB-debug.apk
```

### 4. Android Studio 使用方式

运行模块始终是 `app`，但需要在 Build Variants 中选择：

```
app → demoADebug
```

或者：

```
app → demoBDebug
```

运行按钮旁一般只看到同一个 `app` 运行配置。

### 5. App 专属代码和资源

如果某个 Flavor 有自己的实现，可以放在：

```
app/src/main/                 # 全部 App 共用
app/src/demoA/                # A 专用
app/src/demoB/                # B 专用
app/src/debug/                # 全部 Debug 共用
app/src/release/              # 全部 Release 共用
app/src/demoADebug/           # A Debug 专用
```

例如：

```
app/src/demoA/res/mipmap/ic_launcher.webp
app/src/demoB/res/mipmap/ic_launcher.webp
```

同名资源会覆盖 `src/main` 中的默认资源。
