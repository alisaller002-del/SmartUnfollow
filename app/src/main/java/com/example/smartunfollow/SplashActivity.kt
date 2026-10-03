package com.example.smartunfollow

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#101820")

        val glow = Color.parseColor("#4FC3F7")
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#101820"))
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.ic_logo)
            layoutParams = LinearLayout.LayoutParams(dp(140), dp(140))
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
        }
        val name = TextView(this).apply {
            text = "ALI TRUSTED SALLER"
            textSize = 28f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.12f
            setTextColor(Color.WHITE)
            setShadowLayer(30f, 0f, 0f, glow)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(28) }
            alpha = 0f
        }
        val number = TextView(this).apply {
            text = "03467314519"
            textSize = 20f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            letterSpacing = 0.2f
            setTextColor(glow)
            setShadowLayer(20f, 0f, 0f, glow)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
            alpha = 0f
            translationY = dp(30).toFloat()
        }
        layout.addView(logo)
        layout.addView(name)
        layout.addView(number)
        setContentView(layout)

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start()
        name.animate().alpha(1f).setStartDelay(500).setDuration(900).start()
        number.animate().alpha(1f).translationY(0f).setStartDelay(1000).setDuration(800).start()

        ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.08f).apply {
            startDelay = 1000
            duration = 900
            repeatCount = 2
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.08f).apply {
            startDelay = 1000
            duration = 900
            repeatCount = 2
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3200)
    }
}
