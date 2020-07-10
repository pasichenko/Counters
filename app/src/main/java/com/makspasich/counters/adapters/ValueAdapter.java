package com.makspasich.counters.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makspasich.counters.R;
import com.makspasich.counters.databinding.ItemValueBinding;
import com.makspasich.counters.models.Value;
import com.makspasich.counters.viewholder.ValueViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ValueAdapter extends RecyclerView.Adapter<ValueViewHolder> {
    private final String TAG = "MyLogValueAdapter";

    private String mCounterKey;
    private Context mContext;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mValueIds = new ArrayList<>();
    private List<Value> mValues = new ArrayList<>();

    public ValueAdapter(DatabaseReference ref, String mCounterKey) {
        mDatabaseReference = ref;
        this.mCounterKey = mCounterKey;

        // Create child event listener
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                     A new value has been added, add it to the displayed list
                Value value = null;
                try {
                    value = dataSnapshot.getValue(Value.class);
                    // Update RecyclerView
                    mValueIds.add(dataSnapshot.getKey());
                    mValues.add(value);
                    notifyItemInserted(mValues.size() - 1);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                } catch (Exception e) {
                    Log.d(TAG, "onChildAdded: EXCEPTION PARSE VALUE" + "\n" + dataSnapshot.toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A value has changed, use the key to determine if we are displaying this
                // value and if so displayed the changed value.
                Value newValue = dataSnapshot.getValue(Value.class);
                String valueKey = dataSnapshot.getKey();

                int valueIndex = mValueIds.indexOf(valueKey);
                if (valueIndex > -1) {
                    // Replace with the new data
                    mValues.set(valueIndex, newValue);

                    // Update the RecyclerView
                    notifyItemChanged(valueIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + valueKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A value has changed, use the key to determine if we are displaying this
                // value and if so remove it.
                String valueKey = dataSnapshot.getKey();

                int valueIndex = mValueIds.indexOf(valueKey);
                if (valueIndex > -1) {
                    // Remove data from the list
                    mValueIds.remove(valueIndex);
                    mValues.remove(valueIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(valueIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + valueKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A value has changed position, use the key to determine if we are
                // displaying this value and if so move it.
                Value movedValue = dataSnapshot.getValue(Value.class);
                String valueKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ItemValueBinding binding = ItemValueBinding.inflate(inflater, parent, false);
        return new ValueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ValueViewHolder holder, int position) {
        Value value = mValues.get(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final CharSequence[] items = {"Edit value", "Delete value"};

                final AlertDialog.Builder builderContextMenu = new AlertDialog.Builder(mContext);

                builderContextMenu.setTitle("Select the action");
                builderContextMenu.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                break;
                            case 1:
                                AlertDialog.Builder builderDeleteValue = new AlertDialog.Builder(mContext);
                                builderDeleteValue.setTitle("WARNING");
                                builderDeleteValue.setMessage("This value will be deleted!");
                                builderDeleteValue.setIcon(R.drawable.ic_warning);
                                builderDeleteValue.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DatabaseReference counterValueRef = FirebaseDatabase.getInstance().getReference()
                                                .child("counter-value").child(mCounterKey).child(mValueIds.get(holder.getAdapterPosition()));
                                        counterValueRef.removeValue();
                                    }
                                });
                                builderDeleteValue.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alert = builderDeleteValue.create();
                                alert.show();
                                break;
                        }
                    }
                });
                builderContextMenu.show();
                return true;
            }
        });
        holder.bindToValue(value);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

}
