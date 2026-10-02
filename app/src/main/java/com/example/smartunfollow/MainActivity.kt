package com.example.smartunfollow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var info: TextView

    private val refresh = object : Runnable {
        override fun run() {
            info.text = "Status: " + UnfollowService.statusText +
                "\nRunning: " + UnfollowService.running +
                "\nUnfollowed: " + UnfollowService.unfollowed +
                "\nFriends skipped: " + UnfollowService.skipped
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        info = TextView(this).apply { textSize = 18f }

        val btnSetup = Button(this).apply {
            text = "Accessibility Settings kholo"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        val limit = EditText(this).apply {
            hint = "Max unfollow is session mein (default 50)"
            setText("50")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val btnStart = Button(this).apply {
            text = "START"
            setOnClickListener {
                UnfollowService.maxActions = limit.text.toString().toIntOrNull() ?: 50
                UnfollowService.unfollowed = 0
                UnfollowService.skipped = 0
                UnfollowService.running = true
                UnfollowService.statusText = "Started"
            }
        }
        val btnStop = Button(this).apply {
            text = "STOP"
            setOnClickListener {
                UnfollowService.running = false
                UnfollowService.statusText = "Stopped"
            }
        }

        layout.addView(info)
        layout.addView(btnSetup)
        layout.addView(limit)
        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)
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
