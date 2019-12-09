package com.makspasich.counters;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import static com.makspasich.counters.utils.Constants.EXTRA_COLOR_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_COUNTER_KEY;
import static com.makspasich.counters.utils.Constants.EXTRA_NAME_KEY;

public class DetailCounterActivity extends AppCompatActivity {
    private String mCounterKey;
    private TextView nameCounter;
    private TextView typeCounter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_counter);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        nameCounter = findViewById(R.id.tvNameCounter);
        typeCounter = findViewById(R.id.tvTypeCounter);
        // Get counter key from intent
        getCounterDataFromIntent();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Snackbar", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .show();
            }
        });
    }


    private void getCounterDataFromIntent() {
        mCounterKey = getIntent().getStringExtra(EXTRA_COUNTER_KEY);
        if (mCounterKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_COUNTER_KEY");
        }
        String mNameKey = getIntent().getStringExtra(EXTRA_NAME_KEY);
        if (mNameKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_COUNTER_KEY");
        } else {
            nameCounter.setText("Name: " + mNameKey);
        }
        int mColorKey = getIntent().getIntExtra(EXTRA_COLOR_KEY, -1);
        if (mCounterKey != null) {
            typeCounter.setText("Type: " + getResources().getStringArray(R.array.type_counter)[mColorKey]);

//            switch (mColorKey) {
//                case 0:
//                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(251, 111, 67)));
//                    break;
//                case 1:
//                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(99, 174, 228)));
//                    break;
//                case 2:
//                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.rgb(233, 198, 19)));
//                    break;
//            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
