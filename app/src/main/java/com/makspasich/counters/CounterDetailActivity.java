package com.makspasich.counters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.models.User;
import com.makspasich.counters.models.Value;
import com.makspasich.counters.viewholder.ValueViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CounterDetailActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "MyLogCounDetailActiv";
    private static final String REQUIRED = "Required";

    public static final String EXTRA_COUNTER_KEY = "counter_key";
    public static final String EXTRA_COLOR_KEY = "color_key";
    public static final String EXTRA_NAME_KEY = "name_key";

    //    private DatabaseReference mCounterReference;
    private DatabaseReference mValuesReference;
    //    private ValueEventListener mCounterListener;
    private String mCounterKey;
    private String mNameKey;
    private int mColorKey;
    private ValueAdapter mAdapter;

    //    private TextView mAuthorView;
//    private TextView mNameCounterView;
//    private TextView mTypeCounterView;
    private EditText mValueField;
    private Button mValueButton;
    private RecyclerView mValuesRecycler;
//    private ImageView typeCounterView;

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
        mNameKey = getIntent().getStringExtra(EXTRA_NAME_KEY);
        if (mNameKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_COUNTER_KEY");
        } else {
            getSupportActionBar().setTitle(mNameKey);
        }


        // Initialize Database
        mValuesReference = FirebaseDatabase.getInstance().getReference()
                .child("counter-value").child(mCounterKey);

        // Initialize Views

        mValueField = findViewById(R.id.fieldValueText);
        mValueButton = findViewById(R.id.buttonCounterValue);
        mValuesRecycler = findViewById(R.id.recyclerCounterValues);


        mValueButton.setOnClickListener(this);
        mValuesRecycler.setLayoutManager(new LinearLayoutManager(this));
        mColorKey = getIntent().getIntExtra(EXTRA_COLOR_KEY, -1);
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
        mAdapter = new ValueAdapter(this, mValuesReference);
        mValuesRecycler.setAdapter(mAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        // Clean up values listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCounterValue:
                postValue();
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void postValue() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;
                        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                                .format(new Date());
                        String valueText = mValueField.getText().toString();
                        if (TextUtils.isEmpty(valueText)) {
                            mValueField.setError(REQUIRED);
                        } else {
                            // Create new value object
                            Value value = new Value(uid, authorName, date, valueText);
                            Log.d(TAG, "onDataChange: Value\n" + value.toString());
                            try {
                                // Push the value, it will appear in the list
                                mValuesReference.push().setValue(value);
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: add new value fail");
                                Log.d(TAG, "onDataChange: messssage:\n" + e.getMessage());

                                e.printStackTrace();

                            }
                            // Clear the field
                            mValueField.setText(null);
                            mValuesRecycler.smoothScrollToPosition(mValuesRecycler.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//                     A new value has been added, add it to the displayed list
                    Value value = null;
                    try {
                        value = dataSnapshot.getValue(Value.class);


                        // [START_EXCLUDE]
                        // Update RecyclerView
                        mValueIds.add(dataSnapshot.getKey());
                        mValues.add(value);
                        notifyItemInserted(mValues.size() - 1);
                        mValuesRecycler.smoothScrollToPosition(mValuesRecycler.getAdapter().getItemCount());
//                         [END_EXCLUDE]
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

                    // [START_EXCLUDE]
                    int valueIndex = mValueIds.indexOf(valueKey);
                    if (valueIndex > -1) {
                        // Replace with the new data
                        mValues.set(valueIndex, newValue);

                        // Update the RecyclerView
                        notifyItemChanged(valueIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + valueKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A value has changed, use the key to determine if we are displaying this
                    // value and if so remove it.
                    String valueKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
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
                    // [END_EXCLUDE]
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
            // [END child_event_listener_recycler]

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
