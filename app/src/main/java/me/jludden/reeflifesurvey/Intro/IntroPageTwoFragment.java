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

    private Picasso Picasso;
    SliderLayout mImageCarousel;

    public static IntroPageTwoFragment newInstance() {

        Bundle args = new Bundle();

        IntroPageTwoFragment fragment = new IntroPageTwoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //todo update titlebar

        Picasso = Picasso.with(getContext());
        View viewLayout = inflater.inflate(R.layout.intro_pagetwo_fragment, container, false);
        //viewLayout.setRotation(90);

        mImageCarousel = (SliderLayout) viewLayout.findViewById(R.id.intro_image_carousel);
        mImageCarousel.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        mImageCarousel.setDuration(5000);

        Log.d("jludden.reeflifesurvey", "IntroPageOneFragment loaded");

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

        for (String url : top_slider_images) {
            DefaultSliderView sliderView = new DefaultSliderView(container.getContext());
            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            mImageCarousel.addSlider(sliderView);
        }


        TextImageButton launchFavoriteSites = (TextImageButton) viewLayout.findViewById(R.id.button_launch_favorite_sites);
        Picasso
                .load("https://reeflifesurvey.com/wp-content/uploads/2017/07/Turtle.jpg")
                .into(launchFavoriteSites);
        launchFavoriteSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).launchNewFragment(CardViewFragment.class);
            }
        });


        TextImageButton launchMapView = (TextImageButton) viewLayout.findViewById(R.id.button_launch_map_view);
        Picasso
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
    public void onStop() { //todo restart image sliders when power on
        if(mImageCarousel != null) {
            mImageCarousel.removeAllSliders();
            mImageCarousel.stopAutoCycle();
        }
        super.onStop();
    }
}
