package com.myong.addr2map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myong.addr2map.core.MapLauncher
import com.myong.addr2map.core.PrefsStore
import com.myong.addr2map.databinding.FragmentOptionsBinding

class OptionsFragment : Fragment() {

    private var _b: FragmentOptionsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _b = FragmentOptionsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val ctx = requireContext()

        b.swEnabled.isChecked = PrefsStore.isEnabled(ctx)
        b.swAddrOnly.isChecked = PrefsStore.isAddrOnly(ctx)
        when (PrefsStore.autoDismissSec(ctx)) {
            3 -> b.rbSec3.isChecked = true
            10 -> b.rbSec10.isChecked = true
            else -> b.rbSec6.isChecked = true
        }
        when (PrefsStore.defaultTarget(ctx)) {
            MapLauncher.Target.KAKAO -> b.rbKakao.isChecked = true
            else -> b.rbTmap.isChecked = true
        }

        b.swEnabled.setOnCheckedChangeListener { _, v -> PrefsStore.setEnabled(ctx, v) }
        b.swAddrOnly.setOnCheckedChangeListener { _, v -> PrefsStore.setAddrOnly(ctx, v) }
        b.rgDismiss.setOnCheckedChangeListener { _, id ->
            val sec = when (id) {
                b.rbSec3.id -> 3
                b.rbSec10.id -> 10
                else -> 6
            }
            PrefsStore.setAutoDismissSec(ctx, sec)
        }
        b.rgDefault.setOnCheckedChangeListener { _, id ->
            val t = if (id == b.rbKakao.id) MapLauncher.Target.KAKAO
            else MapLauncher.Target.TMAP
            PrefsStore.setDefaultTarget(ctx, t)
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
