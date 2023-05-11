package com.cicekvolkan.missedconnections.adapter;

import static com.cicekvolkan.missedconnections.model.Chat.LAYOUT_ONE_CHAT;
import static com.cicekvolkan.missedconnections.model.Chat.LAYOUT_TWO_CHAT;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cicekvolkan.missedconnections.databinding.RecycleViewChatTextMeBinding;
import com.cicekvolkan.missedconnections.databinding.RecycleViewChatTextYouBinding;
import com.cicekvolkan.missedconnections.model.Chat;
import com.cicekvolkan.missedconnections.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Chat> chatArraylist;
    Context context;

    public ChatAdapter(ArrayList<Chat> chatArraylist, Context context) {
        this.chatArraylist = chatArraylist;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        switch (chatArraylist.get(position).getViewType()){
            case 1:
                return LAYOUT_ONE_CHAT;
            case 2:
                return LAYOUT_TWO_CHAT;
            case 3:
                return 3;
            default:
                return -1;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case LAYOUT_ONE_CHAT:
                RecycleViewChatTextMeBinding recycleViewChatTextMeBinding = RecycleViewChatTextMeBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new ChatAdapter.ChatHolderMe(recycleViewChatTextMeBinding);
            case LAYOUT_TWO_CHAT:
                RecycleViewChatTextYouBinding recycleViewChatTextYouBinding = RecycleViewChatTextYouBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new ChatAdapter.ChatHolderYou(recycleViewChatTextYouBinding);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String userMail="";
        if(user != null){
            userMail = user.getEmail();
        }
        switch (chatArraylist.get(position).getViewType()){
            case LAYOUT_ONE_CHAT:
                TextView message = ((ChatHolderMe) holder).recycleViewChatTextMeBinding.messageText;
                message.setGravity(Gravity.START);
                ((ChatHolderMe) holder).recycleViewChatTextMeBinding.messageText.setText(chatArraylist.get(position).message);
                ((ChatHolderMe) holder).recycleViewChatTextMeBinding.timeChatText.setText(chatArraylist.get(position).date);
                break;
            case LAYOUT_TWO_CHAT:
                ((ChatHolderYou) holder).recycleViewChatTextYouBinding.messageText.setText(chatArraylist.get(position).message);
                ((ChatHolderYou) holder).recycleViewChatTextYouBinding.timeChatText.setText(chatArraylist.get(position).date);
                break;
        }
    }
    @Override
    public int getItemCount() {
        return chatArraylist.size();
    }

    public class ChatHolderMe extends RecyclerView.ViewHolder{
        RecycleViewChatTextMeBinding recycleViewChatTextMeBinding;
        public ChatHolderMe(RecycleViewChatTextMeBinding recycleViewChatTextMeBinding) {
            super(recycleViewChatTextMeBinding.getRoot());
            this.recycleViewChatTextMeBinding = recycleViewChatTextMeBinding;
        }
    }
    public class ChatHolderYou extends RecyclerView.ViewHolder{
        RecycleViewChatTextYouBinding recycleViewChatTextYouBinding;
        public ChatHolderYou(RecycleViewChatTextYouBinding recycleViewChatTextYouBinding) {
            super(recycleViewChatTextYouBinding.getRoot());
            this.recycleViewChatTextYouBinding = recycleViewChatTextYouBinding;
        }
    }
}
