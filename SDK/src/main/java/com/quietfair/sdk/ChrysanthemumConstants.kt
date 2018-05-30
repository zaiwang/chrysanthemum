package com.quietfair.sdk

object ChrysanthemumConstants {
    @JvmField
    val HTTP_HOST = "http://192.168.31.54:3000/"

    @JvmStatic
    public fun qqBind(openId: String): String {
        return "users/account/qq/$openId"
    }

    @JvmStatic
    public fun userBasic(userId: String): String {
        return "users/account/basic/$userId"
    }

}