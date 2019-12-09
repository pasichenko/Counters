package com.makspasich.counters.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.R;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.models.User;

import java.util.HashMap;
import java.util.Map;

public class AddSharedCounterDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewCouDialFrag";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;
    private TextInputLayout tilSharedKeyCounter;
    private TextInputEditText sharedKeyCounterField;
    private AlertDialog dialog;
    private Button positiveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_add_shared_counter, null);
        builder.setView(view);
        builder.setTitle("Add shared counter");
        builder.setPositiveButton("Ok", null);
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        tilSharedKeyCounter = view.findViewById(R.id.textInputLayoutSharedKeyCounter);
        sharedKeyCounterField = view.findViewById(R.id.fieldSharedKeyCounter);

        sharedKeyCounterField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    tilSharedKeyCounter.setError("Enter shared key counter");
                } else {
                    tilSharedKeyCounter.setError(null);
                }
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();

        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInner) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    positiveButton = button;
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            submitPost();
                        }
                    });
                }
            }
        });
        return dialog;


    }

    private void submitPost() {
        final String sharedKeyCounter = sharedKeyCounterField.getText().toString();
        boolean isEmptyNameCounter = TextUtils.isEmpty(sharedKeyCounter);
        // Title is required
        if (isEmptyNameCounter) {
            tilSharedKeyCounter.setError("Enter name counter");
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(currentUserId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + currentUserId + " is unexpectedly null");
                            Toast.makeText(getContext(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new counter
                            addSharedCounter(currentUserId, user.username, sharedKeyCounter);
                        }
                        setEditingEnabled(true);
                        // Close this fragment dialog
                        dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        sharedKeyCounterField.setEnabled(enabled);
        positiveButton.setEnabled(enabled);
    }

    private void addSharedCounter(final String currentUserId, String idUser, final String sharedKeyString) {
        // Create new counter at /user-counters/$userid/$counterid and at
        // /counters/$counterid simultaneously
        if (sharedKeyString.contains("@")) {
            final String keyUserOwner = sharedKeyString.split("@")[0];
            final String keySharedCounter = sharedKeyString.split("@")[1];


            mDatabase.child("user-counters").
                    child(keyUserOwner).
                    child(keySharedCounter).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Counter counter = dataSnapshot.getValue(Counter.class);
                            if (counter == null) {
                                Log.e(TAG, "Counter " + keySharedCounter + " is unexpectedly null");
                                Toast.makeText(getContext(),
                                        "Error: could not fetch user.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                Map<String, Object> postValues = counter.toMap();
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("/user-counters/" + currentUserId + "/" + keySharedCounter, postValues);
                                mDatabase.updateChildren(childUpdates);

                                Map<String, Object> counterSubscribers = new HashMap<>();
                                counterSubscribers.put(userId,"subscriber");
                                mDatabase.child("counter-subscribers").child(keySharedCounter).updateChildren(counterSubscribers);

//                                childUpdates.put("/counter-subscribers/" + dataSnapshot.getKey(), counterSubscribers);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "getCounter:onCancelled", databaseError.toException());
                            setEditingEnabled(true);
                        }
                    });
        }
    }
}
