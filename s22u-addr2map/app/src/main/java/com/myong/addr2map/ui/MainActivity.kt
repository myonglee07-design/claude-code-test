package com.myong.addr2map.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.myong.addr2map.core.MapLauncher
import com.myong.addr2map.core.PrefsStore
import com.myong.addr2map.databinding.ActivityMainBinding
import com.myong.addr2map.service.AddrAccessibilityService
import com.myong.addr2map.service.SuRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val notiPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        b.btnAccessRoot.setOnClickListener { rootEnableAccessibility() }

        b.btnOverlay.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        b.btnOverlayRoot.setOnClickListener { rootGrantOverlay() }

        b.btnNoti.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notiPerm.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                toast("이 안드로이드 버전은 알림 권한 요청 불필요")
            }
        }

        b.rgDefault.setOnCheckedChangeListener { _, id ->
            val t = if (id == b.rbKakao.id) MapLauncher.Target.KAKAO
            else MapLauncher.Target.TMAP
            PrefsStore.setDefaultTarget(this, t)
        }

        b.btnTest.setOnClickListener {
            ChooserOverlay.show(this, "서울특별시 강남구 테헤란로 123")
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        when (PrefsStore.defaultTarget(this)) {
            MapLauncher.Target.TMAP -> b.rbTmap.isChecked = true
            MapLauncher.Target.KAKAO -> b.rbKakao.isChecked = true
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun statusText(rootLine: String): String {
        val a11y = isAccessibilityEnabled()
        val overlay = Settings.canDrawOverlays(this)
        val tmap = MapLauncher.isInstalled(this, MapLauncher.TMAP_PKG)
        val kakao = MapLauncher.isInstalled(this, MapLauncher.KAKAO_PKG)
        return buildString {
            appendLine("접근성(복사 자동감지): ${ox(a11y)}")
            appendLine("오버레이 팝업 권한: ${ox(overlay)}")
            appendLine("티맵 설치: ${ox(tmap)}")
            appendLine("카카오맵 설치: ${ox(kakao)}")
            append(rootLine)
        }
    }

    private fun refreshStatus() {
        b.tvStatus.text = statusText("루트: 확인 중…")
        scope.launch {
            val root = withContext(Dispatchers.IO) { SuRunner.isRootAvailable() }
            b.tvStatus.text = statusText("루트: ${ox(root)}")
        }
    }

    private fun ox(v: Boolean) = if (v) "✓ 켜짐/있음" else "✗ 꺼짐/없음"

    private fun isAccessibilityEnabled(): Boolean {
        val expected = ComponentName(this, AddrAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    private fun rootEnableAccessibility() {
        val comp = ComponentName(this, AddrAccessibilityService::class.java)
            .flattenToString()
        val cmd = "C=\"$comp\"; " +
            "CUR=\$(settings get secure enabled_accessibility_services); " +
            "if [ \"\$CUR\" = \"null\" ] || [ -z \"\$CUR\" ]; then NEW=\"\$C\"; " +
            "elif echo \"\$CUR\" | grep -q \"\$C\"; then NEW=\"\$CUR\"; " +
            "else NEW=\"\$CUR:\$C\"; fi; " +
            "settings put secure enabled_accessibility_services \"\$NEW\"; " +
            "settings put secure accessibility_enabled 1"
        runRoot(cmd, "접근성 자동 켜기")
    }

    private fun rootGrantOverlay() {
        runRoot(
            "appops set $packageName SYSTEM_ALERT_WINDOW allow",
            "오버레이 자동 허용"
        )
    }

    private fun runRoot(cmd: String, label: String) {
        scope.launch {
            val r = withContext(Dispatchers.IO) { SuRunner.run(cmd) }
            toast(if (r.success) "$label 완료" else "$label 실패 (루트 거부?)")
            refreshStatus()
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
