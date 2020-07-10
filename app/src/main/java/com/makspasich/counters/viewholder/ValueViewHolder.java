package com.makspasich.counters.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.makspasich.counters.R;
import com.makspasich.counters.databinding.ItemValueBinding;
import com.makspasich.counters.models.Value;

import java.text.DecimalFormat;

public class ValueViewHolder extends RecyclerView.ViewHolder {

    private ItemValueBinding binding;

    public ValueViewHolder(ItemValueBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bindToValue(Value value) {
        binding.valueDate.setText(value.date);
        binding.valueAuthor.setText(value.author);
        binding.valueData.setText(itemView.getContext().getResources().getString(R.string.value, (String) new DecimalFormat("000000.000").format(value.value)));
    }
}
