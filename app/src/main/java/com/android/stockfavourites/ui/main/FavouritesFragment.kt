package com.android.stockfavourites.ui.main

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.stockfavourites.R
import com.android.stockfavourites.databinding.FavouritesFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.favourites_fragment) {

    companion object {
        fun newInstance() = FavouritesFragment()
    }

    private var _binding: FavouritesFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavouritesViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    @Inject lateinit var adapter: RecyclerViewAdapter
    private var newSymbol: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                private val background = ColorDrawable()
                private val backgroundColor = ContextCompat.getColor(requireContext(), R.color.negative)
                private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

                //https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
                //Red background when swiping to delete items
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {

                    val itemView = viewHolder.itemView
                    val isCanceled = dX == 0f && !isCurrentlyActive

                    if (isCanceled) {
                        clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        return
                    }

                    // Draw the red delete background
                    background.color = backgroundColor
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

                //Remove the red background if item is released
                private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
                    c?.drawRect(left, top, right, bottom, clearPaint)
                }

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
            viewModel.updateAllFavourites()
            binding.fabRefresh.visibility = View.GONE
            binding.updateAllProgressBar.visibility = View.VISIBLE
        }

        viewModel.refreshStatus.observe(viewLifecycleOwner, {
            if(it == true) {
                Toast.makeText(requireContext(),"Stocks updated", Toast.LENGTH_SHORT).show()
                binding.fabRefresh.visibility = View.VISIBLE
                binding.updateAllProgressBar.visibility = View.GONE
            }
        })

        viewModel.errorType.observe(viewLifecycleOwner, {
            if (it != "") Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            binding.fabRefresh.visibility = View.VISIBLE
            binding.updateAllProgressBar.visibility = View.GONE
        })

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
                            result?.result?.forEachIndexed { index, _ ->
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

                viewModel.getStock(symbol, companyName)

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