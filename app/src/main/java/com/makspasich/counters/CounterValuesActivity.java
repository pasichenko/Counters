package com.makspasich.counters;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makspasich.counters.adapters.ValueAdapter;
import com.makspasich.counters.databinding.ActivityCounterDetailBinding;
import com.makspasich.counters.fragment.NewValueDialogFragment;

import static com.makspasich.counters.utils.Constants.EXTRA_COLOR_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_COUNTER_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_NAME_KEY;

public class CounterValuesActivity extends BaseActivity {
    private final String TAG = "MyLogCounDetailActiv";

    private ActivityCounterDetailBinding binding;
    private DatabaseReference mValuesReference;
    private String mCounterKey;
    private ValueAdapter mAdapter;

    private boolean checkRecyclerScrollListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCounterDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Get counter key from intent
        getCounterDataFromIntent();

        // Initialize Database
        mValuesReference = FirebaseDatabase.getInstance().getReference()
                .child("counter-value").child(mCounterKey);

        // Initialize Views
        binding.fabAddValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewValueDialogFragment dialogFragment = new NewValueDialogFragment(mCounterKey);
//                dialogFragment.setCancelable(false);
                dialogFragment.show(getSupportFragmentManager(), "newValue");
            }
        });

        binding.recyclerCounterValues.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getCounterDataFromIntent() {
        mCounterKey = getIntent().getStringExtra(EXTRA_COUNTER_KEY);
        if (mCounterKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_COUNTER_KEY");
        }
        String mNameKey = getIntent().getStringExtra(EXTRA_NAME_KEY);
        if (mNameKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_COUNTER_KEY");
        } else {
            getSupportActionBar().setTitle(mNameKey);
        }
        int mColorKey = getIntent().getIntExtra(EXTRA_COLOR_KEY, -1);
        if (mCounterKey != null) {
            switch (mColorKey) {
                case 0:
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(251, 111, 67)));
                    break;
                case 1:
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(99, 174, 228)));
                    break;
                case 2:
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(233, 198, 19)));
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for values
        mAdapter = new ValueAdapter(mValuesReference, mCounterKey);
        binding.recyclerCounterValues.setAdapter(mAdapter);
//        mValuesRecycler.addOnScrollListener(new CustomScrollListener(this));
        RecyclerView.OnScrollListener mListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                final int visibleItemCount = layoutManager.getChildCount();
                final int totalItemCount = layoutManager.getItemCount();
                final int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (!checkRecyclerScrollListener) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE || (visibleItemCount + pastVisibleItems > totalItemCount)) {
                        Log.i(TAG, "End of list");
                        binding.recyclerCounterValues.clearOnScrollListeners();
                        binding.recyclerCounterValues.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                if (dy > 0) {
                                    binding.fabAddValue.hide();
                                } else {
                                    binding.fabAddValue.show();
                                }
                                super.onScrolled(recyclerView, dx, dy);
                            }
                        });
                        Log.d(TAG, "onScrollStateChanged: listener ADDED");
                    }
                }
            }
        };
        binding.recyclerCounterValues.addOnScrollListener(mListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Clean up values listener
        mAdapter.cleanupListener();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
