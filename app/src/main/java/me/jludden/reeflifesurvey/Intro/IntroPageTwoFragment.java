package me.jludden.reeflifesurvey.Intro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;

import me.jludden.reeflifesurvey.BrowseFish.CardViewFragment;
import me.jludden.reeflifesurvey.InterfaceComponents.TextImageButton;
import me.jludden.reeflifesurvey.MainActivity;
import me.jludden.reeflifesurvey.MapViewFragment;
import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 11/4/2017.
 *
 * The second page of the intro will be a collage
 *  one or more imagecarousel
 *  links to other fragments
 *
 * Best of / Random * Surveysite map
 Images
 * Favorite Sites
 * ReefLifeSurvey.com
 * Best of Species (sharks, cool fish) + THEIR FAVORITED FISH IMAGES
 * Quiz Mode!
 *
 * Random Site?
 * Favorite fish?
 * All Fish?
 *
 */

public class IntroPageTwoFragment extends Fragment {

    private Picasso mPicasso;
    SliderLayout mImageCarousel;
    SliderLayout mImageCarouselSmall;

    public static IntroPageTwoFragment newInstance() {

        Bundle args = new Bundle();

        IntroPageTwoFragment fragment = new IntroPageTwoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //todo update titlebar

        mPicasso = Picasso.with(getContext());
        View viewLayout = inflater.inflate(R.layout.intro_pagetwo_fragment, container, false);
        //viewLayout.setRotation(90);

        mImageCarousel = (SliderLayout) viewLayout.findViewById(R.id.intro_image_carousel);
        mImageCarousel.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        mImageCarousel.setDuration(12000);

        Log.d("jludden.reeflifesurvey", "IntroPageOneFragment loaded");


  /*      for (String url : top_slider_images) {
            DefaultSliderView sliderView = new DefaultSliderView(container.getContext());
            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            mImageCarousel.addSlider(sliderView);
        }*/


        mImageCarouselSmall = (SliderLayout) viewLayout.findViewById(R.id.intro_image_carousel_TWO);
//        mImageCarouselSmall.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        mImageCarouselSmall.setDuration(10000);

        startSlider(mImageCarousel, top_slider_images, null);
        startSlider(mImageCarouselSmall, bigFishURLs, bigFishNames);


/*        for(int i = 0; i < bigFishURLs.length ; i++) {
            String bigFishName = bigFishNames[i];
            String bigFishURL = bigFishURLs[i];
            TextSliderView sliderView = new TextSliderView(container.getContext());
            sliderView
                    .description(bigFishName)
                    .image(bigFishURL)
                    //todo set launch link to open fish details
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            mImageCarouselSmall.addSlider(sliderView);
        }*/



        TextImageButton launchFavoriteSites = (TextImageButton) viewLayout.findViewById(R.id.button_launch_favorite_sites);
        mPicasso
                .load("https://reeflifesurvey.com/wp-content/uploads/2017/07/Turtle.jpg")
                .into(launchFavoriteSites);
        launchFavoriteSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).launchNewFragment(CardViewFragment.class);
            }
        });


        TextImageButton launchMapView = (TextImageButton) viewLayout.findViewById(R.id.button_launch_map_view);
        mPicasso
                .load("https://reeflifesurvey.com/wp-content/uploads/2016/11/Carrie-Bow-Cay_Belize-2-2-small.jpeg")
                .placeholder(R.drawable.ic_map_action)
                .into(launchMapView);
        launchMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).launchNewFragment(MapViewFragment.class);
            }
        });

        TextImageButton launchWebsite = (TextImageButton) viewLayout.findViewById(R.id.button_launch_website);
        launchWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getString(R.string.website_launch_url);
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });


        return viewLayout;
    }

    @Override
    public void onResume() {
        startSlider(mImageCarousel, top_slider_images, null);
        startSlider(mImageCarouselSmall, bigFishURLs, bigFishNames);

        super.onResume();
    }

    @Override
    public void onStop() { //todo restart image sliders when power on
        if(mImageCarousel != null) {
            mImageCarousel.removeAllSliders();
            mImageCarousel.stopAutoCycle();
        }
        if(mImageCarouselSmall != null) {
            mImageCarouselSmall.removeAllSliders();
            mImageCarouselSmall.stopAutoCycle();
        }
        super.onStop();
    }


    public void startSlider(SliderLayout slider, String[] urls, @Nullable String[] descriptions){
        slider.startAutoCycle();
        for(int i = 0; i < urls.length ; i++) {
            String url = urls[i];

            BaseSliderView sliderView;
            if(descriptions != null && descriptions.length > 0){
                sliderView = new TextSliderView(getContext());
                sliderView.description(descriptions[i]); //todo unhandled error
                //todo set launch link to open fish details
            } else {
                sliderView = new DefaultSliderView(getContext());
            }

            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            slider.addSlider(sliderView);
        }

    }

    String[] top_slider_images = {
            "https://reeflifesurvey.com/wp-content/uploads/2017/08/DSC_4282_KrisOkeefe-11.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2017/08/DSC_4282_KrisOkeefe-2.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2017/08/DSC_4282_KrisOkeefe-1.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2015/08/Ningaloo-Diver1.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2015/08/P7267769.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2015/08/Bumphead-MaoriToni_1080x400.jpg",
            "https://reeflifesurvey.com/wp-content/uploads/2016/11/South-Reef_Belize-13-small.jpeg",
            "https://reeflifesurvey.com/wp-content/uploads/2016/11/Cephalopholis-cruentata_Belize-small.jpeg",
            "https://reeflifesurvey.com/wp-content/uploads/2016/11/Caranx-latus_Belize-small.jpeg"
    };

    String[] bigFishURLs = {
            "https://images.reeflifesurvey.com/0/species_1c_5704a01e5ad90.w1300.h866.jpg",
            "https://images.reeflifesurvey.com/0/species_17_576b40231c6c2.w1300.h866.JPG",
            "https://images.reeflifesurvey.com/0/species_e7_57463ab55fe49.w1300.h866.jpg",
            "https://images.reeflifesurvey.com/0/species_2f_574694db6acd7.w1300.h866.jpg"
    };

    String[] bigFishNames = {
            "White tip reef shark",
            "Green Turtle",
            "Humphead Maori Wrasse",
            "Giant Cuttle"
    };
}
