package com.example.pocketpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pocketpal.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SignUp_Page extends AppCompatActivity  {
    private EditText etUsername, etEmail2, etPassword2;
    private Button btnSignUp;

    FirebaseAuth auth;
    FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        //Find The Attributes using findViewById
        ImageView imgBack2 = findViewById(R.id.imgBack2);
        TextView tvSignUpDesc = findViewById(R.id.tvSignUpDesc);
        etUsername = findViewById(R.id.etUsername);
        etEmail2 = findViewById(R.id.etEmail2);
        etPassword2 = findViewById(R.id.etPassword2);

        String hint = "Password (Atleast 6 characters)";
        int startIndex = hint.indexOf("(");
        int endIndex = hint.indexOf(")") + 1;
        if (startIndex >= 0 && endIndex >= 0) {
            SpannableString spannableHint = new SpannableString(hint);
            spannableHint.setSpan(new RelativeSizeSpan(0.9f), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            etPassword2.setHint(spannableHint);
        }
        btnSignUp = findViewById(R.id.btnSignUp2);

        reference = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();


        imgBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        tvSignUpDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignIn_Page.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gmail = etEmail2.getText().toString();
                String password = etPassword2.getText().toString();

                if (validateUsername() && validateEmail() && validatePassword()) {
                    auth.createUserWithEmailAndPassword(gmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Set the user's display name after successful signup
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    String username = etUsername.getText().toString();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();


                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignUp_Page.this, " Successfully Signed Up", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(SignUp_Page.this, SignIn_Page.class);
                                                intent.putExtra("username",username);
                                                startActivity(intent);

                                            } else {
                                                Toast.makeText(SignUp_Page.this, "Failed to set username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(SignUp_Page.this, "Failed To Sign Up: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    // Email validation method
    private boolean isValidEmail(String email) {
        boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (!isValid) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        }
        return isValid;
    }


    public Boolean validateUsername() {
        String val = etUsername.getText().toString().trim();
        if (val.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            return false;
        } else {
            etUsername.setError(null);
            return true;
        }
    }

    public Boolean validateEmail() {
        String val = etEmail2.getText().toString().trim();
        if (val.isEmpty()) {
            etEmail2.setError("Email cannot be empty");
            return false;
        } else if (!isValidEmail(val)) {
            etEmail2.setError("Please enter a valid email address");
            return false;
        } else {
            etEmail2.setError(null);
            return true;
        }
    }


    public Boolean validatePassword() {
        String val = etPassword2.getText().toString().trim();
        if (val.isEmpty()) {
            etPassword2.setError("Password cannot be empty");
            return false;
        } else if (val.length() < 6) {
            etPassword2.setError("Password must be at least 6 characters");
            return false;
        } else {
            etPassword2.setError(null);
            return true;
        }
    }

}


