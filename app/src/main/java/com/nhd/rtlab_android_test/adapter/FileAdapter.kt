package com.nhd.rtlab_android_test.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhd.rtlab_android_test.R
import com.nhd.rtlab_android_test.databinding.LayoutFileItemBinding
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class FileAdapter(
    private val context: Context,
    private val data: MutableList<Model> = mutableListOf(),
    private val callback: Callback,
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    fun clearData() {
        this.data.clear()
        notifyDataSetChanged()
    }

    fun addItem(item: Model) {
        this.data.add(item)
        this.data.sortWith { t1, t2 -> t1.name.compareTo(t2.name) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_file_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Model = data[position]
        holder.binding.tvName.text = item.name
        if (item.isImported) {
            holder.binding.imgView.visibility = View.VISIBLE
            holder.binding.imgImport.visibility = View.GONE
        } else {
            holder.binding.imgView.visibility = View.GONE
            holder.binding.imgImport.visibility = View.VISIBLE
        }
        holder.binding.imgView.setOnClickListener {
            callback.viewFile(item)
        }
        holder.binding.imgImport.setOnClickListener {
            callback.importFile(item)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    data class Model(
        val name: String,
        val path: String,
        val isImported: Boolean,
    ) {
        companion object {
            fun fromFile(file: File, isImported: Boolean): Model {
                return Model(file.name, file.path, isImported)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: LayoutFileItemBinding = LayoutFileItemBinding.bind(itemView)
    }

    interface Callback {
        fun viewFile(item: Model)
        fun importFile(item: Model)
    }
}