# Smarola Android / React Native Architecture

This project is the reusable architecture extracted from `reference/release-all-1.7.16`.
Business APIs, private repositories, private routers, signing credentials, analytics and account SDKs are intentionally not included.

## Modules

| Module | Responsibility |
| --- | --- |
| `app` | The single Application module; `demoA/demoB` are Product Flavors |
| `core` | App config, navigation, sharing, preferences, clipboard, lifecycle and small UI utilities |
| `main` | Four demonstration Activity tabs and the shared bottom navigation |
| `webview` | Reusable WebView Activity, file chooser, downloads and opt-in JS bridge |
| `rnhome` | React Native container and shared native module |
| `rnlibrary` | Local React Native 0.79.6 Android runtime dependencies |

## Product Flavor builds

Only `app` applies `com.android.application`. After a Gradle sync, select `demoADebug` or `demoBDebug` for the `app` module in Android Studio's Build Variants window.

The included modules build with:

```bash
cd android
./gradlew :app:assembleDemoADebug
./gradlew :app:assembleDemoBDebug
```

Flavor profiles are centralized in `android/gradle/config/app-profiles.gradle`. To add an App variant, add one profile and change:

- `applicationId`
- `appName`
- `scheme`
- `primaryColor`
- `defaultWebUrl`
- `versionNameSuffix`
- `rnEnabled`

Do not store release signing passwords or service secrets in the profile file.

## Navigation

Native navigation is exposed through `AppNavigator`:

```kotlin
AppNavigator.openTab(context, AppTab.WEB)
AppNavigator.openWebView(context, url = "https://example.com")
AppNavigator.openReactNative(context)
```

The four demonstration tabs are separate Activities. `BaseBottomTabActivity` supplies the same bottom menu and prevents duplicate rapid clicks.

## WebView

The WebView module supports URL or inline HTML loading, progress, errors and retry, history navigation, file selection, DownloadManager, external schemes and cleanup.

```kotlin
AppNavigator.openWebView(context, url = "https://example.com")

AppNavigator.openWebView(
    context,
    html = ownedHtml,
    title = "Bridge demo",
    enableBridge = true
)
```

The JS interface is only injected for caller-owned inline HTML when `enableBridge` is explicitly true. It is not injected into arbitrary remote pages. Available methods are `close`, `showToast`, `share`, `openNativePage` and `getAppInfo`.

## React Native bridge

The module name remains `ConnectNativeModule` for compatibility. It provides:

- Toast, close page and Native tab navigation
- WebView navigation
- text and local-image system sharing
- app/flavor information
- clipboard helpers
- string, boolean and integer preferences
- Native-to-JS event emission

Regenerate the embedded release bundle after changing JavaScript:

```bash
npm run index-js-bundle
```

## baseCore migration policy

`baseCore 1.0.133` was used as the newer reference, especially for system sharing, file/URI handling and Activity tracking. The full library was not copied because it depends on obsolete Gradle APIs, private Maven artifacts, legacy social SDKs and unrelated business infrastructure. Reusable features were rewritten against SDK 36 and the current module boundaries.
