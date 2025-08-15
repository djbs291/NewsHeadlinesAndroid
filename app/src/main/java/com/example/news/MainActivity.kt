@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.news.data.NewsRepository
import com.example.news.data.remote.RemoteModule
import com.example.news.domain.Article
import com.example.news.ui.HeadLinesState
import com.example.news.ui.HeadlinesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val api = RemoteModule.provideApi()
        val repo = NewsRepository(
            api = api,
            apiKey = BuildConfig.NEWS_API_KEY,
            sourceId = "bbc-news"
        )
        val vm = HeadlinesViewModel(repo)

        setContent {
            MaterialTheme {
                HeadlinesScreen(title = "BBC News", vm = vm)
            }
        }
    }
}

@Composable
fun HeadlinesScreen(title: String, vm: HeadlinesViewModel) {
    LaunchedEffect(Unit) { vm.load() }
    val state by vm.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when (val s = state) {
                is HeadLinesState.Loading -> CircularProgressIndicator(Modifier.padding(16.dp))
                is HeadLinesState.Error   -> Text("Error: ${s.message}", Modifier.padding(16.dp))
                is HeadLinesState.Success -> {
                    LazyColumn {
                        items(s.items) { article ->
                            ArticleRow(article)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(article: Article) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Story 1 only */ }
            .padding(12.dp)
    ) {
        AsyncImage(
            model = article.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = article.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            article.sourceString?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
