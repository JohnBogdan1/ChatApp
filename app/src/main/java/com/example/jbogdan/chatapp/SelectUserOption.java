package com.example.jbogdan.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SelectUserOption extends AppCompatActivity {

    private Button start_chat;
    private Button get_info;
    TextView text;
    private DatabaseReference mDatabase;
    double lat1, lat2, lon1, lon2, timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user_option);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        text = findViewById(R.id.user_option);
        text.setText("Select an option");

        start_chat = findViewById(R.id.start_chat);
        start_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectUserOption.this, ChatActivity.class));
            }
        });

        get_info = findViewById(R.id.get_info);
        get_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            String username = ds.child("username").getValue(String.class);
                            if (username.equals(UserChatInterlocutors.recvUser)) {
                                lat2 = (double) ds.child("latitude").getValue();
                                lon2 = (double) ds.child("longitude").getValue();
                                timestamp = (long) ds.child("timestamp").getValue();
                            } else if (username.equals(UserChatInterlocutors.sendUser)) {
                                lat1 = (double) ds.child("latitude").getValue();
                                lon1 = (double) ds.child("longitude").getValue();
                            }

                        }

                        String message = String.format("User %1$s was at: %2$f meters, %3$f minutes ago.", UserChatInterlocutors.recvUser, distance(lat1, lat2, lon1, lon2), (System.currentTimeMillis() / 1000 - timestamp) / 60);
                        Toast.makeText(SelectUserOption.this, message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mDatabase.addListenerForSingleValueEvent(eventListener);
            }
        });
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        // ignoring height
        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}
