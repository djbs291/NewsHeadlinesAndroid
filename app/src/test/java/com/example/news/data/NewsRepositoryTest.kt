package com.example.news.data

import com.example.news.data.remote.ArticleObject
import com.example.news.data.remote.NewsApi
import com.example.news.data.remote.NewsResponse
import com.example.news.data.remote.SourceObject
import com.example.news.domain.Article
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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
        Assert.assertEquals(2, items.size)
        Assert.assertEquals("Newer", items[0].title)
        Assert.assertEquals("Older", items[1].title)

        // spot-check mapping
        Assert.assertEquals("BBC News", items[0].sourceString)
        Assert.assertEquals("2025-01-02T10:00:00Z", items[0].whenPublished)
        Assert.assertEquals("https://example.com/a.jpg", items[0].imageUrl)
        Assert.assertEquals("https://example.com/a", items[0].url)
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

        Assert.assertEquals(1, items.size)
        Assert.assertEquals("(no title)", items.first().title)
    }
}