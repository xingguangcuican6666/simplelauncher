package com.example.simplelauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.url_input)
        val openBtn = findViewById<Button>(R.id.open_btn)
        val shortcutBtn = findViewById<Button>(R.id.shortcut_btn)

        openBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "请输入网址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openInFullscreen(url)
        }

        shortcutBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "请输入网址以创建快捷方式", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createPinnedShortcut(url)
        }
    }

    private fun openInFullscreen(url: String) {
        // 尝试用 Chrome Custom Tab（Chrome 内核）打开；若不可用则启动内置 FullscreenActivity（WebView）
        val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            // 指定 Chrome 包名以优先使用 Chrome（若设备装有 Chrome）
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pm = packageManager
        if (chromeIntent.resolveActivity(pm) != null) {
            // 若 Chrome 存在，直接打开（Custom Tabs 会在 Chrome 中以接近原生的体验打开）
            startActivity(chromeIntent)
        } else {
            // 回退到内置 WebView 全屏 Activity
            val i = Intent(this, FullscreenActivity::class.java)
            i.putExtra("url", url)
            startActivity(i)
        }
    }

    private fun createPinnedShortcut(url: String) {
        val shortcutIntent = Intent(this, FullscreenActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("url", url)
        }


        // 获取 ShortcutManager（仅在 API >= O 可用）
        val shortcutManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // getSystemService(Class) requires API >= 23; since we check for O(26)
            // this is safe.
            getSystemService(ShortcutManager::class.java)
        } else null

        // Android O+ 推荐使用 ShortcutManager.requestPinShortcut。为兼容旧设备，
        // 我们在 API 级别上做检查，并在不支持时使用旧广播方法回退。
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
            val builder = ShortcutInfo.Builder(this, "shortcut_" + url.hashCode())
                .setShortLabel(url)
                .setLongLabel(url)
                .setIntent(shortcutIntent)

            // Icon API 在 API 23+ 可用；只有在运行时 API >= M 时才设置 Icon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder.setIcon(Icon.createWithResource(this, R.drawable.ic_launcher))
            }

            val shortcut = builder.build()
            shortcutManager.requestPinShortcut(shortcut, null)
            Toast.makeText(this, "已请求固定到主屏幕（系统会提示）", Toast.LENGTH_SHORT).show()
            return
        }

        // 旧版创建快捷方式的兼容做法（某些 OEM 仍支持）
        val addIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, url)
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this@MainActivity, R.drawable.ic_launcher))
        }
        sendBroadcast(addIntent)
        Toast.makeText(this, "已发送安装快捷方式广播（部分系统支持）", Toast.LENGTH_SHORT).show()
    }
}
