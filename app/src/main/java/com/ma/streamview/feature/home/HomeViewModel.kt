package com.ma.streamview.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ma.streamview.common.StreamException
import com.ma.streamview.common.findMostRepeatedValue
import com.ma.streamview.data.model.gql.common.CategoryEdge
import com.ma.streamview.data.model.gql.common.StreamEdge
import com.ma.streamview.data.model.gql.common.VideoNode
import com.ma.streamview.data.repo.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val error: String = "",
    val videos: List<VideoNode> = emptyList(),
    val recommendedVideos: List<VideoNode> = emptyList(),
    val mostPopularCategory: String = "",
    val recommendedStreams: List<StreamEdge> = emptyList(),
    val topCategories: List<CategoryEdge> = emptyList(),
    val topLiveChannels: List<StreamEdge> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    val isHomeEmpty: Boolean
        get() = _uiState.value.topLiveChannels.isEmpty() &&
                _uiState.value.recommendedVideos.isEmpty() &&
                _uiState.value.recommendedStreams.isEmpty()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val topStreams = mediaRepository.getTopStream(first = 20, after = null, tags = null)
                _uiState.update {
                    it.copy(
                        topLiveChannels = topStreams.data?.topStreams?.edges ?: emptyList(),
                        isLoading = false
                    )
                }
                val categories = mediaRepository.getTopCategories(20).data?.topGames?.categoryEdges
                    ?: emptyList()
                _uiState.update {
                    it.copy(
                        topCategories = categories,
                        isLoading = false
                    )
                }
                val watchedList = mediaRepository.getWatchedList()
                if (watchedList.isNotEmpty()) {
                    val mostPopularCategory = findMostRepeatedValue(watchedList.map { it.slug })
                    val videos = mediaRepository.searchVideos(
                        "",
                        mostPopularCategory.toString()
                    ).data?.searchFor?.videos?.items ?: emptyList()
                    val streams =
                        mediaRepository.searchStreams(query = mostPopularCategory.toString()).data?.searchStreams?.streamEdges
                            ?: emptyList()

                    _uiState.update {
                        it.copy(
                            topLiveChannels = topStreams.data?.topStreams?.edges ?: emptyList(),
                            topCategories = categories,
                            mostPopularCategory = mostPopularCategory.toString(),
                            recommendedVideos = videos,
                            recommendedStreams = streams,
                            isLoading = false
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun checkIfRecommendationReady() {
        viewModelScope.launch {
            try {
                val watchedList = mediaRepository.getWatchedList()
                if (watchedList.isEmpty()) {
                    return@launch
                }

                val mostPopularCategory = findMostRepeatedValue(watchedList.map { it.slug })
                val videos = mediaRepository.searchVideos(
                    "",
                    mostPopularCategory.toString()
                ).data?.searchFor?.videos?.items ?: emptyList()
                val streams =
                    mediaRepository.searchStreams(query = mostPopularCategory.toString()).data?.searchStreams?.streamEdges
                        ?: emptyList()

                _uiState.update {
                    it.copy(
                        mostPopularCategory = mostPopularCategory.toString(),
                        recommendedVideos = videos,
                        recommendedStreams = streams,
                    )
                }
            } catch (e: StreamException) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Error fetching recommendations",
                    )
                }
            }
        }
    }
}
