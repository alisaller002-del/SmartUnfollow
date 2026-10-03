package com.example.smartunfollow

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
import kotlin.coroutines.resume
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
        @Volatile var autoScroll = true
        @Volatile var scrollDelayMin = 600L
        @Volatile var scrollDelayMax = 900L
        @Volatile var swipeMs = 300L
    }

    private val tiktokPackages = setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val seenFriends = HashSet<String>()
    private var lastSig = 0
    private var sameCount = 0

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
        val sb = StringBuilder()
        collect(root, follows, friends, sb)
        val sig = sb.toString().hashCode()

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
        if (!running) return

        if (!autoScroll) {
            statusText = "Auto Scroll OFF: khud scroll karo"
            delay(800)
            return
        }

        statusText = "Scrolling..."
        var ok = swipe()
        if (!ok) {
            ok = findScrollable(rootInActiveWindow)
                ?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        }
        seenFriends.clear()

        if (sig == lastSig) sameCount++ else { sameCount = 0; lastSig = sig }
        if (sameCount >= 3 || !ok && sameCount >= 1) {
            running = false
            statusText = "List khatam"
            return
        }
        delay(Random.nextLong(scrollDelayMin, scrollDelayMax + 1))
    }

    private suspend fun swipe(): Boolean = suspendCancellableCoroutine { cont ->
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val path = Path().apply {
            moveTo(x, dm.heightPixels * 0.75f)
            lineTo(x, dm.heightPixels * 0.35f)
        }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, swipeMs))
            .build()
        val sent = dispatchGesture(g, object : GestureResultCallback() {
            override fun onCompleted(d: GestureDescription?) {
                if (cont.isActive) cont.resume(true)
            }
            override fun onCancelled(d: GestureDescription?) {
                if (cont.isActive) cont.resume(false)
            }
        }, null)
        if (!sent && cont.isActive) cont.resume(false)
    }

    private fun collect(
        node: AccessibilityNodeInfo?,
        follows: MutableList<AccessibilityNodeInfo>,
        friends: MutableList<AccessibilityNodeInfo>,
        sb: StringBuilder
    ) {
        if (node == null) return
        val t = node.text?.toString()?.trim()
        if (t != null && t.isNotEmpty()) {
            if (t.equals("Following", ignoreCase = true)) follows.add(node)
            else if (t.equals("Friends", ignoreCase = true)) friends.add(node)
            else if (!t.equals("Follow", ignoreCase = true)) sb.append(t).append('|')
        }
        for (i in 0 until node.childCount) collect(node.getChild(i), follows, friends, sb)
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
