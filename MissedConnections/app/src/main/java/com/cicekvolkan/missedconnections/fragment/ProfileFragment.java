package com.cicekvolkan.missedconnections.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.adapter.PostAdapter;
import com.cicekvolkan.missedconnections.adapter.PostAdapterProfile;
import com.cicekvolkan.missedconnections.databinding.FragmentProfileBinding;
import com.cicekvolkan.missedconnections.model.Post;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    UUID uuidImage;
    public Uri imageData;
    public Bitmap selectedBitmap;
    public ActivityResultLauncher<Intent> activityResultLauncher;
    public ActivityResultLauncher<String> permissionLauncher;
    private NavigationView navigationView;
    private FragmentProfileBinding binding;
    PostAdapterProfile postAdapter;
    ArrayList<Post> postArrayList;
    EditText editText;
    ImageView imageView;
    Button button;

    public ProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        postArrayList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater,container,false);
        navigationView = binding.getRoot().findViewById(R.id.navigationView);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recycleviewProfile.setLayoutManager(new LinearLayoutManager(view.getContext()));
        postAdapter = new PostAdapterProfile(postArrayList,view.getContext());
        binding.recycleviewProfile.setAdapter(postAdapter);
        registerLauncher(view);
        getName(view);
        getImage(view);
        getData();
        binding.profileEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        binding.textViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }
    @SuppressLint("MissingInflatedId")
    public void showPopup(View view){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if(getActivity() != null){
            View popupView = inflater.inflate(R.layout.popup_edit_profile, null);
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            String name = binding.profileName.getText().toString();
            ImageView imageViewProfile = binding.profileImg;
            editText = popupView.findViewById(R.id.profile_name_text_popup);
            imageView = popupView.findViewById(R.id.profile_image_upload);
            imageView.setImageDrawable(imageViewProfile.getDrawable());
            button = popupView.findViewById(R.id.save_profile);
            if (editText != null) {
                editText.setHint(name);
            }
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setImage(view);
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText text = popupView.findViewById(R.id.profile_name_text_popup);
                    String isim = text.getText().toString().trim();
                    setProfile(view,isim);
                    popupWindow.dismiss();
                }
            });
        }
    }
    public void changeProfileData(String name, String image) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("posts");

        Query query = postsRef.whereEqualTo("mail", user.getEmail());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(image.equals("image")){
                        document.getReference().update("name", name);
                    }else {
                        document.getReference().update("name", name, "imageUrl", image);
                    }
                }
            } else {
                //Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
    public void changeProfileData(String name, int dummy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("posts");

        Query query = postsRef.whereEqualTo("mail", user.getEmail());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().update("name", name);
                }
            } else {
                //Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
    public void changeProfileData(String image) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("posts");

        Query query = postsRef.whereEqualTo("mail", user.getEmail());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (!image.equals("image")){
                        document.getReference().update("imageUrl", image);
                    }
                }
            } else {
                //Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
    public void getData(){
        firebaseFirestore.collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        String name,mail,imageUrl,comment,city,district,see;
                        boolean showLoc;
                        postArrayList.clear();
                        for (DocumentSnapshot commentDoc : value) {
                            Map<String, Object> postData = commentDoc.getData();
                            name = (String) postData.get("name");
                            mail = (String) postData.get("mail");
                            comment = (String) postData.get("comment");
                            imageUrl = (String) postData.get("imageUrl");
                            city = (String) postData.get("locationCity");
                            district = (String) postData.get("locationDistrict");
                            see = (String) postData.get("see");
                            Object checkValue = postData.get("check");
                            if (checkValue != null) {
                                showLoc = (boolean) checkValue;
                            } else {
                                showLoc = false;
                            }
                            Timestamp timestamp = (Timestamp) postData.get("date");
                            String formattedDate = null;
                            if (timestamp != null) {
                                long unixTime = timestamp.getSeconds() * 1000;
                                Date date = new Date(unixTime);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm", new Locale("tr"));
                                formattedDate = sdf.format(date);
                            }
                            Post post;
                            if (mail.equals(user.getEmail())) {
                                if(imageUrl.equals("image")){
                                    post = new Post(1,name,mail,comment,city,district,formattedDate,see,showLoc);
                                }else {
                                    post = new Post(1,imageUrl,name,mail,comment,city,district,formattedDate,see,showLoc);
                                }
                                postArrayList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
    public void setProfile(View view, String name) {
        String[] parts = user.getEmail().split("@");
        String username = parts[0];
        if(imageData != null){
            //!name.equals(username) && !name.isEmpty()
            if(!name.isEmpty()){
                ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                progressDialog.setMessage("Güncelleniyor...");
                progressDialog.show();
                //image update
                CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");
                String userEmail = user.getEmail();
                storageReference.child("ProfilPhoto").child(userEmail).putFile(imageData)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> downloadUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                                downloadUrlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        String imageUrl = task.getResult().toString();
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("date", FieldValue.serverTimestamp());
                                        data.put("imageUrl", imageUrl);
                                        usersCollection.document(userEmail).set(data, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //name update
                                                firebaseFirestore.collection("users")
                                                .document(user.getEmail())
                                                .update("name", name)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(view.getContext(),"Profiliniz güncellendi.",Toast.LENGTH_SHORT).show();
                                                        TextView navName = binding.profileName;
                                                        navName.setText(name);
                                                        Picasso.get().load(imageUrl).into(binding.profileImg);
                                                        changeProfileData(name,imageUrl);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(view.getContext(),"Bir hata oluştu. Ağ ayarlarınızı kontrol ettikten sonra tekrar deneyin.",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else {
                ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                progressDialog.setMessage("Güncelleniyor...");
                progressDialog.show();
                CollectionReference usersCollection = FirebaseFirestore.getInstance().collection("users");
                String userEmail = user.getEmail();
                storageReference.child("ProfilPhoto").child(userEmail).putFile(imageData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> downloadUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                            downloadUrlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String imageUrl = task.getResult().toString();
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("date", FieldValue.serverTimestamp());
                                    data.put("imageUrl", imageUrl);
                                    usersCollection.document(userEmail).set(data, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(view.getContext(), "Profiliniz güncellendi.", Toast.LENGTH_SHORT).show();
                                            Picasso.get().load(imageUrl).into(binding.profileImg);
                                            changeProfileData(imageUrl);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        } else{
            //!name.equals(username) && !name.isEmpty()
            if(!name.isEmpty()){
                ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                progressDialog.setMessage("Güncelleniyor...");
                progressDialog.show();
                // sadece name değiştir
                firebaseFirestore.collection("users")
                        .document(user.getEmail())
                        .update("name", name)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                Toast.makeText(view.getContext(),"Profiliniz güncellendi.",Toast.LENGTH_SHORT).show();
                                TextView navName = binding.profileName;
                                navName.setText(name);
                                changeProfileData(name,1);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(view.getContext(),"Ağ ayarlarınızı kontrol ettikten sonra tekrar deneyin.",Toast.LENGTH_LONG).show();
                            }
                        });
            }
            else {
                // Kullanıcı işlem yapmadı
            }
        }
    }
    public void setImage(View view){
        if(ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Galeriye gitmek için izin gerek",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }
    public void registerLauncher(View view){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent intentForResult = result.getData();
                    if(intentForResult != null){
                        imageData = intentForResult.getData();
                        //binding.selectImage.setImageURI(imageData);
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(view.getContext().getContentResolver(),imageData);
                                selectedBitmap = ImageDecoder.decodeBitmap(source);
                                imageView.setImageBitmap(selectedBitmap);
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(view.getContext().getContentResolver(),imageData);
                                imageView.setImageBitmap(selectedBitmap);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        //
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intent);
                }else{
                    Toast.makeText(view.getContext(), "İzin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void getName(View view){
        firebaseFirestore.collection("users")
            .whereEqualTo("mail", user.getEmail())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String name = documentSnapshot.getString("name");
                    TextView navNameTemp = binding.profileName;
                    navNameTemp.setText(name);

                    MainActivity mainActivity = (MainActivity) getActivity();
                    TextView headerTextView = mainActivity.findViewById(R.id.nav_name);
                    headerTextView.setText(name);
                }
            })
            .addOnFailureListener(e -> {
                //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            });
    }
    public void getImage(View view) {
        firebaseFirestore.collection("users").document(user.getEmail())
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if(mainActivity != null) {
                            ImageView headerImageView = mainActivity.findViewById(R.id.nav_img);
                            if(imageUrl.equals("image")){
                                binding.profileImg.setImageResource(R.drawable.user);
                                headerImageView.setImageResource(R.drawable.user);
                            } else {
                                Picasso.get().load(imageUrl).into(binding.profileImg);
                                Picasso.get().load(imageUrl).into(headerImageView);
                            }
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