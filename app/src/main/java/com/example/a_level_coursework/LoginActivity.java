package com.example.a_level_coursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // initialising variables
    private FirebaseAuth mFirebaseAuth;
    private EditText emailID, password, checkboxPassword;
    private CheckBox showPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // initialising database authentication instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        if(mFirebaseAuth.getCurrentUser() != null){     //if user is already logged in
            startActivity(new Intent(this, MainActivity.class));
        }

        //makes password (un)hidden
        showPassword = findViewById(R.id.loginShowPassword);
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkboxPassword = findViewById(R.id.loginPassword);
                if(showPassword.isChecked()){
                    checkboxPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }else{
                    checkboxPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    /* checks to see if inputted email is valid */
    private boolean emailValidation(String sEmail){
        if (sEmail.isEmpty()){
            emailID.setError("Fields can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()){
            emailID.setError("Please enter a valid email");
            return false;
        } else {
            emailID.setError(null);
            return true;
        }
    }

    /* checks if inputted password is valid */
    private boolean passwordValidation(String sPassword) {
        if (sPassword.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    /* logs user in if inputs are valid and correct */
    public void onLoginClick(View view) {
        //retrieves user inputs and converts them into string
        emailID = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        String sEmail = emailID.getText().toString();
        String sPassword = password.getText().toString();

        if(emailValidation(sEmail) && passwordValidation(sPassword)){      //validity check
            mFirebaseAuth.signInWithEmailAndPassword(sEmail, sPassword)     //correctness check
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Login was unsuccessful", Toast.LENGTH_SHORT).show();
                            }else{
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                        }
                    });
        }
    }

    /* loads register page */
    public void onRegisterClick (View view){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

}