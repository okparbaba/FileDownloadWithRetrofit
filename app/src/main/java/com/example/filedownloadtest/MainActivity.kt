package com.example.filedownloadtest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageInfo
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import android.net.Uri
import android.util.Log
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(p0: View?) {
        if (checkPermission()) {
            startDownload()
        } else {
            requestPermission()
        }
    }

    companion object  {
        const val MESSAGE_PROGRESS = "message_progress"
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_download.setOnClickListener(this)
        registerReceiver()

        val m = packageManager
        var s: String = packageName
        try {
            val p = m.getPackageInfo(s, 0)
            s = p.applicationInfo.dataDir
            val lifi = File("$s/files")
            val liofFile = lifi.listFiles()
            videoView2.setVideoURI(Uri.parse(liofFile[2].toString()))
            videoView2.start()
            for (i in liofFile){
                Log.w("yourtag", i.toString())
            }

        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("yourtag", "Error Package name not found ", e)
        }
        //videoView2.setVideoURI(Uri.parse("$s/files/big_buck_bunny.mp4"))


    }

    private fun startDownload() {

        val intent = Intent(this, DownloadService::class.java)
        startService(intent)

    }

    private fun registerReceiver() {

        val bManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(MESSAGE_PROGRESS)
        bManager.registerReceiver(broadcastReceiver, intentFilter)

    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == MESSAGE_PROGRESS) {

                val download = intent.getParcelableExtra<Download>("download")
                download!!.progress?.let { progress.progress = it }
                if (download.progress == 100) {

                    progress_text.text = "File Download Complete"

                } else {

                    progress_text.text = String.format(
                        "Downloaded (%d/%d) MB",
                        download.currentFileSize,
                        download.totalFileSize
                    )

                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startDownload()
            } else {

                Snackbar.make(
                    findViewById(R.id.coordinatorLayout),
                    "Permission Denied, Please allow to proceed !",
                    Snackbar.LENGTH_LONG
                ).show()

            }
        }
    }

}
