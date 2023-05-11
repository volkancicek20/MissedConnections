package com.cicekvolkan.missedconnections.adapter;

import static com.cicekvolkan.missedconnections.model.Post.LAYOUT_ONE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.databinding.RecycleViewAddpostProfileBinding;
import com.cicekvolkan.missedconnections.model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostAdapterProfile extends RecyclerView.Adapter {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    Context context;

    public PostAdapterProfile(ArrayList<Post> postArrayList, Context context) {
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
                RecycleViewAddpostProfileBinding recycleViewAddpostProfileBinding = RecycleViewAddpostProfileBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new PostAdapterProfile.PostHolderProfile(recycleViewAddpostProfileBinding);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String mail,location,city,district,see,comment,name,date;
        switch (postArrayList.get(position).getViewType()){
            case LAYOUT_ONE:
                ImageView delete =((PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleviewDelete;
                mail = postArrayList.get(position).mail;
                comment = postArrayList.get(position).comment;
                name = postArrayList.get(position).name;
                date = postArrayList.get(position).date;
                see = postArrayList.get(position).see;
                city = postArrayList.get(position).locationCity;
                district = postArrayList.get(position).locationDistrict;
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Gönderimi Sil")
                                .setMessage("Bu gönderimi silmek istediğinize emin misiniz?")
                                .setPositiveButton("Sil", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deletePost(view,postArrayList.get(position).mail, postArrayList.get(position).comment,
                                                postArrayList.get(position).date);
                                        deleteView(city,district,comment);
                                    }
                                })
                                .setNegativeButton("Hayır", null).show();
                    }
                });
                location = city + "/" + district;
                ((PostAdapterProfile.PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleViewComment.setText(comment);
                ((PostAdapterProfile.PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleViewName.setText(name);
                ((PostAdapterProfile.PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleViewTime.setText(date);
                ((PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleviewSeeCount.setText(see);
                ((PostAdapterProfile.PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleviewLocationName.setText(location);
                if(postArrayList.get(position).imageUrl == null){
                    ImageView imageView;
                    imageView = ((PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleViewProfilePhoto;
                    imageView.setImageResource(R.drawable.user);
                }else {
                    Picasso.get().load(postArrayList.get(position).imageUrl).into(((PostAdapterProfile.PostHolderProfile) holder).recycleViewAddpostProfileBinding.recycleViewProfilePhoto);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    public class PostHolderProfile extends RecyclerView.ViewHolder{
        RecycleViewAddpostProfileBinding recycleViewAddpostProfileBinding;
        public PostHolderProfile(RecycleViewAddpostProfileBinding recycleViewAddpostProfileBinding) {
            super(recycleViewAddpostProfileBinding.getRoot());
            this.recycleViewAddpostProfileBinding = recycleViewAddpostProfileBinding;
        }
    }
    public void deletePost(View view,String mail,String comment,String date){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM HH:mm", new Locale("tr", "TR"));

        firebaseFirestore.collection("posts")
                .whereEqualTo("mail", mail)
                .whereEqualTo("comment", comment)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Date time = document.getDate("date");
                        String dateString = dateFormat.format(time);
                        if (dateString.equals(date)) {
                            firebaseFirestore.collection("posts").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(view.getContext(), "Gönderim silindi", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(view.getContext(), "Hata oluştu.Ağ ayarlarınızı kontrol ediniz.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("DELETE", "Error getting data", e));
    }
    public void deleteView(String city,String district,String comment){

        String collectionName = "views";
        String documentName = city+district+comment;


        firebaseFirestore.collection(collectionName).document(documentName)
                .collection(user.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        WriteBatch batch = firebaseFirestore.batch();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            batch.delete(document.getReference());
                        }
                        batch.commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection(collectionName).document(documentName)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Log.d(TAG, "Doküman başarıyla silindi.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //Log.w(TAG, "Doküman silinirken hata oluştu.", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Log.w(TAG, "Alt koleksiyon silinirken hata oluştu.", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Alt koleksiyon getirilirken hata oluştu.", e);
                    }
                });

    }
}
