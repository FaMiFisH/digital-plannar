package com.example.a_level_coursework;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TimetableActivity extends AppCompatActivity {
    DocumentReference userRef;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    LinearLayout weekALL, dayLL, weekBLL;
    Switch homeSwitch, editSwitch;
    String userID, school;
    Context txtViewContext;
    Button btnLesson;
    TextView txtDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);

        //initialises firestore
        mFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        //initialise firestore references
        userRef = mFirestore.collection("users").document(userID);

        //retrieve intents
        school = getIntent().getStringExtra("school");

        homeSwitch = findViewById(R.id.homeSwitch);
        homeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    Intent intent = new Intent(TimetableActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        display();
    }

    //initialises user's display
    public void display(){
        //initialise arrays for days of the week + the 2 weeks
        final String[] daysArray = {
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday"
        };
        final String[] weeksArray = {
                "Week A",
                "Week B"
        };
        final String[] lessonsArray = {
                "lesson 1",
                "lesson 2",
                "lesson 3",
                "lesson 4",
                "lesson 5"
        };

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        weekALL = findViewById(R.id.timetableWeekALL);
        weekBLL = findViewById(R.id.timetableWeekBLL);

        //loop for each week
        for (int i = 0; i < 2; i++) {
            //loop for each day
            for (int j = 0; j < 5; j++) {
                dayLL = new LinearLayout(this);
                dayLL.setOrientation(LinearLayout.VERTICAL);
                dayLL.setGravity(1);
                txtViewContext = getApplicationContext();
                txtDay = new TextView(txtViewContext);
                txtDay.setLayoutParams(params);
                txtDay.setText(daysArray[j]);
                dayLL.addView(txtDay);
                //loop for each lesson in the day
                for (int k = 0; k < 5; k++) {
                    createLesson(i, j, k, weeksArray, daysArray, lessonsArray);
                    setText(i, j, k, weeksArray, daysArray, lessonsArray, btnLesson);
                    //Log.d(TAG, "Finished creating " + lessonsArray[k] );
                }
                if (i == 0){
                    weekALL.addView(dayLL);
                }if(i == 1){
                    weekBLL.addView(dayLL);
                }
            }
        }
    }

    //creates user's buttons for lessons
    public  void createLesson(final int i,
                              final int j,
                              final int k,
                              final String[] weeksArray,
                              final String[] daysArray,
                              final String[] lessonsArray){

        Context Context = getApplicationContext();

        //initialises layout parameters and margin for relative layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4,4,4,4);
        //Initialises a  new relative layout
        RelativeLayout mRelativeLayout = new RelativeLayout(Context);
        mRelativeLayout.setLayoutParams(params);

        //Initialising a button
        btnLesson = new Button(Context);
        btnLesson.setId(100*i + 10*j + k);
        btnLesson.setWidth(400);
        btnLesson.setHeight(25);
        btnLesson.setTextColor(Color.parseColor("#000000"));
        btnLesson.setText(getResources().getString(R.string.Free));
        btnLesson.setBackgroundColor(Color.parseColor("#FFFFFF"));

        //initialises the values for the button
        final String week = weeksArray[i];
        final String day = daysArray[j];
        final String lesson = lessonsArray[k];
        //initialising onClick function
        btnLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSwitch = findViewById(R.id.editSwitch);
                if (editSwitch.isChecked()) {
                    Intent intent = new Intent(TimetableActivity.this, ChooseClassActivity.class);
                    intent.putExtra("week", week);
                    intent.putExtra("day", day);
                    intent.putExtra("lesson", lesson);
                    startActivity(intent);
                } else {
                    mFirestore
                            .collection("users")
                            .document(userID)
                            .collection("timetable")
                            .document(week)
                            .collection("days")
                            .document(day)
                            .collection("lessons")
                            .document(lesson)
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                //retrieves classID
                                String classID = documentSnapshot.getString("classID");
                                //initialises intent
                                Intent intent = new Intent(TimetableActivity.this, ClassActivity.class);
                                intent.putExtra("classID", classID);
                                intent.putExtra("school", school);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
        mRelativeLayout.addView(btnLesson);
        dayLL.addView(mRelativeLayout);
    }

    //sets text to the buttons
    public void setText(final int i,
                        final int j,
                        final int k,
                        final String[] weeksArray,
                        final String[] daysArray,
                        final String[] lessonsArray,
                        final Button btnLesson){
        mFirestore
                .collection("users")
                .document(userID)
                .collection("timetable")
                .document(weeksArray[i])
                .collection("days")
                .document(daysArray[j])
                .collection("lessons")
                .whereEqualTo("lesson", lessonsArray[k])
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.exists()){
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

    //runs when user clicks the menu button
    public void onTimetableMenuClick(View view){
        userRef
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Intent intent = new Intent(TimetableActivity.this, MenuActivity.class);
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

    //runes when user clicks the notes button
    public void onTimetableNotesClick(View view){
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }
}
