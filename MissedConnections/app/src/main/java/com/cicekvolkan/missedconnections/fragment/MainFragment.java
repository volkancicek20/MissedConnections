package com.cicekvolkan.missedconnections.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.adapter.PostAdapter;
import com.cicekvolkan.missedconnections.databinding.FragmentMainBinding;
import com.cicekvolkan.missedconnections.model.Post;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment {
    String[] city = {"Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın",
            "Balıkesir", "Bartın", "Batman", "Bayburt", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa",
            "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Düzce", "Edirne", "Elazığ", "Erzincan", "Erzurum",
            "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Iğdır", "Isparta", "İstanbul", "İzmir",
            "Kahramanmaraş", "Karabük", "Karaman", "Kars", "Kastamonu", "Kayseri", "Kırıkkale", "Kırklareli", "Kırşehir",
            "Kilis", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Mardin", "Mersin", "Muğla", "Muş", "Nevşehir",
            "Niğde", "Ordu", "Osmaniye", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Şanlıurfa", "Şırnak",
            "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Uşak", "Van", "Yalova", "Yozgat", "Zonguldak"};
    Map<String, String[]> ilceler = new HashMap<>();
    AutoCompleteTextView autoCompleteTextView,autoCompleteTextView2,autoCompleteTextViewFind,autoCompleteTextViewFind2;
    ArrayList<Post> postArrayList;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> arrayAdapter2;
    ArrayAdapter<String> arrayAdapterFind;
    ArrayAdapter<String> arrayAdapterFind2;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    List<String> ilceList;
    List<String[]> ilce;
    boolean[] check = new boolean[1];
    private FloatingActionButton fab,fab1,fab2;
    private Boolean isOpen = false;
    String selectedCity = "";
    String selectedDistrict = "";
    String selectedCityFind = "";
    String selectedDistrictFind = "";
    String imageUrl = "image";
    SwipeRefreshLayout swipeRefreshLayout;
    FragmentMainBinding binding;
    PostAdapter postAdapter;
    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        postArrayList = new ArrayList<>();
        check[0] = false;
        semts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater,container,false);

        autoCompleteTextView = binding.getRoot().findViewById(R.id.auto_complete_text_city);
        arrayAdapter = new ArrayAdapter<>(requireContext(),R.layout.list_item,city);
        autoCompleteTextView2 = binding.getRoot().findViewById(R.id.auto_complete_text_district);

        autoCompleteTextViewFind = binding.getRoot().findViewById(R.id.auto_complete_text_city_find);
        arrayAdapterFind = new ArrayAdapter<>(requireContext(),R.layout.list_item,city);
        autoCompleteTextViewFind2 = binding.getRoot().findViewById(R.id.auto_complete_text_district_find);

        ilceList = new ArrayList<>(ilceler.keySet());
        ilce = new ArrayList<>(ilceler.values());

        swipeRefreshLayout = binding.getRoot().findViewById(R.id.swipeRefreshLayoutMainfragment);
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
        binding.recyclerViewMainfragment.setLayoutManager(new LinearLayoutManager(view.getContext()));
        postAdapter = new PostAdapter(postArrayList,view.getContext());
        binding.recyclerViewMainfragment.setAdapter(postAdapter);
        fab = binding.fab;
        fab1 = binding.fab1;
        fab2 = binding.fab2;
        getData();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOpen){
                    openMenu();
                }else {
                    closeMenu();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupFind(view);
            }
        });
    }

    public void findGetData(View view,EditText key,PopupWindow popupWindow){
        check[0] = true;
        String text = key.getText().toString();
        ArrayList<String> keyList = new ArrayList<>();
        if(!text.isEmpty()){
            String[] words = text.split(",");
            for(String keys : words){
                String trimmedKey = keys.trim();
                keyList.add(trimmedKey);
            }
        }
        if (!text.isEmpty() && !selectedCityFind.isEmpty() && !selectedDistrictFind.isEmpty()) {
            ProgressDialog progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("Taranıyor..");
            progressDialog.show();
            firebaseFirestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        return;
                    }
                    String name, mail, imageUrl, comment, city, district, see;
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
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
                            formattedDate = sdf.format(date);
                        }
                        assert city != null && district != null;
                        boolean cityDistrictMatch = city.equals(selectedCityFind) && district.equals(selectedDistrictFind);
                        boolean commentMatch = false;
                        for (String key : keyList) {
                            assert comment != null;
                            if (comment.contains(key)) {
                                commentMatch = true;
                                break;
                            }
                        }
                        if (cityDistrictMatch && commentMatch) {

                            Post post;
                            if (imageUrl.equals("image")) {
                                check[0] = false;
                                post = new Post(1, name, mail, comment, city, district, formattedDate, see,showLoc);
                            } else {
                                post = new Post(1, imageUrl, name, mail, comment, city, district, formattedDate, see,showLoc);
                                check[0] = false;
                            }
                            postArrayList.add(post);
                        }
                    }
                    if (check[0]) {
                        progressDialog.dismiss();
                        Toast.makeText(view.getContext(), "Aradığınız kriterlere uygun gönderi bulunamadı.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        postAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }
            });
            return;
        }else if(!text.isEmpty() && !selectedCityFind.isEmpty()){
            ProgressDialog progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("Taranıyor..");
            progressDialog.show();
            firebaseFirestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        return;
                    }
                    String name, mail, imageUrl, comment, city, district, see;
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
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
                            formattedDate = sdf.format(date);
                        }
                        assert  city != null;
                        boolean cityMatch = city.equals(selectedCityFind);
                        boolean commentMatch = false;
                        for (String key : keyList) {
                            assert comment != null;
                            if (comment.contains(key)) {
                                commentMatch = true;
                                break;
                            }
                        }
                        if (cityMatch && commentMatch) {

                            Post post;
                            if (imageUrl.equals("image")) {
                                check[0] = false;
                                post = new Post(1, name, mail, comment, city, district, formattedDate, see,showLoc);
                            } else {
                                post = new Post(1, imageUrl, name, mail, comment, city, district, formattedDate, see,showLoc);
                                check[0] = false;
                            }
                            postArrayList.add(post);
                        }
                    }
                    if (check[0]) {
                        progressDialog.dismiss();
                        Toast.makeText(view.getContext(), "Aradığınız kriterlere uygun gönderi bulunamadı.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        postAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }
            });
            return;
        }else if (!selectedCityFind.isEmpty()){
            ProgressDialog progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("Taranıyor..");
            progressDialog.show();
            firebaseFirestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        return;
                    }
                    String name, mail, imageUrl, comment, city, district, see;
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
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
                            formattedDate = sdf.format(date);
                        }
                        assert city != null;
                        boolean cityMatch = city.equals(selectedCityFind);
                        if (cityMatch) {

                            Post post;
                            if (imageUrl.equals("image")) {
                                check[0] = false;
                                post = new Post(1, name, mail, comment, city, district, formattedDate, see,showLoc);
                            } else {
                                post = new Post(1, imageUrl, name, mail, comment, city, district, formattedDate, see,showLoc);
                                check[0] = false;
                            }
                            postArrayList.add(post);
                        }
                    }
                    if (check[0]) {
                        progressDialog.dismiss();
                        Toast.makeText(view.getContext(), "Aradığınız kriterlere uygun gönderi bulunamadı.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        postAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }
            });
            return;

        }else if(!text.isEmpty()){
            ProgressDialog progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("Taranıyor..");
            progressDialog.show();
            firebaseFirestore.collection("posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        return;
                    }
                    String name, mail, imageUrl, comment, city, district, see;
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
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
                            formattedDate = sdf.format(date);
                        }
                        boolean commentMatch = false;
                        for (String key : keyList) {
                            assert comment != null;
                            if (comment.contains(key)) {
                                commentMatch = true;
                                break;
                            }
                        }
                        if (commentMatch) {

                            Post post;
                            if (imageUrl.equals("image")) {
                                check[0] = false;
                                post = new Post(1, name, mail, comment, city, district, formattedDate, see,showLoc);
                            } else {
                                post = new Post(1, imageUrl, name, mail, comment, city, district, formattedDate, see,showLoc);
                                check[0] = false;
                            }
                            postArrayList.add(post);
                        }
                    }
                    if (check[0]) {
                        progressDialog.dismiss();
                        Toast.makeText(view.getContext(), "Aradığınız kriterlere uygun gönderi bulunamadı.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        postAdapter.notifyDataSetChanged();
                        popupWindow.dismiss();
                    }
                }
            });
            return;
        }else {
            Toast.makeText(view.getContext(), "Bilgileri eksiksiz giriniz.", Toast.LENGTH_SHORT).show();
        }
    }

    public void getData(){
        firebaseFirestore.collection("posts")
                //.orderBy("date", Query.Direction.DESCENDING)
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
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("tr"));
                                formattedDate = sdf.format(date);
                            }

                            Post post;
                            if(imageUrl.equals("image")){
                                post = new Post(1,name,mail,comment,city,district,formattedDate,see,showLoc);
                            }else {
                                post = new Post(1,imageUrl,name,mail,comment,city,district,formattedDate,see,showLoc);
                            }
                            postArrayList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
    public void postUpload(View view,EditText message,PopupWindow popupWindow,boolean[] show){
        long[] timeArray = new long[1];
        CollectionReference postsRef = firebaseFirestore.collection("posts");
        Query query = postsRef.whereEqualTo("mail", user.getEmail());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> matchingDocs = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    matchingDocs.add(document);
                }
                // En yeni tarihli dokümanı bulma
                DocumentSnapshot mostRecentDoc = null;
                for (DocumentSnapshot doc : matchingDocs) {
                    if (mostRecentDoc == null) {
                        mostRecentDoc = doc;
                    } else{
                        Timestamp currentTimestamp = doc.getTimestamp("date");
                        Timestamp mostRecentTimestamp = mostRecentDoc.getTimestamp("date");
                        if (currentTimestamp.compareTo(mostRecentTimestamp) > 0) {
                            mostRecentDoc = doc;
                        }
                    }
                }
                // En yeni tarihli dokümanın date alanını al
                if (mostRecentDoc != null) {

                    Timestamp mostRecentTimestamp = mostRecentDoc.getTimestamp("date");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String formattedTimestamp = formatter.format(mostRecentTimestamp.toDate());
                    LocalDateTime localDateTime = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        Instant instant = Instant.now();
                        localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    }
                    DateTimeFormatter formatter2 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    }
                    String formattedTimestamp2 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        formattedTimestamp2 = localDateTime.format(formatter2);
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date1 = sdf.parse(formattedTimestamp);
                        Date date2 = sdf.parse(formattedTimestamp2);
                        long diff = date2.getTime() - date1.getTime();
                        long diffSeconds = diff / 1000;
                        long diffMinutes = diff / (60 * 1000);
                        long diffHours = diff / (60 * 60 * 1000);
                        timeArray[0] = diffHours;
                        if (diffHours >= 6) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            TextView textname = mainActivity.findViewById(R.id.nav_name);
                            String text = message.getText().toString().trim();
                            if(!text.isEmpty() && !selectedCity.isEmpty() && !selectedDistrict.isEmpty()){
                                ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                                progressDialog.setMessage("Gönderi yükleniyor...");
                                progressDialog.show();
                                firebaseFirestore.collection("users").document(user.getEmail())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                imageUrl = documentSnapshot.getString("imageUrl");
                                                String name = textname.getText().toString();
                                                Map<String, Object> post = new HashMap<>();
                                                post.put("date", FieldValue.serverTimestamp());
                                                if(imageUrl.equals("image")){
                                                    post.put("imageUrl","image");
                                                }else {
                                                    post.put("imageUrl", imageUrl);
                                                }
                                                post.put("mail", user.getEmail());
                                                post.put("name", name);
                                                post.put("comment", text);
                                                post.put("locationCity", selectedCity);
                                                post.put("locationDistrict", selectedDistrict);
                                                post.put("see", "1");
                                                post.put("check", show[0]);
                                                firebaseFirestore.collection("posts").add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        progressDialog.dismiss();
                                                        CollectionReference viewsRef = firebaseFirestore.collection("views");
                                                        String documentId = user.getEmail();
                                                        Map<String, Object> data = new HashMap<>();
                                                        data.put("empty", "empty");
                                                        viewsRef.document(selectedCity+selectedDistrict+text).set(data);
                                                        CollectionReference viewRef = viewsRef.document(selectedCity+selectedDistrict+text).collection(documentId);
                                                        Map<String, Object> viewData = new HashMap<>();
                                                        viewData.put("empty","empty");
                                                        viewRef.document(user.getEmail()).set(data);
                                                        Toast.makeText(view.getContext(), "Gönderim başarılı.", Toast.LENGTH_SHORT).show();
                                                        popupWindow.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(view.getContext(), "Gönderim başarısız.Lütfen ağ ayarlarınızı kontrol ediniz..", Toast.LENGTH_SHORT).show();
                                                        popupWindow.dismiss();
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                        });
                            }else {
                                Toast.makeText(view.getContext(), "Bilgileri eksiksiz giriniz.", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            popupWindow.dismiss();
                            long time = 6-timeArray[0];
                            Toast.makeText(view.getContext(),time+" saat sonra tekrar gönderi paylaşabilirsiniz.",Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    MainActivity mainActivity = (MainActivity) getActivity();
                    TextView textname = mainActivity.findViewById(R.id.nav_name);
                    String text = message.getText().toString().trim();
                    if(!text.isEmpty() && !selectedCity.isEmpty() && !selectedDistrict.isEmpty()){
                        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
                        progressDialog.setMessage("Gönderi yükleniyor...");
                        progressDialog.show();
                        firebaseFirestore.collection("users").document(user.getEmail())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        imageUrl = documentSnapshot.getString("imageUrl");
                                        String name = textname.getText().toString();
                                        Map<String, Object> post = new HashMap<>();
                                        post.put("date", FieldValue.serverTimestamp());
                                        if(imageUrl.equals("image")){
                                            post.put("imageUrl","image");
                                        }else {
                                            post.put("imageUrl", imageUrl);
                                        }
                                        post.put("mail", user.getEmail());
                                        post.put("name", name);
                                        post.put("comment", text);
                                        post.put("locationCity", selectedCity);
                                        post.put("locationDistrict", selectedDistrict);
                                        post.put("see", "1");
                                        post.put("check", show[0]);
                                        firebaseFirestore.collection("posts").add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                progressDialog.dismiss();
                                                CollectionReference viewsRef = firebaseFirestore.collection("views");
                                                String documentId = user.getEmail();
                                                Map<String, Object> data = new HashMap<>();
                                                data.put("empty", "empty");
                                                viewsRef.document(selectedCity+selectedDistrict+text).set(data);
                                                CollectionReference viewRef = viewsRef.document(selectedCity+selectedDistrict+text).collection(documentId);
                                                Map<String, Object> viewData = new HashMap<>();
                                                viewData.put("empty","empty");
                                                viewRef.document(user.getEmail()).set(data);
                                                Toast.makeText(view.getContext(), "Gönderim başarılı.", Toast.LENGTH_SHORT).show();
                                                popupWindow.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(view.getContext(), "Gönderim başarısız.Lütfen ağ ayarlarınızı kontrol ediniz..", Toast.LENGTH_SHORT).show();
                                                popupWindow.dismiss();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                });
                    }else {
                        Toast.makeText(view.getContext(), "Bilgileri eksiksiz giriniz.", Toast.LENGTH_SHORT).show();
                    }
                }
            }else {

            }
        });
    }
    public void showPopupFind(View view){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if(getActivity() != null){
            View popupView = inflater.inflate(R.layout.popup_edit_post, null);
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            AutoCompleteTextView autoCompleteTextViewFind = popupView.findViewById(R.id.auto_complete_text_city_find);
            AutoCompleteTextView autoCompleteTextViewFind2 = popupView.findViewById(R.id.auto_complete_text_district_find);

            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Button find = popupView.findViewById(R.id.find_button);
            EditText key = popupView.findViewById(R.id.popup_edittext_key);

            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            autoCompleteTextViewFind.setAdapter(arrayAdapterFind);
            autoCompleteTextViewFind.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDistrictFind = "";
                    autoCompleteTextViewFind2.setHint("");
                    selectedCityFind = adapterView.getItemAtPosition(i).toString();
                    if (!selectedCityFind.isEmpty()){
                        String[] ilcelerDizisi = ilceler.get(selectedCityFind);
                        arrayAdapterFind2 = new ArrayAdapter<>(requireContext(), R.layout.list_item, ilcelerDizisi);
                        autoCompleteTextViewFind2.setAdapter(arrayAdapterFind2);
                    }
                    EditText hint = popupView.findViewById(R.id.auto_complete_text_city_find);
                    hint.setHint("");
                    autoCompleteTextViewFind.setText("");
                    autoCompleteTextViewFind.setHint("    "+selectedCityFind);
                }
            });
            autoCompleteTextViewFind2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDistrictFind = adapterView.getItemAtPosition(i).toString();
                    autoCompleteTextViewFind2.setText("");
                    autoCompleteTextViewFind2.setHint("        "+selectedDistrictFind);
                }
            });
            key.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b){
                    autoCompleteTextViewFind.setText("");
                    autoCompleteTextViewFind2.setText("");
                }
            });
            find.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findGetData(view,key,popupWindow);
                }
            });
        }
    }
    public void showPopup(View view){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if(getActivity() != null){
            View popupView = inflater.inflate(R.layout.popup_add_post, null);
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            AutoCompleteTextView autoCompleteTextView = popupView.findViewById(R.id.auto_complete_text_city);
            AutoCompleteTextView autoCompleteTextView2 = popupView.findViewById(R.id.auto_complete_text_district);

            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Button push = popupView.findViewById(R.id.push_button);
            EditText message = popupView.findViewById(R.id.popup_edittext_message);
            CheckBox checkBox = popupView.findViewById(R.id.showLocationCheckBox);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            autoCompleteTextView.setAdapter(arrayAdapter);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDistrict = "";
                    autoCompleteTextView2.setHint("");
                    selectedCity = adapterView.getItemAtPosition(i).toString();
                    if (!selectedCity.isEmpty()) {
                        String[] ilcelerDizisi = ilceler.get(selectedCity);
                        arrayAdapter2 = new ArrayAdapter<>(requireContext(), R.layout.list_item, ilcelerDizisi);
                        autoCompleteTextView2.setAdapter(arrayAdapter2);
                    }
                    EditText hint = popupView.findViewById(R.id.auto_complete_text_city);
                    hint.setHint("");
                    autoCompleteTextView.setText("");
                    autoCompleteTextView.setHint("    "+selectedCity);
                }
            });
            autoCompleteTextView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDistrict = adapterView.getItemAtPosition(i).toString();
                    autoCompleteTextView2.setText("");
                    autoCompleteTextView2.setHint("        "+selectedDistrict);
                }
            });
            push.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postUpload(view,message,popupWindow,check);
                }
            });
            message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    autoCompleteTextView.setText("");
                    autoCompleteTextView2.setText("");
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        check[0] = true;
                    } else {
                        check[0] = false;
                    }
                }
            });
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getName();
        getImage();
        getData();
    }
    private void openMenu(){
        isOpen = true;

        fab1.animate().translationY(-getResources().getDimension(R.dimen.stan_60));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.stan_110));
    }
    private void closeMenu(){
        isOpen = false;

        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
    }
    public void getName(){
        firebaseFirestore.collection("users")
                .whereEqualTo("mail", user.getEmail())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String name = documentSnapshot.getString("name");
                        MainActivity mainActivity = (MainActivity) getActivity();
                        TextView headerTextView = mainActivity.findViewById(R.id.nav_name);
                        headerTextView.setText(name);
                    }
                })
                .addOnFailureListener(e -> {
                    //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                });
    }
    public void getImage(){
        firebaseFirestore.collection("users").document(user.getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            MainActivity mainActivity = (MainActivity) getActivity();
                            ImageView headerImageView = mainActivity.findViewById(R.id.nav_img);
                            if(imageUrl.equals("image")){
                                headerImageView.setImageResource(R.drawable.user);
                            }else {
                                Picasso.get().load(imageUrl).into(headerImageView);
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
    public void semts(){
        String[] adanaIlceleri = {"Aladağ", "Ceyhan", "Çukurova", "Feke", "İmamoğlu", "Karaisalı", "Karataş", "Kozan", "Pozantı", "Saimbeyli", "Sarıçam", "Seyhan", "Tufanbeyli", "Yumurtalık", "Yüreğir"};
        ilceler.put("Adana", adanaIlceleri);
        String[] adiyamanIlceleri = {"Besni", "Çelikhan", "Gerger", "Gölbaşı", "Kahta", "Merkez", "Samsat", "Sincik", "Tut"};
        ilceler.put("Adıyaman", adiyamanIlceleri);
        String[] afyonkarahisarIlceleri = {"Başmakçı", "Bayat", "Bolvadin", "Çay", "Çobanlar", "Dazkırı", "Dinar", "Emirdağ", "Evciler", "Hocalar", "İhsaniye", "İscehisar", "Kızılören", "Merkez", "Sandıklı", "Sinanpaşa", "Sultandağı", "Şuhut"};
        ilceler.put("Afyonkarahisar", afyonkarahisarIlceleri);
        String[] agriIlceleri = {"Diyadin", "Doğubayazıt", "Eleşkirt", "Hamur", "Patnos", "Taşlıçay", "Tutak"};
        ilceler.put("Ağrı", agriIlceleri);
        String[] amasyaIlceleri = {"Amasya Merkez", "Göynücek", "Gümüşhacıköy", "Hamamözü", "Merzifon", "Suluova", "Taşova"};
        ilceler.put("Amasya", amasyaIlceleri);
        String[] ankaraIlceleri = {"Akyurt", "Altındağ", "Ayaş", "Bala", "Beypazarı", "Çamlıdere", "Çankaya", "Çubuk", "Elmadağ", "Etimesgut", "Evren", "Gölbaşı", "Güdül", "Haymana", "Kalecik", "Kazan", "Keçiören", "Kızılcahamam", "Mamak", "Nallıhan", "Polatlı", "Pursaklar", "Sincan", "Şereflikoçhisar"};
        ilceler.put("Ankara", ankaraIlceleri);
        String[] antalyaIlceleri = {"Akseki", "Aksu", "Alanya", "Demre", "Döşemealtı", "Elmalı", "Finike", "Gazipaşa", "Gündoğmuş", "İbradi", "Kaş", "Kemer", "Kepez", "Konyaaltı", "Korkuteli", "Kumluca", "Manavgat", "Muratpaşa", "Serik"};
        ilceler.put("Antalya", antalyaIlceleri);
        String[] artvinIlceleri = {"Ardanuç", "Arhavi", "Borçka", "Hopa", "Murgul", "Şavşat", "Yusufeli"};
        ilceler.put("Artvin", artvinIlceleri);
        String[] aydinIlceleri = {"Bozdoğan", "Buharkent", "Çine", "Didim", "Efeler", "Germencik", "İncirliova", "Karacasu", "Karpuzlu", "Koçarlı", "Köşk", "Kuşadası", "Kuyucak", "Nazilli", "Söke", "Sultanhisar", "Yenipazar"};
        ilceler.put("Aydın", aydinIlceleri);
        String[] balikesirIlceleri = {"Altıeylül", "Ayvalık", "Balya", "Bandırma", "Bigadiç", "Burhaniye", "Dursunbey", "Edremit", "Erdek", "Gönen", "Havran", "İvrindi", "Kepsut", "Manyas", "Marmara", "Savaştepe", "Sındırgı", "Susurluk"};
        ilceler.put("Balıkesir", balikesirIlceleri);
        String[] bartinIlceleri = {"Amasra", "Kurucaşile", "Ulus"};
        ilceler.put("Bartın", bartinIlceleri);
        String[] batmanIlceleri = {"Beşiri", "Gercüş", "Hasankeyf", "Kozluk", "Sason"};
        ilceler.put("Batman", batmanIlceleri);
        String[] bayburtIlceleri = {"Aydıntepe", "Demirözü", "Merkez"};
        ilceler.put("Bayburt", bayburtIlceleri);
        String[] bilecikIlceleri = {"Bozüyük", "Gölpazarı", "İnhisar", "Osmaneli", "Pazaryeri", "Söğüt", "Yenipazar"};
        ilceler.put("Bilecik", bilecikIlceleri);
        String[] bingolIlceleri = {"Adaklı", "Genç", "Karlıova", "Kiğı", "Solhan", "Yayladere", "Yedisu"};
        ilceler.put("Bingöl", bingolIlceleri);
        String[] bitlisIlceleri = {"Adilcevaz", "Ahlat", "Güroymak", "Hizan", "Mutki", "Tatvan"};
        ilceler.put("Bitlis", bitlisIlceleri);
        String[] boluIlceleri = {"Dörtdivan", "Gerede", "Göynük", "Kıbrıscık", "Mengen", "Mudurnu", "Seben", "Yeniçağa"};
        ilceler.put("Bolu", boluIlceleri);
        String[] burdurIlceleri = {"Ağlasun", "Altınyayla", "Bucak", "Çavdır", "Çeltikçi", "Gölhisar", "Karamanlı", "Kemer", "Tefenni", "Yeşilova"};
        ilceler.put("Burdur", burdurIlceleri);
        String[] bursaIlceleri = {"Büyükorhan", "Gemlik", "Gürsu", "Harmancık", "İnegöl", "İznik", "Karacabey", "Keles", "Kestel", "Mudanya", "Mustafakemalpaşa", "Nilüfer", "Orhaneli", "Orhangazi", "Osmangazi", "Yenişehir"};
        ilceler.put("Bursa", bursaIlceleri);
        String[] canakkaleIlceleri = {"Ayvacık", "Bayramiç", "Biga", "Bozcaada", "Çan", "Eceabat", "Ezine", "Gelibolu", "Gökçeada", "Lapseki", "Yenice"};
        ilceler.put("Çanakkale", canakkaleIlceleri);
        String[] cankiriIlceleri = {"Atkaracalar", "Bayramören", "Çerkeş", "Eldivan", "Ilgaz", "Kızılırmak", "Korgun", "Kurşunlu", "Orta", "Şabanözü", "Yapraklı"};
        ilceler.put("Çankırı", cankiriIlceleri);
        String[] corumIlceleri = {"Alaca", "Bayat", "Boğazkale", "Dodurga", "İskilip", "Kargı", "Laçin", "Mecitözü", "Oğuzlar", "Ortaköy", "Osmancık", "Sungurlu", "Uğurludağ"};
        ilceler.put("Çorum", corumIlceleri);
        String[] denizliIlceleri = {"Acıpayam", "Babadağ", "Baklan", "Bekilli", "Beyağaç", "Bozkurt", "Buldan", "Çal", "Çameli", "Çardak", "Çivril", "Güney", "Honaz", "Kale", "Sarayköy", "Serinhisar", "Tavas"};
        ilceler.put("Denizli", denizliIlceleri);
        String[] diyarbakirIlceleri = {"Bağlar", "Bismil", "Çermik", "Çınar", "Dicle", "Eğil", "Ergani", "Hani", "Hazro", "Kayapınar", "Kocaköy", "Kulp", "Lice", "Silvan", "Sur", "Yenişehir"};
        ilceler.put("Diyarbakır", diyarbakirIlceleri);
        String[] duzceIlceleri = {"Akçakoca", "Cumayeri", "Çilimli", "Düzce Merkez", "Gölyaka", "Gümüşova", "Kaynaşlı", "Yığılca"};
        ilceler.put("Düzce", duzceIlceleri);
        String[] edirneIlceleri = {"Enez", "Havsa", "İpsala", "Keşan", "Lalapaşa", "Meriç", "Merkez", "Uzunköprü"};
        ilceler.put("Edirne", edirneIlceleri);
        String[] elazigIlceleri = {"Ağın", "Alacakaya", "Arıcak", "Baskil", "Karakoçan", "Keban", "Kovancılar", "Maden", "Palu", "Sivrice", "Elazığ Merkez"};
        ilceler.put("Elazığ", elazigIlceleri);
        String[] erzincanIlceleri = {"Çayırlı", "İliç", "Kemah", "Kemaliye", "Otlukbeli", "Refahiye", "Tercan", "Üzümlü", "Erzincan Merkez"};
        ilceler.put("Erzincan", erzincanIlceleri);
        String[] erzurumIlceleri = {"Aşkale", "Aziziye", "Çat", "Hınıs", "Horasan", "İspir", "Karaçoban", "Karayazı", "Köprüköy", "Narman", "Oltu", "Olur", "Palandöken", "Pasinler", "Pazaryolu", "Şenkaya", "Tekman", "Tortum", "Uzundere", "Yakutiye"};
        ilceler.put("Erzurum", erzurumIlceleri);
        String[] eskisehirIlceleri = {"Alpu", "Beylikova", "Çifteler", "Günyüzü", "Han", "İnönü", "Mahmudiye", "Mihalıççık", "Mihalgazi", "Odunpazarı", "Sarıcakaya", "Seyitgazi", "Sivrihisar", "Tepebaşı"};
        ilceler.put("Eskişehir", eskisehirIlceleri);
        String[] gaziantepIlceleri = {"Araban", "İslahiye", "Karkamış", "Nizip", "Nurdağı", "Oğuzeli", "Şahinbey", "Şehitkamil", "Yavuzeli"};
        ilceler.put("Gaziantep", gaziantepIlceleri);
        String[] giresunIlceleri = {"Alucra", "Bulancak", "Çamoluk", "Çanakçı", "Dereli", "Doğankent", "Espiye", "Eynesil", "Görele", "Güce", "Keşap", "Piraziz", "Şebinkarahisar", "Tirebolu"};
        ilceler.put("Giresun", giresunIlceleri);
        String[] gumushaneIlceleri = {"Kelkit", "Köse", "Kürtün", "Şiran", "Torul"};
        ilceler.put("Gümüşhane", gumushaneIlceleri);
        String[] hakkariIlceleri = {"Çukurca", "Şemdinli", "Yüksekova"};
        ilceler.put("Hakkari", hakkariIlceleri);
        String[] hatayIlceleri = {"Altınözü", "Antakya", "Arsuz", "Belen", "Defne", "Dörtyol", "Erzin", "Hassa", "İskenderun", "Kırıkhan", "Kumlu", "Payas", "Reyhanlı", "Samandağ", "Yayladağı"};
        ilceler.put("Hatay", hatayIlceleri);
        String[] igdirIlceleri = {"Aralık", "Karakoyunlu", "Tuzluca"};
        ilceler.put("Iğdır", igdirIlceleri);
        String[] ispartaIlceleri = {"Aksu", "Atabey", "Eğirdir", "Gelendost", "Gönen", "Keçiborlu", "Senirkent", "Sütçüler", "Şarkikaraağaç", "Uluborlu", "Yalvaç", "Yenişarbademli"};
        ilceler.put("Isparta", ispartaIlceleri);
        String[] istanbulIlceleri = {"Adalar", "Arnavutköy", "Ataşehir", "Avcılar", "Bağcılar", "Bahçelievler", "Bakırköy", "Başakşehir", "Bayrampaşa", "Beşiktaş", "Beykoz", "Beylikdüzü", "Beyoğlu", "Büyükçekmece", "Çatalca", "Çekmeköy", "Esenler", "Esenyurt", "Eyüp", "Fatih", "Gaziosmanpaşa", "Güngören", "Kadıköy", "Kağıthane", "Kartal", "Küçükçekmece", "Maltepe", "Pendik", "Sancaktepe", "Sarıyer", "Silivri", "Sultanbeyli", "Sultangazi", "Şile", "Şişli", "Tuzla", "Ümraniye", "Üsküdar", "Zeytinburnu"};
        ilceler.put("İstanbul", istanbulIlceleri);
        String[] izmirIlceleri = {"Aliağa", "Balçova", "Bayındır", "Bayraklı", "Bergama", "Beydağ", "Bornova", "Buca", "Çeşme", "Çiğli", "Dikili", "Foça", "Gaziemir", "Güzelbahçe", "Karabağlar", "Karaburun", "Karşıyaka", "Kemalpaşa", "Kınık", "Kiraz", "Konak", "Menderes", "Menemen", "Narlıdere", "Ödemiş", "Seferihisar", "Selçuk", "Tire", "Torbalı", "Urla"};
        ilceler.put("İzmir", izmirIlceleri);
        String[] kahramanmarasIlceleri = {"Afşin", "Andırın", "Çağlayancerit", "Dulkadiroğlu", "Ekinözü", "Elbistan", "Göksun", "Nurhak", "Onikişubat", "Pazarcık", "Türkoğlu"};
        ilceler.put("Kahramanmaraş", kahramanmarasIlceleri);
        String[] karabukIlceleri = {"Eflani", "Eskipazar", "Karabük", "Ovacık", "Safranbolu", "Yenice"};
        ilceler.put("Karabük", karabukIlceleri);
        String[] karamanIlceleri = {"Ayrancı", "Başyayla", "Ermenek", "Karaman", "Kazımkarabekir", "Sarıveliler"};
        ilceler.put("Karaman", karamanIlceleri);
        String[] karsIlceleri = {"Akyaka", "Arpaçay", "Digor", "Kağızman", "Kars", "Sarıkamış", "Selim", "Susuz"};
        ilceler.put("Kars", karsIlceleri);
        String[] kastamonuIlceleri = {"Abana", "Ağlı", "Araç", "Azdavay", "Bozkurt", "Cide", "Çatalzeytin", "Daday", "Devrekani", "Doğanyurt", "Hanönü", "İhsangazi", "İnebolu", "Kastamonu", "Küre", "Pınarbaşı", "Seydiler", "Şenpazar", "Taşköprü", "Tosya"};
        ilceler.put("Kastamonu", kastamonuIlceleri);
        String[] kayseriIlceleri = {"Akkışla", "Bünyan", "Develi", "Felahiye", "Hacılar", "İncesu", "Kocasinan", "Melikgazi", "Özvatan", "Pınarbaşı", "Sarıoğlan", "Sarız", "Talas", "Tomarza", "Yahyalı", "Yeşilhisar"};
        ilceler.put("Kayseri", kayseriIlceleri);
        String[] kirikkaleIlceleri = {"Bahşılı", "Balışeyh", "Çelebi", "Delice", "Karakeçili", "Keskin", "Kırıkkale", "Sulakyurt", "Yahşihan"};
        ilceler.put("Kırıkkale", kirikkaleIlceleri);
        String[] kirklareliIlceleri = {"Babaeski", "Demirköy", "Kırklareli", "Kofçaz", "Lüleburgaz", "Pehlivanköy", "Pınarhisar"};
        ilceler.put("Kırklareli", kirklareliIlceleri);
        String[] kirsehirIlceleri = {"Akçakent", "Akpınar", "Boztepe", "Çiçekdağı", "Kaman", "Kırşehir", "Mucur"};
        ilceler.put("Kırşehir", kirsehirIlceleri);
        String[] kilisIlceleri = {"Elbeyli", "Kilis", "Musabeyli", "Polateli"};
        ilceler.put("Kilis", kilisIlceleri);
        String[] kocaeliIlceleri = {"Başiskele", "Çayırova", "Darıca", "Derince", "Dilovası", "Gebze", "Gölcük", "İzmit", "Kandıra", "Karamürsel", "Kartepe", "Körfez"};
        ilceler.put("Kocaeli", kocaeliIlceleri);
        String[] konyaIlceleri = {"Ahırlı", "Akören", "Akşehir", "Altınekin", "Beyşehir", "Bozkır", "Cihanbeyli", "Çeltik", "Çumra", "Derbent", "Derebucak", "Doğanhisar", "Emirgazi", "Ereğli", "Hadim", "Halkapınar", "Hüyük", "Ilgın", "Kadınhanı", "Karapınar", "Karapınar", "Karatay", "Kulu", "Meram", "Sarayönü", "Selçuklu", "Seydişehir", "Taşkent", "Tuzlukçu", "Yalıhüyük", "Yunak"};
        ilceler.put("Konya", konyaIlceleri);
        String[] kutahyaIlceleri = {"Altıntaş", "Aslanapa", "Çavdarhisar", "Domaniç", "Dumlupınar", "Emet", "Gediz", "Hisarcık", "Pazarlar", "Şaphane", "Simav", "Tavşanlı"};
        ilceler.put("Kütahya", kutahyaIlceleri);
        String[] malatyaIlceleri = {"Akçadağ", "Arapgir", "Arguvan", "Battalgazi", "Darende", "Doğanşehir", "Doğanyol", "Hekimhan", "Kale", "Kuluncak", "Pütürge", "Yazıhan", "Yeşilyurt"};
        ilceler.put("Malatya", malatyaIlceleri);
        String[] manisaIlceleri = {"Ahmetli", "Akhisar", "Alaşehir", "Demirci", "Gölmarmara", "Gördes", "Kırkağaç", "Köprübaşı", "Kula", "Salihli", "Sarıgöl", "Saruhanlı", "Selendi", "Soma", "Turgutlu"};
        ilceler.put("Manisa", manisaIlceleri);
        String[] mardinIlceleri = {"Artuklu", "Dargeçit", "Derik", "Kızıltepe", "Mazıdağı", "Midyat", "Nusaybin", "Ömerli", "Savur"};
        ilceler.put("Mardin", mardinIlceleri);
        String[] mersinIlceleri = {"Akdeniz", "Anamur", "Aydıncık", "Bozyazı", "Çamlıyayla", "Erdemli", "Gülnar", "Mut", "Silifke", "Tarsus"};
        ilceler.put("Mersin", mersinIlceleri);
        String[] muglaIlceleri = {"Bodrum", "Dalaman", "Datça", "Fethiye", "Köyceğiz", "Marmaris", "Menteşe", "Milas", "Ortaca", "Seydikemer", "Ula", "Yatağan"};
        ilceler.put("Muğla", muglaIlceleri);
        String[] musIlceleri = {"Bulanık", "Hasköy", "Korkut", "Malazgirt", "Merkez", "Varto"};
        ilceler.put("Muş", musIlceleri);
        String[] nevsehirIlceleri = {"Acıgöl", "Avanos", "Derinkuyu", "Gülşehir", "Hacıbektaş", "Kozaklı", "Merkez", "Ürgüp"};
        ilceler.put("Nevşehir", nevsehirIlceleri);
        String[] nigdeIlceleri = {"Altunhisar", "Bor", "Çamardı", "Çiftlik", "Merkez", "Ulukışla"};
        ilceler.put("Niğde", nigdeIlceleri);
        String[] orduIlceleri = {"Akkuş", "Altınordu", "Aybastı", "Çamaş", "Çatalpınar", "Çaybaşı", "Fatsa", "Gölköy", "Gülyalı", "Gürgentepe", "İkizce", "Kabadüz", "Kabataş", "Korgan", "Kumru", "Mesudiye", "Perşembe", "Ulubey", "Ünye"};
        ilceler.put("Ordu", orduIlceleri);
        String[] osmaniyeIlceleri = {"Bahçe", "Düziçi", "Hasanbeyli", "Kadirli", "Osmaniye Merkez", "Sumbas", "Toprakkale"};
        ilceler.put("Osmaniye", osmaniyeIlceleri);
        String[] rizeIlceleri = {"Ardeşen", "Çamlıhemşin", "Çayeli", "Derepazarı", "Fındıklı", "Güneysu", "Hemşin", "İkizdere", "İyidere", "Kalkandere", "Pazar", "Rize Merkez"};
        ilceler.put("Rize", rizeIlceleri);
        String[] sakaryaIlceleri = {"Adapazarı", "Akyazı", "Arifiye", "Erenler", "Ferizli", "Geyve", "Hendek", "Karapürçek", "Karasu", "Kaynarca", "Kocaali", "Pamukova", "Sapanca", "Serdivan", "Söğütlü", "Taraklı"};
        ilceler.put("Sakarya", sakaryaIlceleri);
        String[] samsunIlceleri = {"Alaçam", "Asarcık", "Atakum", "Ayvacık", "Bafra", "Canik", "Çarşamba", "Havza", "İlkadım", "Kavak", "Ladik", "Salıpazarı", "Tekkeköy", "Terme", "Vezirköprü", "Yakakent"};
        ilceler.put("Samsun", samsunIlceleri);
        String[] siirtIlceleri = {"Baykan", "Eruh", "Kurtalan", "Merkez", "Pervari", "Şirvan"};
        ilceler.put("Siirt", siirtIlceleri);
        String[] sinopIlceleri = {"Ayancık", "Boyabat", "Dikmen", "Durağan", "Erfelek", "Gerze", "Saraydüzü", "Sinop Merkez", "Türkeli"};
        ilceler.put("Sinop", sinopIlceleri);
        String[] sivasIlceleri = {"Akıncılar", "Altınyayla", "Divriği", "Doğanşar", "Gemerek", "Gölova", "Hafik", "İmranlı", "Kangal", "Koyulhisar", "Suşehri", "Şarkışla", "Ulaş", "Yıldızeli", "Zara"};
        ilceler.put("Sivas", sivasIlceleri);
        String[] sanliurfaIlceleri = {"Akçakale", "Birecik", "Bozova", "Ceylanpınar", "Halfeti", "Harran", "Hilvan", "Siverek", "Suruç", "Viranşehir"};
        ilceler.put("Şanlıurfa", sanliurfaIlceleri);
        String[] sirnakIlceleri = {"Beytüşşebap", "Cizre", "Güçlükonak", "İdil", "Silopi", "Uludere"};
        ilceler.put("Şırnak", sirnakIlceleri);
        String[] tekirdagIlceleri = {"Çerkezköy", "Çorlu", "Ergene", "Hayrabolu", "Kapaklı", "Malkara", "Marmaraereğlisi", "Muratlı", "Saray", "Şarköy"};
        ilceler.put("Tekirdağ", tekirdagIlceleri);
        String[] tokatIlceleri = {"Almus", "Artova", "Başçiftlik", "Erbaa", "Niksar", "Pazar", "Reşadiye", "Sulusaray", "Turhal", "Yeşilyurt", "Zile"};
        ilceler.put("Tokat", tokatIlceleri);
        String[] trabzonIlceleri = {"Akçaabat", "Araklı", "Arsin", "Beşikdüzü", "Çarşıbaşı", "Çaykara", "Dernekpazarı", "Düzköy", "Hayrat", "Köprübaşı", "Maçka", "Of", "Ortahisar", "Sürmene", "Tonya", "Vakfıkebir", "Yomra"};
        ilceler.put("Trabzon", trabzonIlceleri);
        String[] tunceliIlceleri = {"Çemişgezek", "Hozat", "Mazgirt", "Nazımiye", "Ovacık", "Pertek"};
        ilceler.put("Tunceli", tunceliIlceleri);
        String[] usakIlceleri = {"Banaz", "Eşme", "Karahallı", "Sivaslı", "Ulubey"};
        ilceler.put("Uşak", usakIlceleri);
        String[] vanIlceleri = {"Bahçesaray", "Başkale", "Çaldıran", "Çatak", "Edremit", "Erciş", "Gevaş", "Gürpınar", "İpekyolu", "Muradiye", "Özalp", "Saray", "Tuşba"};
        ilceler.put("Van", vanIlceleri);
        String[] yalovaIlceleri = {"Altınova", "Armutlu", "Çınarcık", "Çiftlikköy", "Termal"};
        ilceler.put("Yalova", yalovaIlceleri);
        String[] yozgatIlceleri = {"Akdağmadeni", "Aydıncık", "Boğazlıyan", "Çandır", "Çayıralan", "Çekerek", "Kadışehri", "Saraykent", "Sarıoğlan", "Sefaatli", "Sorgun", "Şefaatli", "Yenifakılı", "Yerköy"};
        ilceler.put("Yozgat", yozgatIlceleri);
        String[] zonguldakIlceleri = {"Alaplı", "Çaycuma", "Devrek", "Gökçebey", "Kilimli", "Kozlu"};
        ilceler.put("Zonguldak", zonguldakIlceleri);
    }
}