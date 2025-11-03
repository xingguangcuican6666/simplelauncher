# SimpleLauncher

一个最小的 Android 应用，功能：

- 提供文本框输入网址
- 点击“打开（全屏）”会优先使用 Chrome（Chrome 内核）打开网页，若设备未安装 Chrome 则使用内置 WebView 全屏打开
- 提供“创建桌面快捷方式”，会在主屏幕创建一个快捷方式，点击后直接以全屏打开该网址

---

构建说明（在 Termux proot 环境，已安装 JDK 21）：

前提：你的 Termux proot 环境需要安装 Android SDK 命令行工具、Android 平台和构建工具，并有网络下载能力。以下是概要步骤：

1) 安装必要工具（示例，依据你的发行版调整）

   - 确保已经安装 openjdk-21（你已说明已安装）
   - 安装 unzip、wget 等工具

2) 获取 Android SDK Command-line tools

   - 从 Google 下载 commandlinetools-linux 并解压到 `$HOME/android-sdk`（或其他路径）
   - 设置环境变量：
     - `export ANDROID_SDK_ROOT=$HOME/android-sdk`
     - `export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools`

3) 使用 `sdkmanager` 安装平台与构建工具（示例）

   - `sdkmanager --sdk_root=${ANDROID_SDK_ROOT} "platform-tools" "platforms;android-33" "build-tools;33.0.2" "cmdline-tools;latest"`
   - 接受许可：`yes | sdkmanager --licenses`

4) 在项目根目录运行 Gradle 构建

   - 本仓库未包含 Gradle Wrapper 的二进制（你可以在有 Gradle 的机器上运行 `gradle wrapper` 来生成 wrapper），或者在 Termux 中安装 gradle（如果可用）
   - 推荐在 Termux 中安装 Gradle 或使用本地安装的 Gradle：`gradle assembleDebug`

注意与常见问题：

- Android Gradle Plugin 与 Gradle 版本、JDK 版本强相关。若出现不兼容错误，请根据错误提示调整 `com.android.tools.build:gradle` 版本与本地 Gradle 版本（或使用 Gradle Wrapper）。
- 如果你希望我在当前环境尝试自动安装 SDK 并构建 APK，请告诉我允许我运行终端命令，我会检测并尽力执行；若环境缺少网络或必要的工具，构建会失败并我会把错误输出贴给你。

---

开发者备注：

- 该应用尝试优先使用 Chrome（包名 `com.android.chrome`）打开链接，从而达到“Chrome 内核”的效果；如果目标设备上没有 Chrome，会回退到内置的 WebView（系统 WebView 通常也是 Chromium 内核的变体）。
- 快捷方式使用 `ShortcutManager`（Android O+）创建固定快捷方式；旧版使用广播尝试兼容部分 Launcher。某些 OEM Launcher 可能限制或提示用户确认。

如果你想让我现在在当前环境尝试自动安装 Android SDK 并编译，请回复“现在尝试构建”，我会继续并在操作前显示要执行的命令列表。