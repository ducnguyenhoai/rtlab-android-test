package com.nhd.rtlab_android_test.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhd.rtlab_android_test.R
import com.nhd.rtlab_android_test.databinding.LayoutXmlTagItemBinding

@SuppressLint("NotifyDataSetChanged")
class XmlTagAdapter(
    private val context: Context,
    private val data: MutableList<String> = mutableListOf(),
) : RecyclerView.Adapter<XmlTagAdapter.ViewHolder>() {

    fun setData(data: List<String>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_xml_tag_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: String = data[position]
        holder.binding.tv.text = item
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: LayoutXmlTagItemBinding = LayoutXmlTagItemBinding.bind(itemView)
    }
}