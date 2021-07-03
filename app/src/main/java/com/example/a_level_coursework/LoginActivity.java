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
    FirebaseAuth mFirebaseAuth;
    EditText emailID, password, checkboxPassword;
    CheckBox showPassword;
    String sEmail, sPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //initialises firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        //logs user in if user has already logged in before
        if(mFirebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(this, MainActivity.class));
        }

        //listener to respond when checkbox is checked
        showPassword = findViewById(R.id.loginShowPassword);
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //if it is checked, make the password visible
                checkboxPassword = findViewById(R.id.loginPassword);
                if(showPassword.isChecked()){
                    checkboxPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }else{ //if not, make it disclosed
                    checkboxPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    //checks to see if inputted email is valid
    private boolean emailValidation(){
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

    //checks if inputted password is valid
    private boolean passwordValidation() {
        if (sPassword.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    //logs user in if inputs are valid and correct
    public void onLoginClick(View view) {
        //retrieves user inputs and converts them into string
        emailID = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        sEmail = emailID.getText().toString();
        sPassword = password.getText().toString();
        //confirms if inputs are valid or invalid
        if (emailValidation() && passwordValidation()){
            //checks if inputs are correct
            mFirebaseAuth.signInWithEmailAndPassword(sEmail, sPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                //informs the user if login was unsuccessful
                                Toast.makeText(LoginActivity.this, "Login was unsuccessful", Toast.LENGTH_SHORT).show();
                            }else{
                                //if successful, logs user in
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            }
                        }
                    });
        }
    }

    //opens the register screen
    public void onRegisterClick (View view){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

}