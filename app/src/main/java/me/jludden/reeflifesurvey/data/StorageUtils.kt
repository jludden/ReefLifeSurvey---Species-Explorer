package me.jludden.reeflifesurvey.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import com.squareup.picasso.Picasso
import android.util.Log
import io.reactivex.Observable
import me.jludden.reeflifesurvey.R
import java.io.*
import me.jludden.reeflifesurvey.data.InfoCard.CardDetails
import me.jludden.reeflifesurvey.data.StorageUtils.Companion.writeToExternal
import java.lang.ref.WeakReference


/**
 * Created by Jason on 12/2/2017.
 */

/*TODO add permission
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 */

class SaveToStorageTask(val root: String, context: Context) : AsyncTask<CardDetails, Int, Boolean>() {
    private val contextRef: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: CardDetails): Boolean {
        val context : Context? = contextRef.get()
        Log.d(StorageUtils.TAG, "SaveToStorageTask doInBackground + has context: ${context!=null}")

        if (context != null) {

            Log.d(StorageUtils.TAG, "SaveToStorageTask " +
                    "\n data dir = $root")

            var count = 0
            //params.forEach {  } //todo

            for(card in params){
                if(card.imageURLs != null){
                    count++
                    val bitmap = StorageUtils.loadBitmap(card.imageURLs[0], context)
                    StorageUtils.saveToInternalStorage(bitmap, root, card.getFileName(0))
                }
                if(count > 3) break
            }

            return true
        }
        return false
    }

    override fun onPreExecute() {
        //todo set up progress bar
    }

    override fun onProgressUpdate(vararg values: Int?) {
        //todo update progress bar
    }

    override fun onPostExecute(result: Boolean?) {
        //todo saved successfully or not?
        Log.d(StorageUtils.TAG, "SaveToStorageTask onPostExecute")

    }
}

// 1234_1, 12345_2
private fun InfoCard.CardDetails.getFileName(imageNumber: Int): String {
    if(imageNumber > 0 ) TODO("not impl")
    return this.id
}

class StoredImageLoader(context: Context){

    val placeholderImage: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_map_action)

    var path: String? = null
    var externalStorage: Boolean = writeToExternal

    init {

        path = SharedPreferencesUtils.loadStoredOfflinePath(externalStorage, context)
        if(path == null) Log.e(StorageUtils.TAG, "StoredImageLoader no saved file path found")
        Log.d(StorageUtils.TAG, "StoredImageLoader initiated path: $path  external storage: $externalStorage")


    }

    fun loadPrimaryCardImage(card: CardDetails): Bitmap {
        val image = loadImageFromStorage(card.getFileName(0))
        return image ?: placeholderImage
    }

    //loads an image stored on device
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

class StorageUtils{
    //static methods, that are hopefully more reusable... todo
    companion object {

        val TAG : String = "offlineUtils"

        val writeToExternal = false

        /*
        * UTIL
        * */
        public fun canReadFromExternalStorage(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state ||
                    Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

        public fun canWriteToExternalStorage(): Boolean {
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
        public fun storeSites(cardList: List<CardDetails>, siteCodes: List<String>, context: Context) {

            //todo
            if(canWriteToExternalStorage()) Log.d(TAG, "CAN WRITE TO EXTERNAL STORAGE")

            val cw = ContextWrapper(context)
            val folderName = "images"
            val path : String

            if(writeToExternal){
                path = cw.getExternalFilesDir(folderName).absolutePath
            } else {
                path = cw.getDir(folderName, Context.MODE_PRIVATE).absolutePath
            }
            Log.d(TAG, "store sites (external storage ? $writeToExternal) path: $path")


            //todo
            //store current site config
            SharedPreferencesUtils.setSitesStoredOffline(siteCodes, path, writeToExternal, context)
            SaveToStorageTask(path, context)
                    .execute(*cardList.toTypedArray())
        }




        /** Download the image using Picasso **/
        public fun loadBitmap(url: String, context: Context): Bitmap {

            /*
        Bitmap theBitmap = null;
        theBitmap = Glide.
        with(YourActivity.this).
        load("Url of your image").
        asBitmap().
        into(-1, -1).
        get();*/


            val bitMap: Bitmap = Picasso
                    .with(context)
                    .load(url)
                    .get()

            return bitMap
            //saveToInternalStorage(bitMap, context, "your preferred image name");
        }

        /** Save it on your device **/
        //todo filename - pass in fish id. increment so its like 123_1, 123_2 for each image for fish id 123
        fun saveToInternalStorage(bitmapImage: Bitmap, path: String, fileName: String): Boolean {
            // Create imageDir
            val newImageFile: File = File(path, fileName+".png")
            Log.d(TAG, "saveToInternalStorage AAA absoute path: "+newImageFile.absolutePath)

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
        public fun loadImageFileDirectory(directory: String) : Array<File> {
            val file : File = File(directory)

            if(!file.isDirectory) Log.e(TAG, "loadDirectory - file is not a directory" )
            val imageFiles : Array<File> = file.listFiles(IMAGE_FILTER)
            Log.d(TAG, "loadDirectory - ${imageFiles.size} image files found in $directory" )
            return imageFiles
        }

        //todo i doubt this is good for memory
        public fun loadBitmapDirectory(directory: String) : List<Bitmap> {
            val file : File = File(directory)

            if(!file.isDirectory) Log.e(TAG, "loadDirectory - file is not a directory" )
            val imageFiles : Array<String> = file.list(IMAGE_FILTER)

            Log.d(TAG, "loadDirectory - ${imageFiles.size} image files found in $directory" )
            val images : List<Bitmap> = imageFiles.map { BitmapFactory.decodeFile(it) }
            return images
        }

        public fun loadStoredFishCards(context: Context) : Observable<CardDetails> {

            val storedSites = SharedPreferencesUtils.loadSitesStoredOffline(context)
            Log.d(TAG, "loadStoredFishCards loading ${storedSites.size} saved sites: ${storedSites.toString()}")

            val dataRepo = DataRepository.getInstance(context)

            return dataRepo
                    .getSurveySitesAll(SurveySiteType.CODES)
                    .filter({ storedSites.contains(it.code) })
                    .flatMap({ dataRepo.getFishSpeciesForSite(it) })
        }



    }


    /** Retrieve your image from device and set to imageview **/
    //Provide your image path and name of the image your previously used.
    /*
    Bitmap b= loadImageFromStorage(String path, String name)
    ImageView img=(ImageView)findViewById(R.id.your_image_id);
    img.setImageBitmap(b);*/

}



