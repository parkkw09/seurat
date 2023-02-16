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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.peter.seurat.CustomScope
import app.peter.seurat.R
import app.peter.seurat.databinding.FragmentDashboardBinding
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private val scope = CustomScope()

    private var mGoogleSignInClient: GoogleSignInClient? = null
    lateinit var credential: GoogleAccountCredential

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        Log.d(TAG, "StartActivityForResult() res[${result.resultCode}]")

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            Log.d(TAG, "StartActivityForResult() data[${data?.data}}]")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "StartActivityForResult() CANCEL")
        }
    }

    private val startForPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        Log.d(TAG, "startForPermissionResult() res[${result.resultCode}]")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "StartActivityForResult() OK")
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "StartActivityForResult() CANCEL")
        }
    }

    private var accessToken: String? = null
    private var idToken: String? = null

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
                        startForPermissionResult.launch(requireActivity().intent)
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

    private fun processCommand1() {
        Log.d(TAG, "processCommand1()")
        scope.launch(Dispatchers.IO) {
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
            } catch (e: Exception) {
                Log.e(TAG, "processCommand1() Exception [${e.localizedMessage}]")
            }
        }
    }

    private fun processCommand2() {
        Log.d(TAG, "processCommand2()")
        scope.launch(Dispatchers.IO) {
            try {
                val commandUrl = "https://www.googleapis.com/youtube/v3/search"
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
            } catch (e: Exception) {
                Log.e(TAG, "processCommand2() Exception [${e.localizedMessage}]")
            }
        }

    }

    private fun processCommand3() {
        Log.d(TAG, "processCommand3()")
        scope.launch(Dispatchers.IO) {
            try {
//                val commandUrl = "https://www.googleapis.com/youtube/v3/subscriptions/list?part=id"
                val commandUrl = "https://www.googleapis.com/youtube/v3/search"
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
            } catch (e: Exception) {
                Log.e(TAG, "processCommand3() Exception [${e.localizedMessage}]")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        val binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.apply {
            dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
                textStatus.text = it
            })
            button1.setOnClickListener { processCommand1() }
            button2.setOnClickListener { processCommand2() }
            button3.setOnClickListener { processCommand3() }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
        login()
    }

    companion object {
        const val TAG = "SEURAT"
        const val VALUE_SCOPE_YOUTUBE = "https://www.googleapis.com/auth/youtube"
    }
}