package app.peterkwp.customlayout2.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.peterkwp.customlayout2.R
import app.peterkwp.customlayout2.adapter.DefaultAdapter
import app.peterkwp.customlayout2.dialog.CustomDialog

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val data = arrayListOf("item1", "item2", "item3", "item4", "item5", "item6")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val list: RecyclerView = root.findViewById(R.id.recycler)
        val fragmentToolbar: Toolbar = root.findViewById(R.id.toolbar)
        val image: ImageView = root.findViewById(R.id.image)
        homeViewModel.text.observe(this, Observer {
            fragmentToolbar.title = it
        })

        image.setOnClickListener {
            context?.run {
                CustomDialog.Builder(this)
                    .setButtonTitle("확인")
                    .setView(ImageView(this).apply {
                        setImageResource(R.drawable.iu01)
                    })
                    .setListener {
                        Log.d("HomeFragment", "Custom Dialog")
                    }
                    .show()
            }
        }

        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DefaultAdapter(data)
        }
        return root
    }
}