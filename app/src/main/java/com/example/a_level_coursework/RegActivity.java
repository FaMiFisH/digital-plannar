package com.example.a_level_coursework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class RegActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirestore;
    DocumentReference regRef;
    String userID;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);

        //setting toolbar title
        String reg = getIntent().getStringExtra("reg");
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(reg + " Registration");

        //initialising firebase
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        userID = mFirebaseAuth.getCurrentUser().getUid();
        //initialising references
        regRef = mFirestore
                .collection("users")
                .document(userID)
                .collection("timetable")
                .document(reg);

        getNotes();
    }

    public void getNotes(){
        regRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    //finds the notes edit text
                    TextView txtNotes = findViewById(R.id.regNotes);
                    //retrieves already saved notes from firestore and displays them
                    String notes = documentSnapshot.getString("notes");
                    txtNotes.setText(notes);
                }
            }
        });
    }

    public void onSaveNotesClick(View view){
        TextView txtNotes = findViewById(R.id.regNotes);
        String notes = txtNotes.getText().toString();

        Map<String, Object> note = new HashMap<>();
        note.put("notes", notes);
        regRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegActivity.this, "Notes Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
