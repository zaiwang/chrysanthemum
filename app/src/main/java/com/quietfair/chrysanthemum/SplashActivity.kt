package com.quietfair.chrysanthemum

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        var delayTime = 3000L
        val startTime = System.currentTimeMillis()
        launch(CommonPool) {
            checkAndInit()
            val currentTime = System.currentTimeMillis()
            val timeConsuming = currentTime - startTime
            if (timeConsuming < 3000) {
                delayTime = 3000 - timeConsuming
            }
            delay(delayTime, TimeUnit.MILLISECONDS)
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkAndInit() {

    }
}
