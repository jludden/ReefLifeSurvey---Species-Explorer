package me.jludden.reeflifesurvey.BrowseFish;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;

import java.util.Enumeration;
import java.util.List;

import me.jludden.reeflifesurvey.BrowseFish.model.InfoCard;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.model.SurveySiteList;

import static me.jludden.reeflifesurvey.SharedPreferencesUtils.setUpFavoritesButton;

/**
 * Created by Jason on 10/25/2017.
 */

public class DetailsViewAdapter extends PagerAdapter {

    private List<InfoCard.CardDetails> mData;
    private DetailsViewFragment mDetailsViewFragment;

    private RequestManager glide;
    public DetailsViewAdapter(DetailsViewFragment parent, List<InfoCard.CardDetails> cardList){
        this.glide = Glide.with(parent); //cache the Glide RequestManager object
        this.mData = cardList;
        this.mDetailsViewFragment = parent;
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

       // ImageView imageView = (ImageView) viewLayout.findViewById(R.id.details_image_main);
        TextView textView = (TextView) viewLayout.findViewById(R.id.details_text);
        SliderLayout imageCarousel = (SliderLayout) viewLayout.findViewById(R.id.details_image_carousel);
        final CheckBox mFavoriteBtn = (CheckBox) viewLayout.findViewById(R.id.favorite_btn); //star to favorite the fish


        Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter instantiateItem mData empty:"+mData.isEmpty());

        if(!mData.isEmpty()){
             Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter instantiateItem mData not empty");
            final InfoCard.CardDetails cardDetails = mData.get(position);

            //set up favorites star button
            setUpFavoritesButton(cardDetails, mFavoriteBtn, mDetailsViewFragment.getActivity());


           /* glide
                .load(cardDetails.imageURL)
                .into(imageView);*/

            String newText =
                    cardDetails.cardName + "\n" +
                    cardDetails.commonNames + "\n" +
                    "Num sightings " + cardDetails.numSightings + "\n" +
                    "Found in "+cardDetails.FoundInSites.size()+" sites" + "\n";

            Enumeration<SurveySiteList.SurveySite> siteKeys = cardDetails.FoundInSites.keys();
            SurveySiteList.SurveySite site;
            while(siteKeys.hasMoreElements()) {
                site = siteKeys.nextElement();

                int numSightings = cardDetails.FoundInSites.get(site);
                newText = newText.concat("\n" + site.getSiteName() + "\t" + numSightings);

            }

            /*  for(String addl : cardDetails.imageURLs){
                newText = newText.concat("\n"+addl);
            }*/

            //add on page change listener if needed

            imageCarousel.stopAutoCycle();
            if(cardDetails.imageURLs == null){
                Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter card details no images to load");
                newText.concat("\n No Images Found");
//                imageCarousel.setVisibility(View.INVISIBLE);
            }
            else {
                for (String url : cardDetails.imageURLs) {
                    TextSliderView textSliderView = new TextSliderView(container.getContext());
                    textSliderView
                            .image(url)
                            .setScaleType(BaseSliderView.ScaleType.Fit);
                    imageCarousel.addSlider(textSliderView);
                }
            }
            textView.setText(newText);
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
