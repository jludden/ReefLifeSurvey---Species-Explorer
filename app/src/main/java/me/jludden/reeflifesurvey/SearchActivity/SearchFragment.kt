package me.jludden.reeflifesurvey.SearchActivity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import me.jludden.reeflifesurvey.Data.SearchResult
import kotlinx.android.synthetic.main.activity_search_results_item.view.*
import me.jludden.reeflifesurvey.FishSpeciesCards.DetailsViewFragment
import me.jludden.reeflifesurvey.R


/**
 * Created by Jason on 11/12/2017.
 *
 * SearchFragment Class
 *  implements the View portion of the SearchContract MVP architecture
 */
class SearchFragment : Fragment(), SearchContract.View {
    companion object {
        const val TAG: String = "SearchResultsFragment"
        fun newInstance() = SearchFragment()
    }

    override lateinit var presenter: SearchContract.Presenter
    override var isActive: Boolean = false
        get() = isAdded

    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: SearchResultsAdapter
    internal var itemListener: SearchResultItemListener = object : SearchResultItemListener {
        override fun onItemClicked(item: SearchResult) {
            presenter.onItemClicked(item)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.activity_search_results_fragment, container, false)
        recyclerView = root.findViewById(R.id.search_results_cards) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewAdapter = SearchResultsAdapter(ArrayList(), itemListener)
        recyclerView.adapter = viewAdapter

        return root
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    //TODO animateView when loading/clear data (see cardviewfragment.animateView)
    override fun setProgressIndicator(active: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //
    override fun addSearchResult(result: SearchResult) {
        //val curText = search_results_text.text
        val newResult = result.name + "_" + result.description
        //val newText = "$curText \n $newResult"
        //search_results_text.text = newText
        Log.d(TAG, "searchfragment addSearchResult "+newResult)

        viewAdapter.updateItems(element = result)
    }

    //todo
    override fun clearSearchResults() {
        viewAdapter.updateItems(list = ArrayList())
    }

    override fun launchResultDetails(searchResult: SearchResult) {
        Log.d(TAG, "searchfragment launchResultDetails CALLED")

        (activity as SearchActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_results_container, DetailsViewFragment.newInstance(searchResult), DetailsViewFragment.TAG)
                .addToBackStack(null)
                .commit()
    }

    /**
     * SearchResultsAdapter class
     *
     * Some cool extension functions courtesy of
     * https://antonioleiva.com/extension-functions-kotlin/
     */
    class SearchResultsAdapter(
            initialList: MutableList<SearchResult>, private val itemListener: SearchResultItemListener)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var resultsList: MutableList<SearchResult> = initialList
        fun updateItems(element: SearchResult? = null, list: MutableList<SearchResult>? = null){
            if(list!=null) resultsList = list
            if(element!=null) resultsList.add(element)
            notifyDataSetChanged()
        }

        fun ViewGroup.inflate(layoutRes: Int): View {
            return LayoutInflater.from(context).inflate(layoutRes, this, false)
        }

        override fun getItemCount() = resultsList.size

        /**
         * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
         * an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                = SearchResultsViewHolder(parent.inflate(R.layout.activity_search_results_item))


        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the [ViewHolder.itemView] to reflect the item at the given
         * position.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the
         * item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int)
                = (holder as SearchResultsViewHolder).bind(resultsList.get(position), itemListener)
    }

    /**
     * ViewHolder class
     */
    class SearchResultsViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(item: SearchResult, listener: SearchResultItemListener)
                = with(itemView) {
            results_card_image.loadURL(item.imageURL)
            results_card_name.text = item.name
            results_card_description.text = item.description
            setOnClickListener({ listener.onItemClicked(item) })
        }

        fun ImageView.loadURL(url: String) {
            Picasso.with(context).load(url).into(this)
            //Glide.with(context).load(url).into(this)
        }
    }

    interface SearchResultItemListener {
        fun onItemClicked(item: SearchResult)
    }
}