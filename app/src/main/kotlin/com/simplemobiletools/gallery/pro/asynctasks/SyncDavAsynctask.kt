package com.simplemobiletools.gallery.pro.asynctasks

import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import com.thegrizzlylabs.sardineandroid.model.Response
import java.io.IOException
import java.lang.RuntimeException
import java.util.*

class SyncDavAsynctask(val username: String, val password: String, val davUrl: String): AsyncTask<Void, Void, Void>() {

    private val CLASS_TAG = "SyncDavAsynctask"

    override fun doInBackground(vararg params: Void?): Void? {
        Log.i(CLASS_TAG, "Starting sync...")
        val sardine = OkHttpSardine()
        sardine.setCredentials(username, password)
        try {
            sardine.list(davUrl)
        }
        catch (e: IOException){
            Log.e(CLASS_TAG, e.message)
            e.printStackTrace()
        }
        return null;
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}
