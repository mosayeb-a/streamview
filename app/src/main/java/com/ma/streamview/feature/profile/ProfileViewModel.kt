package com.ma.streamview.feature.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ma.streamview.data.model.gql.common.VideoEdge
import com.ma.streamview.data.model.gql.user.User
import com.ma.streamview.data.model.helix.Stream
import com.ma.streamview.data.repo.MediaRepository
import com.ma.streamview.data.repo.source.Channel
import com.ma.streamview.feature.player.navigation.USER_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val videos: List<VideoEdge> = emptyList(),
    val streams: List<Stream> = emptyList(),
    val user: User? = null,
    val isFollowed: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    private val userId = savedStateHandle.get<String>(USER_ID)!!
//    private val userLogin = savedStateHandle.get<String>(LOGIN)!!

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val user = mediaRepository.getUser(userId)
                    .data?.getUser
                _state.update { it.copy(user = user) }

                val videos = mediaRepository.getUserVideos(userId, 30)
                    .data.userMedia.videos?.edges ?: emptyList()
                _state.update { it.copy(videos = videos) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val isUserFollowed = mediaRepository.getFollowedChannels()
                    .any { it.id == userId }
                _state.update { it.copy(isFollowed = isUserFollowed) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun followChannel(channel: Channel) {
        viewModelScope.launch {
            if (!state.value.isFollowed) {
                _state.update { it.copy(isLoading = true) }

                try {
                    mediaRepository.followChannel(channel)
                    _state.update { it.copy(isFollowed = true) }
                } finally {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun unfollowChannel(id: String) {
        viewModelScope.launch {
            if (state.value.isFollowed) {
                _state.update { it.copy(isLoading = true) }

                try {
                    mediaRepository.unfollowChannel(id)
                    _state.update { it.copy(isFollowed = false) }
                } finally {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}