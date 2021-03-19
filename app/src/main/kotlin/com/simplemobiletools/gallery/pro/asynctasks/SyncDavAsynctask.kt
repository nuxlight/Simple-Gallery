package com.simplemobiletools.gallery.pro.asynctasks

import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import java.lang.RuntimeException

class SyncDavAsynctask(val username: String, val password: String, val davUrl: String): AsyncTask<Void, Void, List<DavResource>>() {

    private val CLASS_TAG = "SyncDavAsynctask"

    override fun doInBackground(vararg params: Void?): List<DavResource> {
        Log.i(CLASS_TAG, "Starting sync...")
        val sardine = OkHttpSardine()
        sardine.setCredentials(username, password)
        try {
            return sardine.list(davUrl)
        }
        catch (e : SardineException){
            Log.e(CLASS_TAG, e.message)
            throw RuntimeException(e.message)
        }
    }
}
