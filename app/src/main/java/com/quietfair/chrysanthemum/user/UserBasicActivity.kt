package com.quietfair.chrysanthemum.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.quietfair.chrysanthemum.MainActivity
import com.quietfair.chrysanthemum.R
import com.quietfair.sdk.user.UserManager
import kotlinx.android.synthetic.main.activity_user_basic.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class UserBasicActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    private val TAG = "UserBasicActivity"
    private var userId: String? = null
    private lateinit var adapter: ArrayAdapter<CharSequence>
    private var ageRange = 0
    private var liveProvince = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_basic)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setTitle(R.string.title_basic)
        userId = intent?.getStringExtra("quietfair.user_id")
        if (userId == null) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        province_select_textView.setOnClickListener(this)
        adapter = ArrayAdapter.createFromResource(this, R.array.age_range, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun updateUserBasic() = runBlocking {
        val loadingProgressBar = ProgressDialog.show(this@UserBasicActivity, null, getString(R.string.loading))
        Log.d(TAG, "start update")
        var resultCode = -10
        val sex = if (radioButtonMale.isChecked) {
            1
        } else {
            2
        }
        val updateJob = launch(CommonPool) {
            resultCode = UserManager.updateUserBasic(userId!!, sex, ageRange, liveProvince, name_editText.text.toString())
        }
        updateJob.join()
        loadingProgressBar.dismiss()
        if (resultCode == 0) {
            startActivity(Intent(this@UserBasicActivity, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this@UserBasicActivity, R.string.unknown_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        ageRange = position + 1
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.province_select_textView -> {
                startActivityForResult(Intent(this, ProvinceSelectActivity::class.java), 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            0 -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        province_select_textView.text = data.getStringExtra("quietfair.province")
                        liveProvince = data.getIntExtra("quietfair.province_index", 0)
                        province_select_textView.setTextColor(Color.BLACK)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_basic_submit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.action_save -> {
            if (name_editText.text.toString() != "") {
                if (liveProvince == 0) {
                    Toast.makeText(this@UserBasicActivity, R.string.no_location_set, Toast.LENGTH_SHORT).show()
                    false
                } else {
                    updateUserBasic()
                    true
                }
            } else {
                name_editText.error = getString(R.string.name_cannot_empty)
                name_editText.requestFocus()
                false
            }
        }
        else ->
            super.onOptionsItemSelected(item)
    }

}
