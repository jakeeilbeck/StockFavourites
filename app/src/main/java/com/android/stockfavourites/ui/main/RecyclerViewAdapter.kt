package com.android.stockfavourites.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.stockfavourites.R
import com.android.stockfavourites.data.local.StockTable
import com.android.stockfavourites.databinding.ListItemBinding
import javax.inject.Inject

class RecyclerViewAdapter @Inject constructor(private val context: Context) :
    ListAdapter<StockTable, RecyclerViewAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val listItemBinding = ListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stock: StockTable = getItem(position)
        holder.quote.text = "%.2f".format(stock.price)
        holder.changePercent.text = stock.changePercent
        holder.symbol.text = stock.symbol
        holder.companyName.text = stock.companyName
        holder.priceChange.text = stock.change

        //Change text color based on current price compared to previous close
        if (stock.price!! < stock.previousClose!!) {
            holder.quote.setTextColor(ContextCompat.getColor(context, R.color.negative))
            holder.changePercent.setTextColor(ContextCompat.getColor(context, R.color.negative))
            holder.priceChange.setTextColor(ContextCompat.getColor(context, R.color.negative))
        } else {
            holder.quote.setTextColor(ContextCompat.getColor(context, R.color.positive))
            holder.changePercent.setTextColor(ContextCompat.getColor(context, R.color.positive))
            holder.priceChange.setTextColor(ContextCompat.getColor(context, R.color.positive))
        }
    }

    public override fun getItem(position: Int): StockTable {
        return super.getItem(position)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<StockTable>() {
            override fun areItemsTheSame(
                oldItem: StockTable,
                newItem: StockTable
            ): Boolean = oldItem.symbol == newItem.symbol

            override fun areContentsTheSame(
                oldItem: StockTable,
                newItem: StockTable
            ): Boolean = oldItem == newItem
        }
    }

    inner class ViewHolder(binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val quote: TextView = binding.quote
        val changePercent: TextView = binding.percentChange
        val priceChange: TextView = binding.priceChange
        val symbol: TextView = binding.ticker
        val companyName: TextView = binding.companyName
    }
}