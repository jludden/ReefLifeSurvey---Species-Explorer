package me.jludden.reeflifesurvey.fullscreenquiz;

/**
 * Created by Jason on 6/17/2017.
 *
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import me.jludden.reeflifesurvey.fullscreenquiz.FullScreenImageListener;
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


         //Add Image View
        Button btnClose;
        mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewLayout = mLayoutInflater.inflate(R.layout.image_pager_item, container, false);
        //viewLayout.setRotationY(180); //todo testing
        mImgDisplay = (TouchImageView) viewLayout.findViewById(R.id.full_screen_image_view);
        container.addView(viewLayout);

        //Log.d("jludden.reeflifesurvey"  , "FullScreenImageAdapter instantiateItem mCardList empty:"+mCardList.isEmpty());

        //Set the Card Image
        if(!mCardList.isEmpty()){
           // Log.d("jludden.reeflifesurvey"  , "FullScreenImageAdapter instantiateItem mCardList not empty");
            FishSpecies cardDetails = mCardList.get(position);
//            mListener.updateItemDescription(cardDetails);

            if(Objects.equals(cardDetails.getPrimaryImageURL(), "")){
                Picasso.with(mActivity)
                        .load(R.drawable.ic_menu_camera)
                        .into(mImgDisplay);
            } else {
                //set up image
                Picasso.with(mActivity)
                        .load(cardDetails.getPrimaryImageURL())
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
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
        //
        // todo glide clear
        // InfoCard.FishSpecies cardDetails = mCardList.get(position);

    }

    /**
     * Called when a certain swipe ontouch event is detected
     * Show some details about the image, such as common names for an image for a fish
     * TODO change toast to snackbar - destroy it if we change pages
     */
    public void showDetails(){
        Log.d("jludden.reeflifesurvey"  , "FullScreenImageAdapter swipe event for " + mCurrentPos);
        if (mImgDisplay != null) {
            //Consider a sliding up panel:
            //https://github.com/umano/AndroidSlidingUpPanel
            //or a snackbar

//            Snackbar.make(mActivity.findViewById(android.R.id.content), "Swipe Up Detected", Snackbar.LENGTH_LONG)
//                               .setAction("Action", null).show();
            String description = mCardList.get(mCurrentPos-1).commonNames; //idk why -1 but it works
            Toast.makeText(mActivity, description , Toast.LENGTH_SHORT).show();

//            View rootView = mActivity.getWindow().getDecorView().getRootView();
//            Snackbar.make(rootView, description, Snackbar.LENGTH_SHORT).show();

        }
    }

    public int findPageForItem(FishSpecies speciesID) {
        return mCardList.indexOf(speciesID);
    }

    public FishSpecies findItemForPage(int page) {
        return mCardList.get(page);
    }
}
