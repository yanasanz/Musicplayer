package com.example.musicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.dto.Album
import com.example.musicplayer.dto.Song
import com.example.musicplayer.model.SongModel
import com.example.musicplayer.repository.Repository
import com.example.musicplayer.repository.RepositoryImpl
import kotlinx.coroutines.launch

class SongViewModel : ViewModel() {

    private val repository = RepositoryImpl()

    private val _data = MutableLiveData(SongModel())
    val data: LiveData<SongModel>
        get() = _data

    init {
        getAlbum()
    }

    fun getAlbum() {
        viewModelScope.launch {
            repository.getAlbum(object : Repository.GetAlbumCallback {
                override fun onResult(album: Album) {
                    if (album.tracks != null) {
                        getAlbumWithTime(album)
                    }
                    _data.postValue(SongModel(album = album, currentSong = album.tracks?.first()))
                }
                override fun onError(e: Exception) {
                    _data.postValue(SongModel(error = true))
                }
            })
        }
    }

    fun getAlbumWithTime(album: Album): Album {
        viewModelScope.launch {
            if (album.tracks != null) {
                repository.setTracksDuration(album.tracks)
            }
        }
        return album
    }

    fun playSong(currentSong: Song) {
        val tracks = _data.value?.album?.tracks?.map { track ->
            if (track == currentSong) track.copy(isPlaying = true)
            else track.copy(isPlaying = false)
        }
        val album = _data.value?.album?.copy(tracks = tracks)
        _data.postValue(SongModel(album = album, currentSong = currentSong))
    }

    fun pauseSong(currentSong: Song) {
        val tracks = _data.value?.album?.tracks?.map { track -> track.copy(isPlaying = false) }
        val album = _data.value?.album?.copy(tracks = tracks)
        _data.postValue(SongModel(album = album, currentSong = currentSong))
    }

    fun findCurrentSong(): Song? = _data.value?.currentSong

    fun findFirst(): Song? = _data.value?.album?.tracks?.first()
}