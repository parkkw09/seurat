package app.peterkwp.seurat.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.peterkwp.seurat.R
import app.peterkwp.seurat.databinding.FragmentDashboardBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    private var mGoogleSignInClient: GoogleSignInClient? = null

    private fun setup() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        activity?.apply {
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }
    }

    private fun processCommand1() {
        activity?.apply {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_GET_TOKEN)
        }
    }

    private fun processCommand2() {
        activity?.apply {
            mGoogleSignInClient!!.signOut().addOnCompleteListener(this,
                OnCompleteListener<Void?> { dashboardViewModel.updateStatus("SignOUT") })
        }

    }

    private fun processCommand3() {
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this)[DashboardViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.apply {
            dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
                textStatus.text = it
            })
            button1.setOnClickListener { processCommand1() }
            button2.setOnClickListener { processCommand2() }
            button3.setOnClickListener { processCommand3() }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    companion object {
        const val RC_GET_TOKEN = 9002
    }
}