package com.makspasich.counters.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makspasich.counters.R;
import com.makspasich.counters.models.Counter;

public class CounterViewHolder extends RecyclerView.ViewHolder {

    private TextView nameCounterView;
    private TextView authorView;
    public ImageView typeCounterView;

    public CounterViewHolder(View itemView) {
        super(itemView);

        nameCounterView = itemView.findViewById(R.id.counterTitle);
        authorView = itemView.findViewById(R.id.counterAuthor);
        typeCounterView = itemView.findViewById(R.id.counterTypeImage);
    }

    public void bindToCounter(Counter counter, Context context) {
        nameCounterView.setText(counter.name_counter);
        authorView.setText(counter.counter_creator);
    }
}
