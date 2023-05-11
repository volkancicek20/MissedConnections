package com.cicekvolkan.missedconnections.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.adapter.MessageAdapter;
import com.cicekvolkan.missedconnections.adapter.PostAdapterProfile;
import com.cicekvolkan.missedconnections.databinding.FragmentMessageBinding;
import com.cicekvolkan.missedconnections.model.Chat;
import com.cicekvolkan.missedconnections.model.Message;
import com.cicekvolkan.missedconnections.model.Post;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class MessageFragment extends Fragment {


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FragmentMessageBinding binding;
    ArrayList<Message> messageArrayList;
    MessageAdapter messageAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        messageArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater,container,false);

        swipeRefreshLayout = binding.getRoot().findViewById(R.id.swipeRefreshLayoutMesssageFragment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setDistanceToTriggerSync(200);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recycleviewMessages.setLayoutManager(new LinearLayoutManager(view.getContext()));
        messageAdapter = new MessageAdapter(messageArrayList,view.getContext());
        binding.recycleviewMessages.setAdapter(messageAdapter);
        getData();
    }

    public  void getData(){

        ArrayList<String> check = new ArrayList<>();

        CollectionReference chatsRef = firebaseFirestore.collection("chats");

        chatsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String[] chatParticipants = document.getId().split(">");
                    String senderEmail = chatParticipants[0];
                    String receiverEmail = chatParticipants[1];
                    if(senderEmail.equals(user.getEmail()) || receiverEmail.equals(user.getEmail())){
                        if(senderEmail.equals(user.getEmail())){
                            if(!check.contains(receiverEmail+">"+senderEmail)){
                                check.add(receiverEmail+">"+senderEmail);

                                CollectionReference colRef = chatsRef.document(senderEmail+">"+receiverEmail).collection(senderEmail+">"+receiverEmail);
                                colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        messageArrayList.clear();
                                        firebaseFirestore.collection("users").document(receiverEmail)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        String[] name = new String[1];
                                                        String[] mail = new String[1];
                                                        if (documentSnapshot.exists()) {
                                                            name[0] = documentSnapshot.getString("name");
                                                            mail[0] = documentSnapshot.getString("mail");
                                                        }
                                                        Message message = new Message(1,mail[0],name[0]);
                                                        messageArrayList.add(message);
                                                        messageAdapter.notifyDataSetChanged();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }else {
                            if(!check.contains(senderEmail+">"+receiverEmail)){
                                check.add(senderEmail+">"+receiverEmail);

                                CollectionReference colRef = chatsRef.document(receiverEmail+">"+senderEmail).collection(receiverEmail+">"+senderEmail);
                                colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        messageArrayList.clear();

                                        firebaseFirestore.collection("users").document(senderEmail)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        String[] name = new String[1];
                                                        String[] mail = new String[1];
                                                        if (documentSnapshot.exists()) {
                                                            name[0] = documentSnapshot.getString("name");
                                                            mail[0] = documentSnapshot.getString("mail");
                                                        }
                                                        Message message = new Message(1,mail[0],name[0]);
                                                        messageArrayList.add(message);
                                                        messageAdapter.notifyDataSetChanged();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    }
                }
            } else {

            }
        });
    }

}