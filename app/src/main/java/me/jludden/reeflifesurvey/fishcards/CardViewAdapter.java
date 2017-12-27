package me.jludden.reeflifesurvey.fishcards;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import me.jludden.reeflifesurvey.data.model.InfoCard;
import me.jludden.reeflifesurvey.BuildConfig;
import me.jludden.reeflifesurvey.R;
import me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils;
import me.jludden.reeflifesurvey.data.utils.StoredImageLoader;

import java.util.ArrayList;
import java.util.List;
import static me.jludden.reeflifesurvey.fishcards.CardViewFragment.TAG;
import static me.jludden.reeflifesurvey.data.utils.SharedPreferencesUtils.setUpFavoritesButton;

/**
 * {@link RecyclerView.Adapter} that can display a {@link InfoCard.CardDetails} and makes a call to the
 * specified {TODO add OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private CardViewHeader mHeader;
    private List<InfoCard.CardDetails> mCardList = new ArrayList<>(); //visible cards, the Adapters backing data source
    private List<InfoCard.CardDetails> mAllCardList = new ArrayList<>(); //All cards, including any hidden / filtered ones
    private List<String> mHiddenList = new ArrayList<>(); //list of hidden cards (just their string IDs) TODO delete in favor of ALL + SHOWN model
    //private RequestManager glide;
    private Picasso picasso;
    //private RequestOptions mGlideRequestOptions;

    private RecyclerView mRecyclerView;
    private CardViewFragment mCardViewFragment;
    private StoredImageLoader mStoredImageLoader;
    private final CardViewFragment.OnCardViewFragmentInteractionListener mListener;

    private static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    public CardViewAdapter(CardViewFragment parent, RecyclerView listView, CardViewFragment.OnCardViewFragmentInteractionListener listener) {
       // this.glide = Glide.with(parent); //cache the Glide RequestManager object
        this.picasso = Picasso.with(parent.getContext().getApplicationContext());
        this.mHeader = new CardViewHeader("hi this is a header"); //todo
        //mListener = listener;
        this.mRecyclerView = listView;
        this.mCardViewFragment = parent;
        this.mListener = listener;
        this.mStoredImageLoader = new StoredImageLoader(parent.getContext().getApplicationContext());

        //setHasStableIds(true);//todo not sure what this does testing it out
    }

    /**
     *Called when RecyclerView needs a new ViewHolder of the given view type
     * todo - can add additional view types, such as map cards, etc.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_header, parent, false);
            return new HeaderViewHolder(view);

        } else if(viewType == TYPE_ITEM){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_item, parent, false);
            return new FishCardViewHolder(view);
        }
        Log.e("jludden.reeflifesurvey" , "CardViewAdapter invalid viewtype");
        return null;
    }

    /**
     * the item view type. basically if it is the first item, it is a header.
     * todo this could be expanded. mCardList[position] == mapcard, fishcard, etc.
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position){
        return position==0 ? TYPE_HEADER : TYPE_ITEM;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link FishCardViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder The FishCardViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    //    Log.d("jludden.reeflifesurvey"  , "CardViewAdapter onBindVH: "+ mCardList.size()+ "size-> pos"+ position);

        if(holder instanceof HeaderViewHolder) {
            final HeaderViewHolder vhHeader = (HeaderViewHolder) holder;
            String headerText = (getItemCount() <= 1) ? "No items loaded" : "";
            vhHeader.mContentView.setText(headerText);
        }
        //todo clean this mess up
        else if(holder instanceof FishCardViewHolder) {
            final FishCardViewHolder vhItem = (FishCardViewHolder) holder;

            final int realPosition = getDataSourcePosition(position);

            if (vhItem.mContentView == null || (mCardList.size() <= realPosition)) {
                Log.d(TAG, "CardViewAdapter onBindView vhItem somethings null. cardlistsize:  " + mCardList.size());
                return;
            }

            final InfoCard.CardDetails cardDetails = mCardList.get(realPosition);
             Log.d("jludden.reeflifesurvey"  , "CardViewAdapter onBind FishCardVH. CardListSize: "+ mCardList.size()+ " HiddenListSize: "+mHiddenList.size()+" adapter pos: "+ position + " datasource pos: "+position);

            boolean loadedSuccessfully = cardDetails.tryLoadPrimaryImageOffline(mStoredImageLoader, vhItem.mImageView);

            //even if offline, picasso may have it cached
            if(!loadedSuccessfully) {
                if(cardDetails.getPrimaryImageURL().equals("")){
                    picasso.load(R.drawable.ic_menu_camera).into(vhItem.mImageView);
                } else {
                    picasso
                            .load(cardDetails.getPrimaryImageURL())
                            .placeholder(R.drawable.ic_menu_camera)
                            .into(vhItem.mImageView);
                }
            }

            vhItem.mOverlayView.setText(cardDetails.cardName); //picture overlay text

            //set up details below image
            vhItem.mContentView.setText(cardDetails.toString());
            //if(getPref(cardDetails.id,PREF_FAVORITED)) cardDetails.favorited = true;

            setUpFavoritesButton(cardDetails, vhItem.mFavoriteBtn, mCardViewFragment.getActivity());

            vhItem.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Card onClick - Launching new fragment");

                    mListener.onFishDetailsRequested(mCardList.get(realPosition),vhItem.mImageView);
                }
            });

           /* holder.setOn
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        }*/
        }
    }

    /**
     *
     * @param AdapterPos
     */
    private void hideCard(int AdapterPos) {
        //Log.d(TAG,"hide card called for pos: "+AdapterPos);
        int realPosition = getDataSourcePosition(AdapterPos);
        mHiddenList.add(mCardList.get(realPosition).getId());

        notifyItemRemoved(AdapterPos); //same as notifyItemRangeRemoved(pos,1)
        notifyItemRangeChanged(AdapterPos, getItemCount() - AdapterPos);

        //  mRecyclerView.setItemAnimator(null); //new AlphaInAnimator()
      //  mRecyclerView.removeViewAt(pos);
      //  notifyDataSetChanged();

    }

    //hides cards from the adapter
    public void applyFilter() {
        Log.d(TAG,"apply filter Adapter. getItemCount: "+getItemCount()+" cardlistsize: "+mCardList.size());

        int realPosition;
        InfoCard.CardDetails cardDetails;
        //loop through each displayed item in the adapter
        //NOTE THIS WILL ONLY WORK TO HIDE, not to
        int i = 1;
        while(i < getItemCount()){
            realPosition = getDataSourcePosition(i);
            cardDetails = mCardList.get(realPosition);
            if(getItemViewType(i) == CardViewAdapter.TYPE_ITEM && !cardDetails.getFavorited(mCardViewFragment.getActivity())) {
                hideCard(i);
            }
            else {
                i++;
            }
        }
    }

//
//    private void unhide() {
//
//    }

    /**
     * Returns the position to use for this data element that corresponds to the data source (mCardList)
     *  the adapter position will be off due to other viewholder objects not in the card list, such as the header
     *  as well as cards hidden from the card list
     * Thanks to CCrama - SlideForReddit - CommentAdapter.java
     * @param adapterPosition
     * @return
     */
    private int getDataSourcePosition(int adapterPosition) {
        if(adapterPosition <= 0 ) Log.e("jludden.reeflifesurvey" , "getDataSourcePosition - adapter position zero or null");
        int pos = adapterPosition != 0 ? adapterPosition-1 : adapterPosition; //for the header, decrement all by 1
        int hiddenCards = getHiddenCountUpTo(pos);
        int diff = 0;
        for(int i = 0; i < hiddenCards; i++){
            diff++;
            if (mCardList.size() > (pos + diff)
                    && (mHiddenList.contains(mCardList.get(pos + diff).getId())))
                i--; //CAREFUL decrementing loop
        }

        if (BuildConfig.DEBUG && ((pos + diff)>=mCardList.size())) {
            throw new AssertionError("position in datasource list out of bounds. pos: "+(pos+diff)+" size: "+mCardList.size());
        }
        return (pos + diff);
    }

    private int getHiddenCountUpTo(int pos){
        int count = 0;
        for (int i = 0; (i <= pos && i < mCardList.size()); i++) {
            if (mHiddenList.contains(mCardList.get(i).getId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Called when a view created by this adapter has been recycled.
     *
     * <p>A view is recycled when a {@link RecyclerView.LayoutManager} decides that it no longer
     * needs to be attached to its parent {@link RecyclerView}. This can be because it has
     * fallen out of visibility or a set of cached views represented by views still
     * attached to the parent RecyclerView. If an item view has large or expensive data
     * bound to it such as large bitmaps, this may be a good place to release those
     * resources.</p>
     * <p>
     * RecyclerView calls this method right before clearing FishCardViewHolder's internal data and
     * sending it to RecycledViewPool. This way, if FishCardViewHolder was holding valid information
     * before being recycled, you can call {@link RecyclerView.ViewHolder#getAdapterPosition()} to get
     * its adapter position.
     *
     * @param holder The FishCardViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        //Log.d("jludden.reeflifesurvey"  , "CardViewAdapter onViewRecycled: " + holder.toString() );
//        if(holder instanceof )
        //((FishCardViewHolder) holder).mImageView);
        if(holder instanceof FishCardViewHolder) {
            final FishCardViewHolder vhItem = (FishCardViewHolder) holder;
            //glide.clear(vhItem.mImageView);
            vhItem.mFavoriteBtn.setButtonDrawable(SharedPreferencesUtils.FAVORITES_OUTLINE);
        }
    }

    @Override
    public int getItemCount() { return mCardList.size() - mHiddenList.size() + 1; } //adding 1 for the header
    //public int getItemCount() { return mCardList.size() +1; } //adding 1 for the header

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    /**
     * Returns a filter that can be used to constrain data with a filtering
     * pattern.
     *
     * Called by CardViewAdapter.getFilter().filter(constraint)
     *
     *  TODO choose:
     *  Fast filtering - return a basic list of cards in performFiltering(). Set this new list using updateItems(). pros - filters on background thread. cons - no animation
     *  Slow filtering - loop through adapter positions in publishResults(). call hideCard() if it should be excluded. pros - animate the removal of cards. cons - more work on UI thread

     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        Log.d(TAG,"CardViewAdapter getFilter()");

        return new Filter() {
            /**
             * Invoked in a worker thread to filter the data according to the
             * constraint. When the constraint is null, the original
             * data must be restored.
             * TODO i aint doing nothing here. could implement faster filtering...
             * @param constraint the constraint used to filter the data
             * @return the results of the filtering operation THE LIST<STRING> of CARDS TO REMOVE
             */
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(TAG,"CardViewAdapter getFilter() - background thread performFiltering(). constraint: "+constraint+" filterFAV: "+ CardViewFragment.CardViewSettings.FILTER_FAVORITES);

                if(constraint == "" || constraint.length() <= 0){ //restore the original list
                    //results.values = mCardList;
                    //results.count = mCardList.size();
                    return null; //we will do additional filtering on UI thread and animate out the cards
                }
                else {
                    FilterResults results = new FilterResults();
                    List<String> removeList = new ArrayList<>();

                    //todo refactor this whole method
                    for(InfoCard.CardDetails card : mCardList){
                        if(!card.cardName.toLowerCase().contains(constraint)&&
                                !card.commonNames.toLowerCase().contains(constraint)) removeList.add(card.getId());
                    }

                    results.values = removeList;
                    results.count = removeList.size();
                    return results;
                }
               // return results;
            }

            /**
             * Invoked in the UI thread to publish the filtering results in the
             * user interface.
             * NOTE here that I am applying filters in UI thread. But i am using the FilterResults method to recreate the full list...
             * @param constraint the constraint used to filter the data
             * @param results    the results of the filtering operation
             */
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(TAG,"CardViewAdapter getFilter() - publishResults() constraint: "+constraint+" filterFAV: "+ CardViewFragment.CardViewSettings.FILTER_FAVORITES);
//                Log.d(TAG,"CardViewAdapter getFilter() - publishResults(): "+results.count+" constraint: "+constraint);

                if((!CardViewFragment.CardViewSettings.FILTER_FAVORITES)&&(constraint == "" || constraint.length() <= 0)) { //restore the original list
                    mHiddenList = new ArrayList<>();
                    notifyDataSetChanged();
//                    if (results.count > 0) {
//                        updateItems((List<InfoCard.CardDetails>) results.values);
//                    }
                }
                else if(!((constraint == "" || constraint.length() <= 0))){
                    mHiddenList = new ArrayList<>();
                    mHiddenList.addAll((List<String>) results.values);
                    notifyDataSetChanged();
                }
               // else{ //filter out cards, one at a time. this could use the same model as the restore, but this way we have animations
                    if(CardViewFragment.CardViewSettings.FILTER_FAVORITES)  applyFilter();
                //}


            }
        };
    }

    /**
     * FishCardViewHolder holds all the views needed in the fish based card
     */
    // TODO define viewholder for these cards RENAME VIEWHOLDER TO something like CardViewHolder
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class FishCardViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView; //card image
        public final TextView mContentView; //text details beneath card
        public final TextView mOverlayView; //text overlaid on top of the card (bottom right)
        public final CheckBox mFavoriteBtn; //star to favorite the fish

        public  TextView mContentLabel;
        //public final TextView mIdView;
        // public CardDetails mItem;

        public FishCardViewHolder(View view) {
            super(view);
            mView = view;
            // mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.info_text);
            mImageView = (ImageView) view.findViewById(R.id.card_image);
            mOverlayView = (TextView) view.findViewById(R.id.overlay_text);
            mFavoriteBtn = (CheckBox) view.findViewById(R.id.favorite_btn);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    /**
     * HeaderViewHolder holds all the views needed in the header of the fishy recycler view
     */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;

        public HeaderViewHolder(View view) {
            super(view);
            mView = view;
            // mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.header_text);
        }
    }

    /**
     * return the current data list:
     */
    public  List<InfoCard.CardDetails> getCardList() {
        return mCardList;
    }

    /**
     * Sets the data in the adapter
     * @param data
     */
    public void updateItems(List<InfoCard.CardDetails> data){
        Log.d(TAG, "cardview adapter update items");

        mCardList = data;
        this.mHiddenList = new ArrayList<>();
        this.mAllCardList = new ArrayList<>();
        this.mAllCardList.addAll(data);
        notifyDataSetChanged();
    }

    //todo a class to define the header object. possibly move to an outer class if it grows
    //todo not really using this header for anything besides some white space. consider deleting
    private class CardViewHeader {
        private String headerText;
        CardViewHeader(String headerText){
            this.headerText = headerText;
        }
    }
}
