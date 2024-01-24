package com.example.pocketpal;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {


    private int[] imageResources = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3
    };

    // Declare your GoogleSignInClient and other variables here
    private FirebaseAuth firebaseAuth;
    private Handler handler;
    private Runnable runnable;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private ImageView imageView;

    private static final int RC_SIGN_IN = 123; // Request code for Google Sign-In

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        setContentView(R.layout.activity_main);
        radioButton1 = findViewById(R.id.rb1);
        radioButton2 = findViewById(R.id.rb2);
        radioButton3 = findViewById(R.id.rb3);
        imageView = findViewById(R.id.imgMain);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the user is already signed in with Firebase Authentication
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to HomePage.class
            navigateToHomepage();
        }


        handler = new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                // Change image every 5 seconds
                changeImage();
                handler.postDelayed(this, 5000); // 5000 milliseconds = 5 seconds
            }
        };


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignIn_Page.class);
                startActivity(intent);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp_Page.class);
                startActivity(intent);
            }
        });

    }


    // Method to change the image and apply animation
    private void changeImage() {
        int currentImageIndex = radioButton1.isChecked() ? 0 : (radioButton2.isChecked() ? 1 : 2);
        int nextImageIndex = (currentImageIndex + 1) % imageResources.length;
        int nextImageResource = imageResources[nextImageIndex];

        // Apply fade-out animation to the current image
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        imageView.startAnimation(fadeOut);

        // Set the next image and apply fade-in animation
        imageView.setImageResource(nextImageResource);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        imageView.startAnimation(fadeIn);

        // Update RadioButton selection based on the next image
        if (nextImageResource == R.drawable.img1) {
            radioButton1.setChecked(true);
            radioButton1.setTextColor(ContextCompat.getColor(this, R.color.button_active_color));
        } else if (nextImageResource == R.drawable.img2) {
            radioButton2.setTextColor(ContextCompat.getColor(this, R.color.button_active_color));
            radioButton2.setChecked(true);
        } else if (nextImageResource == R.drawable.img3) {
            radioButton3.setTextColor(ContextCompat.getColor(this, R.color.button_active_color));
            radioButton3.setChecked(true);
        }
    }


    void navigateToHomepage() {
        finish();
        Intent intent = new Intent(MainActivity.this, HomePage.class);
        startActivity(intent);
    }

    // Start the handler when the activity becomes visible
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 5000); // Start the handler after 5 seconds
    }

    // Stop the handler when the activity becomes invisible
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Stop the handler
    }

}


