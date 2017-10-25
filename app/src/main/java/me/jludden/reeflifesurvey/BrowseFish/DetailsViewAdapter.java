package me.jludden.reeflifesurvey.BrowseFish;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.List;

import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;
import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 10/25/2017.
 */

public class DetailsViewAdapter extends PagerAdapter {

    private List<InfoCard.CardDetails> mData;
    private RequestManager glide;
    public DetailsViewAdapter(DetailsViewFragment parent, List<InfoCard.CardDetails> cardList){
        this.glide = Glide.with(parent); //cache the Glide RequestManager object
        mData = cardList;
    }

    /**
     * Determines whether a page View is associated with a specific key object
     * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
     * required for a PagerAdapter to function properly.
     *
     * @param view   Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     * @return true if <code>view</code> is associated with the key object <code>object</code>
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        final View viewLayout = inflater.inflate(R.layout.details_view_pager_item, container, false);

        ImageView imageView = (ImageView) viewLayout.findViewById(R.id.details_image_main);
        TextView textView = (TextView) viewLayout.findViewById(R.id.details_text);

        Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter instantiateItem mData empty:"+mData.isEmpty());

        //Set the Card Image
        if(!mData.isEmpty()){
             Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter instantiateItem mData not empty");
            InfoCard.CardDetails cardDetails = mData.get(position);

            glide
                .load(cardDetails.imageURL)
                .into(imageView);

            textView.setText(
                    cardDetails.cardName + "\n" +
                    cardDetails.commonNames + "\n" +
                    "Has multiple images: "+cardDetails.hasMultipleImages + "\n" +
                    "Found in "+cardDetails.FoundInSites.size()+" sites"
                    );

        }

        container.addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);

        // todo glide clear
        // InfoCard.CardDetails cardDetails = mData.get(position);
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mData.size();
    }

}
