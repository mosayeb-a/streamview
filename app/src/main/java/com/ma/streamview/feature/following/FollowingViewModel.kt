package com.ma.streamview.feature.following

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ma.streamview.data.model.gql.user.UserMedia
import com.ma.streamview.data.model.gql.user.UserNode
import com.ma.streamview.data.repo.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowingState(
    val channels: List<UserNode> = emptyList(),
    val liveChannels: List<UserMedia> = emptyList(),
    val usernames: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FollowingState())
    val state: StateFlow<FollowingState> = _state

    init {
        loadFollowingData()
    }

    private fun loadFollowingData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val followedChannels = mediaRepository.getFollowedChannels()
            val userIds = followedChannels.map { it.id }
            val userMedia = mediaRepository.getUserStreams(userIds).data?.userStreams ?: emptyList()

            _state.update {
                it.copy(
                    channels = followedChannels,
                    liveChannels = userMedia,
                    isLoading = false
                )
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val newChannels = mediaRepository.getFollowedChannels()
            if (_state.value.channels.size == newChannels.size) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val userIds = newChannels.map { it.id }
            val streams = mediaRepository.getUserStreams(userIds).data?.userStreams ?: emptyList()

            _state.update {
                it.copy(
                    channels = newChannels,
                    liveChannels = streams,
                    isLoading = false
                )
            }
        }
    }

    fun getChannelById(id: String) {
        viewModelScope.launch { mediaRepository.getChannelById(id) }
    }
}
