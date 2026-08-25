<div align="center">

# SystemBarsModernizer (SBM)

**让 Android 应用拥抱真·沉浸式边到边（Edge-to-Edge）现代化系统栏体验**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?style=flat-square)](https://www.android.com)
[![API](https://img.shields.io/badge/API-29%2B%20(Android%2010%2B)-brightgreen.svg?style=flat-square)](https://android-arsenal.com/api?level=29)
[![LibXposed](https://img.shields.io/badge/LibXposed-API%20102-blue.svg?style=flat-square)](https://github.com/libxposed/api)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg?style=flat-square)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

[English](#features) • [简体中文](#-项目简介) • [配置文档](#-规则配置与-extraaction-引擎) • [预设规则](#-内置适配应用)

---

</div>

## 📖 项目简介

在现代 Android 系统（特别是 Android 10+ 的手势导航时代）中，许多主流国内应用依然保留着传统的黑底导航栏、生硬的状态栏垫高（Padding）或者自定义的底部占位 View（如 72px 导航占位条），破坏了全面屏的无缝视觉体验。

**SystemBarsModernizer (SBM)** 是一个基于现代化 **LibXposed (API 102)** 标准构建的高性能 Xposed 模块。它通过在系统框架层与视图层进行精准拦截与参数改写，为各类应用强制启用 **Edge-to-Edge 边到边沉浸式渲染**，并提供细粒度的布局边距矫正引擎，彻底消除黑条、切边与多余垫高。

---

## ✨ 核心特性

- 🚀 **全面适配现代 LibXposed API 102**：
  采用现代化的无侵入 Xposed 架构，支持 `onPackageReady`、`onHotReloading` / `onHotReloaded` 生命周期管理与 RemotePreferences 配置共享。
- 🎨 **真·边到边沉浸式（Edge-to-Edge）**：
  强制解锁系统栏透明底色，关闭 Android 系统的深色对比度遮罩（`isStatusBarContrastEnforced / isNavigationBarContrastEnforced = false`），并支持自动根据背景亮度适配亮/暗色状态栏与导航手势条。
- 🛠️ **强大的 ExtraAction 布局矫正引擎**：
  - **Padding（内边距）矫正**：动态重置或注入顶部状态栏/底部导航栏 Insets；
  - **Margin（外边距）矫正**：动态拦截 `View.setLayoutParams`，精准改写子容器的 `topMargin` / `bottomMargin`；
  - **占位 View 消除（`isGone`）**：锁定指定占位 View（如 `v_navbar_placeholder`）为 `View.GONE` 且高度归零；
  - **DecorView 索引定位（`viewId = "decor"`）**：支持定位未命名的 DecorView 子容器（如 `decorView.getChildAt(0)`）。
- 🔍 **4 级智能页面匹配体系**：
  1. **精确类名匹配**：`scope["com.xxx.MainActivity"]`
  2. **`$` 内部类/影子 Activity 向上继承**：`XRiverActivity$App01` 自动继承 `XRiverActivity` 的规则
  3. **`*` 前缀通配符批量匹配**：`"com.alipay.android.phone.msgboxapp.ui.activity.*"`
  4. **通用兜底规则与排除列表（`exclusive`）**
- ⚡ **极致性能与零开销架构**：
  - `WeakHashMap<Activity, PageConfig?>` 弱引用缓存，页面配置 O(1) 内存直取；
  - Fast-Path 短路机制：非目标页面与普通 View 在第 1 行指令直接放行，**单次排版拦截仅需 ~26ns**；
  - 120Hz/144Hz 高刷设备丝滑满帧，零掉帧与零内存泄露。
- 🛡️ **编译期调试隔离**：
  View 树结构转储（`dumpViewHierarchy`）受 `BuildConfig.DEBUG` 守卫，正式 Release 包零反射与日志消耗。

---

## 🏗️ 工作原理与架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Android 进程启动                        │
└──────────────────────────────┬──────────────────────────────┘
                               │
                ┌──────────────▼──────────────┐
                │ onPackageReady (LibXposed)  │
                └──────────────┬──────────────┘
                               │ 读取 RemotePreferences
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
    ┌───────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐
    │ Activity.onCreate│ │ View.setPadding│ │setLayoutParams│
    └───────┬────────┘ └───────┬────────┘ └───────┬────────┘
            │                  │                  │
   设置 Window 沉浸      Fast-Path 短路    Fast-Path 短路
   注入系统透明栏颜色     执行 Padding 改写  执行 Margin 改写
            │                  │                  │
            └──────────────────┴──────────────────┘
```

---

## ⚙️ 规则配置与 ExtraAction 引擎

SBM 采用结构化的数据模型描述每个应用和页面的沉浸式行为：

### 1. `PageConfig` 结构说明

```kotlin
data class PageConfig(
    val edgeToEdge: Boolean = false,          // 是否启用全屏边到边沉浸
    val clearTranslucent: Boolean = false,     // 是否清除半透明 Window 标记
    val windowBackgroundColor: Int? = null,    // 强制设置 Window 背景颜色
    val statusColor: Int = Color.TRANSPARENT,   // 状态栏颜色
    val navigationColor: Int = Color.TRANSPARENT, // 导航栏颜色
    val extraActions: List<ExtraAction> = emptyList(), // 额外的布局矫正动作
    val uiModeWBC: Pair<Int, Int>? = null      // 深色/浅色模式下的背景色自动切换
)
```

### 2. `ExtraAction` 结构与常用场景

```kotlin
data class ExtraAction(
    val viewId: String,               // 目标 View ID（如 "decor"、"content" 或具体 R.id 名称）
    val isGroup: Boolean = false,      // 是否作为 ViewGroup 查找子 View
    val isTop: Boolean = false,        // 针对顶部（状态栏）还是底部（导航栏）
    val isPadding: Boolean = true,     // true 表示修改 Padding，false 表示修改 Margin
    val useSystemInsets: Boolean = false, // 是否自动使用系统栏高度作为边距
    val customInset: Int = -1,         // 自定义边距像素值（设为 0 即清空边距）
    val self: Boolean = true,          // 是否作用于自身（false 则作用于 childIndex 对应子 View）
    val childIndex: Int = -1,          // 目标子 View 索引（如 0）
    val isGone: Boolean = false        // 是否强制设为 View.GONE 消除占位 View
)
```

#### 常见配置示例：

- **场景 A：清空特定 View 的底部 Padding**
  ```kotlin
  ExtraAction(viewId = "nebulax_root_view", isTop = false, isPadding = true, customInset = 0)
  ```
- **场景 B：清空 DecorView 第 0 个子容器的底部 Margin（如淘宝详情页）**
  ```kotlin
  ExtraAction(viewId = "decor", isGroup = true, self = false, childIndex = 0, isTop = false, isPadding = false, customInset = 0)
  ```
- **场景 C：隐藏多余的导航栏占位 View（如支付宝消息中心）**
  ```kotlin
  ExtraAction(viewId = "v_navbar_placeholder", isGone = true)
  ```

---

## 📱 内置适配应用（部分展示）

| 应用名称 | 包名 | 适配页面 / 规则 | 说明 |
| :--- | :--- | :--- | :--- |
| **淘宝** | `com.taobao.taobao` | `TBMainActivity`<br>`NewDetailActivity` | 首页纯白导航栏；商品详情页清空 DecorView 子容器 `bottomMargin` 消除底部黑条 |
| **支付宝** | `com.eg.android.AlipayGphone` | `XRiverActivity` (小程序/H5)<br>`CSPushActivity` (推送卡片)<br>`ContactMainPageActivity` (通讯录)<br>`com.alipay.android.phone.msgboxapp.ui.activity.*` | 消除 H5 根布局 padding、清空 DecorView 容器底边距、隐藏所有消息盒子页面的 `v_navbar_placeholder` 占位块 |
| **闲鱼** | `com.taobao.idlefish` | 通用边到边 | 全局页面强制开启沉浸式边到边 |
| **京东** | `com.jingdong.app.mall` | 通用边到边 (排除商品详情及结算页) | 避免特殊悬浮结算页变形，其余页面全沉浸 |
| **小黑盒** | `com.max.xiaoheihe` | `MainActivity`<br>`ChannelsDetailActivity`<br>`WebActionActivity` | 适配浅色/深色主题动态切色，文章页全沉浸 |
| **豆瓣** | `com.douban.frodo` | 通用边到边 | 统一现代化导航条底色 |

---

## 🗺️ 路线图与规划（Roadmap & TODO）

目前项目的核心 Hook 引擎与规则执行体系已完全就绪，但当前的规则配置仍以代码预设与调试配置为主。管理端 App 的可视化与动态管理能力正在积极重构中：

- [ ] **应用列表可视化（App List）**：
  - 扫描并展示设备上已安装的全部第三方与系统应用；
  - 异步加载应用图标、名称、包名与沉浸式启用状态；
  - 采用 Room 数据库 + MVVM + DiffUtil 扁平化自定义 View 极速渲染。
- [ ] **规则配置动态编辑器（Rule Editor & CRUD）**：
  - 支持按应用查看所有页面规则（`PageConfig`）与高级动作（`ExtraAction`）；
  - 提供可视化的规则添加、修改、删除（增删查改）与实时搜索能力；
  - 支持快捷配置：一键设置 `Edge-to-Edge`、一键添加 `decor` 索引 / 占位 View 隐藏 / Padding 与 Margin 改写。
- [ ] **配置实时同步推送（Sync to LSPosed / Hook Engine）**：
  - 动态保存并直接更新 `RemotePreferences`；
  - 无需重启 LSPosed 或手机，应用切到前台即可实时读取最新配置并热生效。
- [ ] **规则云端订阅与导入导出**：
  - 支持规则配置 JSON 导出与本地备份导入；
  - 支持在线订阅社区贡献的 App 沉浸式适配规则池。

---

## 🚀 安装与使用

### 环境要求
- **Android 版本**：Android 10.0 (API 29) 及以上
- **框架支持**：LSPosed (推荐最新版本) 或支持 LibXposed 的现代 Xposed 环境

### 使用步骤
1. 在 [Releases](../../releases) 中下载最新的 APK 并安装；
2. 打开 LSPosed 管理器，在模块列表中勾选 **SystemBarsModernizer** 并启用；
3. 选择需要生效的目标应用作用域（如淘宝、支付宝、微信等）；
4. 强行停止目标应用后重新打开，即可享受丝滑的边到边沉浸式体验。

---

## 🛠️ 源码构建

本项目采用标准 Gradle 构建系统：

```bash
# 1. 克隆本仓库
git clone https://github.com/Hal1ucinogen/SystemBarsModernizer.git
cd SystemBarsModernizer

# 2. 编译 Debug 版本 APK
./gradlew assembleDebug

# 3. 编译 Release 版本 APK
./gradlew assembleRelease
```

构建生成的 APK 位于：
* `app/build/outputs/apk/debug/app-debug.apk`
* `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 🤝 贡献与反馈

欢迎提交 Issue 和 Pull Request！
- 如果你发现了某个 App 在启用后出现界面被遮挡、黑底未能消除或视图错位；
- 欢迎通过 Logcat 抓取 View 层级并提交新的 `ExtraAction` 适配规则。

---

## 📄 开源协议

本项目基于 [Apache License 2.0](LICENSE) 开源。
