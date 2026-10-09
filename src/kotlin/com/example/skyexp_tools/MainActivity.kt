package com.example.skyexp_tools

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "skyexp/native")
            .setMethodCallHandler { call, result ->
                if (call.method == "specs") result.success(specs()) else result.notImplemented()
            }
    }

    private fun specs(): Map<String, Any> {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val st = StatFs(Environment.getDataDirectory().path)
        val bat = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bat?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bat?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = bat?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val temp = bat?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
        return mapOf(
            "brand" to Build.BRAND,
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "release" to Build.VERSION.RELEASE,
            "sdk" to Build.VERSION.SDK_INT,
            "hardware" to Build.HARDWARE,
            "board" to Build.BOARD,
            "abis" to Build.SUPPORTED_ABIS.joinToString(", "),
            "ramTotal" to mi.totalMem,
            "ramFree" to mi.availMem,
            "diskTotal" to st.totalBytes,
            "diskFree" to st.availableBytes,
            "battery" to pct,
            "charging" to (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL),
            "temp" to temp / 10.0
        )
    }
}
