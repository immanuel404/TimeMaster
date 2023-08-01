package com.example.timemaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class uCategoryAdapter (private val newsList : ArrayList<uCategory>) : RecyclerView.Adapter<uCategoryAdapter.MyViewHolder>(){

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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_category, parent,false)
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.txtCategory.text = currentItem.Name
    }

    override fun getItemCount(): Int {
        return newsList.size
    }


    // VIEW HOLDER _ADD LISTENER
    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val txtCategory : TextView = itemView.findViewById(R.id.txtCategory)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }
}