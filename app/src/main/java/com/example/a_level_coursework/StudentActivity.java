package com.example.a_level_coursework;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class StudentActivity extends AppCompatActivity {
    FirebaseFirestore mFirestore;
    FirebaseAuth mFirebaseAuth;
    DocumentReference userRef, studentRef;
    String userID, classID, studentName, notes, sNewNotes, school;
    CheckBox checkboxHomework, checkboxEquipment;
    EditText studentNote, newNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student);

        //initialising firebase authentication + firestore + current user ID
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();

        //retrieves strings from intent
        classID = getIntent().getStringExtra("classID");
        studentName = getIntent().getStringExtra("studentName");
        school = getIntent().getStringExtra("school");

        //initialise firestore references
        userRef = mFirestore
                .collection("users")
                .document(userID);
        studentRef = mFirestore
                .collection("schools")
                .document(school)
                .collection("classes")
                .document(classID)
                .collection("students")
                .document(studentName);

        //initialise checkboxes + notes
        checkboxHomework = findViewById(R.id.checkboxHomework);
        checkboxEquipment = findViewById(R.id.checkboxEquipment);
        studentNote = findViewById(R.id.studentNotes);

        //fills the interface
        setInterface();
    }
    //initialises the interface
    public void setInterface(){
        //displays clicked students name
        TextView txtStudenName = findViewById(R.id.txtStudentName);
        txtStudenName.setText(studentName);
        //accesses firestore
        studentRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                //retrieves boolean values to see if any of the checkboxes are already checked
                Boolean homework = document.getBoolean("homework");
                Boolean equipment = document.getBoolean("equipment");
                //ticks the check boxes of boolean values are true
                if(homework){
                    checkboxHomework.setChecked(true);
                }
                if (equipment){
                    checkboxEquipment.setChecked(true);
                }
                //retrieves saved notes + displays them
                notes = document.getString("notes");
                studentNote.setText(notes);
            }
        });
    }
    //runs when user clicks homework checkbox
    public void onCheckboxHomeworkClick(View view){
        //updates homework field
        //can't write since it overwrites other fields in the document
        if (checkboxHomework.isChecked()) {
            studentRef.update("homework", true);
        }else {
            studentRef.update("homework", false);
        }
    }
    //runs when user clicks equipment checkbox
    public void onCheckboxEquipmentClick(View view){
        //updates equipment field
        if (checkboxEquipment.isChecked()) {
            studentRef.update("equipment", true);
        }else {
            studentRef.update("equipment", false);
        }
    }
    //runs when user clicks save button
    public void onSaveNotesClick(View view){
        //sets new notes as a variable and adds to firestore
        newNotes = findViewById(R.id.studentNotes);
        sNewNotes = newNotes.getText().toString();
        studentRef.update("notes", sNewNotes);
    }
}
