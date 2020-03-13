package com.smentkowski.projects.yelpski

import com.google.gson.GsonBuilder
import com.smentkowski.projects.yelpski.utils.NetworkManager
import com.smentkowski.projects.yelpski.utils.WebService
import com.smentkowski.projects.yelpski.utils.YELP_API_URL
import com.smentkowski.projects.yelpski.utils.YELP_API_VERSION
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class YelpAPIServiceTest {

    private var mockWebServer = MockWebServer()
    private lateinit var yelpService: WebService.YelpService

    private var sampleYelpSearchResponse: String = "{ \"total\": 8228, \"businesses\": [ { \"rating\": 4, \"price\": \"\$\", \"phone\": \"+14152520800\", \"id\": \"E8RJkjfdcwgtyoPMjQ_Olg\", \"alias\": \"four-barrel-coffee-san-francisco\", \"is_closed\": false, \"categories\": [ { \"alias\": \"coffee\", \"title\": \"Coffee & Tea\" } ], \"review_count\": 1738, \"name\": \"Four Barrel Coffee\", \"url\": \"https://www.yelp.com/biz/four-barrel-coffee-san-francisco\", \"coordinates\": { \"latitude\": 37.7670169511878, \"longitude\": -122.42184275 }, \"image_url\": \"http://s3-media2.fl.yelpcdn.com/bphoto/MmgtASP3l_t4tPCL1iAsCg/o.jpg\", \"location\": { \"city\": \"San Francisco\", \"country\": \"US\", \"address2\": \"\", \"address3\": \"\", \"state\": \"CA\", \"address1\": \"375 Valencia St\", \"zip_code\": \"94103\" }, \"distance\": 1604.23, \"transactions\": [\"pickup\", \"delivery\"] }], \"region\": { \"center\": { \"latitude\": 37.767413217936834, \"longitude\": -122.42820739746094 } } }"
    private var sampleYelpBusinessReviewsResponse: String = "{ \"reviews\": [ { \"id\": \"xAG4O7l-t1ubbwVAlPnDKg\", \"rating\": 5, \"user\": { \"id\": \"W8UK02IDdRS2GL_66fuq6w\", \"profile_url\": \"https://www.yelp.com/user_details?userid=W8UK02IDdRS2GL_66fuq6w\", \"image_url\": \"https://s3-media3.fl.yelpcdn.com/photo/iwoAD12zkONZxJ94ChAaMg/o.jpg\", \"name\": \"Ella A.\" }, \"text\": \"Went back again to this place since the last time i visited the bay area 5 months ago, and nothing has changed. Still the sketchy Mission, Still the cashier...\", \"time_created\": \"2016-08-29 00:41:13\", \"url\": \"https://www.yelp.com/biz/la-palma-mexicatessen-san-francisco?hrid=hp8hAJ-AnlpqxCCu7kyCWA&adjust_creative=0sidDfoTIHle5vvHEBvF0w&utm_campaign=yelp_api_v3&utm_medium=api_v3_business_reviews&utm_source=0sidDfoTIHle5vvHEBvF0w\" }, { \"id\": \"1JNmYjJXr9ZbsfZUAgkeXQ\", \"rating\": 4, \"user\": { \"id\": \"rk-MwIUejOj6LWFkBwZ98Q\", \"profile_url\": \"https://www.yelp.com/user_details?userid=rk-MwIUejOj6LWFkBwZ98Q\", \"image_url\": null, \"name\": \"Yanni L.\" }, \"text\": \"The \\\"restaurant\\\" is inside a small deli so there is no sit down area. Just grab and go.\\n\\nInside, they sell individually packaged ingredients so that you can...\", \"time_created\": \"2016-09-28 08:55:29\", \"url\": \"https://www.yelp.com/biz/la-palma-mexicatessen-san-francisco?hrid=fj87uymFDJbq0Cy5hXTHIA&adjust_creative=0sidDfoTIHle5vvHEBvF0w&utm_campaign=yelp_api_v3&utm_medium=api_v3_business_reviews&utm_source=0sidDfoTIHle5vvHEBvF0w\" }, { \"id\": \"SIoiwwVRH6R2s2ipFfs4Ww\", \"rating\": 4, \"user\": { \"id\": \"rpOyqD_893cqmDAtJLbdog\", \"profile_url\": \"https://www.yelp.com/user_details?userid=rpOyqD_893cqmDAtJLbdog\", \"image_url\": null, \"name\": \"Suavecito M.\" }, \"text\": \"Dear Mission District,\\n\\nI miss you and your many delicious late night food establishments and vibrant atmosphere.  I miss the way you sound and smell on a...\", \"time_created\": \"2016-08-10 07:56:44\", \"url\": \"https://www.yelp.com/biz/la-palma-mexicatessen-san-francisco?hrid=m_tnQox9jqWeIrU87sN-IQ&adjust_creative=0sidDfoTIHle5vvHEBvF0w&utm_campaign=yelp_api_v3&utm_medium=api_v3_business_reviews&utm_source=0sidDfoTIHle5vvHEBvF0w\" } ], \"total\": 3, \"possible_languages\": [\"en\"] }"

    @Before
    fun setup() {
        mockWebServer.start()
        val gson = GsonBuilder().create()
        yelpService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WebService.YelpService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testYelpSearch() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(sampleYelpSearchResponse)
        mockWebServer.enqueue(response)
        // Act
        val yelpSearchResponse = yelpService.getBusinesses(BuildConfig.YELP_AUTH, "San Diego", "coffee", 35).execute()

        assert(yelpSearchResponse.body() != null)
        assert((yelpSearchResponse.body()?.businesses?.size ?: 0) == 1)
        assert(yelpSearchResponse.body()!!.businesses[0].name == "Four Barrel Coffee")
        assert(yelpSearchResponse.body()!!.businesses[0].imageUrl == "http://s3-media2.fl.yelpcdn.com/bphoto/MmgtASP3l_t4tPCL1iAsCg/o.jpg")
    }

    @Test
    fun testYelpBusinessTopReview() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(sampleYelpBusinessReviewsResponse)
        mockWebServer.enqueue(response)
        // Act
        val yelpBusinessReviewsResponse = yelpService.getBusinessReviews(BuildConfig.YELP_AUTH, "1").execute()

        assert(yelpBusinessReviewsResponse.body() != null)
        assert((yelpBusinessReviewsResponse.body()?.reviews?.size ?: 0) == 3)
        assert(yelpBusinessReviewsResponse.body()!!.reviews[0].text == "Went back again to this place since the last time i visited the bay area 5 months ago, and nothing has changed. Still the sketchy Mission, Still the cashier...")
        assert(yelpBusinessReviewsResponse.body()!!.reviews[0].rating == 5)
    }
}
