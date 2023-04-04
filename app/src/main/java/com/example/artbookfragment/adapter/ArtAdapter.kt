package com.example.artbookfragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.artbookfragment.databinding.RecyclerRowBinding
import com.example.artbookfragment.model.Art
import com.example.artbookfragment.view.MainFragmentDirections

class ArtAdapter (val artList: List<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder (val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.recyclerRowBinding.recyclerRowText.setText(artList.get(position).title)
        holder.itemView.setOnClickListener {
            val action= MainFragmentDirections.actionMainFragmentToDetailsFragment(info = "old", selectedArt = artList.get(position))
            Navigation.findNavController(it).navigate(action)
        }
    }

}