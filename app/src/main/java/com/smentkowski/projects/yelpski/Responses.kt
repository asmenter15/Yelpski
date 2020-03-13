package com.smentkowski.projects.yelpski

import com.google.gson.annotations.SerializedName

data class YelpSearchResponse(
    var total: Int,
    var businesses: Array<Business>
)

data class YelpBusinessReviewsResponse(
    var reviews: Array<BusinessReview>
)

data class BusinessReview (
    var text: String,
    var rating: Int
)

data class Business (
    var id: String,
    var name: String,
    @SerializedName("image_url")
    var imageUrl: String,
    var topReview: String?
)
