package com.makspasich.counters.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makspasich.counters.adapters.CounterAdapter;
import com.makspasich.counters.databinding.FragmentCountersBinding;

public class MyCountersFragment extends Fragment {

    private static final String TAG = "MyLogMyCountersFragment";
    private FragmentCountersBinding binding;
    private DatabaseReference mRootReference;
    private DatabaseReference mCountersReference;
    private CounterAdapter mAdapter;

    public MyCountersFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentCountersBinding.inflate(inflater, container, false);
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mCountersReference = mRootReference
                .child("user-counters").child(getUid());

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new NewCounterDialogFragment();
//                dialogFragment.setCancelable(false);
                dialogFragment.show(getChildFragmentManager(), "tag");
            }
        });

        binding.counterList.setHasFixedSize(true);
        binding.counterList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.counterList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.fab.hide();
                } else {
                    binding.fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for values
        mAdapter = new CounterAdapter(mCountersReference);
        binding.counterList.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Clean up values listener
        mAdapter.cleanupListener();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
