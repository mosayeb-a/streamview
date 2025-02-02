package com.ma.streamview.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ma.streamview.common.StreamException
import com.ma.streamview.common.UiMessage
import com.ma.streamview.common.UiMessageManager
import com.ma.streamview.data.model.gql.common.CategoryEdge
import com.ma.streamview.data.model.gql.common.StreamEdge
import com.ma.streamview.data.model.gql.common.UserEdge
import com.ma.streamview.data.model.gql.common.VideoNode
import com.ma.streamview.data.repo.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val isLoading: Boolean = false,
    val videos: List<VideoNode> = emptyList(),
    val streams: List<StreamEdge> = emptyList(),
    val categories: List<CategoryEdge> = emptyList(),
    val channels: List<UserEdge> = emptyList(),
    val query: String = "",
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        queryFlow
            .debounce(300)
            .onEach { query ->
                if (query.isNotBlank()) {
                    search(query)
                } else {
                    clearResults()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        if (_state.value.query == newQuery) {
            return
        }

        _state.update { it.copy(
            query = newQuery,
            isLoading = newQuery.isNotBlank()
        )}

        queryFlow.value = newQuery
    }

    private fun clearResults() {
        _state.update { it.copy(
            isLoading = false,
            videos = emptyList(),
            streams = emptyList(),
            categories = emptyList(),
            channels = emptyList(),
        )}
    }

    private fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }


                val videoResult = mediaRepository
                    .searchVideos(cursor = "", query = query)
                    .data?.searchFor?.videos?.items ?: emptyList()
                _state.update { it.copy(videos = videoResult) }


                val streamResult = mediaRepository
                    .searchStreams(query = query)
                    .data?.searchStreams?.streamEdges ?: emptyList()
                _state.update { it.copy(streams = streamResult) }


                val categoryResult = mediaRepository
                    .searchCategories(query = query)
                    .data?.searchCategories?.categoryEdges ?: emptyList()
                _state.update { it.copy(categories = categoryResult) }


                val channelResult = mediaRepository
                    .searchChannels(query = query)
                    .data?.searchUsers?.edges ?: emptyList()
                _state.update { it.copy(channels = channelResult) }

            } catch (e: StreamException) {
                viewModelScope.launch {
                    UiMessageManager.sendEvent(
                        event = UiMessage(
                            message = "Something wrong about playback source",
                        )
                    )
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}