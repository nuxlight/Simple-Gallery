package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.util.Log
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.gallery.pro.interfaces.DirectoryDao
import com.simplemobiletools.gallery.pro.interfaces.MediumDao
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream

class DavHelperService(val activity: BaseSimpleActivity, val directoryDao: DirectoryDao, val mediaDB: MediumDao) {

    private val CLASSE_NAME = "DavHelperService"

    private val SIMPLE_GALLERY_DAV_FOLDER = "simple-gallery"
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
            checkRootFolder(sardine)
            pushPictureToDav(sardine)
            val resources = sardine.list(formatUrl(WEBDAV_URL))
            resources.forEach {
                Log.i(CLASSE_NAME, it.name)
            }
            activity.toast("Dav synchronization ok")
        }
        else {
            activity.toast("No credentials please configure WebDab")
        }
    }

    /**
     * This function push all photo added in Simple Gallery to Dav server if not exist
     */
    private fun pushPictureToDav(sardineClient: OkHttpSardine){
        val dirs = directoryDao.getAll()
        dirs.forEach {
            val TMP_URL = WEBDAV_URL+"/"+SIMPLE_GALLERY_DAV_FOLDER+"/"+it.name+"/"
            Log.d(CLASSE_NAME, "Path debug : $TMP_URL")
            // Check if folder exist in WebDav server
            if (!sardineClient.exists(formatUrl(TMP_URL))){
                sardineClient.createDirectory(formatUrl(TMP_URL))
                Log.i(CLASSE_NAME, "Create new directory "+it.name+" on server")
            }
            else{
                Log.i(CLASSE_NAME, "Directory "+it.name+" already exist on server")
            }
            val mediums = mediaDB.getMediaFromPath(it.path)
            mediums.forEach {
                val FILE = TMP_URL+it.name
                Log.d(CLASSE_NAME, "Path debug : $FILE")
                if (!sardineClient.exists(formatUrl(FILE))){
                    val imageFile = FileInputStream(File(it.path))
                    sardineClient.put(formatUrl(FILE), IOUtils.toByteArray(imageFile))
                    Log.i(CLASSE_NAME, "Upload file "+it.name+" on server")
                }
                else {
                    Log.i(CLASSE_NAME, "File "+it.name+" already exist on server")
                }
            }
        }
    }

    /**
     * This function check if the root folder Simple Gallery is present in server if not,
     * this function create this folder
     */
    private fun checkRootFolder(sardine: OkHttpSardine) {
        if (!sardine.exists(formatUrl("$WEBDAV_URL/$SIMPLE_GALLERY_DAV_FOLDER"))){
            Log.i(CLASSE_NAME, "Root folder not present in server, create it")
            sardine.createDirectory(formatUrl("$WEBDAV_URL/$SIMPLE_GALLERY_DAV_FOLDER"))
            Log.d(CLASSE_NAME, "Root folder created")
        }
        else  {
            Log.i(CLASSE_NAME, "Root folder present in server, passed")
        }
    }

    private fun formatUrl(url: String) : String {
        if (url.startsWith("http") || url.startsWith("https")){
            return url
        }
        else {
            return "http://$url"
        }
    }
}
