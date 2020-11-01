package com.rajeev.stepscounter.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rajeev.stepscounter.R;

public class LoginActivity extends AppCompatActivity {


    private TextInputLayout email_field;
    private TextInputLayout password_field;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;
    private Button loginButton;
    private FirebaseAuth firebaseAuthentication;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuthentication = FirebaseAuth.getInstance();
        currentUser = firebaseAuthentication.getCurrentUser();



        //forgot password action
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setPaintFlags(forgotPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_field.getEditText().getText().toString();
                if (isValidEmail()) {
                    //reset password you will get a mail
                    firebaseAuthentication.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this
                                                , "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please provide an email to reset the password", Toast.LENGTH_SHORT).show();
                }
            }
        });


        email_field = findViewById(R.id.emailTextInputLayout);
        email_field.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verifyEmail();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        password_field = findViewById(R.id.passwordTextInputLayout);
        password_field.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verifyPassword();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //register button action
        registerTextView = findViewById(R.id.registerTextView);
        registerTextView.setPaintFlags(registerTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });


        //login button action
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_field.getEditText().getText().toString().trim();
                String password = password_field.getEditText().getText().toString().trim();
                if (isValid()) {
                    loginUser(email, password);
                }
            }
        });
    }


    /**
     * Checks whether the email is valid or not
     *
     * @return Returns true if the email is valid
     */
    private boolean isValidEmail() {
        String emailInput = email_field.getEditText().getText().toString().trim();
        return !emailInput.isEmpty();
    }


    /**
     * Verifies the email and alerts the user if an issue is found
     */
    private void verifyEmail() {
        String emailInput = email_field.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            email_field.setError("Field can't be empty");
            email_field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else {
            email_field.setError(null);
        }
    }


    /**
     * Verifies the password and alerts the user if an issue is found
     */
    private void verifyPassword() {
        String passwordInput = password_field.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            password_field.setError("Field can't be empty");
            password_field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else {
            password_field.setError(null);
        }
    }


    /**
     * Checks whether the password is valid or not
     *
     * @return Returns true if the password is valid
     */
    private boolean isValidPassword() {
        String passwordInput = password_field.getEditText().getText().toString().trim();
        return !passwordInput.isEmpty();
    }


    /**
     * Checks whether both of the email and the password are valid or not
     *
     * @return Returns whether both of the email and the password are valid or not
     */
    private boolean isValid() {
        return isValidEmail() && isValidPassword();
    }


    /**
     * Verifies the email and password and alerts the user if an issue is found
     */
    private void verify() {
        verifyEmail();
        verifyPassword();
    }


    /**
     * Logs the user in to the account using the firebase api
     *
     * @param email    The user's email address
     * @param password The user's password
     */
    public void loginUser(final String email, final String password) {
        firebaseAuthentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    // saveSharedPreferences(email, password);
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}