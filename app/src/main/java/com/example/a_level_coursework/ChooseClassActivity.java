package com.example.a_level_coursework;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

import java.util.HashMap;
import java.util.Map;

public class ChooseClassActivity extends AppCompatActivity {
    private static final String TAG = "ChooseClassActivity";
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    DocumentReference userRef;
    LinearLayout mLinearLayout;
    String userID, school, week, day, lesson;
    Context Context;
    Button btnClass;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_class);

        //initialising database
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        //initialise references
        userRef = mFirestore.collection("users").document(userID);

        //retrieving intents
        week = getIntent().getStringExtra("week");
        day = getIntent().getStringExtra("day");
        lesson = getIntent().getStringExtra("lesson");

        getClasses();
    }

    //method to get users classes
    public void getClasses(){
        userRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        school = document.getString("school");
                        //runs query to retrieve users school
                        mFirestore
                                .collection("schools")
                                .document(school)
                                .collection("classes")
                                .whereEqualTo("userID", userID)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot document : task.getResult()){
                                        String classID = document.getString("classID");
                                        String colour = document.getString("colour");
                                        createClass(classID, colour);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void createClass(final String classID, final String colour){
        //initialising the linear layout
        Context = getApplicationContext();
        mLinearLayout = findViewById(R.id.chooseClassLL);
        //initialising layout parameters and margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(128,16,128,16);

        //Initialising a button
        btnClass = new Button(Context);
        btnClass.setLayoutParams(params);
        btnClass.setTextColor(Color.parseColor("#FFFFFF"));
        btnClass.setId(R.id.txtClassID);
        btnClass.setText(classID);
        btnClass.setBackgroundColor(Color.parseColor(colour));
        //initialising onClick function
        btnClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //writes the class for that lesson to firebase
                Map<String, Object> input = new HashMap<>();
                input.put("classID", classID);
                input.put("user_ID", userID);
                input.put("colour", colour);
                input.put("lesson", lesson);
                mFirestore
                        .collection("users")
                        .document(userID)
                        .collection("timetable")
                        .document(week)
                        .collection("days")
                        .document(day)
                        .collection("lessons")
                        .document(lesson)
                        .set(input)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirestore
                                .collection("users")
                                .document(userID)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    String school = documentSnapshot.getString("school");
                                    Intent intent = new Intent(ChooseClassActivity.this, TimetableActivity.class);
                                    intent.putExtra("school", school);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                });
            }
        });
        mLinearLayout.addView(btnClass);
    }
}
