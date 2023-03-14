package com.nhd.rtlab_android_test.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nhd.rtlab_android_test.BuildConfig
import com.nhd.rtlab_android_test.R
import com.nhd.rtlab_android_test.adapter.FileAdapter
import com.nhd.rtlab_android_test.database.DB
import com.nhd.rtlab_android_test.database.FileEntity
import com.nhd.rtlab_android_test.databinding.ActivityMainBinding
import com.nhd.rtlab_android_test.utils.copyFile
import com.nhd.rtlab_android_test.utils.getInstanceIDFromXmlFile
import com.nhd.rtlab_android_test.utils.resolveContentUri
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var database: DB

    private var folder: File? = null
    private var fileAdapter: FileAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DB(this@MainActivity)

        setupRcvFile()

        binding.btnSelectFolder.setOnClickListener {
            checkPermissionReadStorage()
        }


    }

    private fun setupRcvFile() {
        binding.rcvFile.setHasFixedSize(true)
        binding.rcvFile.layoutManager = LinearLayoutManager(
            this@MainActivity,
            LinearLayoutManager.VERTICAL,
            false,
        )
        binding.rcvFile.addItemDecoration(
            DividerItemDecoration(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
            ),
        )
        fileAdapter = FileAdapter(
            this@MainActivity,
            callback = object : FileAdapter.Callback {
                override fun viewFile(item: FileAdapter.Model) {
                    handleViewFile(item)
                }

                override fun importFile(item: FileAdapter.Model) {
                    handleImportFile(item)
                }
            },
        )
        binding.rcvFile.adapter = fileAdapter
    }

    private fun handleViewFile(item: FileAdapter.Model) {
        val intent = Intent(this@MainActivity, ViewActivity::class.java)
        intent.putExtra("path", item.path)
        startActivity(intent)
    }

    private var instanceIDToCopy: String? = null
    private var fileToCopy: File? = null
    private fun handleImportFile(item: FileAdapter.Model) {
        showSnackBar("XML parsing...")
        val file = File(item.path)
        getInstanceIDFromXmlFile(file) { instanceID ->
            if (instanceID.isEmpty()) {
                showError("Could not get instanceID")
                dismissSnackBar()
            } else {
                instanceIDToCopy = instanceID
                fileToCopy = file
                dismissSnackBar()
                checkPermissionWriteStorage()
            }
        }
    }

    private fun checkPermissionWriteStorage() {
        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            copyFileToOfficialDataFolder()
        } else {
            writeExternalStoragePermissionResult.launch(WRITE_EXTERNAL_STORAGE)
        }
    }

    private val writeExternalStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                copyFileToOfficialDataFolder()
            }
        }

    private fun copyFileToOfficialDataFolder() {
        if (instanceIDToCopy == null || fileToCopy == null) {
            return
        }
        val outputDirectory = File(Environment.getExternalStorageDirectory(), "official-data")
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        val outputFilePath = "$outputDirectory/${fileToCopy!!.name}"
        copyFile(fileToCopy!!, outputFilePath) { isSuccess ->
            if (isSuccess) {
                showSnackBar("Inserting new record to database...")
                insertToDatabase(instanceIDToCopy!!, outputFilePath)
            } else {
                showError("Could not copy file to official folder")
                dismissSnackBar()
            }
        }
    }

    private fun insertToDatabase(instanceID: String, outputFilePath: String) {
        val item = FileEntity(
            id = 0,
            instanceID = instanceID,
            path = outputFilePath,
        )
        database.insertFile(item)

        runOnUiThread {
            loadDataFromFolder()
        }
    }

    private fun checkPermissionReadStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                selectFolder()
            } else {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                manageExternalStoragePermissionResult.launch(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri,
                    )
                )
            }
        } else {
            if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectFolder()
            } else {
                readExternalStoragePermissionResult.launch(READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val manageExternalStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    selectFolder()
                }
            }
        }

    private val readExternalStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                selectFolder()
            }
        }

    private fun selectFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        selectFolderResult.launch(intent)
    }

    private val selectFolderResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    val path: String = resolveContentUri(this@MainActivity, uri)
                    val file = File(path)
                    if (file.exists() && file.isDirectory) {
                        folder = file
                        loadDataFromFolder()
                    }
                }
            }
        }

    private fun loadDataFromFolder() {
        if (folder == null) {
            return
        }
        fileAdapter?.clearData()
        showSnackBar("Loading file from directory...")
        binding.tvFolderPath.text = getString(R.string.label_folder).plus(" ").plus(folder!!.path)
        val listFiles: Array<out File>? = folder!!.listFiles()
        if (listFiles != null) {
            showSnackBar("Checking file is imported or not...")
            for (index in listFiles.indices) {
                val file: File = listFiles[index]
                if (file.name.endsWith(".xml")) {
                    val outputDirectory = File(Environment.getExternalStorageDirectory(), "official-data")
                    val outputFile = File(outputDirectory, file.name)
                    runOnUiThread {
                        fileAdapter?.addItem(FileAdapter.Model.fromFile(file, outputFile.exists()))
                    }
                    if (index == listFiles.lastIndex) {
                        dismissSnackBar()
                    }
                }
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
            val progressBar = ProgressBar(this@MainActivity)
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
                this@MainActivity,
                error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}