package com.makspasich.counters;

import android.app.ProgressDialog;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getDisplayName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}
