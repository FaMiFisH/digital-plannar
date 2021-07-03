package com.example.a_level_coursework;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class ClassActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    DocumentReference userRef, classRef;
    EditText txtStudent, classNotes;
    LinearLayout studentsLL;
    String userID, stxtStudent, classID, school, notes;
    Context context;
    Button btnStudent;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_);

        //initialising firebase authentication + firestore + current user ID
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        //gets intents
        classID = getIntent().getStringExtra("classID");
        school = getIntent().getStringExtra("school");

        //initialises toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(classID);

        //initialise firestore references
        userRef = mFirestore
                .collection("users")
                .document(userID);
        classRef = mFirestore
                .collection("schools")
                .document(school)
                .collection("classes")
                .document(classID);

        //initialises the interface
        getNotes();
        getStudents();
    }

    //retrieves saved notes from firestore
    public void getNotes(){
        //accessing firestore
        classRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    //retrieves saved notes
                    notes = documentSnapshot.getString("notes");
                    //displays the notes
                    classNotes = findViewById(R.id.classNotes);
                    classNotes.setText(notes);
                }
            }
        });
    }

    //retrieves students from firestore
    public void getStudents(){
        //accessing firestore
        classRef
                .collection("students")
                //running a query to retrieve all students in the class
                .whereEqualTo("userID", userID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        //retrieves students individually
                        String studentName = document.getString("name");
                        //displays the students
                        createStudent(studentName);
                    }
                }
            }
        });
    }

    //adds students to the interface
    public void createStudent(final String studentName){
        //initialising context and layout
        context = getApplicationContext();
        studentsLL = findViewById(R.id.LLclass_students);
        //initialises layout parameters and margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        //params.setMargins(16,0,16,0);
        //creates a new button
        btnStudent = new Button(context);
        btnStudent.setLayoutParams(params);
        btnStudent.setTextColor(Color.parseColor("#000000"));
        btnStudent.setId(R.id.txtClass_AddStudent);
        btnStudent.setText(studentName);
        btnStudent.setBackgroundColor(Color.TRANSPARENT);
        //initialising onClick function
        btnStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieves the switch and checks if it is checked
                Switch delete = findViewById(R.id.studentDelete);
                //if it is checked, removes student from firestore and interface
                if(delete.isChecked()){
                    classRef
                            .collection("students")
                            .document(studentName)
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //notifies user that student has been successfully removed
                            Toast.makeText(context, studentName + " successfully removed", Toast.LENGTH_SHORT).show();
                            //refreshes the page to remove the delete student from interface
                            finish();
                            startActivity(getIntent());
                        }
                    });
                }else { //if its not checked, open the students interface
                    //initialises intent to open students interface
                    Intent intent = new Intent(ClassActivity.this, StudentActivity.class);
                    //transfers the following variables to the students activity class
                    intent.putExtra("classID", classID);
                    intent.putExtra("studentName", studentName);
                    intent.putExtra("school", school);
                    startActivity(intent);
                }
            }
        });
        //adds button to the layout
        studentsLL.addView(btnStudent);
    }

    //adds student to firestore
    public void writeStudent(final String studentName){
        //initialising HashMap to add to firestore
        Map<String, Object> input = new HashMap<>();
        input.put("userID", userID);
        input.put("name", studentName);
        input.put("homework", false);
        input.put("equipment", false);
        input.put("notes", "");
        //writes to firestore
        classRef
                .collection("students")
                .document(studentName)
                .set(input)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //adds student to the interface
                        createStudent(studentName);
                    }
                });
    }

    //runs when user clicks add button
    public void onStudentAddClick(View view){
        //retrieving inputted student name and converting it to string
        txtStudent = findViewById(R.id.txtClass_AddStudent);
        stxtStudent = txtStudent.getText().toString();
        if(validation(stxtStudent)){
            //adds student to firestore
            writeStudent(stxtStudent);
            //notifies user that student has successfully been added
            Toast.makeText(context, stxtStudent + " successfully added", Toast.LENGTH_SHORT).show();
        }else{
            txtStudent.setError("Empty input");
        }
    }

    //runs when user clicks save button
    public void onClassNoteSaveClick(View view){
        //retrieves the notes text and converts it to string
        EditText newClassNotes = findViewById(R.id.classNotes);
        String sNewClassNotes = newClassNotes.getText().toString();
        //updating firestore with the new notes
        //can't write since it will overwrite the whole student document
        //so must update
        classRef
                .update("notes", sNewClassNotes)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Notes saved", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Notes were not saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //validation for user inout
    public boolean validation(final String input){
        if (input.isEmpty()){
            return false;
        }else{
            return true;
        }
    }
}
