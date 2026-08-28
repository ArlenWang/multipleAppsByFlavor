推荐多个 `com.android.application` 模块


| 对比项                  | 单 App + Product Flavor | 多 Application 模块    |
| ----------------------- | ----------------------- | ---------------------- |
| Android Studio 运行选择 | 先切换 Build Variant    | 运行按钮旁直接选 App   |
| 代码复用                | 很高                    | 抽到公共模块后同样很高 |
| App 差异较小时          | 最合适                  | 稍显复杂               |
| App 依赖不同            | Gradle 条件配置容易变乱 | 每个 App 独立、清晰    |
| Manifest/SDK/插件差异   | 合并和判断较多          | 天然隔离               |
| 独立签名与发布          | 可以，但配置集中        | 更直观                 |
| 编译任务数量            | Flavor × BuildType     | App 模块 × BuildType  |
| 长期维护                | 品牌差异扩大后容易臃肿  | 更适合持续分化         |

适用判断：

* 如果只是换包名、名称、图标、主题色、域名：Product Flavor 更简单。
* 如果不同 App 将来可能有不同 SDK、初始化流程、权限、Manifest、渠道、页面或发布节奏：多个 Application 模块更合适。
* 如果两者都有：可以采用“多 Application 模块 + 每个模块自己的环境 Flavor”，例如：

```
app-demo-a
├── dev
├── pre
└── production

app-demo-b
├── dev
├── pre
└── production
```

最终生成：

```
app-demo-a:assembleDevDebug
app-demo-a:assembleProductionRelease
app-demo-b:assembleDevDebug
app-demo-b:assembleProductionRelease
```

你的参考工程已经存在多个应用壳，而且你希望在 Android Studio 运行下拉框直接选择 App，因此当前的结构更合适：

```
app-demo-a ─┐
app-demo-b ─┼── app-common → main/core/webview/rnhome
未来 app-c ─┘
```

建议保留现在的多 Application 模块方案；以后如果需要测试、预发布、正式环境，再在每个 App 模块中添加环境 Flavor。
