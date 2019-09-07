package com.makspasich.counters.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.text.DateFormat;
import java.util.Date;

public class NewValueDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewValDialFrag";
    private static final String REQUIRED = "Required";

    private DatabaseReference mValuesReference;
    private String mCounterKey;

    private AlertDialog dialogAddNewValue;

    private TextView title;
    private ImageButton flashLight;

    private TextView dateField;
    private Button setNowDatetimeButton;
    private TextView valueField;

    private boolean isTurnOn = false;
    private Button positiveButton;

    public NewValueDialogFragment() {
    }

    public NewValueDialogFragment(String mCounterKey) {
        this();
        this.mCounterKey = mCounterKey;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mValuesReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("counter-value")
                .child(mCounterKey);

        dialogAddNewValue = getAlertDialogBuilder().create();
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

    private AlertDialog.Builder getAlertDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View customBar = inflater.inflate(R.layout.fragment_dialog_new_value_custom_bar, null);
        View content = inflater.inflate(R.layout.fragment_dialog_new_value, null);

        builder.setCustomTitle(customBar);
        builder.setView(content);
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("cancel", null);

        title = customBar.findViewById(R.id.title);
        title.setText("Add new value");
        flashLight = customBar.findViewById(R.id.flashLight);
        flashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatusFlashLight();
                if (isTurnOn) {
                    flashLight.setImageResource(R.drawable.ic_flashlight_on);
                } else {
                    flashLight.setImageResource(R.drawable.ic_flashlight_off);
                }

            }
        });

        dateField = content.findViewById(R.id.textViewDate);
        setNowDatetimeButton = content.findViewById(R.id.buttonNowDatetime);
        valueField = content.findViewById(R.id.fieldValueText);
        setNowDatetimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentDate();
            }
        });

        setCurrentDate();
        return builder;
    }


    private void changeStatusFlashLight() {
        CameraManager camManager;
        String cameraId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                camManager = (CameraManager) getActivity()
                        .getSystemService(Context.CAMERA_SERVICE);
                // Usually front camera is at 0 position.
                cameraId = camManager.getCameraIdList()[0];

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isTurnOn) {
                        camManager.setTorchMode(cameraId, false);
                        isTurnOn = false;
                    } else {
                        camManager.setTorchMode(cameraId, true);
                        isTurnOn = true;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
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
                            if (isTurnOn) {
                                changeStatusFlashLight();
                            }
                            dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setCurrentDate() {
        String date = DateFormat
                .getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
                .format(new Date());
        dateField.setText(date);
    }
}
