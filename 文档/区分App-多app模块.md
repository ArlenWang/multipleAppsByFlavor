在当前“多 Application 模块”架构下，不同 App 常见需要区分的不只是代码和依赖，还包括资源、Manifest、初始化、签名发布、功能入口等。

## 1. 在代码里区分 App

当前项目已经通过 Manifest Metadata 注入 App 信息：

```
// app-demo-a
manifestPlaceholders = [
    appFlavor: "demoA",
    appScheme: "smarolaa"
]
```

公共代码中通过 `AppConfig` 获取：

```
val config = AppConfig.from(context)

when (config.flavor) {
    "demoA" -> {
        // A 的逻辑
    }
    "demoB" -> {
        // B 的逻辑
    }
}
```

现有实现：[AppConfig.kt](/Users/arlenwang/Desktop/smarola/ArchitectureProject/project/android/core/src/main/java/com/smarola/core/AppConfig.kt)

不过不建议大量散落：

```
if (app == "demoA") {
    // ...
} else {
    // ...
}
```

少量显示差异可以这样处理；较大的行为差异更适合接口隔离：

```
interface AppBehavior {
    fun createHomeTabs(): List<AppTab>
    fun initializeSdk()
    fun openCustomerService(context: Context)
}
```

A 提供：

```
class DemoABehavior : AppBehavior {
    override fun createHomeTabs() =
        listOf(AppTab.HOME, AppTab.WEB, AppTab.PROFILE)

    override fun initializeSdk() {
        // 初始化 A 专属 SDK
    }
}
```

B 提供：

```
class DemoBBehavior : AppBehavior {
    override fun createHomeTabs() =
        listOf(AppTab.HOME, AppTab.RN, AppTab.PROFILE)

    override fun initializeSdk() {
        // 初始化 B 专属 SDK
    }
}
```

然后在各自 Application 中注册实现，公共模块只依赖 `AppBehavior` 接口。

需要注意：公共 Library 模块不能引用 `app-demo-a.BuildConfig`，因为依赖方向是 App 依赖 Library。公共代码的 App 差异应使用：

* `AppConfig`
* Manifest Metadata
* Resources
* 接口注册
* 运行时功能配置

## 2. 引入不同模块

这是多 Application 模块最直接的优势。

例如新增：

```
feature-a
feature-b
feature-payment
feature-customer-service
```

只给 A 引入：

```
// app-demo-a/build.gradle
dependencies {
    implementation project(':app-common')
    implementation project(':feature-a')
    implementation project(':feature-payment')
}
```

只给 B 引入：

```
// app-demo-b/build.gradle
dependencies {
    implementation project(':app-common')
    implementation project(':feature-b')
    implementation project(':feature-customer-service')
}
```

这样：

* A 的 APK 不会包含 `feature-b`
* B 的 APK 不会包含 `feature-a`
* 不需要通过运行时开关隐藏无用功能
* 两个 App 可以使用不同版本的第三方 SDK

如果公共代码要跳转可选模块，不能直接引用可选模块的 Activity 类，否则公共模块必须依赖它。可以使用统一接口：

```
interface CustomerServiceLauncher {
    fun open(context: Context)
}
```

或者使用约定路由：

```
Intent().setClassName(
    context.packageName,
    "com.smarola.customer.CustomerServiceActivity"
)
```

接口注册方案更加安全、容易测试。

## 3. App 名称、图标和主题

当前使用 `resValue` 区分名称和主题色：

```
resValue "string", "app_name", '"Smarola A"'
resValue "color", "brand_primary", "#6750A4"
```

图标、启动图等复杂资源建议放到各自模块：

```
app-demo-a/src/main/res/
├── mipmap-xxxhdpi/ic_launcher.webp
├── drawable/splash_background.xml
└── values/strings.xml

app-demo-b/src/main/res/
├── mipmap-xxxhdpi/ic_launcher.webp
├── drawable/splash_background.xml
└── values/strings.xml
```

Application 模块资源会覆盖 `app-common` 中的同名资源。

常见差异包括：

* App 名称
* Logo 和桌面图标
* 启动页
* 品牌色
* 字体
* 底部菜单图标
* 默认占位图
* 文案和多语言

## 4. Manifest 差异

不同 App 经常有不同的：

* 权限
* Scheme
* Deep Link 域名
* Activity
* Service
* ContentProvider
* BroadcastReceiver
* FileProvider authority
* 第三方 SDK Metadata
* 网络安全配置

例如 A 独有相机权限：

```
<!-- app-demo-a/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
</manifest>
```

B 不声明，相应权限就不会进入 B 的 APK。

覆盖共享 Manifest 属性时使用：

```
<application
    xmlns:tools="http://schemas.android.com/tools"
    android:networkSecurityConfig="@xml/demo_a_network_security"
    tools:replace="android:networkSecurityConfig" />
```

移除共享组件：

```
<activity
    android:name="com.example.SharedActivity"
    tools:node="remove" />
```

## 5. Application 初始化

不同 App 可能初始化不同 SDK：

```
app-demo-a
└── DemoAApplication.kt

app-demo-b
└── DemoBApplication.kt
```

建议公共初始化放在基类：

```
open class BaseSmarolaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initActivityTracker()
        initCommonPlatform()
    }
}
```

A：

```
class DemoAApplication : BaseSmarolaApplication() {
    override fun onCreate() {
        super.onCreate()
        initDemoAOnlySdk()
    }
}
```

B：

```
class DemoBApplication : BaseSmarolaApplication() {
    override fun onCreate() {
        super.onCreate()
        initDemoBOnlySdk()
    }
}
```

## 6. 首页和底部菜单

不同 App 经常拥有不同 Tab：

```
interface TabProvider {
    fun tabs(): List<AppTab>
}
```

A：

```
listOf(
    AppTab.HOME,
    AppTab.WEB,
    AppTab.PROFILE
)
```

B：

```
listOf(
    AppTab.HOME,
    AppTab.RN,
    AppTab.PROFILE
)
```

还可以区分：

* 默认首页
* Tab 数量和顺序
* Tab 名称与图标
* 某个 Tab 使用 Native、RN 还是 WebView
* Deep Link 对应页面

## 7. RN 差异

不同 App 可能区分：

* RN Component 名称
* RN Bundle
* Native Module
* 首屏参数
* 是否启用 RN
* RN 页面列表

例如：

```
app-demo-a/src/main/assets/demo_a.android.bundle
app-demo-b/src/main/assets/demo_b.android.bundle
```

或者共用 Bundle，通过 Initial Properties 传递 App 信息：

```
val initialProperties = Bundle().apply {
    putString("appFlavor", AppConfig.from(context).flavor)
    putString("appName", AppConfig.from(context).appName)
}
```

JS 中：

```
function App({appFlavor}) {
  if (appFlavor === 'demoA') {
    return <DemoAHome />;
  }

  return <DemoBHome />;
}
```

如果业务差异很大，使用不同 Bundle；如果只是主题、入口和少量功能差异，共用 Bundle 更好。

## 8. WebView 差异

常见差异包括：

* 默认域名
* JS Bridge 白名单
* User-Agent
* Cookie 域
* Deep Link
* 允许访问的 Host
* 下载能力
* 是否允许文件选择

例如配置允许的域名：

```
manifestPlaceholders = [
    defaultWebUrl: "https://a.example.com",
    trustedWebHost: "a.example.com"
]
```

公共 WebView 根据 `AppConfig` 判断当前 App 的安全白名单。

## 9. 签名和发布

每个 App 通常有独立：

* 签名文件
* applicationId
* versionCode/versionName
* APK/AAB 名称
* Google Play/应用市场账号
* 混淆规则
* Mapping 文件
* Firebase 配置

例如：

```
// app-demo-a
signingConfigs {
    release {
        storeFile file(signingProperties["demoAStoreFile"])
    }
}
```

签名密码应放在未提交的配置或 CI Secret 中，不能直接写进仓库。

## 10. 第三方服务配置

经常需要区分：

* Firebase `google-services.json`
* 推送 App ID
* 地图 Key
* 分享平台 App ID
* 统计平台
* 崩溃监控
* 支付渠道
* 客服 SDK

可以分别放置：

```
app-demo-a/google-services.json
app-demo-b/google-services.json
```

不同 App 模块应用不同插件和依赖，互不污染。

## 建议分类

对于当前工程，可以按差异大小选择实现方式：


| 差异                    | 推荐实现                                  |
| ----------------------- | ----------------------------------------- |
| 名称、颜色、URL、Scheme | Resource、Manifest Placeholder、AppConfig |
| 少量代码分支            | `AppConfig.flavor`                        |
| 首页、Tab、初始化行为   | 公共接口 + App 实现                       |
| 独立业务功能            | App 模块引入不同 feature 模块             |
| 第三方 SDK              | 各 Application 模块独立依赖和初始化       |
| 权限、组件、Deep Link   | App 模块自己的 Manifest                   |
| 图标、启动图、文案      | App 模块自己的 Resources                  |
| RN 少量差异             | Initial Properties                        |
| RN 业务完全不同         | 独立 Bundle                               |
| 签名、版本、发布        | 各 Application 模块独立配置               |

最重要的原则是：简单配置差异使用 `AppConfig/资源`，行为差异使用接口，完整业务差异使用独立模块。这样不会让公共代码充满 `if (demoA)`。
