package me.jludden.reeflifesurvey.Intro;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.squareup.picasso.Picasso;

import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 11/4/2017.
 *
 * The First Page of the Intro will be a large splash screen
 */

public class IntroPageOneFragment extends Fragment {

    private RequestManager glide;
    private RequestOptions mGlideRequestOptions;


    public static IntroPageOneFragment newInstance() {

        Bundle args = new Bundle();

        IntroPageOneFragment fragment = new IntroPageOneFragment();
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final String[] urls = {
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
        int num = 8;

        Log.d("jludden.reeflifesurvey", "IntroPageOneFragment loaded");
        //todo update titlebar
        final View viewLayout = inflater.inflate(R.layout.intro_pageone_fragment, container, false);
        final ImageView imageView= (ImageView) viewLayout.findViewById(R.id.intro_fullscreen_image);

        /*this.glide = Glide.with(this); //cache the Glide RequestManager object
        this.mGlideRequestOptions = new RequestOptions().placeholder(R.drawable.ic_menu_camera); //todo change placeholder image
        glide.load(urls[num]).apply(mGlideRequestOptions).into(imageView);*/


   /*     Picasso
                .with(getContext())
                .load(urls[num])
                .rotate(90f)
                .into(imageView);*/


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        //.resize(width,0)


        Picasso
                .with(getContext())
                .load(urls[num])
                .rotate(90f)
               // .resize(0,height)
                .into(imageView);

        final String imageURL = urls[num];
        final int imageDelay = 30000;

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                Picasso
                        .with(getContext())
                        .load(urls[index++])
                        .rotate(90f)
                        .placeholder(imageView.getDrawable()) //placeholder = current image, to minimize gap
                        .into(imageView);
                if(index > urls.length-1){
                    index = 0;
                }
                handler.postDelayed(this, imageDelay);
            }
        };
        handler.postDelayed(runnable, imageDelay);










/*
        imageView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    //wait until layout to call picasso so it can properly fill the screen
                    @Override
                    public void onGlobalLayout() {
                        // Ensure we call this only once
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            imageView.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }

                        Picasso.with(getContext())
                                .load(imageURL)
                                .placeholder(R.drawable.ic_menu_camera)
                                .resize(0, imageView.getHeight())
                                .into(imageView);
                    }
                });
*/


        /*SliderLayout mImageCarousel = (SliderLayout) viewLayout.findViewById(R.id.intro_fullscreen_carousel);
        //mImageCarousel.setRotationY();
        mImageCarousel.stopAutoCycle();
        for (String url : urls) {
            TextSliderView textSliderView = new TextSliderView(container.getContext());
            textSliderView
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            mImageCarousel.addSlider(textSliderView);
        }*/


        return viewLayout;
    }

}
