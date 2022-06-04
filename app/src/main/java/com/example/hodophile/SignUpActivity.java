package com.example.hodophile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText signUpEmail, signUpPassword, signUpUsername;
    private Button signUpButton;
    private ImageButton backToLoginButton;
    private FirebaseAuth myFirebaseAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        signUpEmail = findViewById(R.id.SignUpEmail);
        signUpPassword = findViewById(R.id.SignUpPassword);
        signUpUsername = findViewById(R.id.SignUpUsername);
        signUpButton = findViewById(R.id.SignUpButton);
        myFirebaseAuth = FirebaseAuth.getInstance();
        backToLoginButton = findViewById(R.id.BackToLoginButton);

        signUpButton.setOnClickListener(v -> {
            String email = signUpEmail.getText().toString();
            String pwd = signUpPassword.getText().toString();
            String name = signUpUsername.getText().toString();
            if (email.isEmpty() && pwd.isEmpty() && name.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Fields are empty!!",
                        Toast.LENGTH_SHORT).show();
            } else if (name.isEmpty()) {
                signUpUsername.setError("Please enter your username");
                signUpUsername.requestFocus();
            } else if (email.isEmpty()) {
                signUpEmail.setError("Please enter your email");
                signUpEmail.requestFocus();
            } else if (pwd.isEmpty()) {
                signUpPassword.setError("Please enter your password");
                signUpPassword.requestFocus();
            } else if (pwd.length() < 6) {
                signUpPassword.setError("Password must be at least 6 characters");
                signUpPassword.requestFocus();
            } else {
                myFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(
                        SignUpActivity.this, task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Sign up failed",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                database = FirebaseDatabase
                                        .getInstance(getString(R.string.database_link))
                                        .getReference("Profiles")
                                        .child(myFirebaseAuth.getCurrentUser().getUid());
                                database.child("username").setValue(name).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Sign up successful",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        });
            }
        });

        backToLoginButton.setOnClickListener(v -> {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
        });
    }
}