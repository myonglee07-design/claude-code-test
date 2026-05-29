package com.myong.addr2map.ui

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.myong.addr2map.core.MapLauncher
import com.myong.addr2map.core.PrefsStore
import com.myong.addr2map.databinding.FragmentMainBinding
import com.myong.addr2map.service.A11yAuto
import com.myong.addr2map.service.SuRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    private var _b: FragmentMainBinding? = null
    private val b get() = _b!!

    private val notiPerm =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshStatus()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _b = FragmentMainBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        b.btnAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        b.btnAccessRoot.setOnClickListener { rootEnableAccessibility() }
        b.btnOverlay.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}")
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
        b.btnPaste.setOnClickListener {
            val cb = requireContext().getSystemService(ClipboardManager::class.java)
            val t = cb?.primaryClip?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)?.coerceToText(requireContext())?.toString()?.trim().orEmpty()
            if (t.isEmpty()) toast("클립보드가 비어 있음") else b.etAddress.setText(t)
        }
        b.btnGoTmap.setOnClickListener { goMap(MapLauncher.Target.TMAP) }
        b.btnGoKakao.setOnClickListener { goMap(MapLauncher.Target.KAKAO) }
        b.btnTest.setOnClickListener {
            ChooserOverlay.show(requireContext(), "서울특별시 강남구 테헤란로 123")
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        maybeAutoEnableA11y()
    }

    private fun maybeAutoEnableA11y() {
        if (!PrefsStore.isAutoA11y(requireContext())) return
        if (A11yAuto.isAccessibilityEnabled(requireContext())) return
        viewLifecycleOwner.lifecycleScope.launch {
            val rootOK = withContext(Dispatchers.IO) { SuRunner.isRootAvailable() }
            if (!rootOK) return@launch
            val cmd = A11yAuto.buildEnableCmd(requireContext())
            val r = withContext(Dispatchers.IO) { SuRunner.run(cmd) }
            if (r.success) {
                toast("접근성 자동 켜짐 (루트)")
                refreshStatus()
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }

    private fun goMap(target: MapLauncher.Target) {
        val addr = b.etAddress.text?.toString()?.trim().orEmpty()
        if (addr.isEmpty()) {
            toast("주소를 입력하거나 붙여넣으세요")
            return
        }
        MapLauncher.open(requireContext(), target, addr)
    }

    private fun statusText(rootLine: String): String {
        val ctx = requireContext()
        val a11y = A11yAuto.isAccessibilityEnabled(ctx)
        val overlay = Settings.canDrawOverlays(ctx)
        val tmap = MapLauncher.isInstalled(ctx, MapLauncher.TMAP_PKG)
        val kakao = MapLauncher.isInstalled(ctx, MapLauncher.KAKAO_PKG)
        return buildString {
            appendLine("접근성(복사 자동감지): ${ox(a11y)}")
            appendLine("오버레이 팝업 권한: ${ox(overlay)}")
            appendLine("티맵 설치: ${ox(tmap)}")
            appendLine("카카오맵 설치: ${ox(kakao)}")
            append(rootLine)
        }
    }

    private fun refreshStatus() {
        _b ?: return
        b.tvStatus.text = statusText("루트: 확인 중…")
        viewLifecycleOwner.lifecycleScope.launch {
            val root = withContext(Dispatchers.IO) { SuRunner.isRootAvailable() }
            _b?.tvStatus?.text = statusText("루트: ${ox(root)}")
        }
    }

    private fun ox(v: Boolean) = if (v) "✓ 켜짐/있음" else "✗ 꺼짐/없음"

    private fun rootEnableAccessibility() {
        runRoot(A11yAuto.buildEnableCmd(requireContext()), "접근성 자동 켜기")
    }

    private fun rootGrantOverlay() {
        runRoot(
            "appops set ${requireContext().packageName} SYSTEM_ALERT_WINDOW allow",
            "오버레이 자동 허용"
        )
    }

    private fun runRoot(cmd: String, label: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { SuRunner.run(cmd) }
            toast(if (r.success) "$label 완료" else "$label 실패 (루트 거부?)")
            refreshStatus()
        }
    }

    private fun toast(m: String) =
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show()
}
