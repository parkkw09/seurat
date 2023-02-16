package app.peter.seurat.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
                .requestScopes(Scope(VALUE_SCOPE_YOUTUBE))
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }
    }

    private fun processCommand1() {
        Log.d(TAG, "processCommand1()")
        activity?.apply {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startForResult.launch(signInIntent)
        }
    }

    private fun processCommand2() {
        activity?.apply {
            mGoogleSignInClient!!.signOut().addOnCompleteListener(this,
                OnCompleteListener<Void?> { dashboardViewModel.updateStatus("SignOut") })
        }

    }

    private fun processCommand3() {
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
    }

    companion object {
        const val TAG = "SEURAT"
        const val VALUE_SCOPE_YOUTUBE = "https://www.googleapis.com/auth/youtube"
    }
}