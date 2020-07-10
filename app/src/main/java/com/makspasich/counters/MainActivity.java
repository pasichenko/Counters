package com.makspasich.counters;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.makspasich.counters.fragment.AddSharedCounterDialogFragment;
import com.makspasich.counters.fragment.MyCountersFragment;
import com.makspasich.counters.utils.CircularTransformation;
import com.squareup.picasso.Picasso;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "MyLogMainActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initFirebase();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new MyCountersFragment())
                    .commit();
            this.setTitle(getString(R.string.menu_counters));

            navigationView.setCheckedItem(R.id.nav_counters);
        }
    }


    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initFirebase() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        TextView login = navigationView.getHeaderView(0).findViewById(R.id.login);
        TextView username = navigationView.getHeaderView(0).findViewById(R.id.username);
        ImageView usersPhoto = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Uri photoUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            login.setText(getDisplayName());
            username.setText(getEmail());
            Picasso.get()
                    .load(photoUri)
                    .placeholder(R.drawable.icon_launcher)
                    .error(R.drawable.ic_warning)
                    .transform(new CircularTransformation(0))
                    .into(usersPhoto);
            Log.d(TAG, "initFirebase: " + photoUri.toString());
        } else {
            Intent intent = new Intent(this, GoogleSignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_counters:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new MyCountersFragment())
                        .commit();
                this.setTitle(getString(R.string.menu_counters));
                break;
            case R.id.nav_add_share_counter:
                DialogFragment dialogFragment = new AddSharedCounterDialogFragment();
//                dialogFragment.setCancelable(false);
                dialogFragment.show(getSupportFragmentManager(), "tag");
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                // Google sign out
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
