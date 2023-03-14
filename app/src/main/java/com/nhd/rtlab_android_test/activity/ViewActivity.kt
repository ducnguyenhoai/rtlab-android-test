package com.nhd.rtlab_android_test.activity

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nhd.rtlab_android_test.adapter.XmlTagAdapter
import com.nhd.rtlab_android_test.databinding.ActivityViewBinding
import com.nhd.rtlab_android_test.utils.getXmlToView
import java.io.File

@Suppress("SameParameterValue")
class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding

    private var xmlTagAdapter: XmlTagAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRcv()
        getData()
    }

    private fun setupRcv() {
        binding.rcv.setHasFixedSize(true)
        binding.rcv.layoutManager = LinearLayoutManager(
            this@ViewActivity,
            LinearLayoutManager.VERTICAL,
            false,
        )
        binding.rcv.addItemDecoration(
            DividerItemDecoration(
                this@ViewActivity,
                LinearLayoutManager.VERTICAL,
            ),
        )
        xmlTagAdapter = XmlTagAdapter(this@ViewActivity)
        binding.rcv.adapter = xmlTagAdapter
    }

    private fun getData() {
        val path = intent.getStringExtra("path")
        if (path == null) {
            showError("This file is not exists")
        } else {
            val file = File(path)
            if (!file.exists()) {
                showError("This file is not exists")
            }
            showSnackBar("XML parsing...")
            getXmlToView(file) {
                runOnUiThread {
                    xmlTagAdapter?.setData(it)
                }
                dismissSnackBar()
            }
        }
    }

    private var snackBar: Snackbar? = null
    private fun showSnackBar(text: String) {
        runOnUiThread {
            if (snackBar != null) {
                snackBar!!.dismiss()
                snackBar = null
            }
            snackBar = Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE)
            val viewGroup =
                snackBar!!.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
            val progressBar = ProgressBar(this@ViewActivity)
            val layoutParams = LinearLayout.LayoutParams(100, 100)
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            progressBar.layoutParams = layoutParams
            viewGroup.addView(progressBar)
            snackBar!!.show()
        }
    }

    private fun dismissSnackBar() {
        runOnUiThread {
            if (snackBar != null) {
                snackBar!!.dismiss()
                snackBar = null
            }
        }
    }

    private fun showError(error: String) {
        runOnUiThread {
            Toast.makeText(
                this@ViewActivity,
                error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}