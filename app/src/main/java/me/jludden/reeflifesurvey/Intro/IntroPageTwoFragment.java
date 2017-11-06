package me.jludden.reeflifesurvey.Intro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import me.jludden.reeflifesurvey.MainActivity;
import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 11/4/2017.
 *
 * The second page of the intro will be a collage
 *  one or more imagecarousel
 *  links to other fragments
 *
 * Best of / Random Images
 * Surveysite map
 * Favorite Sites
 * ReefLifeSurvey.com
 *
 * Quiz Mode!
 * Random Site?
 * Favorite fish?
 * All Fish?
 *
 */

public class IntroPageTwoFragment extends Fragment {
    public static IntroPageTwoFragment newInstance() {

        Bundle args = new Bundle();

        IntroPageTwoFragment fragment = new IntroPageTwoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //todo update titlebar

        View viewLayout = inflater.inflate(R.layout.intro_pagetwo_fragment, container, false);
        //viewLayout.setRotation(90);

        SliderLayout imageCarousel = (SliderLayout) viewLayout.findViewById(R.id.intro_image_carousel);
       imageCarousel.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);


        Log.d("jludden.reeflifesurvey", "IntroPageOneFragment loaded");

        String[] urls = {
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

        for (String url : urls) {
            DefaultSliderView sliderView = new DefaultSliderView(container.getContext());
            sliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            imageCarousel.addSlider(sliderView);
        }


       //MyButton launchFavoriteSites = (MyButton) viewLayout.findViewById(R.id.button_launch_favorite_sites);
        //ImageButton launchFavoriteSites = (ImageButton) viewLayout.findViewById(R.id.button_launch_favorite_sites);
        TextImageButton launchFavoriteSites = (TextImageButton) viewLayout.findViewById(R.id.button_launch_favorite_sites);

        Picasso
                .with(getContext())
                .load("https://reeflifesurvey.com/wp-content/uploads/2017/07/Turtle.jpg")
                //.fit()
                .into(launchFavoriteSites);


        launchFavoriteSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).launchNewCardViewFragment("");
            }
        });



        return viewLayout;
    }
}
