package com.example.news.data

import com.example.news.data.remote.NewsApi
import com.example.news.domain.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZonedDateTime


// STORY 1: Fetch headlines and sort them in descending order

class NewsRepository (
    private val api: NewsApi,     // API Service
    private val apiKey: String,   // API key for authentication
    private val sourceId: String  // Source ID (e.g. "bbc-news")
) {
    suspend fun getTopHeadlines(): List<Article> = withContext(Dispatchers.IO) {
        val resp = api.topHeadlines(sourceId, apiKey = apiKey)
        resp.articles.map { dto->
            Article(
                title = dto.title ?: "(no title)",
                imageUrl = dto.url2Image,
                whenPublished = dto.whenPublished,
                sourceString = dto.source?.name,
                description = dto.description,
                content = dto.text,
                url = dto.url
            )
        }.sortedByDescending { article ->
            epochMillis(article.whenPublished)
        }
    }
    private fun epochMillis(value: String?): Long {    // robust ISO-8601 parsing
        if (value.isNullOrBlank()) return 0L           // empty → oldest
        return try {                                    // try Instant first (e.g., 2025-01-01T10:00:00Z)
            Instant.parse(value).toEpochMilli()
        } catch (_: Exception) {
            try {                                       // try OffsetDateTime
                OffsetDateTime.parse(value).toInstant().toEpochMilli()
            } catch (_: Exception) {
                try {                                   // try ZonedDateTime
                    ZonedDateTime.parse(value).toInstant().toEpochMilli()
                } catch (_: Exception) {
                    0L                                  // fallback to oldest
                }
            }
        }
    }

}
