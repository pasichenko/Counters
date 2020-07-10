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

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makspasich.counters.R;
import com.makspasich.counters.databinding.FragmentDialogNewValueBinding;
import com.makspasich.counters.databinding.FragmentDialogNewValueCustomBarBinding;
import com.makspasich.counters.models.User;
import com.makspasich.counters.models.Value;

import java.text.DateFormat;
import java.util.Date;

public class NewValueDialogFragment extends DialogFragment {
    private static final String TAG = "MyLogNewValDialFrag";
    private static final String REQUIRED = "Required";
    private FragmentDialogNewValueCustomBarBinding barBinding;
    private FragmentDialogNewValueBinding contentBinding;
    private DatabaseReference mValuesReference;
    private String mCounterKey;

    private AlertDialog dialogAddNewValue;

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
        barBinding = FragmentDialogNewValueCustomBarBinding.bind(customBar);
        View content = inflater.inflate(R.layout.fragment_dialog_new_value, null);
        contentBinding = FragmentDialogNewValueBinding.bind(content);
        builder.setCustomTitle(barBinding.getRoot());
        builder.setView(contentBinding.getRoot());
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("cancel", null);

        barBinding.title.setText("Add new value");
        barBinding.flashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatusFlashLight();
                if (isTurnOn) {
                    barBinding.flashLight.setImageResource(R.drawable.ic_flashlight_on);
                } else {
                    barBinding.flashLight.setImageResource(R.drawable.ic_flashlight_off);
                }

            }
        });

        contentBinding.buttonNowDatetime.setOnClickListener(new View.OnClickListener() {
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
                        String valueText = contentBinding.fieldValueText.getText().toString();
                        String dateText = contentBinding.textViewDate.getText().toString();

                        if (TextUtils.isEmpty(valueText)) {
                            contentBinding.fieldValueText.setError(REQUIRED);
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
                            contentBinding.fieldValueText.setText(null);
                            contentBinding.textViewDate.setText(null);
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
        contentBinding.textViewDate.setText(date);
    }
}
