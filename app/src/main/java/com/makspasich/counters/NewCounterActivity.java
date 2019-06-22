package com.makspasich.counters;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewCounterActivity extends BaseActivity {

    private static final String TAG = "NewCounterActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mNameCounterField;
//    private EditText mTypeCounterField;
    private FloatingActionButton mSubmitButton;
    Spinner typeCounterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_counter);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mNameCounterField = findViewById(R.id.fieldTitle);
//        mTypeCounterField = findViewById(R.id.fieldBody);
        mSubmitButton = findViewById(R.id.fabSubmitCounter);
        typeCounterSpinner = findViewById(R.id.spinner);


        // Настраиваем адаптер
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.type_counter, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Вызываем адаптер
        typeCounterSpinner.setAdapter(adapter);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String name_counter = mNameCounterField.getText().toString();
//        final String type_counter = mTypeCounterField.getText().toString();
        final int type_counter = typeCounterSpinner.getSelectedItemPosition();

        // Title is required
        if (TextUtils.isEmpty(name_counter)) {
            mNameCounterField.setError(REQUIRED);
            return;
        }

        // Body is required
//        if (TextUtils.isEmpty(type_counter)) {
//            mTypeCounterField.setError(REQUIRED);
//            return;
//        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewCounterActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewCounter(userId, user.username, name_counter, type_counter);
                            //TODO
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        mNameCounterField.setEnabled(enabled);
        typeCounterSpinner.setEnabled(enabled);
//        mTypeCounterField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.show();
        } else {
            mSubmitButton.hide();
        }
    }

    // [START write_fan_out]
    private void writeNewCounter(String userId, String id_counter_creator, String name_counter, int type_counter) {
        // Create new counter at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("counters").push().getKey();
        Counter counter = new Counter(userId, id_counter_creator, name_counter, type_counter);
        Map<String, Object> postValues = counter.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/counters/" + key, postValues);
        childUpdates.put("/user-counters/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}
