package com.quietfair.sdk.user

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.quietfair.sdk.ChrysanthemumConstants
import com.quietfair.sdk.R
import com.quietfair.utils.http.NetworkErrorDesc
import com.quietfair.utils.http.OkHttpNetworkDataAcquisition
import com.quietfair.utils.http.OnHttpResultGotListener
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object UserManager {
    private val TAG = "UserManager"

    private lateinit var mContext: Context

    public fun initContext(context: Context) {
        mContext = context.applicationContext
    }

    public fun getToken(): String? {
        val sharedPreferences = mContext.getSharedPreferences("quietfair.user", Context.MODE_PRIVATE)
        return sharedPreferences.getString("quietfair.user.token", null)
    }

    public fun getUserId(): String? {
        val sharedPreferences = mContext.getSharedPreferences("quietfair.user", Context.MODE_PRIVATE)
        return sharedPreferences.getString("quietfair.user.user_id", null)
    }

    public fun saveUserIdAndToken(userId: String, token: String) {
        val sharedPreferences = mContext.getSharedPreferences("quietfair.user", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("quietfair.user.user_id", userId).putString("quietfair.user.token", token).apply()
    }

    public fun getUserBasic(): User? {
        val userId = getUserId()
        val token = getToken()
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
            return null
        }
        val user = User()
        user.userId = userId
        user.token = token
        val heads = mapOf(Pair("user_token", token))
        OkHttpNetworkDataAcquisition.setHttpHeaders(heads)
        try {
            val http = OkHttpNetworkDataAcquisition.getData(ChrysanthemumConstants.HTTP_HOST + ChrysanthemumConstants.userBasic(userId!!))
            val httpJSONObject = JSONObject(http)
            when (httpJSONObject.getInt("code")) {
                0 -> {
                    val resultJSONObject = httpJSONObject.getJSONObject("result")
                    val sex = resultJSONObject.getInt("sex")
                    user.sex = sex
                    val ageRange = resultJSONObject.getInt("age_range")
                    user.ageRange = ageRange
                    val liveProvince = resultJSONObject.getInt("live_province")
                    user.liveProvince = liveProvince
                    return user
                }
                10081004 -> {
                    return user
                }
                else -> {
                    return null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "any error", e)
            return null
        }
    }

    public fun updateUserBasic(userId: String, sex: Int, ageRange: Int, liveProvince: Int, nickName: String): Int {
        if (sex == 0 || ageRange == 0 || liveProvince == 0) {
            return -1
        }
        return try {
            val dataJSONObject = JSONObject()
            dataJSONObject.put("sex", sex)
            dataJSONObject.put("age_range", ageRange)
            dataJSONObject.put("live_province", liveProvince)
            dataJSONObject.put("nick_name", nickName)
            val result = OkHttpNetworkDataAcquisition.postStringSynchronous(ChrysanthemumConstants.HTTP_HOST + ChrysanthemumConstants.userBasic(userId!!), dataJSONObject.toString())
            val resultJSONObject = JSONObject(result)
            resultJSONObject.getInt("code")
        } catch (e: Exception) {
            Log.e(TAG, "any error", e)
            -2
        }
    }

    public interface BindResultListener {
        fun onBindFailed(errorCode: Int, desc: String?)
        fun onBindSuccess(user: User)
    }

    public fun bindQQ(openId: String, bindResultListener: BindResultListener) {
        OkHttpNetworkDataAcquisition.postString(ChrysanthemumConstants.HTTP_HOST + ChrysanthemumConstants.qqBind(openId), null, object : OnHttpResultGotListener {
            override fun onErrorGot(tag: String, desc: NetworkErrorDesc) {
                bindResultListener.onBindFailed(desc.error, desc.desc)
            }

            override fun onPostResult(tag: String, result: String) {
                try {
                    val jsonObject = JSONObject(result)
                    val code = jsonObject.getInt("code")
                    if (code == 0) {
                        val resultJSONObject = jsonObject.getJSONObject("result")
                        val register = resultJSONObject.getInt("register")
                        val token = resultJSONObject.getString("token")
                        val userId = resultJSONObject.getString("user_id")
                        saveUserIdAndToken(userId, token)
                        when (register) {
                            1 -> {
                                //登录成功
                                val sex = resultJSONObject.getInt("sex")
                                val ageRange = resultJSONObject.getInt("age_range")
                                val liveProvince = resultJSONObject.getInt("live_province")
                                bindResultListener.onBindSuccess(User(token, userId, sex, ageRange, liveProvince))
                            }
                            2 -> {
                                //尚未填写基本资料
                                bindResultListener.onBindSuccess(User(token, userId, 0, 0, 0))
                            }
                            else -> {
                                //该用户尚未注册，走注册流程
                                bindResultListener.onBindSuccess(User(token, userId, 0, 0, 0))
                            }
                        }
                    } else {
                        bindResultListener.onBindFailed(code, mContext.getString(R.string.unknown_error))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "any error", e)
                    bindResultListener.onBindFailed(-1, e.message)
                }

            }

        })

    }
}