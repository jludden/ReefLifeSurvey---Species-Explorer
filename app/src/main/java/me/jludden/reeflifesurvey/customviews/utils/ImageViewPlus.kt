package me.jludden.reeflifesurvey.customviews.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import me.jludden.reeflifesurvey.R
import me.jludden.reeflifesurvey.customviews.BaseDisplayableImage
import me.jludden.reeflifesurvey.customviews.ImageDrawer

/**
 * Created by Jason on 2/14/2018.
 */
class ImageViewPlus<T : BaseDisplayableImage>: LinearLayout {

    constructor(context: Context) : super(context) {
        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

     /*   val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ImageDrawer, 0, 0)
        try {
            DRAWER_HEIGHT_COLLAPSED = a.getDimension(R.styleable.ImageDrawer_collapsedHeight, 200f) //todo def 236.25? 200? or consider the .toInt value
            DRAWER_EXPANDABLE =  a.getBoolean(R.styleable.ImageDrawer_expandable, false)
            DRAWER_STYLE = a.getInteger(R.styleable.ImageDrawer_drawerStyle, 111)
            Log.d(ImageDrawer.TAG, "constructor 2.b my typed attributes. collapsedheight: $DRAWER_HEIGHT_COLLAPSED + expandable:$DRAWER_EXPANDABLE + style:$DRAWER_STYLE")
        } finally {
            a.recycle()
        }*/

        inflateViews(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflateViews(context)
    }

    fun inflateViews(context: Context){

    }

}