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

    //initialising variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirestore;
    private EditText firstName, surname, email, school, password, confirmPassword;
    private boolean emailCheck = true;
    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
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

    //initialising key names for the HashMap
    private final String usersCollection = "users";
    private final String usersUserID = "user_ID";
    private final String usersFirstName = "first name";
    private final String usersSurname = "surname";
    private final String usersEmail = "email";
    private final String usersSchool = "school";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        //initialising database instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    /* check for numbers in the string */
    //PROBLEM: a number and letter combination still works
    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    /* checks if firstName is a valid input */
    private boolean firstnameValidation(String sFirstName) {
        if (sFirstName.isEmpty()) {
            firstName.setError("Field can not be empty");
            return false;
        } else if (isNumeric(sFirstName)) {
            firstName.setError("Field contains a number");
            return false;
        } else {
            firstName.setError(null);
            return true;
        }
    }

    /* checks if surname is a valid input */
    private boolean surnameValidation(String sSurname) {
        if (sSurname.isEmpty()) {
            surname.setError("Field can not be empty");
            return false;
        } else if (isNumeric(sSurname)) {
            surname.setError("Field contains a number");
            return false;
        } else {
            surname.setError(null);
            return true;
        }
    }

    //checks if email is a valid input
    private boolean emailValidation(String sEmail){
        if (sEmail.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(sEmail).matches()) {
            email.setError("Please enter a valid email");
            return false;
        } else {
            checkEmail(sEmail);
            return !emailCheck;
        }
    }

    /* checks if email already exists */
    private void checkEmail(String sEmail){
        mFirebaseAuth
                .fetchSignInMethodsForEmail(sEmail)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean check = !task.getResult().getSignInMethods().isEmpty();
                        if(check){
                            email.setError("Email already exists");
                            emailCheck = true;
                        }else {
                            emailCheck = false;
                        }
                    }
                });
    }

    //checks if school is a valid input
    private boolean schoolValidation(String sSchool) {
        if (sSchool.isEmpty()) {
            school.setError("Field can not be empty");
            return false;
        } else if (isNumeric(sSchool)) {
            school.setError("Field contains a number");
            return false;
        } else {
            school.setError(null);
            return true;
        }
    }

    /* checks if password is valid and strong enough */
    private boolean passwordValidation(String sPassword) {
        if (sPassword.isEmpty()) {
            password.setError("Field can not be empty");
            return false;
        }else if (!passwordPattern.matcher(sPassword).matches()) {
            password.setError("Password is too weak");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    /* checks if the passwords match */
    private boolean confirmPasswordValidation(String sPassword, String sConfirmPassword) {
        if (sConfirmPassword.isEmpty()) {
            confirmPassword.setError("Field can not be empty");
            return false;
        } else if (!sConfirmPassword.equals(sPassword)) {
            confirmPassword.setError("Passwords do not match");
            return false;
        } else {
            confirmPassword.setError(null);
            return true;
        }
    }

    /* executes the submit */
    public void onSubmitClick(View view) {

        //retrieves user inputs
        email = findViewById((R.id.regEmail));
        firstName = findViewById(R.id.regFirstName);
        surname = findViewById(R.id.regSurname);
        password = findViewById(R.id.regPassword);
        confirmPassword = findViewById(R.id.regConfirmPassword);
        school = findViewById(R.id.regSchool);

        //converts user inputs to strings
        String sEmail = email.getText().toString();
        String sPassword = password.getText().toString();
        String sFirstName = firstName.getText().toString();
        String sSurname = surname.getText().toString();
        String sConfirmPassword = confirmPassword.getText().toString();
        String sSchool = school.getText().toString();

        if (firstnameValidation(sFirstName)
                && surnameValidation(sSurname)
                && emailValidation(sEmail)
                && schoolValidation(sSchool)
                && passwordValidation(sPassword)
                && confirmPasswordValidation(sPassword, sConfirmPassword)) {
            mFirebaseAuth
                    .createUserWithEmailAndPassword(sEmail, sPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration was unsuccessful", Toast.LENGTH_SHORT).show();
                            }else{
                                String userID = mFirebaseAuth.getCurrentUser().getUid();
                                //initialises database reference
                                DocumentReference userRef = mFirestore
                                        .collection(usersCollection)
                                        .document(userID);

                                Map<String,Object> user = new HashMap<>();
                                user.put(usersFirstName, sFirstName);
                                user.put(usersSurname, sSurname);
                                user.put(usersSchool, sSchool);
                                user.put(usersEmail, sEmail);
                                user.put(usersUserID, userID);
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
