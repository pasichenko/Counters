package com.makspasich.counters.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.CounterValuesActivity;
import com.makspasich.counters.DetailCounterActivity;
import com.makspasich.counters.R;
import com.makspasich.counters.databinding.ItemCounterBinding;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.viewholder.CounterViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.makspasich.counters.utils.Constants.EXTRA_COLOR_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_COUNTER_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_NAME_KEY;

public class CounterAdapter extends RecyclerView.Adapter<CounterViewHolder> {
    private final String TAG = "MyLogCounterAdapter";

    private Context mContext;
    private DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mCounterIds = new ArrayList<>();
    private List<Counter> mCounters = new ArrayList<>();

    public CounterAdapter(DatabaseReference ref) {
        this.mDatabaseReference = ref;

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded: datasnapsot " + dataSnapshot);
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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mContext = recyclerView.getContext();
    }

    @NonNull
    @Override
    public CounterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ItemCounterBinding binding = ItemCounterBinding.inflate(inflater, parent, false);
        return new CounterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final CounterViewHolder holder, final int position) {
        final Counter counter = mCounters.get(holder.getAdapterPosition());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch CounterValuesActivity
                Intent intent = new Intent(mContext, CounterValuesActivity.class);
                intent.putExtra(EXTRA_COUNTER_KEY, mCounterIds.get(holder.getAdapterPosition()));
                intent.putExtra(EXTRA_NAME_KEY, counter.name_counter);
                intent.putExtra(EXTRA_COLOR_KEY, counter.type_counter);
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {


            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = {"Detail", "Share", "Delete counter"};

                final AlertDialog.Builder builderContextMenu = new AlertDialog.Builder(mContext);

                builderContextMenu.setTitle("Select the action");
                builderContextMenu.setItems(items, new DialogInterface.OnClickListener() {
                    private void detailCounter() {
                        Intent intent = new Intent(mContext, DetailCounterActivity.class);
                        intent.putExtra(EXTRA_COUNTER_KEY, mCounterIds.get(holder.getAdapterPosition()));
                        intent.putExtra(EXTRA_NAME_KEY, counter.name_counter);
                        intent.putExtra(EXTRA_COLOR_KEY, counter.type_counter);
                        mContext.startActivity(intent);
                    }

                    private void shareCounter() {
                        String userAccId = counter.uid;
                        String idCounter = mCounterIds.get(holder.getAdapterPosition());
                        String data = userAccId + '@' + idCounter;

                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, data);
                        mContext.startActivity(Intent.createChooser(share, "Share Text"));
                    }

                    private void deleteCounter() {
                        AlertDialog.Builder builderDeleteCounter = new AlertDialog.Builder(mContext);
                        builderDeleteCounter.setTitle("WARNING");
                        builderDeleteCounter.setMessage("This a counter and its values will be deleted!");
                        builderDeleteCounter.setIcon(R.drawable.ic_warning);
                        builderDeleteCounter.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: I click Ok Delete, " + mCounterIds.get(holder.getAdapterPosition()));
                                mRootReference.
                                        child("counter-subscribers").
                                        child(mCounterIds.get(holder.getAdapterPosition())).
                                        addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Log.d(TAG, "onDataChange: " + dataSnapshot);
                                                Map<String, Object> counterSubscribers = new HashMap<>();
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    counterSubscribers.put(snapshot.getKey(), snapshot.getValue());
                                                }
                                                Log.d(TAG, "onDataChange: " + counterSubscribers);
                                                Log.d(TAG, "onDataChange: " + counterSubscribers.get(getUid()));
                                                if (Objects.equals(counterSubscribers.get(getUid()), "owner")) {
                                                    DatabaseReference globalCounterRef = mRootReference
                                                            .child("counters").child(mCounterIds.get(holder.getAdapterPosition()));
                                                    globalCounterRef.removeValue();

                                                    for (Map.Entry entry : counterSubscribers.entrySet()) {
                                                        if (Objects.equals(entry.getValue(), "subscriber")) {
                                                            DatabaseReference userCounterRef = mRootReference
                                                                    .child("user-counters").child((String) entry.getKey()).child(mCounterIds.get(holder.getAdapterPosition()));
                                                            userCounterRef.removeValue();
                                                        }
                                                    }
                                                    DatabaseReference subscribeListRef = mRootReference
                                                            .child("counter-subscribers").child(mCounterIds.get(holder.getAdapterPosition()));
                                                    subscribeListRef.removeValue();
                                                    DatabaseReference userCounterRef = mRootReference
                                                            .child("user-counters").child(getUid()).child(mCounterIds.get(holder.getAdapterPosition()));
                                                    userCounterRef.removeValue();
                                                    DatabaseReference userCounterValuesRef = mRootReference
                                                            .child("counter-value").child(mCounterIds.get(holder.getAdapterPosition()));
                                                    userCounterValuesRef.removeValue();
                                                } else if (Objects.equals(counterSubscribers.get(getUid()), "subscriber")) {
                                                    DatabaseReference userCounterRef = mRootReference
                                                            .child("user-counters").child(getUid()).child(mCounterIds.get(holder.getAdapterPosition()));
                                                    userCounterRef.removeValue();

                                                    for (DataSnapshot stock : dataSnapshot.getChildren()) {
                                                        if (Objects.equals(stock.getKey(), getUid()))
                                                            stock.getRef().removeValue();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError
                                                                            databaseError) {
                                            }
                                        });
                            }
                        });
                        builderDeleteCounter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog alert = builderDeleteCounter.create();
                        alert.show();
                    }

                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                detailCounter();
                                break;
                            case 1:
                                shareCounter();
                                break;
                            case 2:
                                deleteCounter();
                                break;
                        }
                    }
                });
                builderContextMenu.show();
                return true;
            }
        });
        holder.bindToCounter(counter);
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
