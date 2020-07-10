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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.R;
import com.makspasich.counters.databinding.FragmentDialogNewCounterBinding;
import com.makspasich.counters.models.Counter;
import com.makspasich.counters.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewCounterDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewCouDialFrag";
    private static final String REQUIRED = "Required";
    private FragmentDialogNewCounterBinding binding;
    private DatabaseReference mDatabase;
    private int selectedTypeCounter;
    private AlertDialog dialog;
    private Button positiveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_dialog_new_counter, null);
        binding = FragmentDialogNewCounterBinding.bind(view);
        builder.setView(binding.getRoot());
        builder.setTitle("Add new counter");
        builder.setPositiveButton("Ok", null);
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.type_counter));

        binding.spinnerTypeCounter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTypeCounter = i;
            }
        });
        binding.spinnerTypeCounter.setAdapter(arrayAdapter);
        binding.spinnerTypeCounter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) binding.spinnerTypeCounter.showDropDown();
            }
        });
        binding.spinnerTypeCounter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                binding.spinnerTypeCounter.showDropDown();
                return false;
            }
        });
        binding.fieldNameCounter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    binding.textInputLayoutNameCounter.setError("Enter name counter");
                } else {
                    binding.textInputLayoutNameCounter.setError(null);
                }
            }
        });
        binding.spinnerTypeCounter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0) {
                    binding.textInputLayoutTypeCounter.setError("Select type counter");
                } else {
                    binding.textInputLayoutTypeCounter.setError(null);
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
        final String name_counter = binding.fieldNameCounter.getText().toString();
        final String type_counter = binding.spinnerTypeCounter.getText().toString();
        boolean isEmptyNameCounter = TextUtils.isEmpty(name_counter);
        boolean isEmptyTypeCounter = TextUtils.isEmpty(type_counter);
        // Title is required
        if (isEmptyNameCounter || isEmptyTypeCounter) {
            if (isEmptyNameCounter)
                binding.textInputLayoutNameCounter.setError("Enter name counter");
            if (isEmptyTypeCounter)
                binding.textInputLayoutNameCounter.setError("Select type counter");
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getContext(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new counter
                            writeNewCounter(userId, user.username, name_counter, selectedTypeCounter);
                            //TODO
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
        binding.fieldNameCounter.setEnabled(enabled);
        binding.spinnerTypeCounter.setEnabled(enabled);
        positiveButton.setEnabled(enabled);
    }

    private void writeNewCounter(String userId, String usernameCreator, String name_counter, int type_counter) {
        // Create new counter at /user-counters/$userid/$counterid and at
        // /counters/$counterid simultaneously
        String key = mDatabase.child("counters").push().getKey();
        Counter counter = new Counter(userId, usernameCreator, name_counter, type_counter);
        Map<String, Object> postValues = counter.toMap();
        Map<String, Object> counterSubscribers = new HashMap<>();
        counterSubscribers.put(userId, "owner");

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/counters/" + key, postValues);
        childUpdates.put("/user-counters/" + userId + "/" + key, postValues);
        childUpdates.put("/counter-subscribers/" + key, counterSubscribers);
        mDatabase.updateChildren(childUpdates);
    }
}
