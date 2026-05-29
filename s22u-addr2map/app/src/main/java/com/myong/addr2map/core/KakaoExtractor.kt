package com.myong.addr2map.core

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object KakaoExtractor {

    // TODO(집): 실제 카카오대리 기사앱 패키지명으로 교체 (정찰로 확인)
    const val PKG = "com.kakao.driver"

    private const val MAX_DEPTH = 20
    private const val MAX_COUNT = 100
    private const val DEBOUNCE_MS = 1500L
    private const val DONE_LOCK_MS = 8000L
    private const val TAG = "KakaoExtract"
    private const val RECON_TAG = "KakaoRecon"

    private var lastFireAt = 0L
    private var doneLockUntil = 0L

    fun handle(context: Context, root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        val now = System.currentTimeMillis()
        if (now < doneLockUntil) return null
        if (now - lastFireAt < DEBOUNCE_MS) return null

        if (PrefsStore.isRecon(context)) {
            Log.d(RECON_TAG, "=== dump start ===")
            dumpNode(root, 0, intArrayOf(0))
            Log.d(RECON_TAG, "=== dump end ===")
        }

        val dest = findDestination(root) ?: return null
        lastFireAt = now
        doneLockUntil = now + DONE_LOCK_MS
        Log.d(TAG, "destination=$dest")
        return dest
    }

    private fun findDestination(root: AccessibilityNodeInfo): String? {
        // TODO(집, Claude Code 정찰 결과로 채움):
        //   예시 A) viewId 기반
        //     root.findAccessibilityNodeInfosByViewId("$PKG:id/destination_text")
        //         .firstOrNull()?.text?.toString()
        //   예시 B) 라벨 인접 텍스트 ("도착" / "목적지" 근처 텍스트)
        //   예시 C) 화면 좌표/계층 기반
        return null
    }

    private fun dumpNode(node: AccessibilityNodeInfo?, depth: Int, counter: IntArray) {
        if (node == null) return
        if (depth > MAX_DEPTH) return
        if (counter[0] > MAX_COUNT) return
        val t = node.text
        val cd = node.contentDescription
        val id = node.viewIdResourceName
        if (t != null || cd != null || id != null) {
            counter[0]++
            Log.d(
                RECON_TAG,
                "N[$depth] cls=${node.className} text=$t desc=$cd id=$id"
            )
        }
        for (i in 0 until node.childCount) {
            dumpNode(node.getChild(i), depth + 1, counter)
        }
    }
}
