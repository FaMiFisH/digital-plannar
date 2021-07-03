package com.example.a_level_coursework;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotesActivity extends AppCompatActivity {
    private static final String TAG = "NotesActivity";
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    DocumentReference notesRef, checkListRef;
    CheckBox checkList;
    Context Context;
    LinearLayout mLinearLayout;
    EditText Task, notes, saveNotes;
    String sTask, userID, newNotes, sSaveNotes, sDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes);

        /*final Calendar myCalendar = Calendar.getInstance();
        EditText task = findViewById(R.id.txtNotesCheckListTask);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NotesActivity.this,
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });*/

        //initialises the database
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();
        //initialising database references
        notesRef = mFirestore.collection("Notebook").document(userID);
        checkListRef = mFirestore.collection("Checklist").document(userID);
        //calling methods to fill the display of teh interface
        getNotes();
        getChecklist();

    }

    /*private void updateLabel(){
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Log.d(TAG, "updateLabel: " + sdf);
    }*/

    //method to get notes
    public void getNotes(){
        //query to retrieve the notebook field from the users Notebook
        notesRef.get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    //finds the note edit text
                    notes = findViewById(R.id.txtNotesNotes);
                    //converts the retrieved data from database to string
                    newNotes = documentSnapshot.getString("notebook");
                    notes.setText(newNotes);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Context, "Could not retrieve notes", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //creates a checkbox by taking in a string value and setting that as the text
    public void createCheckList(final String retrievedTask){
        //validation for an empty string
        if(retrievedTask.equals("")){
            return;
        }else {
            //initialises the layout
            Context = getApplicationContext();
            mLinearLayout = findViewById(R.id.checklistLL);
            //initialising layout parameters
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            //initialising checkbox margins
            params.setMargins(20, 16, 0, 16);
            //initialising checkbox
            checkList = new CheckBox(Context);
            checkList.setLayoutParams(params);
            checkList.setText(retrievedTask);
            checkList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //deletes the task from the database once ticked
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(retrievedTask, FieldValue.delete());
                    checkListRef.update(updates);
                    Toast.makeText(Context, retrievedTask + " was successfully removed", Toast.LENGTH_SHORT).show();
                    //refreshes the page to remove the delete task from interface
                    finish();
                    startActivity(getIntent());
                }
            });
            mLinearLayout.addView(checkList);
        }
    }
    //method to retrieve the already set tasks by the user
    public void getChecklist(){
        //a simple query to retrieve the logged in users checklist
        mFirestore.collection("Checklist")
                .whereEqualTo("user", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //converts the data from the database to string
                                sDocument = document.getData().toString();
                                //removes any of the target special characters
                                String result1 = sDocument.replace("{","");
                                String result2 = result1.replace("}","");
                                String result3 = result2.replace("=","");
                                String finalResult = result3.replace("user"+userID, "");
                                //puts the string into an array
                                List<String> myList = new ArrayList<String>(Arrays.asList(finalResult.split(",")));
                                //gets each item from the list and
                                String eachTask = "";
                                for (int i = 0; i < myList.size(); i++) {
                                    eachTask = myList.get(i);
                                    if(!eachTask.equals(" ")){
                                        createCheckList(eachTask);
                                    }
                                }
                            }
                        }
                    }
                });
    }
    //validation fro inputted task
    public boolean validation(final String task){
        if(task.isEmpty()){
            return false;
        }else{
            return true;
        }
    }
    //listener for when the user adds a task to the checklist
    public void onChecklistAddClick(View view){
        //retrieves the user input
        Task = findViewById(R.id.txtNotesCheckListTask);
        //converts user input to string
        sTask = Task.getText().toString();
        if(validation(sTask)){
            //Initialises the HashMap to add to database
            Map<String, Object> task = new HashMap<>();
            task.put(sTask, "");
            task.put("user", userID);
            //writes to the database
            checkListRef.set(task, SetOptions.merge()).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //displays the just inputted task to the user
                    createCheckList(sTask);
                    Toast.makeText(Context, sTask + " was successfully added", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Task.setError("Empty input");
        }

    }
    //listener for when the user presses the save button
    public void onSaveNotesClick(View view){
        saveNotes = findViewById(R.id.txtNotesNotes);
        sSaveNotes = saveNotes.getText().toString();

        Map<String, Object> note = new HashMap<>();
        note.put("notebook", sSaveNotes);
        note.put("user_ID", userID);
        notesRef.set(note).addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Context, "Notes Saved", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Context, "Notes were not saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
