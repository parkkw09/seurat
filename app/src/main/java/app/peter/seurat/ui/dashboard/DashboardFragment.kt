package app.peter.seurat.ui.dashboard

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.peter.seurat.CustomScope
import app.peter.seurat.R
import app.peter.seurat.Util
import app.peter.seurat.databinding.FragmentDashboardBinding
import app.peter.seurat.model.Playlist
import app.peter.seurat.model.PlaylistItem
import app.peter.seurat.model.Subscription
import app.peter.seurat.model.YoutubeResponse
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.youtube.player.SeuratPlayerFragment
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var videoContainer: SeuratPlayerFragment
    private var bundle: Bundle = Bundle()

    private lateinit var dashboardViewModel: DashboardViewModel
    private val scope = CustomScope()

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var credential: GoogleAccountCredential
    private var player: ExoPlayer? = null

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        Log.d(TAG, "StartActivityForResult() res[${result.resultCode}]")

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            Log.d(TAG, "StartActivityForResult() data[${data?.data}}]")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "StartActivityForResult() CANCEL")
            logout()
        }
    }

    private val startForPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        Log.d(TAG, "startForPermissionResult() res[${result.resultCode}]")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "StartActivityForResult() OK")
            login()
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "StartActivityForResult() CANCEL")
            logout()
        }
    }

    private var accessToken: String? = null
    private var idToken: String? = null

    private var channelIdBySubscription: String? = null
    private var idByPlaylist: String? = null
    private var videoIdByPlaylistItems: String? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            account?.apply {
                Log.d(TAG, "handleSignInResult()[$id][$idToken][$serverAuthCode][]")
                scope.launch {
                    try {
                        credential = GoogleAccountCredential.usingOAuth2(requireContext(),
                            mutableListOf(VALUE_SCOPE_YOUTUBE))
                        credential.selectedAccount = this@apply.account
                        val accessToken = credential.token
                        Log.d(TAG, "handleSignInResult() accessToken[$accessToken]")
                        this@DashboardFragment.accessToken = accessToken
                        this@DashboardFragment.idToken = this@apply.idToken
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "OK", Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: UserRecoverableAuthException) {
                        Log.d(TAG, "NEED PERMISSION[${e.message}]")
                        startForPermissionResult.launch(e.intent)
                    }
                }
            }
        } catch (e: ApiException) {
            Log.w(TAG, "handleSignInResult() signInResult:failed code=" + e.statusCode)
        }
    }

    private fun setup() {
        activity?.apply {
            scope.job = Job()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }
    }

    private fun login() {
        Log.d(TAG, "login()")
        activity?.apply {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startForResult.launch(signInIntent)
        }
    }

    private fun logout() {
        Log.d(TAG, "logout()")
        activity?.apply {
            mGoogleSignInClient!!.signOut().addOnCompleteListener(this,
                OnCompleteListener<Void?> { dashboardViewModel.updateStatus("SignOut") })
        }

    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
//                binding.videoPlayer.player = exoPlayer
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun processCommand1() {
        Log.d(TAG, "processCommand1()")
        try {
            val commandUrl = "https://www.googleapis.com/youtube/v3/subscriptions?part=snippet&mine=true"
            val token = "Bearer $accessToken"
            val connector: HttpsURLConnection = URL(commandUrl).openConnection() as HttpsURLConnection
            connector.apply {
                readTimeout = 5000
                requestMethod = "GET"
                doInput = true
                setRequestProperty("Authorization", token)
                setRequestProperty("user-agent", "HttpURLConnection/1.0.0")
            }
            Log.d(TAG, "processCommand1() URL = [$commandUrl]")
            Log.d(TAG, "processCommand1() header = [${connector.requestProperties}]")
            val content = StringBuilder()
            val resCode = connector.responseCode
            if (resCode == HttpsURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connector.inputStream))
                while (true) {
                    val data = reader.readLine() ?: break
                    content.append(data)
                }
                reader.close()
                connector.disconnect()
            } else {
                Log.e(TAG, "processCommand1() resCode [$resCode][${connector.responseMessage}]")
                connector.disconnect()
                throw Exception("response code[$resCode]")
            }

            Log.d(TAG, "processCommand1() response = [${content}]")
            val response = run {
                Gson().fromJson<YoutubeResponse<Subscription>>(
                    content.toString(),
                    TypeToken.getParameterized(YoutubeResponse::class.java, Subscription::class.java).type)
            }
            Log.d(TAG, "processCommand1() response json = [${Util.toPrettyFormat(response)}]")
            channelIdBySubscription = response.items.first().snippet.resourceId.channelId
        } catch (e: Exception) {
            Log.e(TAG, "processCommand1() Exception [${e.localizedMessage}]")
        }
    }

    private fun processCommand2() {
        Log.d(TAG, "processCommand2()")
        if (channelIdBySubscription.isNullOrEmpty()) {
            Log.d(TAG, "processCommand2() channelId Nothing")
            return
        }
        try {
            val commandUrl = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=${channelIdBySubscription ?: ""}"
            val token = "Bearer $accessToken"
            val connector: HttpsURLConnection = URL(commandUrl).openConnection() as HttpsURLConnection
            connector.apply {
                readTimeout = 5000
                requestMethod = "GET"
                doInput = true
                setRequestProperty("Authorization", token)
                setRequestProperty("user-agent", "HttpURLConnection/1.0.0")
            }
            Log.d(TAG, "processCommand2() URL = [$commandUrl]")
            Log.d(TAG, "processCommand2() header = [${connector.requestProperties}]")
            val content = StringBuilder()
            val resCode = connector.responseCode
            if (resCode == HttpsURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connector.inputStream))
                while (true) {
                    val data = reader.readLine() ?: break
                    content.append(data)
                }
                reader.close()
                connector.disconnect()
            } else {
                Log.e(TAG, "processCommand2() resCode [$resCode][${connector.responseMessage}]")
                connector.disconnect()
                throw Exception("response code[$resCode]")
            }

            Log.d(TAG, "processCommand2() response = [${content}]")
            val response = run {
                Gson().fromJson<YoutubeResponse<Playlist>>(
                    content.toString(),
                    TypeToken.getParameterized(YoutubeResponse::class.java, Playlist::class.java).type)
            }
            Log.d(TAG, "processCommand2() response json = [${Util.toPrettyFormat(response)}]")
            idByPlaylist = response.items[1].id
        } catch (e: Exception) {
            Log.e(TAG, "processCommand2() Exception [${e.localizedMessage}]")
        }

    }

    // paging 이 필요함.
    private fun processCommand3() {
        Log.d(TAG, "processCommand3()")
        if (idByPlaylist.isNullOrEmpty()) {
            Log.d(TAG, "processCommand3() Playlist id Nothing")
            return
        }
        try {
            val commandUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=${idByPlaylist ?: ""}"
            val token = "Bearer $accessToken"
            val connector: HttpsURLConnection = URL(commandUrl).openConnection() as HttpsURLConnection
            connector.apply {
                readTimeout = 5000
                requestMethod = "GET"
                doInput = true
//                    doOutput = true
                setRequestProperty("Authorization", token)
                setRequestProperty("user-agent", "HttpURLConnection/1.0.0")
            }
            Log.d(TAG, "processCommand3() URL = [$commandUrl]")
            Log.d(TAG, "processCommand3() header = [${connector.requestProperties}]")
            val content = StringBuilder()
            val resCode = connector.responseCode
            if (resCode == HttpsURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connector.inputStream))
                while (true) {
                    val data = reader.readLine() ?: break
                    content.append(data)
                }
                reader.close()
                connector.disconnect()
            } else {
                Log.e(TAG, "processCommand3() resCode [$resCode][${connector.responseMessage}]")
                connector.disconnect()
                throw Exception("response code[$resCode]")
            }

            Log.d(TAG, "processCommand3() response = [${content}]")
            val response = run {
                Gson().fromJson<YoutubeResponse<PlaylistItem>>(
                    content.toString(),
                    TypeToken.getParameterized(YoutubeResponse::class.java, PlaylistItem::class.java).type)
            }
            Log.d(TAG, "processCommand3() response json = [${Util.toPrettyFormat(response)}]")
            videoIdByPlaylistItems = response.items.first().snippet.resourceId.videoId
        } catch (e: Exception) {
            Log.e(TAG, "processCommand3() Exception [${e.localizedMessage}]")
        }
    }

    private fun processCommand4() {
        Log.d(TAG, "processCommand4()")
        if (videoIdByPlaylistItems.isNullOrEmpty()) {
            Log.d(TAG, "processCommand3() Playlist id Nothing")
            return
        }

//        player?.let { exoPlayer ->
//            val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.playWhenReady = playWhenReady
//            exoPlayer.seekTo(currentItem, playbackPosition)
//            exoPlayer.prepare()
//        }

        videoContainer.initializePlayer("YouTubePlayerView", object :
            YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
                Log.d(TAG, "onInitializationSuccess()")
                if (!wasRestored) {
//                    player?.cueVideo("Sat9tvi2smU")
                    player?.cueVideo(videoIdByPlaylistItems)
                }
            }

            override fun onInitializationFailure(provider: YouTubePlayer.Provider?, result: YouTubeInitializationResult?) {
                Log.d(TAG, "onInitializationFailure() [${result?.name}]")
                result?.getErrorDialog(requireActivity(), 1234)?.show()
            }
        }, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        videoContainer = SeuratPlayerFragment()
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.apply {
            dashboardViewModel.text.observe(viewLifecycleOwner) {
                textStatus.text = it
            }
            button1.setOnClickListener {
                scope.launch(Dispatchers.IO) {
                    processCommand1()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), channelIdBySubscription ?: "NOTHING", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            button2.setOnClickListener {
                scope.launch(Dispatchers.IO) {
                    processCommand2()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), idByPlaylist ?: "NOTHING", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            button3.setOnClickListener {
                scope.launch(Dispatchers.IO) {
                    processCommand3()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), videoIdByPlaylistItems ?: "NOTHING", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            button4.setOnClickListener {
                processCommand4()
            }
            this@DashboardFragment.childFragmentManager
                .beginTransaction()
                .replace(videoContainer.id, this@DashboardFragment.videoContainer)
                .commitAllowingStateLoss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() KEY [${getString(R.string.server_client_id)}]")
        setup()
        login()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    companion object {
        const val TAG = "SEURAT_DashboardFragment"
        const val VALUE_SCOPE_YOUTUBE = "https://www.googleapis.com/auth/youtube"
    }
}