package com.makspasich.counters.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.makspasich.counters.CounterDetailActivity;
import com.makspasich.counters.R;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.viewholder.CounterViewHolder;

public class MyCountersFragment extends Fragment {

    private static final String TAG = "MyCountersFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Counter, CounterViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public MyCountersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_counters, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.counterList);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query countersQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Counter>()
                .setQuery(countersQuery, Counter.class)
                .build();
        try {

            mAdapter = new FirebaseRecyclerAdapter<Counter, CounterViewHolder>(options) {

                @Override
                public CounterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                    return new CounterViewHolder(inflater.inflate(R.layout.item_counter, viewGroup, false));
                }

                @Override
                protected void onBindViewHolder(CounterViewHolder viewHolder, int position, final Counter model) {
                    final DatabaseReference counterRef = getRef(position);

                    // Set click listener for the whole counter view
                    final String counterKey = counterRef.getKey();
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Launch CounterDetailActivity
                            Intent intent = new Intent(getActivity(), CounterDetailActivity.class);
                            intent.putExtra(CounterDetailActivity.EXTRA_COUNTER_KEY, counterKey);
                            intent.putExtra(CounterDetailActivity.EXTRA_NAME_KEY, model.name_counter);
                            intent.putExtra(CounterDetailActivity.EXTRA_COLOR_KEY, model.type_counter);
                            startActivity(intent);
                        }
                    });


                    switch (model.type_counter) {
                        case 0:
                            viewHolder.typeCounterView.setImageResource(R.drawable.ic_gas);
                            break;
                        case 1:
                            viewHolder.typeCounterView.setImageResource(R.drawable.ic_water);
                            break;
                        case 2:
                            viewHolder.typeCounterView.setImageResource(R.drawable.ic_electricity);
                            break;
                    }
//
                    viewHolder.bindToCounter(model);

                }
            };
        } catch (Exception e) {
            Log.d(TAG, "onActivityCreated: FATAL ERROR");
        }

        mRecycler.setAdapter(mAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("user-counters")
                .child(getUid());
    }

}
