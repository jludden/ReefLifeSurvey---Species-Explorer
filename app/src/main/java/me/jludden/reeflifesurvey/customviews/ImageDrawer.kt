package me.jludden.reeflifesurvey.customviews

import android.app.Activity
import android.content.Context
import android.support.design.widget.CoordinatorLayout
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
import me.jludden.reeflifesurvey.R
import android.support.v7.widget.GridLayoutManager
import com.squareup.picasso.RequestCreator
import kotlinx.android.synthetic.main.image_drawer_item.view.*
import kotlinx.android.synthetic.main.image_drawer.view.*
import android.util.DisplayMetrics




/**
 * View that holds a list of images, with bottom sheet behavior, and the list of images can be expanded into a grid of images
 *  Created by Jason Ludden on 1/2/2018
 */
@CoordinatorLayout.DefaultBehavior(BottomDrawerBehavior::class)
class ImageDrawer<T : BaseDisplayableImage>: LinearLayout {

    private lateinit var interactionListener: ImageDrawer.OnImageDrawerInteractionListener
    private lateinit var viewAdapter: ImageDrawerAdapter<T>
    private var prevSlideOffset = 0.0f
    private var GRID_LAYOUT = false
    private var GRID_WIDTH = 0 //todo
    private var DRAWER_HEIGHT_COLLAPSED: Float = 0f
    private var DRAWER_EXPANDABLE: Boolean = false
    private var DRAWER_STYLE: Int = 1 //todo
    //= (context as Activity).windowManager.defaultDisplay.getMetrics(DisplayMetrics()).
/*                val displayMetrics = DisplayMetrics()
                (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
                val width = displayMetrics.widthPixels / SPAN_COUNT*/

    constructor(context: Context) : super(context) {
        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ImageDrawer, 0, 0)
        try {
            DRAWER_HEIGHT_COLLAPSED = a.getDimension(R.styleable.ImageDrawer_collapsedHeight, 200f) //todo def 236.25? 200? or consider the .toInt value
            DRAWER_EXPANDABLE =  a.getBoolean(R.styleable.ImageDrawer_expandable, false)
            DRAWER_STYLE = a.getInteger(R.styleable.ImageDrawer_drawerStyle, 111)
            Log.d(TAG, "constructor 2.b my typed attributes. collapsedheight: $DRAWER_HEIGHT_COLLAPSED + expandable:$DRAWER_EXPANDABLE + style:$DRAWER_STYLE")
        } finally {
            a.recycle()
        }

        inflateViews(context)
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

        viewAdapter = ImageDrawerAdapter<T>(interactionListener)
        setupRecyclerView(LinearLayoutManager(context, orientation, false))

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        GRID_WIDTH = displayMetrics.widthPixels / SPAN_COUNT
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val behavior = BottomDrawerBehavior.from(this)
        behavior.setBottomSheetCallback(object : BottomDrawerBehavior.BottomSheetCallback() {
            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState    The new state
             */
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d(TAG, "Image Drawer Bottom Sheet OnStateChanged: " + newState)
                interactionListener.onDrawerStateChanged(newState)

                if(newState == BottomDrawerBehavior.STATE_EXPANDED){
                    val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    image_drawer_rv_container.layoutParams = layoutParams //todo
                    image_drawer_content.layoutParams = layoutParams
                    setupRecyclerView(GridLayoutManager(context, SPAN_COUNT))
                }
                else {
                    val newHeight = DRAWER_HEIGHT_COLLAPSED.toInt() // converts dp to px
                    image_drawer_rv_container.layoutParams.height = newHeight //todo
                    image_drawer_content.layoutParams.height = newHeight
                    setupRecyclerView(LinearLayoutManager(context, orientation, false))
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                Log.d(TAG, "Image Drawer Bottom Sheet onSlide. old offset: $prevSlideOffset   new offset: $slideOffset")
                /* offset 0 -> 0.9 alpha
                 *        1 -> 1
                 */
                bottomSheet.alpha = (slideOffset)/10 + 0.9f
                prevSlideOffset = slideOffset
            }
        })
    }

    //sets the layout manager, adapter, and scroll listener for the recyclerview
    private fun setupRecyclerView(layoutManager: LinearLayoutManager) {
        val recyclerView = image_drawer_content//findViewById<RecyclerView>(R.id.image_drawer_content)
        if(recyclerView.layoutManager != null){ //scroll to previous position
            layoutManager.scrollToPosition((recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition())
        }
        recyclerView.clearOnScrollListeners()
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = viewAdapter
        recyclerView.addOnScrollListener(RVScrollListener({interactionListener.onLoadMoreRequested()}, layoutManager))
        GRID_LAYOUT = (layoutManager is GridLayoutManager)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Log.d(TAG, "onFinishInflate: ")
    }

    fun addItem(item: T) {
        Log.d(TAG, "add item $item")
        viewAdapter.updateItems(element = item)
    }

    fun setList(list: MutableList<T>) {
        Log.d(TAG, "add list: ${list.size} items")
        viewAdapter.updateItems(list = list)
        //image_drawer_content.adapter = viewAdapter
    }

    fun scrollTo(position: Int) {
        (findViewById<RecyclerView>(R.id.image_drawer_content).layoutManager as LinearLayoutManager).scrollToPosition(position)
    }

    interface OnImageDrawerInteractionListener {
        fun onImageClicked(item: BaseDisplayableImage, sharedElement: View)

        fun onDrawerStateChanged(newState: Int)

        fun onLoadMoreRequested()
    }

    /**
     * RecyclerView Adapter implementation
     */
    inner class ImageDrawerAdapter<T : BaseDisplayableImage>(private val interactionListener: OnImageDrawerInteractionListener)
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
            = (holder as ImageDrawer<T>.ImageDrawerAdapter<T>.ImageViewHolder).bind(data[position], interactionListener)


    /**
     * ViewHolder
     */
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: BaseDisplayableImage, listener: OnImageDrawerInteractionListener) = with(itemView) {
            image_drawer_item_imageview.loadURL(item.imageURL)

            setOnClickListener({
                item.imageClickListener?.onImageClick(item) //call individual onclick listener if it's set
                listener.onImageClicked(item, itemView)     //call entire drawer listener
            })
        }

        private fun ImageView.loadURL(url: String) {
            val picassoRequest: RequestCreator =
                    if (url == "") Picasso.with(context).load(FALLBACK_DRAWABLE_RES)
                    else Picasso.with(context).load(url)

            val drawerHeight = DRAWER_HEIGHT_COLLAPSED.toInt()
//            Log.d(TAG, "dhb4 ${context.resources.getDimension(R.dimen.imageDrawer_height_collapsed)} dhb4_int ${context.resources.getDimension(R.dimen.imageDrawer_height_collapsed).toInt()} dhaf ${DRAWER_HEIGHT_COLLAPSED} dhaf_int ${DRAWER_HEIGHT_COLLAPSED.toInt()}")

            if (GRID_LAYOUT) {

                val ph = resources.getDrawable(FALLBACK_DRAWABLE_RES) //todo
                ph.setBounds(0, 0, GRID_WIDTH, drawerHeight)

                picassoRequest
                        .placeholder(ph)
                        .error(ph)
                        .resize(GRID_WIDTH, 0)
                        .onlyScaleDown()

                        .into(this)

                } else { //ROW Layout
    //            Log.d(TAG,"drawer height: $drawerHeight")

                    val ph = resources.getDrawable(FALLBACK_DRAWABLE_RES) //todo
                    ph.setBounds(0, 0, drawerHeight, drawerHeight)

                    picassoRequest
                            .placeholder(ph)
                            .error(ph)
                            //  .fit()
                            .resize(0, drawerHeight)
                        .onlyScaleDown()
    //                    .centerCrop()
                            //.centerInside()
                            .into(this)
    //                    .into(this, object : Callback{
    //                        override fun onSuccess() {
    //                        }
    //
    //                        override fun onError() {
    //                            this@loadURL.layoutParams = RecyclerView.LayoutParams(drawerHeight, drawerHeight)
    //                            this@loadURL.setImageDrawable(context.getDrawable(FALLBACK_DRAWABLE_RES))
    //                        }
    //                    })
                }
        }
    }
    }

    /*props https://github.com/juanchosaravia/KedditBySteps/blob/master/app/src/main/java/com/droidcba/kedditbysteps/commons/InfiniteScrollListener.kt#L36*/
    class RVScrollListener(val onLoadMore: () -> Unit, val layoutManager: LinearLayoutManager)
        : RecyclerView.OnScrollListener() {
        private var previousTotal = 0
        private var loading = true
        private var visibleThreshold = 2
        private var firstVisibleItem = 0
        private var visibleItemCount = 0
        private var totalItemCount = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            visibleItemCount = recyclerView.childCount
            totalItemCount = layoutManager.itemCount
            firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false
                    previousTotal = totalItemCount
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                onLoadMore()
                loading = true
            }
        }
    }

    companion object {
        const val TAG = "ImageDrawer"
//        const val FALLBACK_DRAWABLE_RES = R.drawable.image_drawer_placeholder //todo need a white version?
        const val FALLBACK_DRAWABLE_RES = R.drawable.ic_menu_camera //todo need a white version?

        //TODO dimen?
        const val SPAN_COUNT = 4

        const val SCROLLING_VISIBLE_THRESHOLD = 1
    }
}

/**
 * Base class for images that can be displayed in the image drawer
 *  @param imageURL - string or res loaded with picasso
 *  @param identifier - optional object that can be associated with the image and retrieved when the item is clicked (such as a String identifier)
 *  @param imageClickListener - optional listener for when this item, but not others, is clicked
 */
open class BaseDisplayableImage(val imageURL: String,
            val identifier: kotlin.Any?, var imageClickListener: OnBaseDisplayableImageClickListener? = null) {
    interface OnBaseDisplayableImageClickListener {
        fun onImageClick(obj : BaseDisplayableImage)
    }
}