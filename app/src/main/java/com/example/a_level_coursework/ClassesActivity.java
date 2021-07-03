package com.example.a_level_coursework;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class ClassesActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    DocumentReference userRef;
    CollectionReference classRef;
    String stxtClassID, userID, stxtClassRoom, stxtClassSubject, school, Colour;
    Context Context;
    LinearLayout mLinearLayout;
    RelativeLayout mRelativeLayout;
    Button btnClass;
    EditText txtClassID, txtClassRoom, txtClassSubject;
    Spinner spinnerColour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classes);

        //retrieves users school ID from intent
        school = getIntent().getStringExtra("school");

        //initialising firebase authentication + firestore + current user ID
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        //initialising firestore references
        userRef = mFirestore
                .collection("users")
                .document(userID);
        classRef = mFirestore
                .collection("schools")
                .document(school)
                .collection("classes");

        //initialises the spinner
        spinnerColour = findViewById(R.id.classesSpinnerColour);
        //creates an adapter using colours array
        ArrayAdapter<CharSequence> colourAdapter = ArrayAdapter.createFromResource(this,
                R.array.colours, android.R.layout.simple_list_item_1);
        colourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerColour.setAdapter(colourAdapter);

        //initialises the interface
        getClasses();
    }

    //initialises users classes
    public void getClasses(){
        //accesses firestore
        classRef
                //runs query to retrieve all of users classes
                .whereEqualTo("userID", userID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        //retrieves the fields for each class
                        String classID = document.getString("classID");
                        String colour = document.getString("colour");
                        //adds the class to the interface
                        createClass(classID, colour);
                    }
                }
            }
        });
    }
    //creates a class by taking in classID
    public void createClass(final String classID, final String colour){
        //initialises context + layout
        Context = getApplicationContext();
        mLinearLayout = findViewById(R.id.classesLL);
        //initialises layout parameters and margin for relative layout
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(80,16,80,16);
        //Initialises a  new relative layout
        mRelativeLayout = new RelativeLayout(Context);
        mRelativeLayout.setLayoutParams(params);

        //Initialises a new button
        btnClass = new Button(Context);
        //btnClass.setLayoutParams(params);
        btnClass.setTextColor(Color.parseColor("#FFFFFF"));
        btnClass.setId(R.id.txtClassID);
        btnClass.setText(classID);
        btnClass.setHeight(256);
        btnClass.setWidth(1250);
        btnClass.setBackgroundColor(Color.parseColor(colour));
        //initialising onClick function
        btnClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finds the delete switch and checks if it is checked
                Switch delete = findViewById(R.id.switchClassDelete);
                //if it is checked, delete class on click
                if (delete.isChecked()){
                    //removes class from "schools" collection
                    classRef
                            .document(classID)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //removes class from "users" collection
                                    removeFromTT(classID);
                                }
                            });
                }else { //if switch is not checked, opens the clicked class's interface
                    //opens the the class interface
                    Intent intent = new Intent(ClassesActivity.this, ClassActivity.class);
                    //transfers the following variables to the new class
                    intent.putExtra("classID", classID);
                    intent.putExtra("school", school);
                    startActivity(intent);
                }
            }
        });
        //adds class to layout
        mRelativeLayout.addView(btnClass);
        mLinearLayout.addView(mRelativeLayout);
    }
    //method is called when deleting a class
    public void removeFromTT(final String classID){
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
        //nested for loop to go through each day of the 2 weeks
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                //initialises reference to the days
                //because it is accessed twice
                final CollectionReference dayRef = userRef
                        .collection("timetable")
                        .document(weeksArray[i])
                        .collection("days")
                        .document(daysArray[j])
                        .collection("lessons");
                //runs query to retrieve all lessons that day
                dayRef
                        .whereEqualTo("classID", classID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()){
                            if(document.exists()){
                                //retrieves the lesson field
                                String lesson = document.getString("lesson");
                                //deletes the lesson document from the collection
                                dayRef
                                        .document(lesson)
                                        .delete();
                            }
                        }
                    }
                });
            }
        }
        //notifies user that class has been deleted
        Toast.makeText(Context, classID + " is successfully deleted", Toast.LENGTH_SHORT).show();
        //refreshes the page to remove the deleted class from interface
        finish();
        startActivity(getIntent());
    }
    //adds new class
    public void onClassAddClick(View view) {
        //retrieves user inputs and converts it to string
        txtClassID = findViewById(R.id.txtClassID);
        stxtClassID = txtClassID.getText().toString();
        txtClassRoom = findViewById(R.id.txtClassesRoom);
        stxtClassRoom = txtClassRoom.getText().toString();
        txtClassSubject = findViewById(R.id.txtClassesSubject);
        stxtClassSubject = txtClassSubject.getText().toString();
        String colour = spinnerColour.getSelectedItem().toString();
        
        if (validation(stxtClassID, stxtClassRoom, stxtClassSubject)){
            //initialising HashMap to add to database
            Map<String, Object> input = new HashMap<>();
            input.put("userID", userID);
            input.put("classID", stxtClassID);
            input.put("room", stxtClassRoom);
            input.put("subject", stxtClassSubject);
            //sets the hex value for the colours as variables and inputs that to firestore
            switch (colour){
                case "Red":
                    String red = "#EC6969";
                    Colour = red;
                    input.put("colour", red);
                    break;
                case "Green":
                    String green = "#76EC87";
                    Colour = green;
                    input.put("colour", green);
                    break;
                case "Blue":
                    String blue = "#69ECDE";
                    Colour = blue;
                    input.put("colour", blue);
                    break;
                case "Orange":
                    String orange = "#FFB37E";
                    Colour = orange;
                    input.put("colour", orange);
                    break;
                case "Purple":
                    String purple = "#C48CFF";
                    Colour = purple;
                    input.put("colour", purple);
                    break;
            }
            //writes to the database
            classRef
                    .document(stxtClassID)
                    .set(input)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //calls the function to display the just created class
                            createClass(stxtClassID, Colour);
                        }
                    });
        }
    }
    //validation for user inputs
    public boolean validation(final String classID,
                              final String room,
                              final String subject){
        //informs user if any of the 3 inputs are empty
        if(classID.isEmpty()){
            txtClassID.setError("Input is empty");
            return false;
        } else if(room.isEmpty()){
            txtClassRoom.setError("Input is empty");
            return false;
        }else if(subject.isEmpty()){
            txtClassSubject.setError("Input is empty");
            return false;
        }
        //returns false if all of the inputs are empty, else returns true
        else if(classID.isEmpty() || room.isEmpty() || subject.isEmpty()){
            Toast.makeText(Context, "Inputs are empty", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }
}
