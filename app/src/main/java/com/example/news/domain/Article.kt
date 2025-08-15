package com.example.news.domain

data class Article(
    val title: String,           // STORY 1: Title in the cell
    val description: String? = null,    // STORY 2: Detail
    val content: String? = null,        // STORY 2: Detail
    val imageUrl: String?,       // STORY 1&2: Image
    val whenPublished: String?, // STORY 1: Ordering by data
    val sourceString: String?    // STORY 1: Name of the provider in item (optional)
)
