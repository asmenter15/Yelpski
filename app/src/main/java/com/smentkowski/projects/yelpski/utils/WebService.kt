package com.smentkowski.projects.yelpski.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.smentkowski.projects.yelpski.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

class WebService {

    val YELP_API_URL = "https://api.yelp.com/"
    val YELP_API_VERSION = "v3/"

    companion object {
        private var sWebService: WebService? = null

        val instance: WebService
            get() {
                if (sWebService == null) {
                    sWebService = WebService()
                }

                return sWebService as WebService
            }
    }

    var gson: Gson
    val yelpService: YelpService

    init {
        gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(YELP_API_URL + YELP_API_VERSION)
            .build()

        yelpService = retrofit.create(YelpService::class.java)
    }


    interface YelpService {

        @GET("businesses/search")
        fun getBusinesses(@Header("Authorization") auth: String, @Query("location") location: String, @Query("term") searchTerm: String, @Query("offset") offset: Int, @Query("limit") limit: Int = 25): Call<YelpSearchResponse>

        @GET("businesses/{businessId}/reviews")
        fun getBusinessReviews(@Header("Authorization") auth: String, @Path("businessId") businessId: String): Call<YelpBusinessReviewsResponse>
    }
}