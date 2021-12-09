package eu.inkbook.sample.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLConnection
import android.app.DownloadManager
import android.database.Cursor


object FileDownloadHelper {
    val TAG = FileDownloadHelper::class.java.simpleName
    val EXTENSION = "csv"
    val FILE_NAME = "covid"

    suspend fun getFileLastModifiedFromUrl(
        urlStr: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val urlConnection: URLConnection = url.openConnection()
            urlConnection.connectTimeout = 5 * 1000
            urlConnection.readTimeout = 5 * 1000
            urlConnection.connect()
            urlConnection.lastModified
        } catch (socketTimeoutException: SocketTimeoutException) {
            0L
        } catch (e: IOException) {
            0L
        }
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    fun isExternalStorageWritableAndReadable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // "Read_File" in before
    fun getFileContentOrNull(absoluteFilePath: String?): String? {
        return try {
            val fis = FileInputStream(File(absoluteFilePath))
            val isr = InputStreamReader(fis, "UTF-8")
            val bufferedReader = BufferedReader(isr)
            val sb = StringBuilder()
            var line: String = ""
            while (bufferedReader.readLine().also { line = it ?: "" } != null) {
                sb.append(line).append("\n")
            }
            sb.toString()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.localizedMessage)
            null
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, e.localizedMessage)
            null
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage)
            null
        }
    }

    fun isDownloading(context: Context, downloadId: Long): Boolean {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val c: Cursor = downloadManager.query(query)
        if (c.moveToFirst()) {
            val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            c.close()
            return status == DownloadManager.STATUS_RUNNING
        }
        return false
    }

    fun removeOldFile(context: Context): Boolean {
        return try {
            val file = File(context.getExternalFilesDir(null), "$FILE_NAME.$EXTENSION")
            if(file.exists()) file.delete()
            false
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage)
            false
        }
    }

    fun getMimeTypeForCsv(): String {
        return if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            "text/comma-separated-values"
        } else "text/plain" // for unknown reasons android version 22 and lower don't recognize text/comma-separated-values
    }
}