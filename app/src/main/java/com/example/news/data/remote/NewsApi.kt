package com.example.news.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Story 1: fetch top headlines
interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("sources") sources: String,
        @Query("pageSize") pageSize: Int = 50,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
