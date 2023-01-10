package com.example.musicplayer.dto

data class Song(
    val id: Long = 0L,
    val file: String = "",
    var duration: String = "",
    var isPlaying : Boolean = false
)
