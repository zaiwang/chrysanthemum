package com.quietfair.sdk.user

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.quietfair.sdk.ChrysanthemumConstants
import com.quietfair.utils.http.OkHttpNetworkDataAcquisition
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object UserManager {
    private val TAG = "UserManager"
    private val mGson = Gson()

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
        val heads = mapOf(Pair("user_token", token))
        OkHttpNetworkDataAcquisition.setHttpHeaders(heads)
        try {
            val http = OkHttpNetworkDataAcquisition.getData(ChrysanthemumConstants.HTTP_HOST + ChrysanthemumConstants.userBasic(userId!!))
            val httpJSONObject = JSONObject(http)
            return when (httpJSONObject.getInt("code")) {
                0, 10081004 -> {
                    val resultJSONObject = httpJSONObject.getJSONObject("result")
                    mGson.fromJson(resultJSONObject.toString(), User::class.java)
                }
                else -> {
                    null
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

    public fun bindQQ(openId: String, accessToken: String): User? {
        try {
            val result = OkHttpNetworkDataAcquisition.postStringSynchronous(ChrysanthemumConstants.HTTP_HOST + ChrysanthemumConstants.qqBind(openId, accessToken), null)
            val jsonObject = JSONObject(result)
            val code = jsonObject.getInt("code")
            if (code == 0) {
                val resultJSONObject = jsonObject.getJSONObject("result")
                val token = resultJSONObject.getString("token")
                val userId = resultJSONObject.getString("_id")
                saveUserIdAndToken(userId, token)
                return mGson.fromJson(resultJSONObject.toString(), User::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "any error", e)
        }
        return null
    }
}