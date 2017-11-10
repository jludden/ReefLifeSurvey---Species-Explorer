package me.jludden.reeflifesurvey.Intro;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import me.jludden.reeflifesurvey.BrowseFish.CardViewFragment;
import me.jludden.reeflifesurvey.MapViewFragment;

/**
 * Created by Jason on 11/4/2017.
 */

public class IntroViewPagerAdapter extends FragmentStatePagerAdapter {

    public IntroViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return IntroPageOneFragment.newInstance();
            case 0:
                return IntroPageTwoFragment.newInstance();
            case 2:
                return CardViewFragment.newInstance(
                        CardViewFragment.CardType.Fish, "");
            case 3:
                //TODO can the third page be the Favorited sites + favorited species page?
                //once it gets to map, impossible to page back
                return MapViewFragment.newInstance();
            default:
                return MapViewFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}
