package com.efzyn.cekresiapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.ui.main.MainActivity

@SuppressLint("CustomSplashScreen") // Jika menggunakan AppCompatActivity, tidak perlu lagi extends SplashScreen
class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000 // 2 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Pastikan nama layout sesuai

        Handler(Looper.getMainLooper()).postDelayed({
            // Intent untuk memulai MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            // Tutup activity ini
            finish()
        }, SPLASH_TIME_OUT)
    }
}

