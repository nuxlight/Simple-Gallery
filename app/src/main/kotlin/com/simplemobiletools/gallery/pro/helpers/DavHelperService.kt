package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.util.Log
import android.view.Window
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.interfaces.DirectoryDao
import com.simplemobiletools.gallery.pro.interfaces.MediumDao
import com.simplemobiletools.gallery.pro.models.Directory
import com.simplemobiletools.gallery.pro.models.Medium
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream

class DavHelperService(val activity: BaseSimpleActivity, val directoryDao: DirectoryDao,
                       val mediaDB: MediumDao, val window: Window) {

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
            pushPictureToDav(sardine)
            //getPictureFromDav(sardine)
            Snackbar.make(window.findViewById(R.id.directories_refresh_layout),
                "Dav synchronization ok", Snackbar.LENGTH_LONG).show()
        }
        else {
            Snackbar.make(window.findViewById(R.id.directories_refresh_layout),
                "No credentials please configure WebDab", Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * This function get all picture presents in WebDav server if is not present in phone
     */
    private fun getPictureFromDav(sardine: OkHttpSardine) {
        val folders = sardine.list(formatUrl(WEBDAV_URL))
        folders.removeAt(0)
        folders.forEach {
            val subfolder = it.name
            val ls = fetchAllPictureFromFolder(sardine.list(formatUrl("$WEBDAV_URL/$subfolder")),sardine)
        }
    }

    private fun fetchAllPictureFromFolder(foldersToFetch: List<DavResource>, sardine: OkHttpSardine): List<Medium>? {
        val folders = foldersToFetch.filter { !directoryDao.getAll().contains(it) } as ArrayList
        folders.removeAt(0)
        for (folder in folders) {
            if (folder.isDirectory)
                fetchAllPictureFromFolder(sardine.list(formatUrl("${WEBDAV_URL}/${folder.name}")), sardine)
        }
        return null
    }

    /**
     * This function push all photo added in Simple Gallery to Dav server if not exist
     */
    private fun pushPictureToDav(sardineClient: OkHttpSardine){
        val dirs = directoryDao.getAll()
        dirs.forEach {
            val tmpUrl = "$WEBDAV_URL/${it.name}/"
            Log.d(CLASSE_NAME, "Path debug : $tmpUrl")
            // Check if folder exist in WebDav server
            if (!sardineClient.exists(formatUrl(tmpUrl))){
                sardineClient.createDirectory(formatUrl(tmpUrl))
                Log.i(CLASSE_NAME, "Create new directory "+it.name+" on server")
            }
            else{
                Log.i(CLASSE_NAME, "Directory "+it.name+" already exist on server")
            }
            val mediums = mediaDB.getMediaFromPath(it.path)
            mediums.forEach {
                val file = tmpUrl+it.name
                Log.d(CLASSE_NAME, "Path debug : $file")
                if (!sardineClient.exists(formatUrl(file))){
                    val imageFile = FileInputStream(File(it.path))
                    sardineClient.put(formatUrl(file), IOUtils.toByteArray(imageFile))
                    Log.i(CLASSE_NAME, "Upload file "+it.name+" on server")
                }
                else {
                    Log.i(CLASSE_NAME, "File "+it.name+" already exist on server")
                }
            }
        }
    }

    /**
     * =======
     * HELPER FUNCTIONS
     * =======
     */

    private fun formatUrl(url: String) : String {
        if (url.startsWith("http") || url.startsWith("https")){
            return url
        }
        else {
            return "http://$url"
        }
    }

    private fun createNewDirectory(syncDir: String) {
        var newDir = Directory()
        newDir.location
    }

    private fun createNewMedium(it: DavResource?) {
        if (it !=null) {
            var medium = Medium()
            medium.name = it.name
            medium.path = formatUrl(WEBDAV_URL+it.href.toString())
            medium.gridPosition = 0
            medium.isFavorite = false
            mediaDB.insert(medium)
            Log.d(CLASSE_NAME, "New image saved ${it.name}")
        }
    }
}
