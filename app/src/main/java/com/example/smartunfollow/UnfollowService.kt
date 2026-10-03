package com.example.smartunfollow

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
import kotlin.random.Random

class UnfollowService : AccessibilityService() {

    companion object {
        @Volatile var running = false
        @Volatile var unfollowed = 0
        @Volatile var skipped = 0
        @Volatile var maxActions = 50
        @Volatile var statusText = "Idle"
        @Volatile var minDelayMs = 220L
        @Volatile var maxDelayMs = 340L
    }

    private val tiktokPackages = setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val seenFriends = HashSet<String>()
    private var noScrollCount = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        statusText = "Service ON"
        scope.launch {
            while (isActive) {
                if (!running) {
                    delay(300)
                    continue
                }
                try {
                    step()
                } catch (e: Exception) {
                    statusText = "Error: " + e.message
                    delay(1000)
                }
            }
        }
    }

    private suspend fun step() {
        val root = rootInActiveWindow
        if (root == null || root.packageName?.toString() !in tiktokPackages) {
            statusText = "TikTok Following list kholo"
            delay(800)
            return
        }

        val follows = ArrayList<AccessibilityNodeInfo>()
        val friends = ArrayList<AccessibilityNodeInfo>()
        collect(root, follows, friends)

        for (f in friends) {
            val r = Rect()
            f.getBoundsInScreen(r)
            if (seenFriends.add(r.toShortString())) skipped++
        }

        var clicked = 0
        for (target in follows) {
            if (!running) return
            if (unfollowed >= maxActions) {
                running = false
                statusText = "Limit poori ho gayi"
                return
            }
            val clickable = clickableParent(target) ?: continue
            if (clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                unfollowed++
                clicked++
                statusText = "Unfollowing..."
                delay(Random.nextLong(minDelayMs, maxDelayMs + 1))
            }
        }

        if (clicked > 0) noScrollCount = 0
        if (!running) return

        statusText = if (clicked == 0 && friends.isNotEmpty()) "FRIENDS: skip" else "Scroll"
        val scrollable = findScrollable(root)
        val ok = scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        seenFriends.clear()
        if (ok) {
            noScrollCount = 0
        } else {
            noScrollCount++
            if (noScrollCount >= 3) {
                running = false
                statusText = "List khatam"
            }
        }
        delay(Random.nextLong(500L, 800L))
    }

    private fun collect(
        node: AccessibilityNodeInfo?,
        follows: MutableList<AccessibilityNodeInfo>,
        friends: MutableList<AccessibilityNodeInfo>
    ) {
        if (node == null) return
        val t = node.text?.toString()?.trim()
        if (t != null) {
            if (t.equals("Following", ignoreCase = true)) follows.add(node)
            else if (t.equals("Friends", ignoreCase = true)) friends.add(node)
        }
        for (i in 0 until node.childCount) collect(node.getChild(i), follows, friends)
    }

    private fun clickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var n: AccessibilityNodeInfo? = node
        var depth = 0
        while (n != null && depth < 4) {
            if (n.isClickable) return n
            n = n.parent
            depth++
        }
        return null
    }

    private fun findScrollable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val r = findScrollable(node.getChild(i))
            if (r != null) return r
        }
        return null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
