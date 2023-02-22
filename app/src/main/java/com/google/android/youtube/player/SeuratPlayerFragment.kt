package com.google.android.youtube.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.internal.ab

class SeuratPlayerFragment : Fragment(), YouTubePlayer.Provider {

    private var playerView: YouTubePlayerView? = null

    fun initializePlayer(developerKey: String, listener: YouTubePlayer.OnInitializedListener, bundle: Bundle) {
        Log.d(TAG, "initializeView()")
        val key = ab.a(developerKey, "Developer key cannot be null or empty")
        playerView?.apply {
            a(false)
            a(requireActivity(), this, key, listener, bundle)
        }
    }

    override fun initialize(developerKey: String?, listener: YouTubePlayer.OnInitializedListener?) {
        Log.d(TAG, "dev key = $developerKey")
        val key = ab.a(developerKey, "Developer key cannot be null or empty")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView()")
        playerView = YouTubePlayerView(requireActivity(), null, 0, object : YouTubePlayerView.b {
            override fun a(playerView: YouTubePlayerView?, developerKey: String?, listener: YouTubePlayer.OnInitializedListener?) {
                Log.d(SeuratPlayerView.TAG, "a()1")
            }

            override fun a(playerView: YouTubePlayerView?) {
                Log.d(SeuratPlayerView.TAG, "a()2")
            }
        })
        return playerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
    }

    companion object {
        const val TAG = "SEURAT_FRAGMENT"
    }
}