package com.example.smartunfollow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#101820"))
        }
        val name = TextView(this).apply {
            text = "Ali saller"
            textSize = 36f
            setTextColor(Color.WHITE)
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
        }
        val number = TextView(this).apply {
            text = "0300-0000000"
            textSize = 20f
            setTextColor(Color.parseColor("#4FC3F7"))
            alpha = 0f
            translationY = 120f
        }
        layout.addView(name)
        layout.addView(number)
        setContentView(layout)

        name.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900).start()
        number.animate().alpha(1f).translationY(0f).setStartDelay(700).setDuration(900).start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2600)
    }
}
