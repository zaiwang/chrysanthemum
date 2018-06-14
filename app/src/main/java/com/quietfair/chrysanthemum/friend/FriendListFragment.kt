package com.quietfair.chrysanthemum.friend

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quietfair.chrysanthemum.R
import com.quietfair.sdk.user.User
import com.quietfair.sdk.user.UserManager

class FriendListFragment : Fragment() {
    private val TAG = "FriendListFragment"

    companion object {
        fun newInstance() = FriendListFragment()
    }

    private lateinit var viewModel: FriendListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.friend_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FriendListViewModel::class.java)
        val userId = UserManager.getUserId()
        userId?.let {
            viewModel.getFriends(it).observe(this, Observer<List<User>> {
                Log.d(TAG, "get friends:$it")
            })
        }

    }

}
