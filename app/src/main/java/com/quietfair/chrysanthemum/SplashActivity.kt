package com.quietfair.chrysanthemum

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.quietfair.chrysanthemum.user.UserBasicActivity
import com.quietfair.sdk.user.UserManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        UserManager.initContext(this)
        var delayTime: Long
        val startTime = System.currentTimeMillis()
        launch(CommonPool) {
            val user = UserManager.getUserBasic()
            val currentTime = System.currentTimeMillis()
            val timeConsuming = currentTime - startTime
            Log.d(TAG, "init cost $timeConsuming ms, get user $user")
            if (timeConsuming < 3000) {
                delayTime = 3000 - timeConsuming
                delay(delayTime, TimeUnit.MILLISECONDS)
            }
            if (user == null) {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                if (user.sex == 0 || user.ageRange == 0 || user.liveProvince == 0) {
                    val intent = Intent(this@SplashActivity, UserBasicActivity::class.java)
                    intent.putExtra("quietfair.user_id", user.userId)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            finish()

        }
    }

}
