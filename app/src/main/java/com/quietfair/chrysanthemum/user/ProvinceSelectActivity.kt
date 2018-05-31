package com.quietfair.chrysanthemum.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quietfair.chrysanthemum.R
import java.io.InputStreamReader

class ProvinceSelectActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_province_select)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val inputStream =  resources.assets.open("province_city.json")

        val chinaLocations: ArrayList<ChinaLocation> = Gson().fromJson(InputStreamReader(inputStream), object : TypeToken<List<ChinaLocation>>() {}.type)

        viewManager = LinearLayoutManager(this)
        viewAdapter = LocationsAdapter(this, chinaLocations)
        val dividerItemDecoration = DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL)
        recyclerView = findViewById<RecyclerView>(R.id.location_list_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    class LocationsAdapter(private val activity: ProvinceSelectActivity, private val locations: List<ChinaLocation>) : RecyclerView.Adapter<LocationsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
            return ViewHolder(activity, v, locations)
        }

        override fun getItemCount() = locations.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = locations[position].name
        }

        class ViewHolder(private val activity: ProvinceSelectActivity, v: View, private val locations: List<ChinaLocation>) : RecyclerView.ViewHolder(v) {
            val textView: TextView

            init {
                v.setOnClickListener {
                    Log.d(TAG, "Element $adapterPosition clicked.")
                    val intent = Intent()
                    intent.putExtra("quietfair.province", locations[adapterPosition].name)
                    intent.putExtra("quietfair.province_index", (adapterPosition + 1))
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
                textView = v.findViewById(R.id.content_textview)
            }
        }

    }

    companion object {
        private val TAG = "ProvinceSelectActivity"
    }
}
