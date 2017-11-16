package me.jludden.reeflifesurvey.BrowseFish;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.jludden.reeflifesurvey.Data.DataRepository;
import me.jludden.reeflifesurvey.Data.InfoCard;
import me.jludden.reeflifesurvey.Data.SearchResult;
import me.jludden.reeflifesurvey.Data.SearchResultType;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.ReefLifeDataFragment;

/**
 * Created by Jason on 10/25/2017.
 */

public class DetailsViewFragment extends Fragment implements DataRepository.LoadFishCardCallBack {

    private static final String ARG_CARD = "CardDetailsParam";
    private static String ARG_SPECIES_ID = "CardDetailsID";
    public static final String TAG = "DetailsViewFragment";

    ViewPager mViewPager;
    DetailsViewAdapter mViewAdapter;

    List<InfoCard.CardDetails> mData = new ArrayList<>(); //todo

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param cardDetails Parameter 1.
     * @param id Parameter 2. todo
     * @return A new instance of fragment DetailsViewFragment.
     */
    public static DetailsViewFragment newInstance(InfoCard.CardDetails cardDetails, String id) {
        DetailsViewFragment fragment = new DetailsViewFragment();
        Bundle args = new Bundle();
        if(cardDetails != null) args.putParcelable(ARG_CARD, cardDetails);
        //todo this approach may require an addtl enum param SearchResultType
        args.putString(ARG_SPECIES_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailsViewFragment newInstance(SearchResult searchResult) {
        InfoCard.CardDetails card;
        if(searchResult.getType() == SearchResultType.FishSpecies){
            return newInstance(null, searchResult.getId());
        }
        else {
            return newInstance(null, ""); //todo handle survey site
        }
    }

    //todo considering another newInstance() where we just pass in a fish species id and it generates the CardDetails object?

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() == null) {
            Log.d(TAG, "DetailsViewFragment onCreate - no params passed in");
        }

        if (getArguments() != null) {
            Log.d("jludden.reeflifesurvey"  ,"DetailsViewFragment onCreate getArgs size: "+getArguments().size());

//            mCardType = CardViewFragment.CardType.values()[getArguments().getInt(ARG_PARAM1, 0)]; //wow why is getting an enum from an int so difficult
//            mParam2 = getArguments().getString(ARG_PARAM2);
            InfoCard.CardDetails cardDetails = getArguments().getParcelable(ARG_CARD);
            String id = getArguments().getString(ARG_SPECIES_ID);

            if(cardDetails != null) {
                mData.add(cardDetails);
                Log.d("jludden.reeflifesurvey"  ,"DetailsViewFragment onCreate added card passed in from main: "+mData.size());
            }
            else if(id != null){
                Log.d("jludden.reeflifesurvey"  ,"DetailsViewFragment onCreate card ID passed in: "+id);
                DataRepository.Companion.getInstance(getContext()).getFishCard(id, this);
            }
            else {
                onDataNotAvailable();
            }
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //todo update titlebar

        View view = inflater.inflate(R.layout.details_view_fragment, container, false);

        Context context = view.getContext();
        mViewPager = (ViewPager) view.findViewById(R.id.details_view_pager);
        mViewAdapter = new DetailsViewAdapter(this,mData);
        mViewPager.setAdapter(mViewAdapter);

        return view;
    }

    @Override
    public void onFishCardLoaded(@NotNull InfoCard.CardDetails card) {
        mData.add(card);
    }

    @Override
    public void onDataNotAvailable() {
        Log.d(TAG, "onDataNotAvailable: DetailsViewFrag");
        //todo
    }
}
