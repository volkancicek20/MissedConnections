package com.cicekvolkan.missedconnections.fragment;

import static android.content.Context.WIFI_SERVICE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.databinding.FragmentLoginTabBinding;
import com.cicekvolkan.missedconnections.databinding.FragmentSignupTabBinding;
import com.cicekvolkan.missedconnections.view.LoginActivity;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignupTabFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TabLayout tabLayout;
    private int ipAddress;

    private FragmentSignupTabBinding binding;
    public SignupTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        if(user != null){
            Intent intent = new Intent(getActivity().getApplicationContext(),MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupTabBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText mail = binding.signupEmail;
        EditText password = binding.signupPassword;
        EditText confirmPassword = binding.signupConfirm;

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup(view,mail.getText().toString(),password.getText().toString(),confirmPassword.getText().toString());
            }
        });
    }


    public void signup(View view,String mail,String password,String confirmPassword){

        String[] chatParticipants = mail.split("@");
        String checkMail = chatParticipants[0];
        String[] chatParticipants2 = mail.split("\\.");
        String checkMail2 = chatParticipants2[0];

        if (checkMail.contains("/") || checkMail.contains(".") || checkMail.contains("#")
                || checkMail.contains("$") || checkMail.contains("[") || checkMail.contains("]")) {
            Toast.makeText(view.getContext(), "Mailinizde özel karakterler girmeyiniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        /*if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            Toast.makeText(view.getContext(), "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show();
            return;
        }*/

        if(!mail.endsWith(".com")) {
            Toast.makeText(view.getContext(), "Lütfen geçerli bir e-posta adresi girin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(confirmPassword)) {
            Toast.makeText(view.getContext(), "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] check = new boolean[1];
        check[0] = true;

        CollectionReference usersRef = firebaseFirestore.collection("users");
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String[] chatParticipants3 = document.getId().split("\\.");
                    String checkMail3 = chatParticipants3[0];
                    if (checkMail3.equals(checkMail2)) {
                        Toast.makeText(view.getContext(), "Mail kullanımda.Lütfen farklı bir mail adresi kullanınız.", Toast.LENGTH_SHORT).show();
                        check[0] = false;
                        break;
                    }
                }
                if(check[0]){
                    checkIpRegister(view, mail, password);
                }
            }
        });
    }

    public void checkIpRegister(View view,String mail,String password){
        ProgressDialog progressDialog = ProgressDialog.show(view.getContext(), "", "Kayıt ediliyor...", true);
        boolean[] ipCheck = new boolean[1];

        firebaseFirestore.collection("IpAddress").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot snapshot : task.getResult()) {
                    Map<String, Object> check = snapshot.getData();
                    String IP = (String) check.get("ipAddress");
                    String ipString = String.format(
                            "%d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));

                    if(IP.equals(ipString)){
                        ipCheck[0] = false;
                        Toast.makeText(view.getContext(),"Birden fazla hesap oluşturamazsınız!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        break;
                    } else {
                        ipCheck[0] = true;
                    }
                }
                if(ipCheck[0]){
                    if (mail.equals("") || password.equals("")) {
                        Toast.makeText(view.getContext(), "Mail ve Şifrenizi giriniz.", Toast.LENGTH_SHORT).show();
                    } else {
                        auth.createUserWithEmailAndPassword(mail, password)
                                .addOnSuccessListener(authResult -> {
                                    String ipString = String.format(
                                            "%d.%d.%d.%d",
                                            (ipAddress & 0xff),
                                            (ipAddress >> 8 & 0xff),
                                            (ipAddress >> 16 & 0xff),
                                            (ipAddress >> 24 & 0xff));
                                    HashMap<String,Object> ipMap = new HashMap<>();
                                    ipMap.put("ipAddress",ipString);
                                    firebaseFirestore.collection("IpAddress").add(ipMap)
                                            .addOnSuccessListener(documentReference -> {
                                                progressDialog.dismiss(); // progressDialog kapatılıyor
                                                user = authResult.getUser();
                                                String[] parts = user.getEmail().split("@");
                                                String username = parts[0];
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("date", FieldValue.serverTimestamp());
                                                map.put("mail", user.getEmail());
                                                map.put("name", username);
                                                map.put("imageUrl","image");
                                                firebaseFirestore.collection("users").document(user.getEmail()).set(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                Intent intent = new Intent(view.getContext(),MainActivity.class);
                                                                startActivity(intent);
                                                                requireActivity().finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss(); // progressDialog kapatılıyor
                                                Toast.makeText(view.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss(); // progressDialog kapatılıyor
                                    Toast.makeText(view.getContext(),"Mail kullanımda ya da yanlış girildi!",Toast.LENGTH_SHORT).show();
                                    binding.signupEmail.setText("");
                                });
                    }
                }
            } else {
                progressDialog.dismiss(); // progressDialog kapatılıyor
                Toast.makeText(view.getContext(), "Erişilemedi.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}