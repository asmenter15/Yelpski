package com.smentkowski.projects.yelpski

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smentkowski.projects.yelpski.utils.NetworkManager
import com.smentkowski.projects.yelpski.utils.inflate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_business.view.*


interface MainInterface {
    fun searchEntered(searchTerm: String)
}

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var mAdapter: BusinessListAdapter? = null
    private var layoutManager: LinearLayoutManager? = null

    private var currentSearchTerm: String = ""
    private var searchOffset: Int = 0
    private var gettingNextOffset: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdapter = BusinessListAdapter(this)
        businessRecyclerView.adapter = mAdapter
        layoutManager = LinearLayoutManager(this)
        businessRecyclerView.layoutManager = layoutManager
        businessRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val currentlyVisible = layoutManager!!.findLastCompletelyVisibleItemPosition()
                if (currentlyVisible > 0.3 * mAdapter!!.businesses.size) {
                    nextSearchOffsetRequested()
                }
            }
        })

        searchButton.setOnClickListener {_ ->
            searchSubmitted(searchEditText.text.toString())
        }
    }

    private fun searchSubmitted(searchTerm: String) {
        currentSearchTerm = searchTerm
        searchOffset = 0

        doSearch()
    }

    fun nextSearchOffsetRequested() {
        if (!gettingNextOffset) {
            gettingNextOffset = true

            doSearch()
        }
    }

    private fun doSearch() {
        // Hide Keyboard
        val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if (searchOffset == 0) {
            progressBar.visibility = View.VISIBLE
            businessRecyclerView.visibility = View.GONE
        }
        noBusinessesContainer.visibility = View.GONE

        NetworkManager.searchBusinessesInSanDiego(this, currentSearchTerm, searchOffset, object: NetworkManager.NetworkingCallback<Array<Business>> {
            override fun onNetworkResponse(success: Boolean, response: Array<Business>?) {
                progressBar.visibility = View.GONE

                if (success && response != null && response.isNotEmpty()) {
                    // If searchOffset is 0, new search so reset list
                    if (searchOffset == 0) {
                        mAdapter?.businesses = response.toMutableList()
                        businessRecyclerView.scrollToPosition(0)
                    } else {
                        mAdapter?.businesses?.addAll(response)
                    }
                    mAdapter?.notifyDataSetChanged()
                    businessRecyclerView.visibility = View.VISIBLE
                    searchOffset += response.size
                } else {
                    showNoBusinessesView()
                }

                gettingNextOffset = false
            }
        })
    }

    fun showNoBusinessesView() {
        noDataTextView.text = getString(R.string.NO_BUSINESSES_FOUND)
        noDataImageView.background = getDrawable(R.drawable.business_icon)

        noBusinessesContainer.visibility = View.VISIBLE
    }

    class BusinessListAdapter(private var context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var businesses = mutableListOf<Business>()
            set(value) {
                notifyDataSetChanged()
                field = value
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return BusinessViewHolder(parent.inflate(R.layout.list_item_business))
        }

        override fun getItemCount(): Int {
            return businesses.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val business = businesses[position]
            if (holder is BusinessViewHolder) {
                if (business.imageUrl.isNotEmpty()) {
                    Picasso.with(context).load(business.imageUrl).into(holder.imageView)
                } else {
                    Picasso.with(context).load(R.drawable.broken_image).into(holder.imageView)
                }
                holder.titleTextView.text = business.name
                if (business.topReview.isNullOrEmpty()) {
                    holder.topReviewTextView.text = context.getString(R.string.NO_REVIEWS)
                } else {
                    holder.topReviewTextView.text = business.topReview
                }
            }
        }

        class BusinessViewHolder(val view: View): RecyclerView.ViewHolder(view) {
            var imageView = view.businessImageView
            var titleTextView = view.businessTitleTextView
            var topReviewTextView = view.businessTopReviewTextView
        }
    }
}
