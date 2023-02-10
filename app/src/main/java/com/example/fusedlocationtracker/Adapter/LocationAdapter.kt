package com.example.fusedlocationtracker.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fusedlocationtracker.R
import com.example.fusedlocationtracker.RoomDB.LocationEntity

class LocationAdapter(private val list: List<LocationEntity>):RecyclerView.Adapter<LocationAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       // TODO("Not yet implemented")
        val view=LayoutInflater.from(parent.context).inflate(R.layout.single_location_list,parent,false)
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val locatiolistlist = list[position]

        // sets the image to the imageview from our itemHolder class
        holder.tv_index.setText((position+1).toString())
        holder.tv_accuracy.setText(locatiolistlist.accuracy)
        holder.tv_lat.setText(locatiolistlist.lat)
        holder.tv_long.setText(locatiolistlist.longitude)
        holder.tv_ttl_distance.setText(locatiolistlist.total_distance)



    }

    override fun getItemCount(): Int {

        return  list.size
    }
    class ViewHolder(Itemview:View):RecyclerView.ViewHolder(Itemview) {
        val tv_index:TextView=itemView.findViewById(R.id.tv_index)
        val tv_accuracy:TextView=itemView.findViewById(R.id.tv_accuracy)
        val tv_lat:TextView=itemView.findViewById(R.id.tv_lat)
        val tv_long:TextView=itemView.findViewById(R.id.tv_long)
        val tv_ttl_distance:TextView=itemView.findViewById(R.id.tv_ttl_distance)

    }

}