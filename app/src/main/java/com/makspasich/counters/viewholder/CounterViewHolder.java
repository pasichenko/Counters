package com.makspasich.counters.viewholder;

import androidx.recyclerview.widget.RecyclerView;

import com.makspasich.counters.R;
import com.makspasich.counters.databinding.ItemCounterBinding;
import com.makspasich.counters.models.Counter;

public class CounterViewHolder extends RecyclerView.ViewHolder {
    private ItemCounterBinding binding;

    public CounterViewHolder(ItemCounterBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bindToCounter(Counter counter) {
        binding.counterTitle.setText(counter.name_counter);
        binding.counterAuthor.setText(counter.counter_creator);
        switch (counter.type_counter) {
            case 0:
                binding.counterTypeImage.setImageResource(R.drawable.ic_gas);
                break;
            case 1:
                binding.counterTypeImage.setImageResource(R.drawable.ic_water);
                break;
            case 2:
                binding.counterTypeImage.setImageResource(R.drawable.ic_electricity);
                break;
        }
    }
}
