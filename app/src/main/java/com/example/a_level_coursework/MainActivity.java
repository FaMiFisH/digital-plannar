package com.example.a_level_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirestore;
    DocumentReference userRef;
    Calendar calendar;
    String userID, school;
    Switch timetableSwitch;
    TextView week, Day, changeWeek;
    LinearLayout mLinearLayout;
    Button btnLesson, btnReg;
    //makes the "School" variable global within MainActivity class
    String School = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialises current day and week
        initialiseDayWeek();
        //initialises firebase and firestore
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        userID = mFirebaseAuth.getCurrentUser().getUid();
        userRef = mFirestore.collection("users").document(userID);

        //initialises current users school
        userRef .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            //retrieves the "school" field
                            school = documentSnapshot.getString("school");
                            //this makes the value accessible throughout the class
                            School = school;
                        }
                    }
                });


        //initialises the timetable switch
        timetableSwitch = findViewById(R.id.timetableSwitch);
        //initialises on click listener for the switch
        timetableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //runs when switch is clicked
                if(isChecked){
                    //initialises intent to change class
                    Intent intent = new Intent(MainActivity.this, TimetableActivity.class);
                    //transfers the variable to the TimetableActivity class
                    intent.putExtra("school", School);
                    startActivity(intent);
                }
            }
        });
        //initialises the interface
        display();

    }

    public void initialiseDayWeek(){
        calendar = Calendar.getInstance();
        //initialises the week
        week = findViewById(R.id.ttAB);
        int weeks = calendar.get(Calendar.WEEK_OF_YEAR);
        if (weeks % 2 == 0){
            week.setText("Week A");
        }else{
            week.setText("Week B");
        }
        //initialises day of the week
        Day = findViewById(R.id.ttDay);
        String[] days = {"Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String day = "";
        int day_of_the_week = calendar.get(Calendar.DAY_OF_WEEK);
        for (int i = 1; i < 8; i++) {
            if (day_of_the_week == i){
                //if the days is either Saturday or Sunday, it initialises the display for the following monday
                if(i == 1 || i == 7){
                    //sets day to monday
                    day = days[1];
                    Day.setText(day);
                    //changes the week to the next week
                    //retrieves the current week and converts it to string
                    changeWeek = findViewById(R.id.ttAB);
                    String sChangeWeek = changeWeek.getText().toString();
                    //changes the week to the next one
                    if (sChangeWeek == "Week A"){
                        changeWeek.setText("Week B");
                    }if (sChangeWeek == "Week B"){
                        changeWeek.setText("Week A");
                    }
                }else{ //if not, it initialises the current days display
                    day = days[i-1];
                    Day.setText(day);
                }
            }
        }
    }

    public void display(){
        //initialising current day and week
        TextView Week = findViewById(R.id.ttAB);
        String sWeek = Week.getText().toString();
        TextView Day = findViewById(R.id.ttDay);
        String sDay = Day.getText().toString();
        //initialising linear layout
        mLinearLayout = findViewById(R.id.timetableMainLL);
        //initialising strings for AM/PM buttons
        String AM = "AM";
        String PM = "PM";
        //creating AM button
        createReg(AM);
        //creating buttons for the 5 lessons
        for (int i = 1; i < 6; i++) {
            //creates buttons with "Free"
            createLesson(sWeek, sDay, i);
            //checks if user has set a lesson at that hour
            setText(sWeek, sDay, i, btnLesson);
        }
        //creating PM reg
        createReg(PM);
    }

    public void createReg(final String reg){
        //initialises context
        Context Context = getApplicationContext();

        //initialises layout parameters and margin for relative layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(40,12,40,12);
        //Initialises a  new relative layout
        RelativeLayout mRelativeLayout = new RelativeLayout(Context);
        mRelativeLayout.setLayoutParams(params);

        btnReg = new Button(Context);
        btnReg.setWidth(1000);
        btnReg.setHeight(170);
        btnReg.setTextColor(Color.parseColor("#000000"));
        btnReg.setText(reg);
        btnReg.setBackgroundColor(Color.parseColor("#FFFFFF"));
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegActivity.class);
                intent.putExtra("reg", reg);
                startActivity(intent);
            }
        });
        mRelativeLayout.addView(btnReg);
        mLinearLayout.addView(mRelativeLayout);
    }

    public void createLesson(final String week,
                             final String day,
                             final int i){
        //initialises context
        Context Context = getApplicationContext();
        //initialises layout parameters and margin for relative layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,12,0,12);
        //Initialises a  new relative layout
        RelativeLayout mRelativeLayout = new RelativeLayout(Context);
        mRelativeLayout.setLayoutParams(params);
        //initialising the button
        btnLesson = new Button(Context);
        btnLesson.setWidth(1000);
        btnLesson.setHeight(170);
        btnLesson.setTextColor(Color.parseColor("#000000"));
        btnLesson.setText(getResources().getString(R.string.Free));
        btnLesson.setBackgroundColor(Color.parseColor("#FFFFFF"));
        btnLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirestore
                        .collection("users")
                        .document(userID)
                        .collection("timetable")
                        .document(week)
                        .collection("days")
                        .document(day)
                        .collection("lessons")
                        .document("lesson "+i)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //retrieves classID
                            String classID = documentSnapshot.getString("classID");
                            //initialises intent
                            Intent intent = new Intent(MainActivity.this, ClassActivity.class);
                            intent.putExtra("classID", classID);
                            intent.putExtra("school", School);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        mRelativeLayout.addView(btnLesson);
        mLinearLayout.addView(mRelativeLayout);
    }

    public void setText(final String week,
                        final String day,
                        final int i,
                        final Button btnLesson){
        int lesson = i;
        mFirestore
                .collection("users")
                .document(userID)
                .collection("timetable")
                .document(week)
                .collection("days")
                .document(day)
                .collection("lessons")
                .whereEqualTo("lesson", ("lesson " + lesson))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()){
                    if(document.exists()){
                        //String classID = document.getId();
                        String text = document.getString("classID");
                        String colour = document.getString("colour");
                        btnLesson.setText(text);
                        btnLesson.setTextColor(Color.parseColor("#FFFFFF"));
                        btnLesson.setBackgroundColor(Color.parseColor(colour));
                    }
                }
            }
        });
    }


    public void onMainNotesClick(View view){
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }

    public void onMenuClick(View view){
        userRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        intent.putExtra("firstName", document.getString("first name"));
                        intent.putExtra("surname", document.getString("surname"));
                        intent.putExtra("email", document.getString("email"));
                        intent.putExtra("school", document.getString("school"));
                        startActivity(intent);
                    }
                }
            }
        });
    }

}
