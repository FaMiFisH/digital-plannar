package com.example.a_level_coursework;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    EditText FirstName, Surname, Email, School, Password, confirmPassword;
    String sFirstName, sSurname, sEmail, sSchool, sPassword, sConfirmPassword, userID;
    DocumentReference userRef;
    //confirmation variable
    private String confirm = "";
    //validation for numeric input
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    //sets the validation standards for passwords
    private static final Pattern passwordPattern =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=/_])" +  //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        //initialises firebase database
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();
        //initialises references
        userRef = mFirestore
                .collection("users")
                .document(userID);
    }

    //checks for number in the string
    //PROBLEM: a number and letter combination still works
    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    //checks if first name is a valid input
    private boolean firstnameValidation() {
        sFirstName = FirstName.getText().toString();
        if (sFirstName.isEmpty()) {
            FirstName.setError("Fields can't be empty");
            return false;
        } else if (isNumeric(sFirstName)) {
            FirstName.setError("Field contains a number");
            return false;
        } else {
            FirstName.setError(null);
            return true;
        }
    }

    //checks if surname is a valid input
    private boolean surnameValidation() {
        sSurname = Surname.getText().toString();
        if (sSurname.isEmpty()) {
            Surname.setError("Fields can't be empty");
            return false;
        } else if (isNumeric(sSurname)) {
            Surname.setError("Field contains a number");
            return false;
        } else {
            Surname.setError(null);
            return true;
        }
    }

    //checks if email is a valid input
    private boolean emailValidation() {
        sEmail = Email.getText().toString();

        if (sEmail.isEmpty()) {
            Email.setError("Fields can't be empty");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            Email.setError("Please enter a valid email");
            return false;
        } else {
            checkEmail(sEmail);
            if(confirm == "true"){
                return false;
            }else {
                return true;
            }
        }
    }

    //checks if school is a valid input
    private boolean schoolValidation() {
        sSchool = School.getText().toString();
        if (sSchool.isEmpty()) {
            School.setError("Fields can't be empty");
            return false;
        } else if (isNumeric(sSchool)) {
            School.setError("Field contains a number");
            return false;
        } else {
            School.setError(null);
            return true;
        }
    }

    //checks if email already exists
    private void checkEmail(final String email){
        mFirebaseAuth
                .fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean check = !task.getResult().getSignInMethods().isEmpty();
                        if(check){
                            Email.setError("Email already exists");
                            confirm = "true";
                        }else {
                            confirm = "false";
                        }
                    }
                });
    }

    //checks if password is valid and strong enough
    private boolean passwordValidation() {
        sPassword = Password.getText().toString();
        if (sPassword.isEmpty()) {
            Password.setError("Fields can't be empty");
            return false;
        }else if (!passwordPattern.matcher(sPassword).matches()) {
            Password.setError("Password too weak");
            return false;
        } else {
            Password.setError(null);
            return true;
        }
    }

    //checks if the passwords match
    private boolean confirmPasswordValidation() {
        sConfirmPassword = confirmPassword.getText().toString();
        if (sConfirmPassword.isEmpty()) {
            confirmPassword.setError("Fields can't be empty");
            return false;
        } else if (!sConfirmPassword.equals(sPassword)) {
            confirmPassword.setError("Passwords do not match");
            return false;
        } else {
            confirmPassword.setError(null);
            return true;
        }
    }

    //runs when users clicks submit button
    public void onSubmitClick(View view) {
        //retrieves user inputs
        Email = findViewById((R.id.regEmail));
        Password = findViewById(R.id.regPassword);
        FirstName = findViewById(R.id.regFirstName);
        Surname = findViewById(R.id.regSurname);
        Password = findViewById(R.id.regPassword);
        confirmPassword = findViewById(R.id.regConfirmPassword);
        School = findViewById(R.id.regSchool);

        if (firstnameValidation()
                && surnameValidation()
                && emailValidation()
                && schoolValidation()
                && passwordValidation()
                && confirmPasswordValidation()) {

            mFirebaseAuth
                    .createUserWithEmailAndPassword(sEmail, sPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration was unsuccessful", Toast.LENGTH_SHORT).show();
                            }else{
                                Map<String,Object> user = new HashMap<>();
                                user.put("first name", sFirstName);
                                user.put("surname", sSurname);
                                user.put("school", sSchool);
                                user.put("email", sEmail);
                                user.put("user_ID", userID);
                                userRef
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this,"Firestore was unsuccessful", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
        }else{
            Toast.makeText(this, "Error Occurred!", Toast.LENGTH_SHORT).show();
        }
    }
}
