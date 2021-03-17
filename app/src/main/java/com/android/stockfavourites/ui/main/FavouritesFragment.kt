package com.android.stockfavourites.ui.main

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.stockfavourites.Injection
import com.android.stockfavourites.R
import com.android.stockfavourites.data.StockDatabase
import com.android.stockfavourites.databinding.FavouritesFragmentBinding
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment(R.layout.favourites_fragment) {

    companion object {
        fun newInstance() = FavouritesFragment()
    }

    private var _binding: FavouritesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavouritesViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerViewAdapter
    private var newSymbol: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireNotNull(this.activity).application
        val stockDataSource = StockDatabase.getInstance(application).stockDAO
        val viewModelFactory = Injection.provideFavouritesViewmodelFactory(stockDataSource)

        viewModel = ViewModelProvider(this, viewModelFactory).get(FavouritesViewModel::class.java)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavouritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        adapter = RecyclerViewAdapter(requireContext())
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val fabRefresh = binding.fabRefresh

        //Observe Room for live updates
        lifecycleScope.launch {
            viewModel.getAllFavourites().observe(viewLifecycleOwner, {
                adapter.submitList(it)
            })
        }
        recyclerView.adapter = adapter

        //Logic for swipe to delete
        val itemTouchHelperCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewModel.deleteStock(adapter.getItem(viewHolder.layoutPosition))
                }
            }

        //For swipe to delete
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        fabRefresh.setOnClickListener {
            viewModel.updateAll()
        }
    }

    //App bar search autocomplete
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.menu_search, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Search symbol"

        //Search autocomplete logic
        val cursorAdapter = SimpleCursorAdapter(
            this.requireContext(),
            R.layout.autocomplete_item,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
            intArrayOf(R.id.symbol, R.id.company_name),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        searchView.suggestionsAdapter = cursorAdapter

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            //Lookup symbol as the user types
            override fun onQueryTextChange(query: String): Boolean {

                lifecycleScope.launch {

                    if (query.isNotEmpty()) {
                        val result = viewModel.searchSymbol(query)

                        val cursor = MatrixCursor(
                            arrayOf(
                                BaseColumns._ID,
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                                SearchManager.SUGGEST_COLUMN_TEXT_2
                            )
                        )

                        query.let {
                            result.result?.forEachIndexed { index, _ ->
                                cursor.addRow(
                                    arrayOf(
                                        index,
                                        result.result[index]?.symbol,
                                        result.result[index]?.description
                                    )
                                )

                                newSymbol = result.result[index]?.symbol.toString()
                            }
                        }
                        cursorAdapter.changeCursor(cursor)
                    }
                }
                return true
            }
        })

        searchView.setOnSuggestionListener(object :
            androidx.appcompat.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {

                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val symbol = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                val companyName = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2))

                viewModel.searchStock(symbol, companyName)

                searchView.onActionViewCollapsed()

                return true
            }
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}