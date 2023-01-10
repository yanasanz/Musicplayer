package com.example.musicplayer.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.utils.MediaLifecycleObserver
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import com.example.musicplayer.*
import com.example.musicplayer.dto.Album
import com.example.musicplayer.dto.Song
import com.example.musicplayer.repository.RepositoryImpl.Companion.BASE_URL
import com.example.musicplayer.viewmodel.SongViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val mediaObserver = MediaLifecycleObserver()
    lateinit var album: Album
    private val viewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(mediaObserver)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SongAdapter(mediaObserver, object : OnInteractionListener {
            override fun onPlay(song: Song) {
                viewModel.playSong(song)
                binding.play.isChecked = true
            }

            override fun onPause(song: Song) {
                viewModel.pauseSong(song)
                binding.play.isChecked = false
            }


        })
        binding.list.adapter = adapter

        viewModel.data.observe(this) { songModel ->
            if (songModel.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_message,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry) { viewModel.getAlbum() }
                    .show()
            }
            adapter.submitList(songModel.album?.tracks)

            with(binding) {
                album.text = songModel.album?.title
                subtitle.text = songModel.album?.subtitle
                artist.text = songModel.album?.artist
                published.text = songModel.album?.published
                genre.text = songModel.album?.genre

                play.setOnClickListener {

                    var currentSong = requireNotNull(viewModel.findCurrentSong())

                    with(mediaObserver) {
                        if (!play.isChecked) {
                            player?.pause()
                            viewModel.pauseSong(currentSong)
                            play.isChecked = false
                        } else if (currentSong.isPlaying) {
                            player?.start()
                            viewModel.playSong(currentSong)
                            play.isChecked = true
                            player?.setOnCompletionListener {
                                playNextSong()
                            }
                        } else {
                            player?.reset()
                            currentSong = viewModel.findFirst()!!
                            val songUrl = BASE_URL + "/" + currentSong.file
                            player?.setDataSource(songUrl)
                            play()
                            viewModel.playSong(currentSong)
                            play.isChecked = true
                            player?.setOnCompletionListener {
                                playNextSong()
                            }
                        }
                    }
                }

                rewind.setOnClickListener { playPreviousSong() }
                forward.setOnClickListener { playNextSong() }
            }

        }
    }

    override fun onStop() {
        if (mediaObserver.player?.isPlaying == true) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
        }
        super.onStop()
    }

    private fun playNextSong() {
        var song = Song()
        viewModel.data.value?.album?.tracks?.let {
            val index = viewModel.findCurrentSong()?.id?.toInt() ?: 0
            song = if (index == it.size) {
                it.first()
            } else {
                it[index]
            }
        }
        viewModel.playSong(song)
        val songUrl = BASE_URL + "/" + song.file
        mediaObserver.apply {
            player?.reset()
            player?.setDataSource(songUrl)
        }.play()
    }

    private fun playPreviousSong() {
        var song = Song()
        viewModel.data.value?.album?.tracks?.let {
            val index = viewModel.findCurrentSong()?.id?.toInt() ?: 0
            song = if (index == 1) {
                it.last()
            } else {
                it[index - 2]
            }
        }
        viewModel.playSong(song)
        val songUrl = BASE_URL + "/" + song.file
        mediaObserver.apply {
            player?.reset()
            player?.setDataSource(songUrl)
        }.play()
    }
}