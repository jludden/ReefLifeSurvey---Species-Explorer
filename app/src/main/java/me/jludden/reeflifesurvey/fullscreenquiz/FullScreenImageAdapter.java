package me.jludden.reeflifesurvey.fullscreenquiz;

/**
 * Created by Jason on 6/17/2017.
 *
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.support.v4.view.PagerAdapter;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import me.jludden.reeflifesurvey.customviews.TouchImageView;
import me.jludden.reeflifesurvey.data.model.FishSpecies;
import me.jludden.reeflifesurvey.R;


public class FullScreenImageAdapter extends PagerAdapter {

    private final FullScreenImageListener mListener;
    private Activity mActivity;
    private LayoutInflater mLayoutInflater;
    private List<FishSpecies> mCardList;
    private ImageView mImgDisplay;
    private int mCurrentPos;
    private final int PAGE_CHANGE_THRESHOLD = 3;

    // constructor
    public FullScreenImageAdapter(Activity activity, FullScreenImageListener listener) {
        this.mActivity = activity;
        this.mCardList = new ArrayList<>();
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return mCardList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mCurrentPos = position;
        if(position >= (mCardList.size() - PAGE_CHANGE_THRESHOLD)) mListener.onLoadMoreRequested();

        mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewLayout = mLayoutInflater.inflate(R.layout.fullscreen_view_pager_item, container, false);
        mImgDisplay = (TouchImageView) viewLayout.findViewById(R.id.full_screen_image_view);
        container.addView(viewLayout);

        //Set the Card Image
        if(!mCardList.isEmpty()){
            FishSpecies cardDetails = mCardList.get(position);
            if(cardDetails.getPrimaryImageURL().equals("")){
                Picasso.with(mActivity)
                        .load(R.drawable.ic_menu_camera)
                        .into(mImgDisplay);
            } else {
                Picasso.with(mActivity)
                        .load(cardDetails.getPrimaryImageURL())
                        .placeholder(R.drawable.ic_menu_camera)
                        .error(R.drawable.ic_menu_camera)
                        .into(mImgDisplay);
            }
        }

        return viewLayout;
    }

    public void updateItems(List<FishSpecies> data){
        Log.d("jludden.reeflifesurvey"  , "FullScreenImageAdapter update items: " +data.size());

        mCardList = data;
        notifyDataSetChanged();

        //TODO testing

//        if(!mCardList.isEmpty()){
//            Log.d("jludden.reeflifesurvey"  , "FullScreenImageAdapter updateItems drawing not empty");
//            InfoCard.FishSpecies cardDetails = mCardList.get(0);
//
//            //set up image
//            if(mImgDisplay == null) return;
//            // Try to draw from drawable first, then fall back to bitmap
//            Drawable image = cardDetails.image;
//            Bitmap flag = cardDetails.imageBitmap;
//            if(image != null)  mImgDisplay.setImageDrawable(image);
//            else if (flag != null) mImgDisplay.setImageBitmap(flag);
//        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) { container.removeView((ImageView) object); }

    public int findPageForItem(FishSpecies speciesID) {
        return mCardList.indexOf(speciesID);
    }

    public FishSpecies findItemForPage(int page) {
        return mCardList.get(page);
    }
}
