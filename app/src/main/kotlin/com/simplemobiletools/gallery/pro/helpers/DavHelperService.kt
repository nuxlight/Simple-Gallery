package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.util.Log
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.toast
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine

class DavHelperService(val activity: BaseSimpleActivity) {

    private val CLASSE_NAME = "DavHelperService"

    private var WEBDAV_URL: String
    private var WEBDAV_USERNAME: String
    private var WEBDAV_PASSWORD: String

    init {
        val sharePref = activity.getSharedPreferences( "kotlinsharedpreference", Context.MODE_PRIVATE)
        WEBDAV_URL = sharePref.getString("WEBDAV_URL", "").orEmpty()
        WEBDAV_USERNAME = sharePref.getString("WEBDAV_USERNAME", "").orEmpty()
        WEBDAV_PASSWORD = sharePref.getString("WEBDAV_PASSWORD", "").orEmpty()
    }

    fun syncTask(){
        val sardine = OkHttpSardine()
        if (!WEBDAV_USERNAME.isEmpty() && !WEBDAV_PASSWORD.isEmpty() && !WEBDAV_URL.isEmpty()){
            sardine.setCredentials(WEBDAV_USERNAME, WEBDAV_PASSWORD)
            //val resources = sardine.list("http://admin@192.168.1.6:2342/originals")
            val resources = sardine.list(formatUrl(WEBDAV_URL))
            resources.forEach {
                Log.i(CLASSE_NAME, it.displayName)
            }
            activity.toast("Dav synchronization ok")
        }
        else {
            activity.toast("No credentials please configure WebDab")
        }
    }

    fun formatUrl(url: String) : String {
        if (url.startsWith("http") || url.startsWith("https")){
            return url
        }
        else {
            return "http://"+WEBDAV_URL
        }
    }
}
