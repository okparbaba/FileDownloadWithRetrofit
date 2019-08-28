package com.example.filedownloadtest

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.*
import java.io.File.separator
import kotlin.math.pow
import kotlin.math.roundToInt


@Suppress("DEPRECATION")
@SuppressLint("Registered")
class DownloadService:IntentService("Download Service") {
    private lateinit var notificationBuilder:NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var totalFileSize:Int = 0
    override fun onHandleIntent(p0: Intent?) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Download")
            .setContentText("Downloading File")
            .setAutoCancel(true)
        notificationManager.notify(0,notificationBuilder.build())
        initDownload()
    }
    private fun initDownload(){
        val retrofit = Retrofit.Builder()
            //.baseUrl("https://download.learn2crack.com/")
            .baseUrl("http://clips.vorwaerts-gmbh.de/")
            .build()
        val retrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val reques = retrofitInterface.downloadFile()
        try {
            downloadFile(reques.execute().body()!!)
        }catch (e:IOException){
            e.stackTrace
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        }
    }
    @Throws(IOException::class)
    private fun downloadFile(body:ResponseBody) {
        val data = ByteArray(1024 * 4)
        val fileSize:Long = body.contentLength()
        val bis:InputStream = BufferedInputStream(body.byteStream(),1024 * 8)
        //val outputFile = File(filesDir,"big_buck_bunny.mp4")
        val outputFile:File
        val folder = File(
                    (Environment.getExternalStorageDirectory()).toString() +
            separator + "TollCulator"
                )
            var success = true
            if (!folder.exists())
            {
            success = folder.mkdirs()
            }
            if (success)
            {
                outputFile = File(folder,"big_buck_bunny.mp4")
                val output:OutputStream = FileOutputStream(outputFile)
                var total:Long = 0
                val startTime:Long = System.currentTimeMillis()
                var timeCount = 1
                var count: Int
                do {
                    count = bis.read(data)
                    if (count == -1)
                        break

                    totalFileSize = (fileSize / (1024.0.pow(2.0))).toInt()
                    val current:Double = (total / (1024.0.pow(2.0))).roundToInt().toDouble()
                    val progress:Int = ((total * 100)/fileSize).toInt()
                    val currentTime:Long = System.currentTimeMillis() - startTime
                    val download = Download()
                    download.totalFileSize = totalFileSize
                    if (currentTime >1000 * timeCount){
                        download.currentFileSize = current.toInt()
                        download.progress = progress
                        sendNotification(download)
                        timeCount++
                    }
                    output.write(data,0,count)
                    total += count
                }
                while (true)

                onDownloadComplete()
                output.flush()
                output.close()
                bis.close()
                throw IOException()

            }
            else
            {
             // Do something else on failure
            }

    }
    private fun sendNotification(download: Download) {
        sendIntent(download)
        download.progress?.let { notificationBuilder.setProgress(100, it, false) }
        notificationBuilder.setContentText("Downloading file " + download.currentFileSize + "/" + totalFileSize + " MB")
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendIntent(download: Download) {

        val intent = Intent(MainActivity.MESSAGE_PROGRESS)
        intent.putExtra("download", download)
        LocalBroadcastManager.getInstance(this@DownloadService).sendBroadcast(intent)
    }

    private fun onDownloadComplete() {

        val download = Download()
        download.progress = 100
        sendIntent(download)

        notificationManager.cancel(0)
        notificationBuilder.setProgress(0, 0, false)
        notificationBuilder.setContentText("File Downloaded")
        notificationManager.notify(0, notificationBuilder.build())

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        notificationManager.cancel(0)
    }
}