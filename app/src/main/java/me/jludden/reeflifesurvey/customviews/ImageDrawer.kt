package me.jludden.reeflifesurvey.customviews

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_drawer_item.view.*
import me.jludden.reeflifesurvey.R
import android.support.v7.widget.GridLayoutManager
import com.squareup.picasso.RequestCreator
import kotlinx.android.synthetic.main.image_drawer.view.*


/**
 * Created by Jason on 1/2/2018.
 */
class ImageDrawer<T : BaseDisplayableImage>: LinearLayout {

    private lateinit var interactionListener: ImageDrawer.OnImageDrawerInteractionListener
    private lateinit var viewAdapter: ImageDrawerAdapter<T>

    private var prevSlideOffset = 0.0f

    constructor(context: Context) : super(context) {
        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        inflateViews(context)

        val myheight = attrs?.getAttributeIntValue("http://schemas.android.com/apk/r‌​es/android", "layout_height", 98)
        val realheight = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "behavior_peekHeight", 99)
        Log.d(TAG, "constructor 2 attributes: $height + myheight:$myheight + realheight:$realheight")
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflateViews(context)

        val myheight = attrs?.getAttributeIntValue("http://schemas.android.com/apk/r‌​es/android", "layout_height", 98)
        val realheight = attrs?.getAttributeIntValue("http://schemas.android.com/apk/res-auto", "behavior_peekHeight", 99)
        Log.d(TAG, "constructor 3 attributes: $height + myheight:$myheight + realheight:$realheight")
    }

    private fun inflateViews(context: Context) {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.image_drawer, this)

        if (context is ImageDrawer.OnImageDrawerInteractionListener) interactionListener = context
        else throw RuntimeException("$context must implement ImageDrawer.OnImageDrawerInteractionListener")

        val recyclerView = findViewById<RecyclerView>(R.id.image_drawer_content)
        recyclerView.layoutManager = LinearLayoutManager(context, orientation, false)
        viewAdapter = ImageDrawerAdapter<T>(interactionListener)
        recyclerView.adapter = viewAdapter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState    The new state
             */
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d(TAG, "Image Drawer Bottom Sheet OnStateChanged: " + newState)
                interactionListener.onDrawerStateChanged(newState)

                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    val test = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

//                    val test = LayoutParams(LayoutParams.MATCH_PARENT, HEIGHT_GRID_EXPANDED)
                    image_drawer_rv_container.layoutParams = test //todo
                    image_drawer_content.layoutParams = test

                    val recyclerView = findViewById<RecyclerView>(R.id.image_drawer_content)
                    recyclerView.layoutManager = GridLayoutManager(context, SPAN_COUNT)
                    recyclerView.adapter = viewAdapter

                }
                else {

                    val newHeight = context.resources.getDimension(R.dimen.imageDrawer_height_collapsed).toInt() // converts dp to px
                    image_drawer_rv_container.layoutParams.height = newHeight //todo
                    image_drawer_content.layoutParams.height = newHeight

                    val recyclerView = findViewById<RecyclerView>(R.id.image_drawer_content)
                    recyclerView.layoutManager = LinearLayoutManager(context, orientation, false)
                    recyclerView.adapter = viewAdapter
                }
            }

            /**
             * Called when the bottom sheet is being dragged.
             *
             * @param bottomSheet The bottom sheet view.
             * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
             * increases as this bottom sheet is moving upward. From 0 to 1 the sheet
             * is between collapsed and expanded states and from -1 to 0 it is
             */
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d(TAG, "Image Drawer Bottom Sheet onSlide. old offset: $prevSlideOffset   new offset: $slideOffset")

                /* offset 0 -> 0.9 alpha
                 *        1 -> 1
                 */

                bottomSheet.alpha = (slideOffset)/10 + 0.9f
                prevSlideOffset = slideOffset
            }
        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.d(TAG, "onFinishInflate: ")
    }

    fun addItem(item: T) {
        Log.d(TAG, "add item $item")
        viewAdapter.updateItems(item)
    }

    fun setList(list: MutableList<T>) {
        Log.d(TAG, "add list: ${list.size} items")
        viewAdapter.updateItems(list = list)
    }

    interface OnImageDrawerInteractionListener {
        fun onImageClicked(item: BaseDisplayableImage, sharedElement: View)

        fun onDrawerStateChanged(newState: Int)
    }

    /**
     * RecyclerView Adapter implementation
     */
    class ImageDrawerAdapter<T : BaseDisplayableImage>(private val interactionListener: OnImageDrawerInteractionListener)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var data: MutableList<T> = ArrayList()
        fun updateItems(element: T? = null, list: MutableList<T>? = null){
            if(list!=null) data = list
            if(element!=null) data.add(element)
            notifyDataSetChanged()
        }

        override fun getItemCount() = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder
                = ImageViewHolder(parent.inflate(R.layout.image_drawer_item))

        fun ViewGroup.inflate(layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
            = (holder as ImageViewHolder).bind(data[position], interactionListener)
    }

   /* override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(measuredWidth, ((measuredWidth * 1.32f).toInt()));

    }*/

    /**
     * ViewHolder
     */
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BaseDisplayableImage, listener: OnImageDrawerInteractionListener) = with(itemView) {
            image_drawer_item_imageview.loadURL(item.imageURL)
            setOnClickListener({
                item.imageClickListener?.onImageClick(item) //call individual onclick listener if it's set
                listener.onImageClicked(item, itemView) }) //call main_toolbar drawer listener
        }
        private fun ImageView.loadURL(url: String) {
            Log.d(TAG, "measured height: ${this.measuredHeight}")

            val picassoRequest : RequestCreator
            if(url == ""){
                picassoRequest = Picasso.with(context).load(R.drawable.ic_menu_camera)
            } else {
                picassoRequest = Picasso.with(context).load(url)
            }

            picassoRequest
                    .resize(0, 300)
                    //.resize(this.measuredHeight, 0)
                    // .fit() //  .resize(90,90)
                    .error(FALLBACK_DRAWABLE)
                    .into(this)
        }
    }

    companion object {
        const val TAG = "ImageDrawer"
        const val FALLBACK_DRAWABLE = R.drawable.ic_menu_camera

        //TODO dimen?
        const val SPAN_COUNT = 4
    }
}

/**
 * Base class for images that can be displayed in the image drawer
 *  @param imageURL - string or res loaded with picasso
 *  @param identifier - optional object that can be associated with the image and retrieved when the item is clicked (such as a String identifier)
 *  @param imageClickListener - optional listener for when this item, but not others, is clicked
 */
open class BaseDisplayableImage(val imageURL: String, val identifier: kotlin.Any?,
                                var imageClickListener: OnBaseDisplayableImageClickListener? = null) {
    interface OnBaseDisplayableImageClickListener {
        fun onImageClick(obj : BaseDisplayableImage)
    }
}