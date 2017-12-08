package me.jludden.reeflifesurvey.data.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.support.v7.app.NotificationCompat
import com.squareup.picasso.Picasso
import android.util.Log
import io.reactivex.Observable
import me.jludden.reeflifesurvey.MainActivity
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.data.DataRepository
import java.io.*
import me.jludden.reeflifesurvey.data.model.InfoCard.CardDetails
import me.jludden.reeflifesurvey.data.SurveySiteType
import me.jludden.reeflifesurvey.data.model.InfoCard
import me.jludden.reeflifesurvey.data.utils.StorageUtils.Companion.PROGRESS_BAR_NOTIFICATION_ID
import me.jludden.reeflifesurvey.data.utils.StorageUtils.Companion.writeToExternal
import java.lang.ref.WeakReference


/**
 * Created by Jason on 12/2/2017.
 *
 * Holds utilities for saving sites offline. Because we are already holding the .json files by default, we basically just need to download fish images
 *
 */

// holds static, public accessible functions
class StorageUtils{
    companion object {

        val TAG : String = "offlineUtils"

        val PROGRESS_BAR_NOTIFICATION_ID: Int = 123

        val writeToExternal = false //todo every reference to this needs to be checked

        /*
        * UTIL
        * */
        fun canReadFromExternalStorage(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state ||
                    Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        fun canWriteToExternalStorage(): Boolean {
            return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
        }


        /*
        * SAVING
        * */

        /*
        saving scheme

        fish ID? image url?

        fish ID_#: 101_1, 101_2?


         */


        /**
         * Stores a list of fish species images
         *  to sd card or local storage
         *
         * also stores the site codes associated with this list
         *  to SharedPreferences
         *
         * todo param or choice - do we store one image per fish, or all images?
         *
         */
        fun storeSites(cardList: List<CardDetails>, siteCodes: List<String>, context: Context) {

            //todo
            if(canWriteToExternalStorage()) Log.d(TAG, "CAN WRITE TO EXTERNAL STORAGE")

            val cw = ContextWrapper(context)
            val folderName = "images"
            val path : String

            path = if(writeToExternal){
                cw.getExternalFilesDir(folderName).absolutePath
            } else {
                cw.getDir(folderName, Context.MODE_PRIVATE).absolutePath
            }
            Log.d(TAG, "store sites (external storage ? $writeToExternal) path: $path")


            //todo
            //store current site config
            SharedPreferencesUtils.setSitesStoredOffline(siteCodes, path, writeToExternal, context)
            SaveToStorageTask(path, cardList.size, context)
                    .execute(*cardList.toTypedArray())
        }


       /*
        * LOADING
        *
        */

        // filter to identify images based on their extensions
        val EXTENSIONS = arrayOf("png", "jpg", "bmp")
        val IMAGE_FILTER = FilenameFilter { _, name -> EXTENSIONS.any { name.endsWith("." + it) } }

        /*
          a File object can represents both
             - a single file
             - as well as a folder

          To get all files from a specific folder,
          create a File object of that directory and use the
             list() method to find all file names
             listFiles() to return actual file objects

          both methods can take a filter function, such as IMAGE_FILTER
           to only return on files matching image extensions

          BitmapFactory can create a bitmap from a file
        */
        /*
        todo alternatively do we even need a list<bitmap>?
        Glide.with(context).load(new File("your/file/name.jpg")).into(pollWebView);
        list of file could reasonable work just as well and prevent us from creating bitmap until we need it
         */
        fun loadImageFileDirectory(directory: String) : Array<File> {
            val file : File = File(directory)

            if(!file.isDirectory) Log.e(TAG, "loadDirectory - file is not a directory" )
            val imageFiles : Array<File> = file.listFiles(IMAGE_FILTER)
            Log.d(TAG, "loadDirectory - ${imageFiles.size} image files found in $directory" )
            return imageFiles
        }

        //todo i doubt this is good for memory
        fun loadBitmapDirectory(directory: String) : List<Bitmap> {
            val file : File = File(directory)

            if(!file.isDirectory) Log.e(TAG, "loadDirectory - file is not a directory" )
            val imageFiles : Array<String> = file.list(IMAGE_FILTER)

            Log.d(TAG, "loadDirectory - ${imageFiles.size} image files found in $directory" )
            val images : List<Bitmap> = imageFiles.map { BitmapFactory.decodeFile(it) }
            return images
        }

        fun loadStoredFishCards(context: Context) : Observable<CardDetails> {

            val storedSites = SharedPreferencesUtils.loadSitesStoredOffline(context)
            Log.d(TAG, "loadStoredFishCards loading ${storedSites.size} saved sites: ${storedSites.toString()}")

            val dataRepo = DataRepository.getInstance(context)

            return dataRepo
                    .getSurveySitesAll(SurveySiteType.CODES)
                    .filter({ storedSites.contains(it.code) })
                    .flatMap({ dataRepo.getFishSpeciesForSite(it) })
        }

        /**
         * Deletes all images stored offline
         */
        fun clearOfflineSites(activity: Activity) {
            val isExternal: Boolean = writeToExternal //todo
            val path = SharedPreferencesUtils.loadStoredOfflinePath(isExternal, activity.applicationContext)

            val file = File(path)
            if(!file.isDirectory) Log.e(TAG, "clearOfflineSites - file is not a directory at $path" )
            val imageFiles : Array<File> = file.listFiles(IMAGE_FILTER) //todo handle no files found
            Log.d(TAG, "clearOfflineSites - ${imageFiles.size} image files found in $path" )

            //delete images
            val message = activity.resources.getString(R.string.clear_offline_sites_final_prompt, imageFiles.size, path)
            MainActivity.showAlertDialog(activity, message) { _, _ ->
                Log.d(TAG, "DELETING OFFLINE SITES IN $path! (${imageFiles.size} sites)" )
                imageFiles.forEach {
                    it.delete()
                }
            }

            //delete SharedPreferences map of saved sites
            SharedPreferencesUtils.setSitesStoredOffline(ArrayList<String>(), path, isExternal, activity.applicationContext)
        }

    }


    /** Retrieve your image from device and set to imageview **/
    //Provide your image path and name of the image your previously used.
    /*
    Bitmap b= loadImageFromStorage(String path, String name)
    ImageView img=(ImageView)findViewById(R.id.your_image_id);
    img.setImageBitmap(b);*/

}


/**
 * Background task for saving fish images to storage
 */
class SaveToStorageTask(private val root: String, private val speciesToDownload: Int, context: Context)
    : AsyncTask<CardDetails, Int, Boolean>() {

    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val notificationBuilder = NotificationCompat.Builder(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var imageCount = 0
    private var speciesCount = 0

    override fun doInBackground(vararg params: CardDetails): Boolean {
        val context : Context? = contextRef.get()
        Log.d(StorageUtils.TAG, "SaveToStorageTask doInBackground. \n has context: ${context!=null} \n data dir = $root")

        if (context != null) {

            params.forEach {
                if(it.imageURLs != null){
                    //todo download all images per
                    val bitmap = loadBitmap(it.imageURLs[0], context)
                    saveToInternalStorage(bitmap, root, it.getFileName(0))
                    imageCount++
                }
                publishProgress(speciesCount++)
            }

            /*for(card in params){
                if(card.imageURLs != null){
                    count++
                    val bitmap = loadBitmap(card.imageURLs[0], context)
                    saveToInternalStorage(bitmap, root, card.getFileName(0))
                }
                if(count > speciesToDownload) break //sanity
                publishProgress(count)
            }*/

            return true
        }
        return false
    }

    /** Download the image using Picasso. MUST BE ON BACKGROUND THREAD**/
   private fun loadBitmap(url: String, context: Context): Bitmap {
        return Picasso
                .with(context)
                .load(url)
                .get()
    }

    /** Save image to path directory**/
    //todo filename - pass in fish id. increment so its like 123_1, 123_2 for each image for fish id 123
    private fun saveToInternalStorage(bitmapImage: Bitmap, path: String, fileName: String): Boolean {
        val newImageFile = File(path, fileName+".png")
        Log.d(StorageUtils.TAG, "saveToInternalStorage AAA absoute path: "+newImageFile.absolutePath)

        try {
            val fos = FileOutputStream(newImageFile)
            // Use the compress method on the BitMap object to write image to the OutputStream
            //compress quality is ignored when using the PNG format
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }


    override fun onPreExecute() {
        val context : Context? = contextRef.get()
        if(context != null) {
            setupProgressBarNotification(context.resources)
            notificationManager.notify(PROGRESS_BAR_NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        for(progress in values) {
            if(progress != null) {
                notificationBuilder.setProgress(speciesToDownload, progress, false)
                notificationManager.notify(PROGRESS_BAR_NOTIFICATION_ID, notificationBuilder.build())
            }
        }

    }

    override fun onPostExecute(result: Boolean) {
        Log.d(StorageUtils.TAG, "SaveToStorageTask onPostExecute. success = $result")

        val context : Context? = contextRef.get()
        var message = context?.resources?.getString(R.string.notification_download_complete, imageCount, speciesCount)
        if(!result || message == "") message = "Error downloading images ($imageCount of $speciesCount downloaded successfully)"

        notificationBuilder.setContentText(message)
        notificationBuilder.setProgress(0, 0, false) //removes progress bar
        notificationManager.notify(PROGRESS_BAR_NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun setupProgressBarNotification(res: Resources) {
        notificationBuilder.setSmallIcon(R.drawable.ic_store_offline)
        notificationBuilder.setContentTitle(res.getString(R.string.app_name))
        notificationBuilder.setContentText(res.getString(R.string.notification_download_starting, speciesToDownload))
        notificationBuilder.setProgress(speciesToDownload, 0, false) //create progress bar
    }
}

/**
 * Simple object that can be created by any layout trying to inflate an imageview that might already be on disk
 * todo maybe this could handle inflating using picasso and a url as well as backup?
 */
class StoredImageLoader(context: Context){

    val placeholderImage: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_map_action)

    var path: String
    var externalStorage: Boolean = writeToExternal //todo make dialog and prompt

    init {

        path = SharedPreferencesUtils.loadStoredOfflinePath(externalStorage, context)
        if(path == "") Log.e(StorageUtils.TAG, "StoredImageLoader no saved file path found")
        Log.d(StorageUtils.TAG, "StoredImageLoader initiated path: $path  external storage: $externalStorage")


    }

    fun loadPrimaryCardImage(card: CardDetails): Bitmap {
        val image = loadImageFromStorage(card.getFileName(0))
        return image ?: placeholderImage
    }

    /**
     * @return Bitmap returned may be null if image cannot be found
     */
    fun loadImageFromStorage(fileName: String): Bitmap? {
        val b: Bitmap
        try {
            val f = File(path, fileName)
            b = BitmapFactory.decodeStream(FileInputStream(f))
            return b
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

}

// 1234_1, 12345_2
fun InfoCard.CardDetails.getFileName(imageNumber: Int): String {
    if(imageNumber > 0 ) TODO("not impl")
    return "${this.id}.png"
}

