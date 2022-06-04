package com.example.hodophile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hodophile.databinding.ActivityDrawerBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

public class DrawerActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDrawer.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_accommodation, R.id.nav_attractions,
                R.id.nav_map, R.id.nav_weather, R.id.profileFragment)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(DrawerActivity.this, com.example.hodophile.LoginActivity.class);
            startActivity(i);
            finish();
        });

        View header = navigationView.getHeaderView(0);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView profileEmail = header.findViewById(R.id.ProfileEmail);
        TextView profileUsername = header.findViewById(R.id.ProfileUsername);
        ImageView profilePicture = header.findViewById(R.id.profilePicture);
        ConstraintLayout profileLayout = header.findViewById(R.id.drawerHeader);

        DatabaseReference database = FirebaseDatabase
                .getInstance(getString(R.string.database_link))
                .getReference("Profiles").child(user.getUid());

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                profileUsername.setText(username);
                String imageURL = snapshot.child("image").getValue(String.class);
                Picasso.get().load(imageURL).placeholder(R.mipmap.default_profile_picture)
                        .fit().into(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DrawerActivity.this,
                        "Unable to retrieve profile information", Toast.LENGTH_SHORT).show();
            }
        });
        profileEmail.setText(user.getEmail());

        profileLayout.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userID", user.getUid());
            navController.navigate(R.id.profileFragment, bundle);
            drawer.close();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_help) {// User chose the "Settings" item, show the app settings UI...
            Uri webAddress = Uri.parse("");
            Intent goToLink = new Intent(Intent.ACTION_VIEW, webAddress);
            startActivity(goToLink);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}