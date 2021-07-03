package com.example.a_level_coursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    TextView txtViewFirstName, txtViewSurname, txtViewEmail, txtViewSchool;
    String userID, school;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        //initiates the profile variables
        txtViewFirstName = findViewById(R.id.txtViewFirstName);
        txtViewSurname = findViewById(R.id.txtViewSurname);
        txtViewEmail = findViewById(R.id.txtViewEmail);
        txtViewSchool = findViewById(R.id.txtViewSchool);
        //sets the profile for the user
        txtViewFirstName.setText(getIntent().getStringExtra("firstName"));
        txtViewSurname.setText(getIntent().getStringExtra("surname"));
        txtViewEmail.setText(getIntent().getStringExtra("email"));
        school = getIntent().getStringExtra("school");
        txtViewSchool.setText(school);

        //initialises firebase authentication and retrieves current users ID
        mFirebaseAuth = FirebaseAuth.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();
    }

    //opens main interface
    public void onHomeClick(View view){
        startActivity(new Intent(this, MainActivity.class));
    }

    //opens classes interface
    public void onClassesClick(View view){
        Intent intent = new Intent(this, ClassesActivity.class);
        intent.putExtra("school", school);
        startActivity(intent);
    }

    //Logs user out of the app
    public void onLogoutClick(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}
