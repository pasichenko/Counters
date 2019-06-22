package com.makspasich.counters.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.makspasich.counters.R;
import com.makspasich.counters.models.Value;

import java.text.DecimalFormat;

public class ValueViewHolder extends RecyclerView.ViewHolder {

    private TextView dateView;
    private TextView authorView;
    private TextView valueView;

    public ValueViewHolder(View itemView) {
        super(itemView);

        dateView = itemView.findViewById(R.id.valueDate);
        authorView = itemView.findViewById(R.id.valueAuthor);
        valueView = itemView.findViewById(R.id.valueData);
    }

    public void bindToValue(Value value, Context context) {
        dateView.setText(value.date);
        authorView.setText(value.author);

        valueView.setText(context.getResources().getString(R.string.value, (String)new DecimalFormat("000000.000").format(value.value)));
    }
}
