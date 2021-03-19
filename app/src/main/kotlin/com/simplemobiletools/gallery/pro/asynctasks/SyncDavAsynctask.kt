package com.simplemobiletools.gallery.pro.asynctasks

import android.os.AsyncTask
import android.util.Log
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine

class SyncDavAsynctask(val usernam: String, val password: String, val davUrl: String): AsyncTask<Void, Void, List<DavResource>>() {

    override fun doInBackground(vararg params: Void?): List<DavResource> {
        Log.i("DacAsyncTask", "Starting sync...")
        val sardine = OkHttpSardine()
        sardine.setCredentials(usernam, password)
        return sardine.list(davUrl);
    }
}
