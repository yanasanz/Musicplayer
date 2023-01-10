package com.example.musicplayer.repository

import com.example.musicplayer.dto.Album
import com.example.musicplayer.dto.Song

interface Repository {
    fun getAlbum(callback: GetAlbumCallback)
    suspend fun setTracksDuration(songs: List<Song>): String

    interface GetAlbumCallback {
        fun onResult(album: Album){}
        fun onError(e: Exception){}
    }

}