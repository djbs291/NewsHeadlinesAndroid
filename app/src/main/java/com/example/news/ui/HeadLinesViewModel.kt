package com.example.news.ui

import androidx.lifecycle.ViewModel             // base ViewModel class
import androidx.lifecycle.viewModelScope        // Coroutine scope tied to Viewmodel
import com.example.news.data.NewsRepository     // repository dependency
import com.example.news.domain.Article          // domain model
import kotlinx.coroutines.flow.MutableStateFlow // State holder (mutable)
import kotlinx.coroutines.flow.StateFlow        // launch coroutines
import kotlinx.coroutines.launch                // launch coroutines

sealed interface HeadLinesState {                                 // Sealed UI state for the screen
    data object Loading : HeadLinesState                          // loading state while fetching
    data class Success(val items: List<Article>) : HeadLinesState // Success with data
    data class Error(val message: String) : HeadLinesState        // error with message
}

class HeadlinesViewModel(               // ViewModel
    private val repo: NewsRepository    // Inject repository
) : ViewModel() {
    private val _state = MutableStateFlow<HeadLinesState>(HeadLinesState.Loading) // backing state
    val state: StateFlow<HeadLinesState> = _state                                         // public state>

    fun load(){                                    // Trigger fetching
        _state.value = HeadLinesState.Loading      // Set Loading
        viewModelScope.launch {                    // launch coroutine
            runCatching { repo.getTopHeadlines() } // call repository
                .onSuccess { articles ->           // when successful
                    _state.value = HeadLinesState.Success(articles) // push headlines
                }
                .onFailure { t ->              // when failed
                    _state.value = HeadLinesState.Error(t.message ?: "Error") // push error
                }
        }
    }
}
