package com.myong.addr2map.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object MapLauncher {

    const val TMAP_PKG = "com.skt.tmap.ku"
    const val KAKAO_PKG = "net.daum.android.map"

    enum class Target { TMAP, KAKAO }

    fun open(context: Context, target: Target, address: String) {
        val enc = URLEncoder.encode(address.trim(), "UTF-8")
        when (target) {
            Target.TMAP -> launch(
                context, TMAP_PKG,
                "tmap://search?name=$enc",
                "https://tmap.life", "티맵"
            )
            Target.KAKAO -> launch(
                context, KAKAO_PKG,
                "kakaomap://search?q=$enc",
                "https://map.kakao.com/?q=$enc", "카카오맵"
            )
        }
    }

    private fun launch(
        context: Context,
        pkg: String,
        scheme: String,
        webFallback: String,
        label: String
    ) {
        if (isInstalled(context, pkg)) {
            runCatching {
                context.startActivity(viewIntent(scheme))
            }.onFailure { openWeb(context, webFallback) }
        } else {
            Toast.makeText(context, "$label 미설치 — 웹/스토어로 엽니다", Toast.LENGTH_SHORT).show()
            runCatching {
                context.startActivity(
                    viewIntent("market://details?id=$pkg")
                )
            }.onFailure { openWeb(context, webFallback) }
        }
    }

    private fun viewIntent(uri: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    private fun openWeb(context: Context, url: String) {
        runCatching { context.startActivity(viewIntent(url)) }
    }

    fun isInstalled(context: Context, pkg: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        }.getOrDefault(false)
}
