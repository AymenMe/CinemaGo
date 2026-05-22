package com.cinemago.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.cinemago.R;
import com.cinemago.api.RetrofitClient;
import com.cinemago.fragments.ChatFragment;
import com.cinemago.fragments.HomeFragment;
import com.cinemago.fragments.MapFragment;
import com.cinemago.fragments.ProfileFragment;
import com.cinemago.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // --- FASTAPI CONNECTION TEST ---
        testFastApiConnection();
    }

    private void testFastApiConnection() {
        RetrofitClient.getInstance().getFastApiService().getStatus().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "✅ FastAPI Connected!", Toast.LENGTH_SHORT).show();
                    Log.d("FastAPI_Test", "Success: Server is up!");
                } else {
                    Toast.makeText(MainActivity.this, "⚠️ FastAPI Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "❌ FastAPI Connection Failed!", Toast.LENGTH_LONG).show();
                Log.e("FastAPI_Test", "Error: " + t.getMessage());
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}