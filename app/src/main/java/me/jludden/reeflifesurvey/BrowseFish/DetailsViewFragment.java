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

import java.util.ArrayList;
import java.util.List;

import me.jludden.reeflifesurvey.model.InfoCard;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.ReefLifeDataFragment;

/**
 * Created by Jason on 10/25/2017.
 */

public class DetailsViewFragment extends Fragment {

    private final static String ARG_CARD = "CardDetailsParam";
    public static final String TAG = "DetailsViewFragment";


    ViewPager mViewPager;
    DetailsViewAdapter mViewAdapter;

    ReefLifeDataFragment.ReefLifeDataRetrievalCallback mDataRetrievalCallback; //todo unused
    List<InfoCard.CardDetails> mData = new ArrayList<>(); //todo

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param cardDetails Parameter 1.
     * @param param2 Parameter 2. todo
     * @return A new instance of fragment DetailsViewFragment.
     */
    public static DetailsViewFragment newInstance(InfoCard.CardDetails cardDetails, String param2) {
        DetailsViewFragment fragment = new DetailsViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CARD, cardDetails);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //todo considering another newInstance() where we just pass in a fish species id and it generates the CardDetails object?

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        String errMsg = "";

        if(context instanceof ReefLifeDataFragment.ReefLifeDataRetrievalCallback){
            mDataRetrievalCallback = (ReefLifeDataFragment.ReefLifeDataRetrievalCallback) context;
        } else {
            errMsg += context.toString() + "must implement" +
                    ReefLifeDataFragment.ReefLifeDataRetrievalCallback.class.getName();
        }

        if(errMsg.length() > 0){
            throw new ClassCastException(errMsg);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() == null) {
            Log.d("jludden.reeflifesurvey", "DetailsViewFragment onCreate - no params passed in");
        }

        if (getArguments() != null) {
            Log.d("jludden.reeflifesurvey"  ,"DetailsViewFragment onCreate getArgs size: "+getArguments().size());

//            mCardType = CardViewFragment.CardType.values()[getArguments().getInt(ARG_PARAM1, 0)]; //wow why is getting an enum from an int so difficult
//            mParam2 = getArguments().getString(ARG_PARAM2);
            InfoCard.CardDetails cardDetails = getArguments().getParcelable(ARG_CARD);
            if(cardDetails != null) {
                mData.add(cardDetails);
                Log.d("jludden.reeflifesurvey"  ,"DetailsViewFragment onCreate added card passed in from main: "+mData.size());
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
}
