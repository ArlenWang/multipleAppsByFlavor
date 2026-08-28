如果使用“单 App + Product Flavor”，主要通过以下六种方式区分不同 App：

1. `BuildConfig` 区分代码逻辑
2. Flavor Source Set 区分代码
3. Flavor Source Set 区分资源
4. `manifestPlaceholders` 和 Flavor Manifest 区分 Manifest
5. `demoAImplementation/demoBImplementation` 引入不同模块或 SDK
6. 为不同 Flavor 提供不同接口实现

假设只有一个 `app` Application 模块：

```
app
├── src/main
├── src/demoA
└── src/demoB
```

## 1. 定义 Product Flavor

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace "com.smarola.app"
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

            buildConfigField "String", "APP_TYPE", '"demoA"'
            buildConfigField "String", "DEFAULT_WEB_URL", '"https://a.example.com"'
            buildConfigField "boolean", "RN_ENABLED", "true"

            resValue "string", "app_name", '"Smarola A"'
            resValue "color", "brand_primary", "#6750A4"

            manifestPlaceholders = [
                appScheme: "smarolaa",
                appHost  : "home"
            ]
        }

        demoB {
            dimension "brand"

            applicationId "com.smarola.demo.b"
            versionNameSuffix "-b"

            buildConfigField "String", "APP_TYPE", '"demoB"'
            buildConfigField "String", "DEFAULT_WEB_URL", '"https://b.example.com"'
            buildConfigField "boolean", "RN_ENABLED", "false"

            resValue "string", "app_name", '"Smarola B"'
            resValue "color", "brand_primary", "#006C4C"

            manifestPlaceholders = [
                appScheme: "smarolab",
                appHost  : "home"
            ]
        }
    }
}
```

最终产生：

```
demoADebug
demoARelease
demoBDebug
demoBRelease
```

## 2. 通过 BuildConfig 区分代码

在 `app` 模块代码中可以直接读取：

```
when (BuildConfig.APP_TYPE) {
    "demoA" -> {
        // A 的逻辑
    }

    "demoB" -> {
        // B 的逻辑
    }
}
```

也可以使用：

```
if (BuildConfig.RN_ENABLED) {
    AppNavigator.openReactNative(this)
}
```

或者：

```
AppNavigator.openWebView(
    context = this,
    url = BuildConfig.DEFAULT_WEB_URL
)
```

还可以读取自动生成的 Flavor 名称：

```
val flavor = BuildConfig.FLAVOR
```

值分别为：

```
demoA
demoB
```

注意：`core/main/webview` 等 Library 模块不能直接引用 `app.BuildConfig`，因为 Library 不依赖 Application。公共模块需要通过参数、接口、资源或 Manifest Metadata 获取配置。

## 3. 通过 Source Set 区分代码

目录结构：

```
app/src
├── main
│   └── java/com/smarola/app/
│       └── MainActivity.kt
├── demoA
│   └── java/com/smarola/app/
│       └── AppBehaviorImpl.kt
└── demoB
    └── java/com/smarola/app/
        └── AppBehaviorImpl.kt
```

`demoA` 与 `demoB` 可以提供同包名、同类名，但一次构建只能选择一个 Flavor，所以不会冲突。

公共接口：

```
// app/src/main/java/com/smarola/app/AppBehavior.kt
interface AppBehavior {
    fun homeTabs(): List<AppTab>
    fun initializeSdk()
}
```

A 的实现：

```
// app/src/demoA/java/com/smarola/app/AppBehaviorImpl.kt
class AppBehaviorImpl : AppBehavior {
    override fun homeTabs() = listOf(
        AppTab.HOME,
        AppTab.WEB,
        AppTab.RN,
        AppTab.PROFILE
    )

    override fun initializeSdk() {
        // 初始化 A 的 SDK
    }
}
```

B 的实现：

```
// app/src/demoB/java/com/smarola/app/AppBehaviorImpl.kt
class AppBehaviorImpl : AppBehavior {
    override fun homeTabs() = listOf(
        AppTab.HOME,
        AppTab.PROFILE
    )

    override fun initializeSdk() {
        // 初始化 B 的 SDK
    }
}
```

公共代码直接使用：

```
val behavior: AppBehavior = AppBehaviorImpl()
```

这样比到处判断 `BuildConfig.FLAVOR` 更容易维护。

## 4. 区分资源

目录：

```
app/src/main/res/             # 公共资源
app/src/demoA/res/            # A 的资源
app/src/demoB/res/            # B 的资源
```

例如：

```
app/src/demoA/res/mipmap-xxxhdpi/ic_launcher.webp
app/src/demoB/res/mipmap-xxxhdpi/ic_launcher.webp

app/src/demoA/res/drawable/splash_logo.xml
app/src/demoB/res/drawable/splash_logo.xml
```

A 的字符串：

```
<!-- app/src/demoA/res/values/strings.xml -->
<resources>
    <string name="app_name">Smarola A</string>
    <string name="customer_service">A 客服</string>
</resources>
```

B：

```
<!-- app/src/demoB/res/values/strings.xml -->
<resources>
    <string name="app_name">Smarola B</string>
    <string name="customer_service">B 客服</string>
</resources>
```

代码不需要判断 Flavor：

```
titleView.setText(R.string.app_name)
```

构建 A 时使用 A 资源，构建 B 时使用 B 资源。

适合区分：

* 名称
* 图标
* 启动图
* 颜色
* 字体
* Tab 图标
* 文案
* XML 配置
* Raw/Assets 文件

## 5. 区分 Manifest

公共 Manifest：

```
<!-- app/src/main/AndroidManifest.xml -->
<application
    android:name=".AppApplication"
    android:label="@string/app_name">

    <activity
        android:name=".MainActivity"
        android:exported="true">

        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />

            <data
                android:scheme="${appScheme}"
                android:host="${appHost}" />
        </intent-filter>
    </activity>
</application>
```

构建时：

```
demoA → smarolaa://home
demoB → smarolab://home
```

如果差异较大，可以创建独立 Manifest：

```
app/src/demoA/AndroidManifest.xml
app/src/demoB/AndroidManifest.xml
```

例如 A 独有相机权限：

```
<!-- app/src/demoA/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
</manifest>
```

B 不声明这个权限，最终 B 的 APK 就没有相机权限。

还可以区分：

* Activity
* Service
* Receiver
* Provider
* 权限
* Deep Link
* 网络安全配置
* 第三方 SDK Metadata

## 6. 引入不同模块

Flavor 可以使用专属 Dependency Configuration。

```
dependencies {
    implementation project(':core')
    implementation project(':main')

    demoAImplementation project(':feature-a')
    demoAImplementation project(':feature-payment')

    demoBImplementation project(':feature-b')
    demoBImplementation project(':feature-customer-service')
}
```

构建结果：

```
demoA APK
├── core
├── main
├── feature-a
└── feature-payment
```

```
demoB APK
├── core
├── main
├── feature-b
└── feature-customer-service
```

第三方 SDK 也一样：

```
dependencies {
    demoAImplementation "com.example:a-sdk:1.0.0"
    demoBImplementation "com.example:b-sdk:2.0.0"
}
```

不属于当前 Flavor 的模块和 SDK 不会打进 APK。

## 7. 不同 Application 初始化

可以让不同 Flavor 提供同名 Application：

```
app/src/demoA/java/com/smarola/app/AppApplication.kt
app/src/demoB/java/com/smarola/app/AppApplication.kt
```

A：

```
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CommonInitializer.initialize(this)
        DemoASdk.initialize(this)
    }
}
```

B：

```
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CommonInitializer.initialize(this)
        DemoBSdk.initialize(this)
    }
}
```

Manifest 统一引用：

```
<application android:name=".AppApplication" />
```

需要注意：此时 `src/main` 中不能再定义同名 `AppApplication`。

更推荐公共基类：

```
// src/main
abstract class BaseAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CommonInitializer.initialize(this)
    }
}
```

各 Flavor 继承它。

## 8. 区分 Assets 和 RN Bundle

可以分别保存：

```
app/src/demoA/assets/index.android.bundle
app/src/demoB/assets/index.android.bundle
```

或者：

```
app/src/demoA/assets/app-config.json
app/src/demoB/assets/app-config.json
```

公共代码读取同一文件名：

```
assets.open("app-config.json")
```

构建不同 Flavor 时会自动读取对应内容。

如果 RN 只是少量区别，建议共用 Bundle，通过 Native 初始参数传递：

```
Bundle().apply {
    putString("appType", BuildConfig.APP_TYPE)
}
```

## 9. 区分签名、混淆和版本

```
android {
    signingConfigs {
        demoARelease {
            storeFile file(demoAStoreFile)
            storePassword demoAStorePassword
            keyAlias demoAKeyAlias
            keyPassword demoAKeyPassword
        }

        demoBRelease {
            storeFile file(demoBStoreFile)
            storePassword demoBStorePassword
            keyAlias demoBKeyAlias
            keyPassword demoBKeyPassword
        }
    }

    productFlavors {
        demoA {
            signingConfig signingConfigs.demoARelease
        }

        demoB {
            signingConfig signingConfigs.demoBRelease
        }
    }
}
```

也可以设置不同版本：

```
demoA {
    versionCode 10001
    versionName "1.0.1"
}

demoB {
    versionCode 20015
    versionName "2.1.5"
}
```

## 推荐使用规则


| 差异类型               | Product Flavor 实现              |
| ---------------------- | -------------------------------- |
| 少量代码判断           | `BuildConfig`                    |
| 大段代码或初始化不同   | `src/demoA`、`src/demoB`同名实现 |
| 名称、图标、主题、文案 | Flavor Resources                 |
| 权限、组件、Deep Link  | Flavor Manifest                  |
| 不同业务模块           | `demoAImplementation`            |
| 不同第三方 SDK         | Flavor Dependency                |
| 不同 RN 内容           | Flavor Assets 或初始参数         |
| 不同签名、版本         | Flavor Gradle 配置               |

最推荐的组合是：

```
配置差异       → BuildConfig / resValue
资源差异       → src/demoA/res
代码行为差异   → 公共接口 + src/demoA、src/demoB 实现
完整业务差异   → demoAImplementation / demoBImplementation
Manifest 差异 → src/demoA/AndroidManifest.xml
```

这样既能保持单一 App 模块，又能避免公共代码里出现大量 Flavor 判断。
