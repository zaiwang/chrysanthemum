package com.quietfair.chrysanthemum

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.quietfair.chrysanthemum.user.UserBasicActivity
import com.quietfair.sdk.user.UserManager
import com.tencent.tauth.Tencent
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {
    companion object {
        private val TAG = "SplashActivity"
        public val QQ_APP_ID = "1106845963"
    }

    private val KEY_QQ_OPEN_ID = "quietfair.qq.openid"
    private val KEY_QQ_ACCESS_TOKEN = "quietfair.qq.accesstoken"

    private val WAIT_TIME = 1_000

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
            if (timeConsuming < WAIT_TIME) {
                delayTime = WAIT_TIME - timeConsuming
                delay(delayTime, TimeUnit.MILLISECONDS)
            }
            if (user == null) {
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                if (TextUtils.isEmpty(user.gender) || user.birthYear == 0 ||  TextUtils.isEmpty(user.liveProvince)) {
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
