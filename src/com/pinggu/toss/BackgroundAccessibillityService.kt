package com.pinggu.toss  // 이 부분은 원하는 패키지명에 맞게 수정하세요.

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BackgroundAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())  // UI 쓰레드에서 작업을 처리할 핸들러

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            packageNames = arrayOf("viva.republica.toss")  // 토스 앱 패키지명
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.source == null) return

        // 토스 앱이 실행 중인지 확인
        if (isAppInForeground("viva.republica.toss")) {
            val rootNode = rootInActiveWindow ?: return
            // 토스 앱에서 다른 글자가 포함된 버튼을 찾고 클릭
            findAndClickUniqueButton(rootNode)
        }
    }

    private fun isAppInForeground(packageName: String): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningApp = am.getRunningTasks(1).firstOrNull()
        return runningApp?.topActivity?.packageName == packageName
    }

    private fun findAndClickUniqueButton(node: AccessibilityNodeInfo) {
        val textMap = mutableMapOf<String, AccessibilityNodeInfo>()

        // 버튼의 텍스트를 수집
        collectTextNodes(node, textMap)

        // 중복되지 않는 텍스트를 가진 버튼 찾기
        val uniqueButton = textMap.filter { entry ->
            textMap.keys.count { it == entry.key } == 1
        }.values.firstOrNull()

        // 발견된 버튼을 클릭 (1초 이내 처리)
        uniqueButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        // 1초 후에 다른 동작을 하도록 할 수 있습니다
        handler.postDelayed({
            // 추가적인 후속 작업을 이곳에서 처리할 수 있습니다.
        }, 1000)  // 1초 후에 처리
    }

    private fun collectTextNodes(node: AccessibilityNodeInfo, textMap: MutableMap<String, AccessibilityNodeInfo>) {
        if (node.className == "android.widget.Button" && node.text != null) {
            textMap[node.text.toString()] = node
        }

        // 자식 노드 탐색
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) collectTextNodes(child, textMap)
        }
    }

    override fun onInterrupt() {
        // 서비스 중단 시 처리할 작업
    }
}
