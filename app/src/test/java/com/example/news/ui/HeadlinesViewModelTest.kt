package com.example.news.ui

import com.example.news.data.NewsRepository
import com.example.news.data.remote.ArticleObject
import com.example.news.data.remote.NewsApi
import com.example.news.data.remote.NewsResponse
import com.example.news.data.remote.SourceObject
import com.example.news.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HeadlinesViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private class FakeApiSuccess : NewsApi {
        override suspend fun topHeadlines(sources: String, pageSize: Int, apiKey: String): NewsResponse {
            return NewsResponse(
                status = "ok",
                totalResults = 1,
                articles = listOf(
                    ArticleObject(
                        source = SourceObject("bbc-news", "BBC News"),
                        author = "A",
                        title = "Hello",
                        description = "desc",
                        url = "https://e",
                        url2Image = null,
                        whenPublished = "2025-01-01T00:00:00Z",
                        text = "content"
                    )
                )
            )
        }
    }
    private class fakeApiFail(private val message: String): NewsApi {
        override suspend fun topHeadlines(sources: String, pageSize: Int, apiKey: String): NewsResponse {
            throw IllegalStateException(message)
        }
    }

    @Test
    fun `load emits Success when repository succeeds`() = runTest {
        val repo = NewsRepository(FakeApiSuccess(), "dummy", "bbc-news")
        val vm = HeadlinesViewModel(repo)

        // initial state is Loading
        assertEquals(HeadLinesState.Loading, vm.state.first())

        vm.load()

        // wait one tick, them assert Success
        val state = vm.state.first { it !is HeadLinesState.Loading }
        assert(state is HeadLinesState.Success)
        state as HeadLinesState.Success
        assertEquals(1, state.items.size)
        assertEquals("Hello", state.items.first().title)
    }

    @Test
    fun `load emits Error when repository fails`() = runTest {
        val repo = NewsRepository(fakeApiFail("error"), "dummy", "bbc-news")
        val vm = HeadlinesViewModel(repo)

        vm.load()

        val state = vm.state.first { it !is HeadLinesState.Loading }
        assert(state is HeadLinesState.Error)
        state as HeadLinesState.Error

        assert(state.message.isNotBlank())
    }
}

