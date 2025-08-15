package com.example.news.data.remote

import com.squareup.moshi.JsonClass

// STORY 1: Model that maps the answer from NewsAPI to list and detail
@JsonClass(generateAdapter = true)
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleObject>
)

@JsonClass(generateAdapter = true)
data class ArticleObject(
    val source: SourceObject?,
    val author: String?,
    val title: String?,       // Story 1: title in the cell
    val description: String?, // Story 2: detail > description
    val url: String?,
    val url2Image: String?,      // Story 1&2 Image URL
    val whenPublished: String?, // Story 1: ordering by data
    val text: String?            // Story 2: detail > text
)

@JsonClass(generateAdapter = true) // Generate adapter for Source Object being transmitted
data class SourceObject(           // Data class for source fields
    val id: String?,               // source id
    val name: String?              // source display name
)
