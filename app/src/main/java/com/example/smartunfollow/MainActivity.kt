package com.example.smartunfollow

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvStatus: TextView
    private lateinit var tvUnf: TextView
    private lateinit var tvSkip: TextView
    private lateinit var tvSpeed: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun bg(color: String, r: Int) = GradientDrawable().apply {
        setColor(Color.parseColor(color))
        cornerRadius = dp(r).toFloat()
    }

    private fun btn(label: String, color: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 16f
        background = bg(color, 14)
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
        ).apply { topMargin = dp(12) }
        setOnClickListener { onClick() }
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = bg("#1B2735", 18)
        setPadding(dp(18), dp(16), dp(18), dp(16))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(14) }
    }

    private val refresh = object : Runnable {
        override fun run() {
            tvStatus.text = UnfollowService.statusText +
                if (UnfollowService.running) "  (Running)" else ""
            tvUnf.text = "Unfollowed\n" + UnfollowService.unfollowed
            tvSkip.text = "Friends skipped\n" + UnfollowService.skipped
            handler.postDelayed(this, 500)
        }
    }

    private fun setSpeed(name: String, min: Long, max: Long) {
        UnfollowService.minDelayMs = min
        UnfollowService.maxDelayMs = max
        tvSpeed.text = "Speed: $name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#101820")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(40), dp(18), dp(24))
        }

        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_logo)
            layoutParams = LinearLayout.LayoutParams(dp(90), dp(90)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        })
        root.addView(TextView(this).apply {
            text = "ALI TRUSTED SALLER"
            textSize = 24f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.1f
            setTextColor(Color.WHITE)
            setShadowLayer(24f, 0f, 0f, Color.parseColor("#4FC3F7"))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        })
        root.addView(TextView(this).apply {
            text = "03467314519  •  Friends safe"
            textSize = 14f
            setTextColor(Color.parseColor("#4FC3F7"))
            gravity = Gravity.CENTER
        })

        val c1 = card()
        c1.addView(TextView(this).apply {
            text = "STATUS"
            textSize = 12f
            setTextColor(Color.parseColor("#8FA3B8"))
        })
        tvStatus = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
        }
        c1.addView(tvStatus)
        root.addView(c1)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(14) }
        }
        fun box(): TextView = TextView(this).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = bg("#1B2735", 18)
            setPadding(dp(8), dp(18), dp(8), dp(18))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginEnd = dp(6); marginStart = dp(6) }
        }
        tvUnf = box()
        tvSkip = box()
        row.addView(tvUnf)
        row.addView(tvSkip)
        root.addView(row)

        val c2 = card()
        c2.addView(TextView(this).apply {
            text = "Max unfollow is session mein"
            textSize = 13f
            setTextColor(Color.parseColor("#8FA3B8"))
        })
        val limit = EditText(this).apply {
            setText("50")
            setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        c2.addView(limit)
        tvSpeed = TextView(this).apply {
            text = "Speed: Normal"
            textSize = 13f
            setTextColor(Color.parseColor("#8FA3B8"))
            setPadding(0, dp(10), 0, 0)
        }
        c2.addView(tvSpeed)
        val speedRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun sp(label: String, min: Long, max: Long) = Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            background = bg("#2A3B4F", 12)
            layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f)
                .apply { marginEnd = dp(4); marginStart = dp(4); topMargin = dp(6) }
            setOnClickListener { setSpeed(label, min, max) }
        }
        speedRow.addView(sp("Slow", 900L, 1500L))
        speedRow.addView(sp("Normal", 500L, 900L))
        speedRow.addView(sp("Fast", 300L, 600L))
        c2.addView(speedRow)
        root.addView(c2)

        root.addView(btn("START", "#2E7D32") {
            UnfollowService.maxActions = limit.text.toString().toIntOrNull() ?: 50
            UnfollowService.unfollowed = 0
            UnfollowService.skipped = 0
            UnfollowService.running = true
            UnfollowService.statusText = "Started"
        })
        root.addView(btn("STOP", "#C62828") {
            UnfollowService.running = false
            UnfollowService.statusText = "Stopped"
        })
        root.addView(btn("Accessibility Settings", "#37474F") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })

        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#101820"))
            addView(root)
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresh)
    }
}
