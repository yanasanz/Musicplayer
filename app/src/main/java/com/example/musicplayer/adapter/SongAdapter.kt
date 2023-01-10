package com.example.musicplayer

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.SongCardBinding
import com.example.musicplayer.dto.Song
import com.example.musicplayer.repository.RepositoryImpl.Companion.BASE_URL
import com.example.musicplayer.utils.MediaLifecycleObserver

interface OnInteractionListener {
    fun onPlay(song: Song) {}
    fun onPause(song: Song) {}
}

class SongAdapter(
    private val mediaObserver: MediaLifecycleObserver,
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Song, SongViewHolder>(SongDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding, mediaObserver, onInteractionListener)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }
}

class SongViewHolder(
    private val binding: SongCardBinding,
    private val mediaObserver: MediaLifecycleObserver,
    private val listener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(song: Song) = with(binding) {
        name.text = song.id.toString()
        time.text = song.duration
        playPauseButton.isChecked = song.isPlaying
        playPauseButton.setOnClickListener {
            if (!playPauseButton.isChecked) {
                mediaObserver.player?.pause()
                listener.onPause(song)
            } else {
                if (song.isPlaying) {
                    mediaObserver.player?.start()
                    listener.onPause(song)
                    playPauseButton.isChecked = false
                } else {
                    val songUrl = BASE_URL + "/" + song.file
                    mediaObserver.apply {
                        player?.reset()
                        player?.setDataSource(songUrl)
                    }.play()
//                    initialiseSeekBar()
                    listener.onPlay(song)
                    playPauseButton.isChecked = true
                }
            }
        }

//        if (song.isPlaying) {
//            seekBar.visibility = View.VISIBLE
//        } else {
//            seekBar.visibility = View.GONE
//        }
    }

//    private fun initialiseSeekBar() {
//        with(binding) {
//            seekBar.max = mediaObserver.player!!.duration
//            val handler = Handler(Looper.getMainLooper())
//            handler.postDelayed(object : Runnable {
//                override fun run() {
//                    try {
//                        seekBar.progress = mediaObserver.player?.currentPosition!!
//                        handler.postDelayed(this, 1000)
//                    } catch (e: Exception) {
//                        seekBar.progress = 0
//                    }
//                }
//            }, 0)
//
//            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    if (fromUser) mediaObserver.player?.seekTo(progress)
//                }
//                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//            })
//        }
//    }
}

class SongDiffCallBack : DiffUtil.ItemCallback<Song>() {

    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}