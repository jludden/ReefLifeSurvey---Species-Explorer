package me.jludden.reeflifesurvey

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import me.jludden.reeflifesurvey.fishcards.CardViewFragment
import me.jludden.reeflifesurvey.mapsites.MapViewFragment


/**
 * Created by Jason on 11/10/2017.
 *
 * Picture this
 * big ole whale comes into screen
 *
 * post-handler-runnable will delay a transition:
 * animate two buttons coming into the screen
 *  browse species
 *  select reefs
 *
 * swipe up on the screen at this point could jump straight into browse species, maybe, idk
 *
 */
class HomeFragment : Fragment() {
    companion object {
        const val TAG  = "me.jludden.ReefLifeSurvey.HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.home_fragment, container, false)
        /*with(root){

        }*/
//
    Glide
            .with(this)
            .load(
                    this.resources.getIdentifier("whale_portrait", "drawable", activity.packageName)
                    //"https://images.reeflifesurvey.com/0/species_17_576b40231c6c2.w1300.h866.JPG"
            )
            //.into(home_background)
            .into(root.findViewById(R.id.home_background) as ImageView)
//
//
//        Picasso.with(context)
//                .load(
//                        this.resources.getIdentifier("whale_portrait", "drawable", activity.packageName)
//                        //"https://images.reeflifesurvey.com/0/species_17_576b40231c6c2.w1300.h866.JPG"
//                )
                //.into(home_background)
//                .fit()
//                .into(root.findViewById(R.id.home_background) as ImageView)


        with(root) {
            findViewById(R.id.button_launch_browse_species).setOnClickListener({
                (activity as MainActivity).launchNewFragment(CardViewFragment::class.java)
                //todo make sure that there is at least some survey site or rando survey sites loading
            })
            findViewById(R.id.button_launch_select_sites).setOnClickListener({
                (activity as MainActivity).launchNewFragment(MapViewFragment::class.java)
            })
        }


        return root
    }

}