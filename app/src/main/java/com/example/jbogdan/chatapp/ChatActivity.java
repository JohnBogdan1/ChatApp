package com.example.jbogdan.chatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter<Message> adapter;
    ImageButton sendButton;
    EditText messageArea;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        messageArea = findViewById(R.id.message_area);
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Message message = new Message(messageArea.getText().toString(), user.getDisplayName());
                mDatabase.child("messages").child(UserChatInterlocutors.sendUser + "_" + UserChatInterlocutors.recvUser).push().setValue(message);
                mDatabase.child("messages").child(UserChatInterlocutors.recvUser + "_" + UserChatInterlocutors.sendUser).push().setValue(message);
                messageArea.setText("");
                messageArea.requestFocus();
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ListView chatMessages = findViewById(R.id.chat_messages);
            adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.message, mDatabase.child("messages").child(UserChatInterlocutors.sendUser + "_" + UserChatInterlocutors.recvUser)) {
                @Override
                protected void populateView(View v, Message model, int position) {

                    TextView messageText, messageUser, messageTime;
                    messageText = v.findViewById(R.id.message_text);
                    messageUser = v.findViewById(R.id.message_user);
                    messageTime = v.findViewById(R.id.message_time);

                    messageText.setText(model.getMessageText());
                    messageUser.setText(model.getMessageUser());
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                }
            };
            chatMessages.setAdapter(adapter);
        }

    }
}
