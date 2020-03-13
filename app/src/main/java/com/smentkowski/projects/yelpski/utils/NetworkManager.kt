package com.smentkowski.projects.yelpski.utils

import android.content.Context
import android.util.Log
import com.smentkowski.projects.yelpski.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NetworkManager {

    private val TAG = NetworkManager::class.java.simpleName

    const val RETRY_COUNT = 3

    interface NetworkingCallback<T> {
        fun onNetworkResponse(success: Boolean, response: T?)
    }

    fun searchBusinessesInSanDiego(context: Context, searchTerm: String, offset: Int, callback: NetworkingCallback<Array<Business>>) {
        WebService.instance.yelpService.getBusinesses(BuildConfig.YELP_AUTH, "San Diego", searchTerm, offset).enqueue(object: Callback<YelpSearchResponse> {
            override fun onResponse(call: Call<YelpSearchResponse>, response: Response<YelpSearchResponse>) {

                // TODO: Migrate this to use RXKotlin so we don't have to wait on each request and block the UI in the meantime

                Log.i(TAG,"term:" + searchTerm + " offset: " + offset + ":" + response.toString())
                Log.i(TAG,"term:" + searchTerm + " offset: " + offset + ":" + response.body().toString())

                var businessReviewsProcessedCounter = 0
                val businesses = response.body()?.businesses

                // IF no businesses, just return success
                if (businesses?.size == 0) {
                    callback.onNetworkResponse(true, businesses)

                // Otherwise iterate each business and get the top review
                } else {
                    businesses?.forEach { business ->
                        NetworkManager.getTopReviewForBusiness(business.id, object: NetworkingCallback<String> {
                            override fun onNetworkResponse(success: Boolean, response: String?) {
                                businessReviewsProcessedCounter++

                                if (success) {
                                    business.topReview = response
                                }

                                if (businessReviewsProcessedCounter == businesses.size) {
                                    callback.onNetworkResponse(true, businesses)
                                }
                            }
                        })
                    }
                }
            }

            override fun onFailure(call: Call<YelpSearchResponse>, t: Throwable) {
                callback.onNetworkResponse(false, null)
            }
        })
    }

    /**
     * I had to add some unfortunate retry logic here because I kept getting random 429's back from yelp api, which apparently means I am over the rate limit
     * But when I check my dashboard I am nowhere near the limit, so not sure what is going on there.
     * Decided to leave it in just to show a bit of recursion I am using to manage the retries. In a real world case, I would most likely add exponential
     * backoff and put this into an interceptor vs on the individual request.
     */

    fun getTopReviewForBusiness(businessId: String, callback: NetworkingCallback<String>, retries: Int = 0) {
        var retriesMod = retries
        if (retriesMod == RETRY_COUNT) {
            callback.onNetworkResponse(false, "")
        } else {
            WebService.instance.yelpService.getBusinessReviews(BuildConfig.YELP_AUTH, businessId).enqueue(object: Callback<YelpBusinessReviewsResponse> {
                override fun onResponse(call: Call<YelpBusinessReviewsResponse>, response: Response<YelpBusinessReviewsResponse>) {
                    var topReview: BusinessReview? = null

                    Log.i(TAG,"bID:" + businessId + ":" + response.toString())

                    if (response.body() == null) {
                        retriesMod++
                        NetworkManager.getTopReviewForBusiness(businessId, callback, retriesMod)
                    } else {
                        response.body()?.reviews?.forEach { businessReview ->
                            if (topReview == null) {
                                topReview = businessReview
                            } else {
                                if (businessReview.rating > topReview!!.rating) {
                                    topReview = businessReview
                                }
                            }
                        }

                        callback.onNetworkResponse(true, topReview?.text ?: "")
                    }
                }

                override fun onFailure(call: Call<YelpBusinessReviewsResponse>, t: Throwable) {
                    callback.onNetworkResponse(false, "")
                }
            })
        }
    }

}