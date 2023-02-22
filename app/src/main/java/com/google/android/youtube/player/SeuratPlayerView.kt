package com.google.android.youtube.player

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import app.peter.seurat.databinding.SeuratViewBinding
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
import com.google.android.youtube.player.YouTubePlayer.Provider
import com.google.android.youtube.player.internal.ab

class SeuratPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var playerView: YouTubePlayerView? = null

    init {
        playerView = YouTubePlayerView(context, null, 0, object : YouTubePlayerView.b {
            override fun a(playerView: YouTubePlayerView?, developerKey: String?, listener: OnInitializedListener?) {
                Log.d(TAG, "a()1")
            }

            override fun a(playerView: YouTubePlayerView?) {
                Log.d(TAG, "a()2")
            }
        })
    }

    fun initializeView(activity: Activity, provider: Provider, developerKey: String, listener: OnInitializedListener, bundle: Bundle) {
        Log.d(TAG, "initializeView()")
        val key = ab.a(developerKey, "Developer key cannot be null or empty")
        playerView?.apply {
            a(false)
            a(activity, provider, key, listener, bundle)
        }
        val binding = SeuratViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding.seuratLayout.apply {
            addView(playerView)
        }
    }

    companion object {
        const val TAG = "SEURAT_PLAYER"
    }
}