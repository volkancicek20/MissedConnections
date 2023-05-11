package com.cicekvolkan.missedconnections.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.adapter.ChatAdapter;
import com.cicekvolkan.missedconnections.databinding.FragmentChatBinding;
import com.cicekvolkan.missedconnections.model.Chat;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatFragment extends Fragment {

    ArrayList<Chat> chatArrayList;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    ChatAdapter chatAdapter;
    private FragmentChatBinding binding;
    String myName,myMail,anotherMail,anotherName;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        chatArrayList  = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myMail = user.getEmail();
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setReverseLayout(true);
        binding.recycleviewChat.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(chatArrayList,view.getContext());
        binding.recycleviewChat.setAdapter(chatAdapter);
        if(getArguments() != null){
            anotherMail = ChatFragmentArgs.fromBundle(getArguments()).getMail();
            anotherName = ChatFragmentArgs.fromBundle(getArguments()).getName();
            getName(myMail);
            getImage(anotherMail);
            binding.profileName.setText(anotherName);
        }
        getData(view);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                binding.chatMessageEdittext.clearFocus();
            }
        };
        binding.chatMessageEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            final int DELAY = 30 * 1000;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Zamanlayıcıyı başlat
                    handler.postDelayed(runnable, DELAY);
                } else {
                    // Zamanlayıcıyı durdur
                    handler.removeCallbacks(runnable);
                }
            }
        });
        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.chatMessageEdittext.getText().toString().trim();
                if(!message.isEmpty()){
                    sendMessage(message);
                    binding.chatMessageEdittext.setText("");
                }
            }
        });
    }


    public void sendMessage(String message){
        CollectionReference chatsRef = firebaseFirestore.collection("chats");
        String documentId = myMail + ">" + anotherMail;
        Map<String, Object> data = new HashMap<>();
        data.put("empty", "empty");
        chatsRef.document(documentId).set(data);
        CollectionReference messagesRef = chatsRef.document(documentId).collection(documentId);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("name", myName);
        messageData.put("mail",user.getEmail());
        messageData.put("date", FieldValue.serverTimestamp());
        messagesRef.add(messageData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void getData(View view){

        String documentId1 = myMail+">"+anotherMail;
        String documentId2 = anotherMail+">"+myMail;

        CollectionReference chatCollectionRef1 = firebaseFirestore.collection("chats");
        CollectionReference chatCollectionRef2 = firebaseFirestore.collection("chats");
        DocumentReference chatDocumentRef1 = chatCollectionRef1.document(documentId1);
        DocumentReference chatDocumentRef2 = chatCollectionRef2.document(documentId2);
        CollectionReference messageCollectionRef1 = chatDocumentRef1.collection(documentId1);
        CollectionReference messageCollectionRef2 = chatDocumentRef2.collection(documentId2);

        messageCollectionRef1.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value1, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(view.getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                messageCollectionRef2.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(view.getContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        chatArrayList.clear();
                        ArrayList<DocumentSnapshot> mergedList = new ArrayList<>();
                        mergedList.addAll(value1.getDocuments());
                        mergedList.addAll(value2.getDocuments());
                        List<DocumentSnapshot> dateList = new ArrayList<>();
                        for (DocumentSnapshot doc : mergedList) {
                            if (doc.contains("date")) {
                                dateList.add(doc);
                            }
                        }
                        Collections.sort(dateList, new Comparator<DocumentSnapshot>() {
                            @Override
                            public int compare(DocumentSnapshot o1, DocumentSnapshot o2) {
                                Timestamp t1 = o1.getTimestamp("date");
                                Timestamp t2 = o2.getTimestamp("date");

                                if (t1 == null && t2 == null) {
                                    return 0; // eşit
                                } else if (t1 == null) {
                                    return 1; // o1 > o2
                                } else if (t2 == null) {
                                    return -1; // o1 < o2
                                } else {
                                    return t2.compareTo(t1);
                                }
                            }
                        });
                        String[] myName = new String[1];
                        firebaseFirestore.collection("users").document(user.getEmail())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        chatArrayList.clear();
                                        if (documentSnapshot.exists()) {
                                            myName[0] = documentSnapshot.getString("name");
                                        }
                                        Chat chat;
                                        for (DocumentSnapshot snapshot : dateList) {
                                            Map<String, Object> data = snapshot.getData();
                                            if (data.get("name").equals(myName[0])) {
                                                String message = (String) data.get("message");
                                                Timestamp timestamp = (Timestamp) data.get("date");
                                                String formattedDate = null;
                                                if (timestamp != null) {
                                                    long unixTime = timestamp.getSeconds() * 1000;
                                                    Date date = new Date(unixTime);
                                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("tr"));
                                                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
                                                    formattedDate = sdf.format(date);
                                                }
                                                chat = new Chat(1, message, formattedDate);
                                                chatArrayList.add(chat);
                                            } else {
                                                String message = (String) data.get("message");
                                                Timestamp timestamp = (Timestamp) data.get("date");
                                                String formattedDate = null;
                                                if (timestamp != null) {
                                                    long unixTime = timestamp.getSeconds() * 1000;
                                                    Date date = new Date(unixTime);
                                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("tr"));
                                                    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
                                                    formattedDate = sdf.format(date);
                                                }
                                                chat = new Chat(2, message, formattedDate);
                                                chatArrayList.add(chat);
                                            }
                                        }
                                        chatAdapter.notifyDataSetChanged();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
    }
    public void getName(String mail){
        firebaseFirestore.collection("users")
                .whereEqualTo("mail", mail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        myName = documentSnapshot.getString("name");
                        //System.out.println(myName);
                    }
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                });
    }
    public void getImage(String mail) {
        firebaseFirestore.collection("users").document(mail)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            if(imageUrl.equals("image")){
                                ImageView imageView;
                                imageView = binding.chatHeaderImg;
                                imageView.setImageResource(R.drawable.user);
                            }else {
                                Picasso.get().load(imageUrl).into(binding.chatHeaderImg);
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}