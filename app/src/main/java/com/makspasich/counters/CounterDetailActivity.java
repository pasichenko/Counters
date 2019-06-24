package com.makspasich.counters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makspasich.counters.fragment.NewValueDialogFragment;
import com.makspasich.counters.models.Value;
import com.makspasich.counters.viewholder.ValueViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CounterDetailActivity extends BaseActivity {
    private final String TAG = "MyLogCounDetailActiv";

    public static final String EXTRA_COUNTER_KEY = "counter_key";
    public static final String EXTRA_COLOR_KEY = "color_key";
    public static final String EXTRA_NAME_KEY = "name_key";

    private DatabaseReference mValuesReference;
    private String mCounterKey;
    private ValueAdapter mAdapter;

    private RecyclerView mValuesRecycler;
    private FloatingActionButton fab;

    private boolean checkRecyclerScrollListener = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Get counter key from intent
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

        // Initialize Database
        mValuesReference = FirebaseDatabase.getInstance().getReference()
                .child("counter-value").child(mCounterKey);

        // Initialize Views
        mValuesRecycler = findViewById(R.id.recyclerCounterValues);
        fab = findViewById(R.id.fabAddValue);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewValueDialogFragment dialogFragment = new NewValueDialogFragment();
//                dialogFragment.setCancelable(false);
                dialogFragment.setmCounterKey(mCounterKey);
                dialogFragment.show(getSupportFragmentManager(), "newValue");
            }
        });

        mValuesRecycler.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for values
        mAdapter = new ValueAdapter(this, mValuesReference);
        mValuesRecycler.setAdapter(mAdapter);
//        mValuesRecycler.addOnScrollListener(new CustomScrollListener(this));
        RecyclerView.OnScrollListener mListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                final int visibleItemCount = layoutManager.getChildCount();
                final int totalItemCount = layoutManager.getItemCount();
                final int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (!checkRecyclerScrollListener) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE || (visibleItemCount + pastVisibleItems > totalItemCount)) {
                        Log.i(TAG, "End of list");
                        mValuesRecycler.clearOnScrollListeners();
                        mValuesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                if (dy > 0) {
                                    fab.hide();
                                } else {
                                    fab.show();
                                }
                                super.onScrolled(recyclerView, dx, dy);
                            }
                        });
                        Log.d(TAG, "onScrollStateChanged: listener ADDED");
                    }
                }
            }
        };
        mValuesRecycler.addOnScrollListener(mListener);
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

    private class ValueAdapter extends RecyclerView.Adapter<ValueViewHolder> {
        private final String TAG = "MyLogValueAdapter";

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mValueIds = new ArrayList<>();
        private List<Value> mValues = new ArrayList<>();

        ValueAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

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
                        mValuesRecycler.smoothScrollToPosition(mValuesRecycler.getAdapter().getItemCount() - 1);
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

        @NonNull
        @Override
        public ValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_value, parent, false);
            return new ValueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ValueViewHolder holder, int position) {
            Value value = mValues.get(position);
            holder.bindToValue(value, mContext);
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
}
