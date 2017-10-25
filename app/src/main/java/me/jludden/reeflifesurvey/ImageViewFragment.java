package me.jludden.reeflifesurvey;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 6/11/2017.
 */
//TODO unused?
public class ImageViewFragment extends Fragment {

    ImageView mImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("jludden.reeflifesurvey"  ,"ImageViewFragment View Created");

        //TODO this
        View view = inflater.inflate(R.layout.image_view_activity, container, false);
        mImageView = (ImageView) view;
        mImageView.setImageResource(R.drawable.ic_menu_camera);

//        GlideApp.with(this)
        Glide.with(getContext()).load("http://dbzcuiesi59ut.cloudfront.net/0/species_18_574d6ef81a9a2.w1300.h866.jpg").into(mImageView);
        return mImageView;
    }

}
