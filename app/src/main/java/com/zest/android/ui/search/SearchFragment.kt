package com.zest.android.ui.search

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.zest.android.R
import com.zest.android.data.model.Recipe
import com.zest.android.databinding.FragmentSearchBinding
import com.zest.android.ui.main.MainActivity
import com.zest.android.ui.recipes.RecipeViewModel
import javax.inject.Inject
import javax.inject.Provider


/**
 * @Author ZARA.
 */
class SearchFragment : Fragment(), OnSearchCallback {


    private var mQuery: String? = null
    private var mAdapter = SearchAdapter(this)
    private var mMenuSearchItem: MenuItem? = null
    lateinit var binding: FragmentSearchBinding
    lateinit var viewModel: RecipeViewModel

    @Inject
    lateinit var viewModelProvider: Provider<RecipeViewModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).mainComponent.inject(this)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModelProvider.get() as T
        }).get(RecipeViewModel::class.java)

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.searchRecyclerView.adapter = mAdapter

        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onChanged() {
                super.onChanged()
                checkEmptyView()
            }
        })

        arguments?.let {
            mQuery = SearchFragmentArgs.fromBundle(it).text ?: null
            mQuery?.let { nonNullSearchQuery ->
                viewModel.search(nonNullSearchQuery)
            }
        }


        viewModel.recipesData.observe(viewLifecycleOwner, Observer {
            mAdapter.recipes = it
        })

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.search, menu)
        mMenuSearchItem = menu.findItem(R.id.search)
        val searchView = SearchView((context as MainActivity).supportActionBar?.themedContext)
        searchView.queryHint = getString(R.string.action_search)
        mMenuSearchItem?.actionView = searchView

        // These lines are deprecated in API 26 use instead
        mMenuSearchItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)

        mQuery?.let {
            searchView.setQuery(it, false)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mQuery = query
                viewModel.search(mQuery ?: "")
                mMenuSearchItem?.expandActionView()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        searchView.setOnClickListener {
            //nothing yet
        }

        searchView.isIconified = false
    }



    override fun gotoDetailPage(recipe: Recipe) {

    }

    private fun checkEmptyView() {
        binding.searchEmptyContainer.visibility = if (mAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    companion object {

        private val TAG = SearchFragment::class.java.name
    }

}
