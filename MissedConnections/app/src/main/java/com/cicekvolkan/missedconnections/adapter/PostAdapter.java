package com.cicekvolkan.missedconnections.adapter;

import static com.cicekvolkan.missedconnections.model.Post.LAYOUT_ONE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.databinding.RecycleViewAddpostBinding;
import com.cicekvolkan.missedconnections.fragment.MainFragmentDirections;
import com.cicekvolkan.missedconnections.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    Context context;

    int count = 0;

    public PostAdapter(ArrayList<Post> postArrayList, Context context) {
        this.postArrayList = postArrayList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        switch (postArrayList.get(position).getViewType()){
            case 1:
                return LAYOUT_ONE;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case LAYOUT_ONE:
                RecycleViewAddpostBinding recycleViewAddpostBinding = RecycleViewAddpostBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new PostAdapter.PostHolder(recycleViewAddpostBinding);
            default:
                return null;
        }
    }
    int[] valueSee = new int[1];
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String[] userMail = new String[1];
        userMail[0] = null;
        if (user != null) {
            userMail[0] = user.getEmail();
        }
        String mail, name, comment, date, location, city, district, see;
        boolean checkLocation;
        switch (postArrayList.get(position).getViewType()) {
            case LAYOUT_ONE:
                ImageView locationIcon = ((PostHolder) holder).recycleViewAddpostBinding.recycleviewLocationIcon;
                locationIcon.setVisibility(View.INVISIBLE);

                city = "";
                district = "";
                location = "";

                ImageView message = ((PostHolder) holder).recycleViewAddpostBinding.recycleviewPostIcon;
                mail = postArrayList.get(position).mail;
                name = postArrayList.get(position).name;
                comment = postArrayList.get(position).comment;
                date = postArrayList.get(position).date;
                see = postArrayList.get(position).see;
                checkLocation = postArrayList.get(position).check;
                message.setVisibility(View.INVISIBLE);
                if(checkLocation){
                    locationIcon.setVisibility(View.VISIBLE);
                    city = postArrayList.get(position).locationCity;
                    district = postArrayList.get(position).locationDistrict;
                    location = city + "/" + district;
                }
                if (!mail.equals(userMail[0])) {
                    message.setVisibility(View.VISIBLE);
                    message.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainFragmentDirections.ActionMainFragmentToChatFragment action = MainFragmentDirections.actionMainFragmentToChatFragment("", "");
                            action.setMail(mail);
                            action.setName(name);
                            Navigation.findNavController(view).navigate(action);
                        }
                    });
                }
                try {
                    valueSee[0] = Integer.parseInt(see);
                    valueSee[0] = valueSee[0] + 1;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (user != null) {
                    if (!mail.equals(userMail[0])) {
                        String documentId1 = mail;

                        CollectionReference viewsRef = firebaseFirestore.collection("views");
                        DocumentReference docRef = viewsRef.document(city + district + comment);
                        CollectionReference viewRef = docRef.collection(documentId1);
                        viewRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                boolean isAlreadySeen = false;
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    if (doc.getId().equals(userMail[0])) {
                                        isAlreadySeen = true;
                                        break;
                                    }
                                }
                                if (!isAlreadySeen) {
                                    if(count == 0){

                                        seeCountUp(valueSee,mail,userMail,viewRef);
                                    }
                                }
                            }
                        });
                    }
                }

                ((PostHolder) holder).recycleViewAddpostBinding.recycleviewSeeCount.setText(String.valueOf(valueSee[0]));
                ((PostHolder) holder).recycleViewAddpostBinding.recycleViewComment.setText(comment);
                ((PostHolder) holder).recycleViewAddpostBinding.recycleViewName.setText(name);
                ((PostHolder) holder).recycleViewAddpostBinding.recycleViewTime.setText(date);
                ((PostHolder) holder).recycleViewAddpostBinding.recycleviewSeeCount.setText(see);
                ((PostHolder) holder).recycleViewAddpostBinding.recycleviewLocationName.setText(location);
                if(postArrayList.get(position).imageUrl == null){
                    ImageView imageView;
                    imageView = ((PostHolder) holder).recycleViewAddpostBinding.recycleViewProfilePhoto;
                    imageView.setImageResource(R.drawable.user);
                }else {
                    Picasso.get().load(postArrayList.get(position).imageUrl).into(((PostHolder) holder).recycleViewAddpostBinding.recycleViewProfilePhoto);
                }

                count = 0;
                break;
        }
    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder{
        RecycleViewAddpostBinding recycleViewAddpostBinding;
        public PostHolder(RecycleViewAddpostBinding recycleViewAddpostBinding) {
            super(recycleViewAddpostBinding.getRoot());
            this.recycleViewAddpostBinding = recycleViewAddpostBinding;
        }
    }
    public void seeCountUp(int[] valueSee,String mail,String[] userMail,CollectionReference viewRef){
        count++;
        firebaseFirestore.collection("posts").get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task2.getResult()) {
                            String mailx = document.getString("mail");
                            if (mailx != null && mailx.equals(mail)) {
                                firebaseFirestore.collection("posts").document(document.getId())
                                        .update("see", String.valueOf(valueSee[0]))
                                        .addOnSuccessListener(aVoid -> {
                                            Map<String, Object> viewData = new HashMap<>();
                                            viewData.put("empty", "empty");
                                            if (userMail[0] != null) {
                                                viewRef.document(userMail[0]).set(viewData);
                                            }
                                            return;
                                        })
                                        .addOnFailureListener(e -> {

                                        });
                            }
                        }
                    } else {

                    }
                });
    }
}
