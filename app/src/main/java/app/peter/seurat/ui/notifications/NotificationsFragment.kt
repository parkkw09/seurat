package app.peter.seurat.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.peter.seurat.R
import com.google.android.youtube.player.SeuratPlayerView
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
import com.google.android.youtube.player.YouTubePlayer.Provider
import com.google.android.youtube.player.internal.ab

class NotificationsFragment : Fragment(), Provider {

    private lateinit var notificationsViewModel: NotificationsViewModel

    private var bundle: Bundle = Bundle()

    override fun initialize(developerKey: String, onInitializedListener: OnInitializedListener?) {
        Log.d(TAG, "dev key = $developerKey")
        val key = ab.a(developerKey, "Developer key cannot be null or empty")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView()")
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        val textView: TextView = root.findViewById(R.id.text_status)
        val playerView: SeuratPlayerView = root.findViewById(R.id.video_player)
        playerView.initializeView(requireActivity(), this, "YouTubePlayerView", object : OnInitializedListener {
            override fun onInitializationSuccess(provider: Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
                Log.d(TAG, "onInitializationSuccess()")
                if (!wasRestored) {
                    player?.cueVideo("Sat9tvi2smU")
                }
            }

            override fun onInitializationFailure(provider: Provider?, result: YouTubeInitializationResult?) {
                Log.d(TAG, "onInitializationFailure() [${result?.name}]")
                result?.getErrorDialog(requireActivity(), 1234)?.show()
            }
        }, bundle)
        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    companion object {
        const val TAG = "SEURAT2"
    }
}