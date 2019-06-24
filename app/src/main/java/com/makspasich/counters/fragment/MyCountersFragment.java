package com.makspasich.counters.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makspasich.counters.CounterDetailActivity;
import com.makspasich.counters.R;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.viewholder.CounterViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MyCountersFragment extends Fragment {

    private static final String TAG = "MyLogMyCountersFragment";

    private DatabaseReference mCountersReference;

    private CounterAdapter mAdapter;
    private RecyclerView mCountersRecycler;
    private FloatingActionButton fab;

    public MyCountersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_counters, container, false);
        mCountersReference = FirebaseDatabase.getInstance().getReference()
                .child("user-counters").child(getUid());

        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new NewCounterDialogFragment();
//                dialogFragment.setCancelable(false);
                dialogFragment.show(getFragmentManager(), "tag");
            }
        });

        mCountersRecycler = rootView.findViewById(R.id.counterList);
        mCountersRecycler.setHasFixedSize(true);
        mCountersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for values
        mAdapter = new CounterAdapter(getContext(), mCountersReference);
        mCountersRecycler.setAdapter(mAdapter);
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

    private class CounterAdapter extends RecyclerView.Adapter<CounterViewHolder> {
        private final String TAG = "MyLogCounterAdapter";

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCounterIds = new ArrayList<>();
        private List<Counter> mCounters = new ArrayList<>();

        CounterAdapter(final Context context, DatabaseReference ref) {
            this.mContext = context;
            this.mDatabaseReference = ref;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Counter counter = null;
                    try {
                        counter = dataSnapshot.getValue(Counter.class);
                        Log.d(TAG, "onChildAdded: " + counter.toString());

                        // Update RecyclerView
                        mCounterIds.add(dataSnapshot.getKey());
                        mCounters.add(counter);
                        notifyItemInserted(mCounters.size() - 1);
                    } catch (Exception e) {
                        Log.d(TAG, "onChildAdded: EXCEPTION PARSE VALUE" + "\n" + dataSnapshot.toString());
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A value has changed, use the key to determine if we are displaying this
                    // value and if so displayed the changed value.
                    Counter newCounter = dataSnapshot.getValue(Counter.class);
                    String counterKey = dataSnapshot.getKey();

                    int counterIndex = mCounterIds.indexOf(counterKey);
                    if (counterIndex > -1) {
                        // Replace with the new data
                        mCounters.set(counterIndex, newCounter);

                        // Update the RecyclerView
                        notifyItemChanged(counterIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + counterKey);
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A value has changed, use the key to determine if we are displaying this
                    // value and if so remove it.
                    String valueKey = dataSnapshot.getKey();

                    int valueIndex = mCounterIds.indexOf(valueKey);
                    if (valueIndex > -1) {
                        // Remove data from the list
                        mCounterIds.remove(valueIndex);
                        mCounters.remove(valueIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(valueIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + valueKey);
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A value has changed position, use the key to determine if we are
                    // displaying this value and if so move it.
                    Counter movedCounter = dataSnapshot.getValue(Counter.class);
                    String counterKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "counterValues:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load values.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @NonNull
        @Override
        public CounterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_counter, parent, false);
            return new CounterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CounterViewHolder holder, final int position) {
            final Counter counter = mCounters.get(holder.getAdapterPosition());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch CounterDetailActivity
                    Intent intent = new Intent(getActivity(), CounterDetailActivity.class);
                    intent.putExtra(CounterDetailActivity.EXTRA_COUNTER_KEY, mCounterIds.get(holder.getAdapterPosition()));
                    intent.putExtra(CounterDetailActivity.EXTRA_NAME_KEY, counter.name_counter);
                    intent.putExtra(CounterDetailActivity.EXTRA_COLOR_KEY, counter.type_counter);
                    startActivity(intent);
                }
            });

            switch (counter.type_counter) {
                case 0:
                    holder.typeCounterView.setImageResource(R.drawable.ic_gas);
                    break;
                case 1:
                    holder.typeCounterView.setImageResource(R.drawable.ic_water);
                    break;
                case 2:
                    holder.typeCounterView.setImageResource(R.drawable.ic_electricity);
                    break;
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final CharSequence[] items = {"Edit counter", "Delete counter"};

                    final AlertDialog.Builder builderContextMenu = new AlertDialog.Builder(mContext);

                    builderContextMenu.setTitle("Select the action");
                    builderContextMenu.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                                case 0:
                                    break;
                                case 1:
                                    AlertDialog.Builder builderDeleteCounter = new AlertDialog.Builder(mContext);
                                    builderDeleteCounter.setTitle("WARNING");
                                    builderDeleteCounter.setMessage("This a counter and its valueы will be deleted!");
                                    builderDeleteCounter.setIcon(R.drawable.ic_warning);
                                    builderDeleteCounter.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            DatabaseReference globalCounterRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("counters").child(mCounterIds.get(holder.getAdapterPosition()));
                                            globalCounterRef.removeValue();
                                            DatabaseReference userCounterRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("user-counters").child(getUid()).child(mCounterIds.get(holder.getAdapterPosition()));
                                            userCounterRef.removeValue();
                                            DatabaseReference userCounterValuesRef = FirebaseDatabase.getInstance().getReference()
                                                    .child("counter-value").child(mCounterIds.get(holder.getAdapterPosition()));
                                            userCounterValuesRef.removeValue();
                                        }
                                    });
                                    builderDeleteCounter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                                    AlertDialog alert = builderDeleteCounter.create();
                                    alert.show();
                                    break;
                            }
                        }
                    });
                    builderContextMenu.show();
                    return true;
                }
            });
            holder.bindToCounter(counter, mContext);
        }

        @Override
        public int getItemCount() {
            return mCounters.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
