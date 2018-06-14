package com.quietfair.sdk.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        @SerializedName("token")
        var token: String = "",

        @SerializedName("sn")
        var sn: String = "",

        @SerializedName("nick_name")
        var nickName: String? = null,

        @SerializedName("_id")
        var userId: String = "",

        @SerializedName("gender")
        var gender: String? = null,

        @SerializedName("birth_year")
        var birthYear: Int = 0,

        @SerializedName("live_province")
        var liveProvince: String? = null,

        @SerializedName("live_city")
        var liveCity: String? = null,

        @SerializedName("avatar")
        var avatar: String? = null
) : Parcelable