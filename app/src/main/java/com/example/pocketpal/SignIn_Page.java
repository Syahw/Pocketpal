package com.example.pocketpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignIn_Page extends AppCompatActivity {

    private EditText etEmail, etPassword;

    private TextView tvForgotPass;
    private FirebaseAuth auth;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_page);
        ImageView imgBack1 = findViewById(R.id.imgBack);
        TextView tvDescSignIn = findViewById(R.id.tvDescSignIn);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPass = findViewById(R.id.tvForgotPass);

        auth = FirebaseAuth.getInstance();

        imgBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        tvDescSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp_Page.class);
                startActivity(intent);
            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), forgotPasswordPage.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = etEmail.getText().toString().trim();
                String userPassword = etPassword.getText().toString().trim();

                // Perform input validation for email and password
                if (!validateEmail() || !validatePassword()) {
                    return;
                }
                // Call the signInUser method
                signInUser(userEmail, userPassword);
            }
        });
    }

        public Boolean validateEmail() {
            String val = etEmail.getText().toString().trim();
            if (val.isEmpty()) {
                etEmail.setError("Email cannot be empty");
                return false;
            } else {
                etEmail.setError(null);
                return true;
            }
        }

        public Boolean validatePassword() {
            String val = etPassword.getText().toString().trim();
            if (val.isEmpty()) {
                etPassword.setError("Password cannot be empty");
                return false;
            } else {
                etPassword.setError(null);
                return true;
            }
        }


    private void signInUser(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sign in the user using FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, proceed to the next activity
                            Toast.makeText(SignIn_Page.this, "Successfully Signed In", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignIn_Page.this, HomePage.class);

                            intent.putExtra("gmail", email);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign in failed, display an error message
                            Toast.makeText(SignIn_Page.this, "Authentication failed. Please check your email and password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}


