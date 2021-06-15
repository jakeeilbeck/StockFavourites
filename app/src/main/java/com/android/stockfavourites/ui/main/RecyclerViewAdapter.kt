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
import com.android.stockfavourites.data.local.StockAndCandle
import com.android.stockfavourites.databinding.ListItemBinding
import com.robinhood.spark.SparkView
import javax.inject.Inject

class RecyclerViewAdapter @Inject constructor(
    private val context: Context,
    private val sparkChartAdapter: SparkChartAdapter
) : ListAdapter<StockAndCandle, RecyclerViewAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val listItemBinding = ListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(listItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stock: StockAndCandle = getItem(position)
        holder.quote.text = "%.2f".format(stock.stockTable.price)
        holder.changePercent.text = stock.stockTable.changePercent
        holder.symbol.text = stock.stockTable.symbol
        holder.companyName.text = stock.stockTable.companyName
        holder.priceChange.text = stock.stockTable.change

        val lineData: FloatArray? =
            stock.candleTable?.candleClose?.toTypedArray()?.map { it!!.toFloat() }?.toFloatArray()
        if (lineData != null) {
            sparkChartAdapter.setData(lineData)
        }

        holder.sparkView.adapter = sparkChartAdapter

        //Change text color based on current price compared to previous close
        if (stock.stockTable.price!! < stock.stockTable.previousClose!!) {
            holder.quote.setTextColor(ContextCompat.getColor(context, R.color.negative))
            holder.changePercent.setTextColor(ContextCompat.getColor(context, R.color.negative))
            holder.priceChange.setTextColor(ContextCompat.getColor(context, R.color.negative))
            holder.sparkView.lineColor = ContextCompat.getColor(context, R.color.negative)
        } else {
            holder.quote.setTextColor(ContextCompat.getColor(context, R.color.positive))
            holder.changePercent.setTextColor(ContextCompat.getColor(context, R.color.positive))
            holder.priceChange.setTextColor(ContextCompat.getColor(context, R.color.positive))
            holder.sparkView.lineColor = ContextCompat.getColor(context, R.color.positive)
        }
    }

    public override fun getItem(position: Int): StockAndCandle {
        return super.getItem(position)
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<StockAndCandle>() {
            override fun areItemsTheSame(
                oldItem: StockAndCandle,
                newItem: StockAndCandle
            ): Boolean = oldItem.stockTable.symbol == newItem.stockTable.symbol

            override fun areContentsTheSame(
                oldItem: StockAndCandle,
                newItem: StockAndCandle
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
        val sparkView: SparkView = binding.sparkview
    }
}