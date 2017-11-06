package me.jludden.reeflifesurvey.Intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.jludden.reeflifesurvey.R;

/**
 * Created by Jason on 11/4/2017.
 *
 */

public class IntroViewPagerFragment extends Fragment {

    public static final String TAG = "IntroViewPagerFragment";

    public static IntroViewPagerFragment newInstance(){
        return new IntroViewPagerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //todo update titlebar

        View view = inflater.inflate(R.layout.intro_viewpager_fragment, container, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.intro_view_pager);
        viewPager.setAdapter(new IntroViewPagerAdapter(getChildFragmentManager()));

        return view;
    }

}
