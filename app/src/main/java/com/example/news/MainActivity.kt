@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.news

import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
            val imageLoader = com.example.news.ui.provideImageLoader(this,RemoteModule.client)
            val nav = rememberNavController()

            NavHost(navController = nav, startDestination = "headlines") {
                composable("headlines") {
                    HeadlinesScreen(
                        title = "BBC News",
                        vm = vm,
                        imageLoader = imageLoader,
                        onOpen = { article ->
                            nav.currentBackStackEntry?.savedStateHandle?.set("article", article)
                            nav.navigate("article")
                        }
                    )
                }

                composable("article") {
                    // retrieve the article
                    val article =
                        nav.previousBackStackEntry?.savedStateHandle?.get<Article>("article")
                    ArticleDetailScreen(article = article)
                }
            }
        }
    }
}

@Composable
fun HeadlinesScreen(title: String,
                    vm: HeadlinesViewModel,
                    imageLoader: coil.ImageLoader,
                    onOpen: (Article) -> Unit
) {
    LaunchedEffect(Unit) { vm.load() }
    val state by vm.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when (val s = state) {
                is HeadLinesState.Loading -> CircularProgressIndicator(Modifier.padding(16.dp))
                is HeadLinesState.Error   -> Text("Error: ${s.message}", Modifier.padding(16.dp))
                is HeadLinesState.Success -> {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(s.items.size) { idx ->
                            val article = s.items[idx]
                            ArticleRow(article, onOpen, imageLoader)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(article: Article, onOpen: (Article) -> Unit, imageLoader: coil.ImageLoader) {
    Row(
        modifier = Modifier
            .clickable { onOpen(article) }   // clickable FIRST
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        SubcomposeAsyncImage(
            imageLoader = imageLoader,
            model = ImageRequest.Builder(LocalContext.current)
                .data(article.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            loading = {
                CircularProgressIndicator(Modifier.size(20.dp))
            },
            error = {
                Box(
                    Modifier
                        .size(72.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) { Text("-", style = MaterialTheme.typography.titleSmall)}
            },
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Crop

        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = article.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            article.sourceString?.let { Text(it, style = MaterialTheme.typography.bodySmall)}
        }
    }
}

@Composable
private fun ArticleDetailScreen(article: Article?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(article?.title ?: "Article",
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->
        if (article == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No article data")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            article.description?.let{
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            article.content?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
