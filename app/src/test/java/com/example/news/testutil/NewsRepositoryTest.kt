package com.example.news.testutil

import com.example.news.data.remote.ArticleObject
import com.example.news.data.remote.NewsApi
import com.example.news.data.remote.NewsResponse
import com.example.news.data.remote.SourceObject
import com.example.news.data.NewsRepository
import com.example.news.domain.Article
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for story 1 behavior (mapping + sorting) using a fake NewsApi
*/
class NewsRepositoryTest {

    private class FakeNewsApi(private val response: NewsResponse) : NewsApi {
        override suspend fun topHeadlines(
            sources: String,
            pageSize: Int,
            apiKey: String
        ): NewsResponse = response
    }

    @Test
    fun `maps DTOs to domain and sorts by publishedAt desc`() = runTest {
        // Two with different wherePublished timestamps
        val newer = ArticleObject(
            source = SourceObject(id = "bbc-news", name = "BBC News"),
            author = "Author A",
            title = "Newer",
            description = "desc A",
            url = "https://example.com/a",
            url2Image = "https://example.com/a.jpg",
            whenPublished = "2025-01-02T10:00:00Z",
            text = "text A"
        )

        val older = ArticleObject(
            source = SourceObject(id = "bbc-news", name = "BBC News"),
            author = "Author B",
            title = "Older",
            description = "desc B",
            url = "https://example.com/a",
            url2Image = "https://example.com/a.jpg",
            whenPublished = "2025-01-01T10:00:00Z",
            text = "text B"
        )
        val api = FakeNewsApi(
            NewsResponse(
                status = "ok",
                totalResults = 2,
                articles = listOf(newer, older)
            )
        )
        val repo = NewsRepository(
            api = api,
            apiKey = "fake",
            sourceId = "bbc-news"
        )

        // When
        val items: List<Article> = repo.getTopHeadlines()

        // Map field and sort newest first
        assertEquals(2, items.size)
        assertEquals("Newer", items[0].title)
        assertEquals("Older", items[1].title)

        // spot-check mapping
        assertEquals("BBC News", items[0].sourceString)
        assertEquals("2025-01-02T10:00:00Z", items[0].whenPublished)
        assertEquals("https://example.com/a.jpg", items[0].imageUrl)
    }
    @Test
    fun `gracefully handles null title`() = runTest {
        val transmitted = ArticleObject(
            source = null,
            author = null,
            title = null,
            description = null,
            url = null,
            url2Image = null,
            whenPublished = null,
            text = null
        )
        val api = FakeNewsApi(
            NewsResponse(status = "ok", totalResults = 1, articles = listOf(transmitted))
        )

        val repo = NewsRepository(api, "dummy", "bbc-news")

        val items = repo.getTopHeadlines()

        assertEquals(1, items.size)
        assertEquals("(no title)", items.first().title)
    }
}
