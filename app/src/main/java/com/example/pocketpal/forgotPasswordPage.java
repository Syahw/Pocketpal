package com.example.pocketpal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPasswordPage extends AppCompatActivity {

    private EditText editTextEmail;

    private ImageView imgBack;
    private Button buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_page);

        editTextEmail = findViewById(R.id.etEmail3);
        buttonResetPassword = findViewById(R.id.btnForgotPassword);
        imgBack = findViewById(R.id.imgBack3);


        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(forgotPasswordPage.this, SignUp_Page.class);
                startActivity(intent);
            }
        });
    }


    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please Enter Your Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send password reset email to the user
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Password Reset Sent To Email", Toast.LENGTH_LONG).show();
                            finish(); // Finish the activity after sending the email
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed To Sent. Please Check The Email Address", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
