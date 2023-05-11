package com.cicekvolkan.missedconnections.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private AppBarConfiguration appBarConfiguration;

    private NavigationView navigationView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        MaterialToolbar toolbar = binding.topappBar;
        DrawerLayout drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.mainFragment).setOpenableLayout(drawerLayout).build();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        NavController navController = Navigation.findNavController(this,R.id.fragmentNavHost);
        NavigationUI.setupActionBarWithNavController(this,navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView,navController);
        getName();
        getImage();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.logout:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Hesaptan çıkış")
                                .setMessage("Çıkış yapmak istediğinize emin misiniz?")
                                .setPositiveButton("Çıkış yap", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        logout();
                                        drawerLayout.closeDrawers();
                                    }
                                })
                                .setNegativeButton("Hayır", null).show();

                        return true;
                    case R.id.profileFragment:
                        navController.navigate(R.id.profileFragment);
                        drawerLayout.closeDrawers();
                        return true;
                    case R.id.messageFragment:
                        navController.navigate(R.id.messageFragment);
                        drawerLayout.closeDrawers();
                        return true;
                    /*case R.id.settingFragment:
                        navController.navigate(R.id.settingFragment);
                        drawerLayout.closeDrawers();
                        return true;*/
                    default:
                        return NavigationUI.onNavDestinationSelected(item, navController);
                }
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentNavHost);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void getName(){
        firebaseFirestore.collection("users")
            .whereEqualTo("mail", user.getEmail())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String name = documentSnapshot.getString("name");
                    View headerView = navigationView.getHeaderView(0);
                    TextView navNameTemp = headerView.findViewById(R.id.nav_name);
                    navNameTemp.setText(name);
                } else {
                    // Belge bulunamadı
                }
            })
            .addOnFailureListener(e -> {
                //Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            });
    }
    public void getImage() {
        firebaseFirestore.collection("users").document(user.getEmail())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("imageUrl");
                    View headerView = navigationView.getHeaderView(0);
                    ImageView imageView = headerView.findViewById(R.id.nav_img);
                    if(imageUrl.equals("image")){
                        imageView.setImageResource(R.drawable.user);
                    }else {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }
            })
            .addOnFailureListener(e -> {
                //Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            });
    }
}