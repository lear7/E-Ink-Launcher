# E-Ink Launcher

面向墨水屏 / 电子书设备的 Android 桌面启动器（Launcher）。

本仓库基于 [Modificator/E-Ink-Launcher](https://github.com/Modificator/E-Ink-Launcher) 整理与维护，当前本地版本 **0.1.8.7**（`versionCode` 30）。

> Telegram 交流群：https://t.me/EInkLauncher

## 功能

- **应用桌面**：作为系统 Home / Launcher 显示已安装应用
- **网格密度**：可调每行 / 每列图标数量（2–10）
- **字体与名称**：调节应用名称字体大小、名称行数（隐藏 / 1 行 / 2 行 / 不限）
- **应用排序**：按名称 A–Z / Z–A 排序
- **应用管理**：长按图标可隐藏 / 卸载；支持应用管理入口
- **一键锁屏**：通过设备管理员（Device Admin）实现锁屏
- **一键 Wi‑Fi**：桌面开关 Wi‑Fi，可显示连接状态 / SSID
- **网络传书（FTP）**：开启后可用 `ftp://IP:端口` 从电脑传文件到设备
- **自定义图标**：用包名命名图片，放到指定目录即可替换图标
- **状态栏 / 分隔线**：可显示或隐藏状态栏、图标分隔线
- **YotaPhone / EPD**：支持 `EPD_HOME` 等双屏 / 墨水屏相关入口
- **崩溃捕获**：异常时进入崩溃详情页，便于排查

## 自定义图标

1. 在桌面**长按**某个应用，记下其包名
2. 将图标文件重命名为：`包名` + 后缀（如 `com.example.app.png`）
3. 连接电脑，把文件放到设备目录：

```text
Documents/E-Ink Launcher/icon/
```

4. 在设置中开启「显示自定义图标」

锁屏 / Wi‑Fi 特殊图标文件名（可选）：

| 用途 | 文件名 |
|------|--------|
| 锁屏 | `E-ink_Launcher.Lock.png` |
| Wi‑Fi 开 | `E-ink_Launcher.WifiOn.png` |
| Wi‑Fi 关 | `E-ink_Launcher.WifiOff.png` |

## 与上游仓库的关系

上游：[Modificator/E-Ink-Launcher](https://github.com/Modificator/E-Ink-Launcher)

| 分支 | 说明 |
|------|------|
| **`v0.x`**（默认分支） | 当前主维护线。2025–2026 有更新：系统壁纸、Android 12+/13+ 兼容、应用排序、图标缓存与性能优化、构建迁移到 AGP / `build.gradle.kts`、`compileSdk`/`targetSdk` 升至 36 等 |
| **`master`** | 较旧（约 0.1.4 / 2019），README 中「新版本在 master」的表述已过时，请以 `v0.x` 为准 |
| **`dev` / `aosp`** | 其他开发 / 适配分支 |

本仓库大致对应上游 `v0.x` 系列能力（本地版本号略新于上游标注的 0.1.8.6），构建配置仍为传统 Gradle Groovy，并保留 Yota SDK 等相关适配。

官方 Release APK（历史包）：https://github.com/Modificator/E-Ink-Launcher/releases

## 环境要求

- Android Studio / JDK（本地工程当前 Java 侧按 AGP 与 `compileSdk 31` 配置）
- Android SDK，`minSdk 14`，`targetSdk 28`（本地）
- 真机或模拟器；墨水屏 / 电子书设备体验最佳

## 构建

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Release 签名（可选）：

1. 将 keystore 放在模块目录（如 `app/kp.keystore.jks`，已加入 `.gitignore`）
2. 复制 `signing.properties.example` 为 `signing.properties` 并填写密码
3. 执行 `.\gradlew.bat assembleRelease`

请勿将 keystore 或 `signing.properties` 提交到 Git。

## 权限说明（摘要）

| 权限 | 用途 |
|------|------|
| 存储读写 | 文件浏览、自定义图标、传书 |
| Wi‑Fi / 网络 | 开关 Wi‑Fi、FTP |
| 设备管理员 | 一键锁屏 |
| 查询全部应用 | 列出可启动应用（较新系统） |

## 许可证

[Apache License 2.0](LICENSE)

原作者：**Modificator**（Email: yunshangcn@gmail.com）

Thanks: Maciej Haudek \<kontakt@haudek.com\>
