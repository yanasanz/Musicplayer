package com.example.musicplayer.model

import com.example.musicplayer.dto.Album
import com.example.musicplayer.dto.Song

class SongModel (
    val album: Album? = null,
    val currentSong: Song? = null,
    val error: Boolean = false
)