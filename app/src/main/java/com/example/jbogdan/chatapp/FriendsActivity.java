package com.example.jbogdan.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    ListView usersList;
    ArrayList<String> users = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        usersList = findViewById(R.id.users_list);

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserChatInterlocutors.recvUser = users.get(position);
                startActivity(new Intent(FriendsActivity.this, SelectUserOption.class));
            }
        });

        // set a listener on "users" reference(on change)
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String username = ds.child("username").getValue(String.class);

                    Log.d("USERTAG", username);

                    // display just other users
                    if (!username.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()))
                        users.add(username);

                }

                ArrayAdapter<String> adapter = new ArrayAdapter(FriendsActivity.this, android.R.layout.simple_list_item_1, users);

                usersList.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
