package me.jludden.reeflifesurvey.data.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.os.StatFs
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_LONG
import com.squareup.picasso.Picasso
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import io.reactivex.Observable
import me.jludden.reeflifesurvey.Injection
import me.jludden.reeflifesurvey.MainActivity
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.R.id.top_level_layout
import me.jludden.reeflifesurvey.data.DataRepository
import java.io.*
import me.jludden.reeflifesurvey.data.model.InfoCard.CardDetails
import me.jludden.reeflifesurvey.data.SurveySiteType
import me.jludden.reeflifesurvey.data.model.InfoCard
import me.jludden.reeflifesurvey.data.utils.StorageUtils.Companion.NOTIFICATION_CHANNEL_ID
import me.jludden.reeflifesurvey.data.utils.StorageUtils.Companion.NOTIFICATION_ID
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


        val NOTIFICATION_ID: Int = 8856
        val NOTIFICATION_CHANNEL_ID: String = "RLS_DOWNLOADER"

        var writeToExternal = false //todo every reference to this needs to be checked

        val SIZE_KB : Long = 1024

        /*
        * UTIL
        * */
        fun canReadFromExternalStorage(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state ||
                    Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        fun canWriteToExternalStorage(): Boolean {
          //  return false
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
         * Begins the workflow of storing sites and fish species images offline
         *
         *  first, determine the storage space available on local and external sd
         *  then, prompt the user for confirmation and to select the storage location if multiple
         *
         *
         * Stores a list of fish species images
         *  to sd card or local storage
         *
         * also stores the site codes associated with this list
         *  to SharedPreferences
         *
         * todo param or choice - do we store one image per fish, or all images?
         *
         * todo calculate the expected amount of storage space needed?
         *
         * todo show gb/mb/kb instead of just mb
         */
        fun promptToSaveOffline(cardList: List<CardDetails>, siteCodes: List<String>, context: Activity) {

            val builder = AlertDialog.Builder(context)
            builder
                    .setTitle(context.getString(R.string.storage_location_dialog_title)) //todo
                    .setView(R.layout.storage_location_dialog)
            builder.setPositiveButton(R.string.ok) { _, _ -> //dialog, id
                //user accepted
                storeSites(cardList, siteCodes, context)
            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
                // User cancelled the dialog
            }

            val dialog = builder.create()
            dialog.show()

            //todo
            val cw = ContextWrapper(context)
            val folderName = "images"
            val extButton = dialog.findViewById<RadioButton>(R.id.storage_ext_button)
            extButton.setOnClickListener({
                writeToExternal = true
                    Log.d(TAG, "ON RADIO CLICK writeToExt: $writeToExternal")})
            val localButton = dialog.findViewById<RadioButton>(R.id.storage_local_button)
            localButton.setOnClickListener({ writeToExternal = false
                Log.d(TAG, "ON RADIO CLICK writeToExt: $writeToExternal")})

            dialog.findViewById<TextView>(R.id.storage_local_remaining_space).text =
                    context.getString(R.string.storage_location_dialog_space_remaining_message,
                            calcSpaceRemaining(cw.getDir(folderName, Context.MODE_PRIVATE).absolutePath))

            if(canWriteToExternalStorage()){
                    Log.d(TAG, "CAN WRITE TO EXTERNAL STORAGE " +
                        "\n local 1 ${calcSpaceRemaining(Environment.getDataDirectory().path)}" +
                        "\n local 2 ${calcSpaceRemaining(Environment.getDataDirectory().absolutePath)}" +
                        "\n local 3 ${calcSpaceRemaining(cw.getDir(folderName, Context.MODE_PRIVATE).absolutePath)}" +
                        "\n local 4 ${calcSpaceRemaining(context.filesDir.absolutePath)}" +
                        "\n local 5 ${calcSpaceRemaining(context.filesDir.path)}" +
                        "\n ext 1 ${calcSpaceRemaining(Environment.getExternalStorageDirectory().path)} " +
                        "\n ext 2 ${calcSpaceRemaining(cw.getExternalFilesDir(folderName).absolutePath)}" +
                        "\n ext 3 ${calcSpaceRemaining(context.getExternalFilesDir(null).path)}" +
                        "\n ext 4 ${calcSpaceRemaining(context.getExternalFilesDir(null).absolutePath)}" +
                        "\n ext 5 ${calcSpaceRemaining(context.getExternalFilesDir(folderName).path)}" +
                        "\n ext 6 ${calcSpaceRemaining(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)}" +
                        "\n ext 6 ${calcSpaceRemaining(context.externalMediaDirs?.get(0)?.absolutePath!!)} " )
                extButton.isChecked = true
                writeToExternal = true
                dialog.findViewById<TextView>(R.id.storage_ext_remaining_space).text =
                        context.getString(R.string.storage_location_dialog_space_remaining_message,
                                calcSpaceRemaining(Environment.getExternalStorageDirectory().path))
//                              calcSpaceRemaining(cw.getExternalFilesDir(folderName).absolutePath))
            } else {
                extButton.isEnabled = false
                localButton.isChecked = true
                writeToExternal = false
            }
        }

        //returns the disk size
        fun calcDiskSize(path: String) : Long {
            val stat = StatFs(path)
            val bytesAvailable = stat.blockSizeLong * stat.blockCountLong
            return bytesAvailable / (SIZE_KB * SIZE_KB)
        }

        //returns space remaining in MB
        fun calcSpaceRemaining(path: String) : Long {
            val stat = StatFs(path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            return bytesAvailable / (SIZE_KB * SIZE_KB)
        }

        /**
         * Launches a background task to save the sites
         */
        private fun storeSites(cardList: List<CardDetails>, siteCodes: List<String>, context: Context) {
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

            val dataRepo = Injection.provideDataRepository(context)

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
            val imageFiles : Array<File>? = file.listFiles(IMAGE_FILTER)

            if(imageFiles == null || imageFiles.isEmpty()){
                Log.d(TAG, "clearOfflineSites - NO image files found in $path" )
//                Snackbar.make(activity.findViewById<CoordinatorLayout>(top_level_layout)
//                    , R.string.no_downloaded_sites_to_delete_message, LENGTH_LONG)
                MainActivity.showSimpleDialogMessage(activity, activity.resources.getString(R.string.no_downloaded_sites_to_delete_message)) //todo consider snackbar
                return
            }

            Log.d(TAG, "clearOfflineSites - ${imageFiles.size} image files found in $path" )

            //delete images
            val message = activity.resources.getString(R.string.clear_offline_sites_final_prompt, imageFiles.size, path)
            MainActivity.showOkCancelDialog(activity, message) { _, _ ->
                Log.d(TAG, "DELETING OFFLINE SITES IN $path! (${imageFiles.size} sites)" )
                imageFiles.forEach {
                    it.delete()
                }
                MainActivity.showSimpleDialogMessage(activity, activity.resources.getString(R.string.delete_offline_on_success))
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
    private val notificationBuilder = android.support.v4.app.NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var imageCount = 0
    private var speciesCount = 0

    override fun doInBackground(vararg params: CardDetails): Boolean {
        val context : Context? = contextRef.get()
        Log.d(StorageUtils.TAG, "SaveToStorageTask doInBackground. \n has context: ${context!=null} \n data dir = $root")
        var success = true

        if (context != null) {

            params.forEach {
                if(it.imageURLs != null){
                    //todo download all images per
                    try {
                        val bitmap = loadBitmap(it.imageURLs[0], context)
                        if(bitmap!=null) {
                            saveToInternalStorage(bitmap, root, it.getFileName(0))
                        }
                        else {
                            Log.e(StorageUtils.TAG, "Failed to download image for ${it.cardName} [${it.getId()}]: ${it.primaryImageURL}")
                            success = false
                        }
                    } catch (e: IOException){
                        e.printStackTrace()
                        success = false
                    }
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

            return success
        }
        return false
    }

    /** Download the image using Picasso. MUST BE ON BACKGROUND THREAD**/
   private fun loadBitmap(url: String, context: Context): Bitmap? {
        return Picasso
                .with(context)
                .load(url)
                .get()
    }

    /** Save image to path directory**/
    //todo filename - pass in fish id. increment so its like 123_1, 123_2 for each image for fish id 123
    private fun saveToInternalStorage(bitmapImage: Bitmap, path: String, fileName: String): Boolean {
        val newImageFile = File(path, fileName)
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
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        for(progress in values) {
            if(progress != null) {
                notificationBuilder.setProgress(speciesToDownload, progress, false)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
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
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
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

    fun loadPrimaryCardImage(card: CardDetails): Bitmap? {
        return loadImageFromStorage(card.getFileName(0))
    }

    fun loadPrimaryCardImageWithPlaceholder(card: CardDetails): Bitmap {
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

