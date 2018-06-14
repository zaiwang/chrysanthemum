package com.quietfair.chrysanthemum.friend

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Handler
import com.quietfair.sdk.friend.FriendManager
import com.quietfair.sdk.user.User
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class FriendListViewModel : ViewModel() {
    private val TAG = "FriendListViewModel"

    private val mHandler = Handler()

    fun getFriends(userId: String): LiveData<List<User>> {
        val users = MutableLiveData<List<User>>()
        launch(CommonPool) {
            mHandler.post {
                users.value = FriendManager.obtainFriends(userId)
            }
        }
        return users
    }

}
