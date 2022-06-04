package com.example.hodophile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView goToSignUp;
    private FirebaseAuth myFirebaseAuth;
    private FirebaseAuth.AuthStateListener myAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        myFirebaseAuth = FirebaseAuth.getInstance();
        loginButton = findViewById(R.id.LoginButton);
        loginEmail = findViewById(R.id.LoginEmail);
        loginPassword = findViewById(R.id.LoginPassword);
        goToSignUp = findViewById(R.id.goToSignUp);

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String pwd = loginPassword.getText().toString();
            if (email.isEmpty() && pwd.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Fields are empty!!",
                        Toast.LENGTH_SHORT).show();
            } else if (pwd.isEmpty()) {
                loginPassword.setError("Please enter your password");
                loginPassword.requestFocus();
            } else if (email.isEmpty()) {
                loginEmail.setError("Please enter your email");
                loginEmail.requestFocus();
            } else {
                myFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(
                        LoginActivity.this, task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login error. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login successful",
                                        Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(LoginActivity.this, DrawerActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
            }
        });

        goToSignUp.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, com.example.hodophile.SignUpActivity.class);
            startActivity(i);
        });

        myAuthStateListener = firebaseAuth -> {
            FirebaseUser mFirebaseUser = myFirebaseAuth.getCurrentUser();
            if (mFirebaseUser != null) {
                Intent i = new Intent(LoginActivity.this, DrawerActivity.class);
                startActivity(i);
                finish();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        myFirebaseAuth.addAuthStateListener(myAuthStateListener);
    }
}