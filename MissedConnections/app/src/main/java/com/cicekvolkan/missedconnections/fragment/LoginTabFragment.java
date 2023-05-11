package com.cicekvolkan.missedconnections.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cicekvolkan.missedconnections.R;
import com.cicekvolkan.missedconnections.databinding.FragmentLoginTabBinding;
import com.cicekvolkan.missedconnections.view.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginTabFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    FragmentLoginTabBinding binding;
    public LoginTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        /*if(user != null){
            Intent intent = new Intent(getActivity().getApplicationContext(),MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginTabBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(view,binding.loginEmail.getText().toString(),binding.loginPassword.getText().toString());
            }
        });
    }
    public void login(View view,String mail,String password) {
        if (mail.equals("") || password.equals("")) {
            Toast.makeText(view.getContext(), "Mail ve Şifrenizi girin.", Toast.LENGTH_SHORT).show();
        } else {
            ProgressDialog progressDialog = new ProgressDialog(view.getContext());
            progressDialog.setMessage("Giriş yapılıyor...");
            progressDialog.show();
            auth.signInWithEmailAndPassword(mail, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(view.getContext(), "Hatalı giriş! Lütfen mail ve şifrenizi doğru giriniz", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}