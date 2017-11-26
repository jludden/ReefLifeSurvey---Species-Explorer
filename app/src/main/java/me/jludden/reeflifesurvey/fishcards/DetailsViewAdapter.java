package me.jludden.reeflifesurvey.fishcards;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.squareup.picasso.Picasso;

import java.util.Enumeration;
import java.util.List;

import me.jludden.reeflifesurvey.data.InfoCard;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.data.SurveySiteList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static me.jludden.reeflifesurvey.data.SharedPreferencesUtils.setUpFavoritesButton;

/**
 * Created by Jason on 10/25/2017.
 */

public class DetailsViewAdapter extends PagerAdapter {

    private static final String TAG = "DetailsViewAdapter";
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

        final ImageView imageView = (ImageView) viewLayout.findViewById(R.id.details_image_main);
        TextView textView = (TextView) viewLayout.findViewById(R.id.details_text);
      //  SliderLayout imageCarousel = (SliderLayout) viewLayout.findViewById(R.id.details_image_carousel);
        final CheckBox favoriteBtn = (CheckBox) viewLayout.findViewById(R.id.favorite_btn); //star to favorite the fish
        final ImageButton linkBtn = (ImageButton) viewLayout.findViewById(R.id.link_btn);

        Log.d("jludden.reeflifesurvey"  , "DetailsviewAdapter instantiateItem mData empty:"+mData.isEmpty());

        if(!mData.isEmpty()) {
            Log.d(TAG, "DetailsviewAdapter instantiateItem mData not empty");
            final InfoCard.CardDetails cardDetails = mData.get(position);

            //set up favorites star button
            setUpFavoritesButton(cardDetails, favoriteBtn, mDetailsViewFragment.getActivity());


           /* glide
                .load(cardDetails.imageURL)
                .into(imageView);*/

            StringBuilder newText = new StringBuilder(
                    cardDetails.cardName + "\n" +
                            cardDetails.commonNames + "\n" +
                            "Num sightings " + cardDetails.numSightings + "\n" +
                            "Found in " + cardDetails.FoundInSites.size() + " sites" + "\n" +
                            "Number images: " + cardDetails.imageURLs.size() + "\n");

            Enumeration<SurveySiteList.SurveySite> siteKeys = cardDetails.FoundInSites.keys();
            SurveySiteList.SurveySite site;
            while (siteKeys.hasMoreElements()) {
                site = siteKeys.nextElement();

                int numSightings = cardDetails.FoundInSites.get(site);
                newText.append("\n" + site.getSiteName() + "\t" + numSightings);

            }

            /*  for(String addl : cardDetails.imageURLs){
                newText = newText.concat("\n"+addl);
            }*/

            //add on page change listener if needed

            if(cardDetails.imageURLs != null && cardDetails.imageURLs.size() >= 1) {
                Glide.with(mDetailsViewFragment)
                        .load( cardDetails.imageURLs.get(0))
                        .transition(withCrossFade())
                        .into(imageView);
            }

          /*  imageCarousel.stopAutoCycle();
            if (cardDetails.imageURLs == null) {
                Log.d(TAG, "DetailsviewAdapter card details no images to load");
                newText.append("\n No Images Found");
//                imageCarousel.setVisibility(View.INVISIBLE);
            } else {
                for (String url : cardDetails.imageURLs) {
                    DefaultSliderView sliderView = new DefaultSliderView(container.getContext());
                    sliderView
                            .image(url)
                            .setScaleType(BaseSliderView.ScaleType.Fit);
                    imageCarousel.addSlider(sliderView);
                }
            }*/

            LinearLayout additionalImages = (LinearLayout) viewLayout.findViewById(R.id.details_additional_images);
            if (cardDetails.imageURLs == null) {
                Log.d(TAG, "DetailsviewAdapter card details no images to load");
                newText.append("\n No Images Found");
//                imageCarousel.setVisibility(View.INVISIBLE);
            } else {
                for (String url : cardDetails.imageURLs) {
                    ImageView iv = new ImageView(mDetailsViewFragment.getContext());
                    iv.setLayoutParams(new LinearLayout.LayoutParams(250,250));
                    final String newMainURL = url;

                    //todo really shouldnt make a new one for each item
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Picasso.with(mDetailsViewFragment.getContext())
                                    .load(newMainURL)
                                    .placeholder(imageView.getDrawable()) //placeholder = current image, to minimize gap
                                    .into(imageView);
                        }
                    });

                    Picasso.with(mDetailsViewFragment.getContext())
                            .load(url)
                            .placeholder(R.drawable.ic_menu_camera)
                            .into(iv);
                    iv.setPadding(0, 0, 0, 5);
                    additionalImages.addView(iv);
                }
            }


            newText.append("\n").append("SPECIES PAGE URL: ").append(cardDetails.reefLifeSurveyURL);

            textView.setText(newText.toString());

            linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = cardDetails.reefLifeSurveyURL;
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        mDetailsViewFragment.getActivity().startActivity(intent);
                    }
                }
            });
        }

        container.addView(viewLayout);
        return viewLayout;
    }




    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LinearLayout linearLayout = (LinearLayout) object;

        //todo possibly need to do this when power off as well (onStop in fragment?)
        /*SliderLayout imageCarousel = (SliderLayout) linearLayout.findViewById(R.id.details_image_carousel);
        if(imageCarousel != null) {
            imageCarousel.removeAllSliders();
            imageCarousel.stopAutoCycle();
        }*/

        // todo glide clear
        // InfoCard.CardDetails cardDetails = mData.get(position);

        container.removeView(linearLayout);

    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mData.size();
    }


}
