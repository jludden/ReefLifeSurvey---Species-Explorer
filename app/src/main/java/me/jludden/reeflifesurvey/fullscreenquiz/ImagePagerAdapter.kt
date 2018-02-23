package me.jludden.reeflifesurvey.fullscreenquiz

/**
 * Created by Jason on 6/17/2017.
 *
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.support.v4.view.PagerAdapter


import java.util.ArrayList

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Callback

import com.squareup.picasso.Picasso

import me.jludden.reeflifesurvey.customviews.TouchImageView
import me.jludden.reeflifesurvey.data.model.FishSpecies
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BaseDisplayableImage


class ImagePagerAdapter<T : BaseDisplayableImage>(private val mActivity: Activity, private val mListener: ImagePagerAdapterListener) : PagerAdapter() {
    private var mLayoutInflater: LayoutInflater? = null
    private var mCardList: MutableList<T> = ArrayList()
    private var mImgDisplay: ImageView? = null
    private var mCurrentPos: Int = 0
    private val PAGE_CHANGE_THRESHOLD = 3

    interface ImagePagerAdapterListener : Callback {
        fun onLoadMoreRequested()
        override fun onSuccess()
        override fun onError()
    }

    override fun getCount(): Int {
        return mCardList.size
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        mCurrentPos = position
        if (position >= mCardList.size - PAGE_CHANGE_THRESHOLD) mListener.onLoadMoreRequested()

        mLayoutInflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewLayout = mLayoutInflater!!.inflate(R.layout.fullscreen_view_pager_item, container, false)
        mImgDisplay = viewLayout.findViewById<View>(R.id.full_screen_image_view) as TouchImageView
        container.addView(viewLayout)

        //todo offline
        //todo resize?

        //Set the Card Image
        if (!mCardList.isEmpty()) {
            val cardDetails = mCardList[position]
            if (cardDetails.imageURL == "") {
                Picasso.with(mActivity)
                        .load(R.drawable.ic_menu_camera)
                        .into(mImgDisplay, mListener)
            } else {
                Picasso.with(mActivity)
                        .load(cardDetails.imageURL)
                        .placeholder(R.drawable.ic_menu_camera)
                        .error(R.drawable.ic_menu_camera)
                        .into(mImgDisplay, mListener)
            }
        }

        return viewLayout
    }

  /*  fun updateItems(data: List<FishSpecies>) {
        Log.d("jludden.reeflifesurvey", "ImagePagerAdapter update items: " + data.size)

        mCardList = data
        notifyDataSetChanged()

    }*/

    fun addItem(item: T) {
        mCardList.add(item)
        notifyDataSetChanged()
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }

    fun findPageForItem(speciesID: T): Int {
        return mCardList.indexOf(speciesID)
    }

    fun findItemForPage(page: Int): T {
        return mCardList[page]
    }
}
