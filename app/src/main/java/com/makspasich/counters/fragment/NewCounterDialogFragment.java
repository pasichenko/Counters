package com.makspasich.counters.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
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

public class NewCounterDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewCouDialFrag";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;
    private TextInputEditText nameCounterField;
    private AutoCompleteTextView typeCounterField;
    private int selectedTypeCounter;
    private AlertDialog dialog;
    private Button positiveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_new_counter, null);
        builder.setView(view);
        builder.setTitle("Add new counter");
        builder.setPositiveButton("Ok", null);
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.type_counter));
        nameCounterField = view.findViewById(R.id.fieldNameCounter);
        typeCounterField = view.findViewById(R.id.spinnerTypeCounter);

        typeCounterField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTypeCounter = i;
            }
        });
        typeCounterField.setAdapter(arrayAdapter);
        typeCounterField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) typeCounterField.showDropDown();
            }
        });
        typeCounterField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                typeCounterField.showDropDown();
                return false;
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();

        dialog = builder.create();
//        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInner) {
                if (true) {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (button != null) {
                        positiveButton = button;
                        positiveButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getContext(), "sss", Toast.LENGTH_SHORT).show();
                                submitPost();
                            }
                        });
                    }
                }
            }
        });
        return dialog;


    }

    private void submitPost() {
        final String name_counter = nameCounterField.getText().toString();
        final int type_counter = selectedTypeCounter;

        // Title is required
        if (TextUtils.isEmpty(name_counter)) {
            nameCounterField.setError(REQUIRED);
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
                            writeNewCounter(userId, user.username, name_counter, type_counter);
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
        nameCounterField.setEnabled(enabled);
        typeCounterField.setEnabled(enabled);
        positiveButton.setEnabled(enabled);
    }

    private void writeNewCounter(String userId, String id_counter_creator, String name_counter, int type_counter) {
        // Create new counter at /user-counters/$userid/$counterid and at
        // /counters/$counterid simultaneously
        String key = mDatabase.child("counters").push().getKey();
        Counter counter = new Counter(userId, id_counter_creator, name_counter, type_counter);
        Map<String, Object> postValues = counter.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/counters/" + key, postValues);
        childUpdates.put("/user-counters/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
}
