package com.example.artbookfragment.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.artbookfragment.adapter.ArtAdapter
import com.example.artbookfragment.databinding.FragmentMainBinding
import com.example.artbookfragment.model.Art
import com.example.artbookfragment.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding?=null
    private val binding get()=_binding!!
    private lateinit var artAdapter: ArtAdapter
    private lateinit var artList: ArrayList<Art>
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        compositeDisposable= CompositeDisposable()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //viewBinding
        _binding= FragmentMainBinding.inflate(inflater,container,false)
        val view=binding.root

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //database
        val db= Room.databaseBuilder(requireContext(),ArtDatabase::class.java,"Arts").build()
        val artDao=db.artDao()

        //database get
        compositeDisposable.add(
            artDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

        artList=ArrayList<Art>()

        //adapter
        artAdapter= ArtAdapter(artList)

        binding.addArtButton.setOnClickListener {
            val action= MainFragmentDirections.actionMainFragmentToDetailsFragment(info = "new", selectedArt = null)
            Navigation.findNavController(it).navigate(action)
        }

    }

    private fun handleResponse(artList: List<Art>){
        binding.recyclerView.layoutManager= LinearLayoutManager(requireContext())
        val adapter=ArtAdapter(artList)
        binding.recyclerView.adapter=adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
        compositeDisposable.clear()
    }

}