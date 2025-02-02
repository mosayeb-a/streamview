package com.ma.streamview.feature.player

import androidx.annotation.OptIn
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import com.ma.streamview.common.TwitchHelper
import com.ma.streamview.common.UiMessage
import com.ma.streamview.common.UiMessageManager
import com.ma.streamview.data.repo.MediaRepository
import com.ma.streamview.data.repo.source.Channel
import com.ma.streamview.data.repo.source.Watched_video
import com.ma.streamview.feature.player.navigation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val videoUrl: String = "",
    val isLoading: Boolean = false,
    val playWhenReady: Boolean = false,
    val currentPosition: Long = 0L,
    val totalPosition: Long = 0L,
    val isFollowed: Boolean = false,
    val isPlayerReady: Boolean = false,
    val isSubOnly: Boolean = false,
)

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerViewmodel @OptIn(UnstableApi::class) @Inject constructor(
    private val mediaRepository: MediaRepository,
    var player: ExoPlayer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    private val videoThumb = savedStateHandle.get<String>(PLAYER_URL)!!
    private val videoId = savedStateHandle.get<String>(PLAYER_ID)!!
    private val slugName = savedStateHandle.get<String>(SLUG_NAME)!!
    private val isStream = savedStateHandle.get<Boolean>(IS_STREAM)!!
    private val userId = savedStateHandle.get<String>(USER_ID)!!
    private val userLogin = savedStateHandle.get<String>(USERNAME)!!

    private val playerListener = object : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updatePositions()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    _state.update { it.copy(isLoading = false) }
                    println("STATEPlayerViewModel. STATE_READY")
                }
                Player.STATE_IDLE -> {
                    println("STATEPlayerViewModel. STATE_IDLE")
                }
                Player.STATE_BUFFERING -> {
                    _state.update { it.copy(isLoading = true) }
                    println("STATEPlayerViewModel. STATE_BUFFERING")
                }
                Player.STATE_ENDED -> {
                    println("STATEPlayerViewModel. STATE_BUFFERING")
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            when (error) {
                is ExoPlaybackException -> {
                    when (error.type) {
                        ExoPlaybackException.TYPE_SOURCE -> {
                            if (!isStream) {
                                _state.update { it.copy(isSubOnly = true, isLoading = true) }
                                val urls = TwitchHelper.getVideoUrlMapFromPreviewHelix(
                                    url = videoThumb,
                                    type = "vod"
                                )
                                val url = urls.values.first()
                                setMediaSource(url)
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        videoUrl = url,
                                        isSubOnly = false
                                    )
                                }
                            } else {
                                viewModelScope.launch {
                                    UiMessageManager.sendEvent(
                                        event = UiMessage(
                                            message = "Something wrong about playback source",
                                        )
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    init {
        player.addListener(playerListener)
        viewModelScope.launch {
            val result = if (!isStream) {
                mediaRepository.getPlaybackUrl(vodId = videoId, channelName = null)
            } else {
                mediaRepository.getPlaybackUrl(channelName = userLogin, vodId = null)
            }
            _state.update { it.copy(videoUrl = result) }

            if (state.value.videoUrl.isNotEmpty()) {
                setMediaSource(state.value.videoUrl)
            }
        }

        viewModelScope.launch {
            val isUserFollowed = mediaRepository.getFollowedChannels()
                .any { it.id == userId }
            _state.update { it.copy(isFollowed = isUserFollowed) }
        }
        addToWatchedList(state.value.currentPosition)
    }

    private fun addToWatchedList(maxWatchedPositions: Long) {
        viewModelScope.launch {
            val watchedVideo = Watched_video(
                id = videoId,
                userId = videoId,
                maxPositionSeen = maxWatchedPositions,
                slug = slugName
            )
            mediaRepository.addToWatchedList(
                video = watchedVideo,
                maxWatchedPosition = maxWatchedPositions
            )
        }
    }

    fun removeFromWatchedList(id: String) {
        viewModelScope.launch {
            mediaRepository.removeFromWatchedList(id)
        }
    }

    fun followChannel(channel: Channel) {
        viewModelScope.launch {
            if (!state.value.isFollowed) {
                mediaRepository.followChannel(channel)
                _state.update { it.copy(isFollowed = true) }
            }
        }
    }

    fun unfollowChannel(id: String) {
        viewModelScope.launch {
            if (state.value.isFollowed) {
                mediaRepository.unfollowChannel(id)
                _state.update { it.copy(isFollowed = false) }
            }
        }
    }

    private fun setMediaSource(uri: String) {
        val hlsMediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(uri))
        player.setMediaSource(hlsMediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(playerListener)
        player.release()
    }

    fun updatePositions() {
        _state.update {
            it.copy(
                currentPosition = player.currentPosition,
                totalPosition = player.duration
            )
        }
    }

    fun seekTo(position: Long) {
        _state.update { it.copy(currentPosition = player.currentPosition) }
        player.seekTo(position)
    }

    fun play() {
        if (player.isPlaying) return
        player.playWhenReady = true
    }

    fun pause() {
        if (!player.isPlaying) return
        player.playWhenReady = false
    }

    fun seekForward() {
        val newPosition = minOf(player.currentPosition + 10_000, player.duration)
        _state.update { it.copy(currentPosition = newPosition) }
        player.seekTo(newPosition)
    }

    fun seekBackward() {
        val newPosition = maxOf(player.currentPosition - 10_000, 0)
        _state.update { it.copy(currentPosition = newPosition) }
        player.seekTo(newPosition)
    }

    fun isPlaying(): Boolean = player.isPlaying
}