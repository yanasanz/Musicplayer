package com.example.musicplayer.repository

import android.media.MediaMetadataRetriever
import com.example.musicplayer.dto.Album
import com.example.musicplayer.dto.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class RepositoryImpl : Repository {

    private val client = OkHttpClient.Builder()
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<Album>() {}

    companion object {
        const val BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data"
    }

    override fun getAlbum(callback: Repository.GetAlbumCallback) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/album.json")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onResult(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    override suspend fun setTracksDuration(songs: List<Song>): String {
        var txtTime: String = ""
        for (song in songs) {
            val songUrl = "$BASE_URL/${song.id}.mp3"

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(songUrl, HashMap<String, String>())
            val dur =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = dur?.toLong() ?: 0
            mmr.release()
            val seconds: String = ((duration % 60000) / 1000).toString()
            val minutes: String = (duration / 60000).toString()
            if (seconds.length == 1) {
                txtTime = "0" + minutes + ":0" + seconds
            } else {
                txtTime = "0" + minutes + ":" + seconds
            }
            song.duration = txtTime
        }
        return txtTime
    }
}