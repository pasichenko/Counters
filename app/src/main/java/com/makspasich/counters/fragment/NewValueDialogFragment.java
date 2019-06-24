package com.makspasich.counters.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.R;
import com.makspasich.counters.models.User;
import com.makspasich.counters.models.Value;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewValueDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewValDialFrag";
    private static final String REQUIRED = "Required";


    private DatabaseReference mValuesReference;
    private String mCounterKey;

    private TextView dateField;

    private Button setNowDatetimeButton;
    private TextView valueField;
    private AlertDialog dialogAddNewValue;

    private Button positiveButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_new_value, null);
        builder.setView(view);
        builder.setTitle("Add new value");
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("cancel", null);

        dateField = view.findViewById(R.id.textViewDate);
        setNowDatetimeButton = view.findViewById(R.id.buttonNowDatetime);
        valueField = view.findViewById(R.id.fieldValueText);
        setNowDatetimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentDate();
            }
        });

        setCurrentDate();

        mValuesReference = FirebaseDatabase.getInstance().getReference()
                .child("counter-value").child(mCounterKey);
        dialogAddNewValue = builder.create();
        dialogAddNewValue.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInner) {
                Button button = dialogAddNewValue.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    positiveButton = button;
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            submitValue();
                        }
                    });
                }
            }
        });
        return dialogAddNewValue;


    }

    private void submitValue() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;
                        String valueText = valueField.getText().toString();
                        String dateText = dateField.getText().toString();

                        if (TextUtils.isEmpty(valueText)) {
                            valueField.setError(REQUIRED);
                        } else {
                            // Create new value object
                            Value value = new Value(uid, authorName, dateText, valueText);
                            Log.d(TAG, "onDataChange: Value\n" + value.toString());
                            try {
                                // Push the value, it will appear in the list
                                mValuesReference.push().setValue(value);
                            } catch (Exception e) {
                                Log.d(TAG, "onDataChange: add new value fail");
                            }
                            // Clear the field
                            valueField.setText(null);
                            dateField.setText(null);
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    public void setmCounterKey(String mCounterKey) {
        this.mCounterKey = mCounterKey;
    }

    private void setCurrentDate() {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new Date());
        dateField.setText(date);
    }
}
