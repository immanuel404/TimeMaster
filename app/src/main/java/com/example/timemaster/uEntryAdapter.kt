package com.example.timemaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class uEntryAdapter (private val newsList : ArrayList<uEntry>) : RecyclerView.Adapter<uEntryAdapter.MyViewHolder>(){

    // CLICK EVENT
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position : Int)
    }

    fun setOnClickListener(listener : onItemClickListener) {
        mListener = listener
    }


    // DEFAULT GENERATED METHODS
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_entry, parent,false)
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.txtId.text = currentItem.Id.toString()
        holder.txtDate.text = currentItem.Date
        holder.txtDescription.text = currentItem.Description
        holder.txtCategory.text = currentItem.Category
        if (currentItem.ImageUrl != "null") {
            Glide.with(holder.itemView.context).load(currentItem.ImageUrl).into(holder.imgView)
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }


    // VIEW HOLDER _ADD LISTENER
    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var txtId : TextView = itemView.findViewById(R.id.txtId) as TextView
        var txtDate : TextView = itemView.findViewById(R.id.txtDate) as TextView
        var txtDescription : TextView = itemView.findViewById(R.id.txtDescription) as TextView
        var txtCategory : TextView = itemView.findViewById(R.id.txtCategory) as TextView
        var imgView : ImageView = itemView.findViewById(R.id.imgView) as ImageView
        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}