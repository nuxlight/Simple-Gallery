package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.util.Log
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.gallery.pro.interfaces.DirectoryDao
import com.simplemobiletools.gallery.pro.interfaces.MediumDao
import com.simplemobiletools.gallery.pro.models.Directory
import com.simplemobiletools.gallery.pro.models.Medium
import com.thegrizzlylabs.sardineandroid.DavResource
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
    private var WEBDAV_FODLER_SYNCED: Set<String> = emptySet()

    init {
        val sharePref = activity.getSharedPreferences( "kotlinsharedpreference", Context.MODE_PRIVATE)
        WEBDAV_URL = sharePref.getString("WEBDAV_URL", "").orEmpty()
        WEBDAV_USERNAME = sharePref.getString("WEBDAV_USERNAME", "").orEmpty()
        WEBDAV_PASSWORD = sharePref.getString("WEBDAV_PASSWORD", "").orEmpty()
        WEBDAV_FODLER_SYNCED = sharePref.getStringSet("WEBDAV_FODLER_SYNCED", emptySet()).orEmpty()
    }

    fun syncTask(){
        val sardine = OkHttpSardine()
        if (!WEBDAV_USERNAME.isEmpty() && !WEBDAV_PASSWORD.isEmpty() && !WEBDAV_URL.isEmpty()){
            sardine.setCredentials(WEBDAV_USERNAME, WEBDAV_PASSWORD)
            //val resources = sardine.list("http://admin@192.168.1.6:2342/originals")
            checkRootFolder(sardine)
            pushPictureToDav(sardine)
            getPictureFromDav(sardine)
            activity.toast("Dav synchronization ok")
        }
        else {
            activity.toast("No credentials please configure WebDab")
        }
    }

    /**
     * This function get all picture presents in WebDav server if is not present in phone
     */
    private fun getPictureFromDav(sardine: OkHttpSardine) {
        val GET_URL = "$WEBDAV_URL/$SIMPLE_GALLERY_DAV_FOLDER"
        val folders = sardine.list(formatUrl(GET_URL))
        folders.forEach {
            val folder = it.name
            val syncDir = WEBDAV_FODLER_SYNCED.find { it == folder }
            // Test if folder already exist in memory card if not create it
            if (syncDir!=null){
                // Test if dir already exist
                if (directoryDao.getAll().find { it.name == syncDir }==null)
                    createNewDirectory(syncDir)
                // get ALL media
                val webdavPictures = sardine.list(formatUrl("$GET_URL/$folder"))
                importWebdavMedias(webdavPictures, folder, syncDir)
            }
        }
    }

    private fun createNewDirectory(syncDir: String) {
        var newDir = Directory()
        newDir.location
    }

    private fun importWebdavMedias(webdavPictures: List<DavResource>, folder: String?, syncDir: String) {
        webdavPictures.forEach {
            if (it.name!=folder){
                val image = it.name
                val internalPicture = mediaDB.getMediaFromPath(syncDir).find { it.name == image }
                if (internalPicture==null){
                    createNewMedium(it)
                }
                else {
                    Log.d(CLASSE_NAME, "In folder $folder => image : ${it.name} already present")
                }
            }
        }
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
