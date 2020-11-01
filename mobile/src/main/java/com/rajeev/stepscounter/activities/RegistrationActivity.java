package com.rajeev.stepscounter.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rajeev.stepscounter.R;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[0-9A-Za-z\\s-_]+$");


    //https://blog.mailtrap.io/java-email-validation/


    private TextInputLayout email_field;
    private TextInputLayout password_field;
    private TextInputLayout nickname_field;
    private TextView loginTextView;
    private Button registerButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuthentication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        firebaseAuthentication = FirebaseAuth.getInstance();


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


        nickname_field = findViewById(R.id.nickNameTextInputLayout);
        nickname_field.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                verifyNickName();
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

        //switch to the login tab
        loginTextView = findViewById(R.id.loginTextView);
        loginTextView.setPaintFlags(loginTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //register action
        registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValid()) {
                    Toast.makeText(RegistrationActivity.this, "Please recheck your input", Toast.LENGTH_LONG).show();
                    verify();
                } else {
                    String email = email_field.getEditText().getText().toString().trim();
                    String password = password_field.getEditText().getText().toString().trim();
                    String nickName = nickname_field.getEditText().getText().toString().trim();
                    registerUser(email, password, nickName);
                }
            }
        });
    }


    /**
     * Checks whether the email is a valid email by a regular expression and that the field is not empty
     *
     * @return Returns true if the email follows our rules, otherwise false
     */
    private boolean isValidEmail() {
        String emailInput = email_field.getEditText().getText().toString().trim();
        return !emailInput.isEmpty() && EMAIL_PATTERN.matcher(emailInput).matches();
    }


    /**
     * Checks whether the email is a valid email by a regular expression and that the field is not empty, and updates the user based on that
     */
    private void verifyEmail() {
        String emailInput = email_field.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            email_field.setError("Field can't be empty");
            email_field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else if (!EMAIL_PATTERN.matcher(emailInput).matches()) {
            email_field.setError("Please enter a valid email");
        } else {
            email_field.setError(null);
        }
    }


    /**
     * Checks whether the nick name is a valid nick name by a regular expression and that the field is not empty
     *
     * @return Returns true if the nick name follows our rules, otherwise false
     */
    private boolean isValidNickName() {
        String nickNameInput = nickname_field.getEditText().getText().toString().trim();
        Log.d("Debug", nickNameInput.isEmpty() + ", " + NICKNAME_PATTERN.matcher(nickNameInput).matches());
        return !nickNameInput.isEmpty() && NICKNAME_PATTERN.matcher(nickNameInput).matches();
    }


    /**
     * Checks whether the nick name is a valid nick name by a regular expression and that the field is not empty, and updates the user based on that
     */
    private void verifyNickName() {
        String nickNameInput = nickname_field.getEditText().getText().toString().trim();
        if (nickNameInput.isEmpty()) {
            nickname_field.setError("Field can't be empty");
            nickname_field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else if (!NICKNAME_PATTERN.matcher(nickNameInput).matches()) {
            nickname_field.setError("Please enter a valid nickname");
        } else {
            nickname_field.setError(null);
        }
    }


    /**
     * Checks whether the password is a valid password by a regular expression and that the field is not empty
     *
     * @return Returns true if the password follows our rules, otherwise false
     */
    private boolean isValidPassword() {
        String passwordInput = password_field.getEditText().getText().toString().trim();
        return !passwordInput.isEmpty() && PASSWORD_PATTERN.matcher(passwordInput).matches();
    }


    /**
     * Checks whether the password is a valid password by a regular expression and that the field is not empty, and updates the user based on that
     */

    private void verifyPassword() {
        String passwordInput = password_field.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            password_field.setError("Field can't be empty");
            password_field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            password_field.setError("Password too weak");
        } else {
            password_field.setError(null);
        }
    }

    /**
     * Checks whether the fields are valid following their own regular expressions and that the fields are not empty
     *
     * @return Returns whether the fields can be processed or no
     */
    private boolean isValid() {
        return isValidEmail() && isValidPassword() && isValidNickName();
    }


    /**
     * Verifies the fields making sure they follow with their regular expression and that the fields are not empty and updates the user based on that
     */
    private void verify() {
        verifyEmail();
        verifyPassword();
        verifyNickName();
    }


    /**
     * Registers a user
     *
     * @param email    The user's email
     * @param password The user's password
     * @param nickName The user's nick name
     */
    public void registerUser(final String email, final String password, final String nickName) {
        firebaseAuthentication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //if the user could be registered sussessfully
                if (task.isSuccessful()) {
                    FirebaseUser current = firebaseAuthentication.getCurrentUser();
                    String uid = current.getUid();

                    //create username and add steps to the database
                    databaseReference.child(uid).child("name").setValue(nickName);

                    //send a message and go to the login page
                    Toast.makeText(RegistrationActivity.this, "Successfully created an account!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);

                    //failed to register user
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(RegistrationActivity.this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Unknown error has occurred, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}