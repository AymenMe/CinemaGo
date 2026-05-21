package com.cinemago.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.cinemago.R;
import com.cinemago.fragments.ChatFragment;
import com.cinemago.fragments.HomeFragment;
import com.cinemago.fragments.MapFragment;
import com.cinemago.fragments.ProfileFragment;
import com.cinemago.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Load home by default
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)    { loadFragment(new HomeFragment());    return true; }
            if (id == R.id.nav_search)  { loadFragment(new SearchFragment());  return true; }
            if (id == R.id.nav_map)     { loadFragment(new MapFragment());     return true; }
            if (id == R.id.nav_chat)    { loadFragment(new ChatFragment());    return true; }
            if (id == R.id.nav_profile) { loadFragment(new ProfileFragment()); return true; }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}