package com.cicekvolkan.missedconnections.adapter;

import static com.cicekvolkan.missedconnections.model.Chat.LAYOUT_ONE_CHAT;
import com.cicekvolkan.missedconnections.R;
import static com.cicekvolkan.missedconnections.model.Message.LAYOUT_ONE_MESSAGE;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.cicekvolkan.missedconnections.databinding.RecycleViewChatTextMeBinding;
import com.cicekvolkan.missedconnections.databinding.RecycleViewChatTextYouBinding;
import com.cicekvolkan.missedconnections.databinding.RecycleViewMessageBinding;
import com.cicekvolkan.missedconnections.fragment.MainFragmentDirections;
import com.cicekvolkan.missedconnections.fragment.MessageFragmentDirections;
import com.cicekvolkan.missedconnections.model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kotlin.time.TimeSource;

public class MessageAdapter extends RecyclerView.Adapter {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Message> messageArrayList;
    Context context;
    public MessageAdapter(ArrayList<Message> messageArrayList, Context context) {
        this.messageArrayList = messageArrayList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        switch (messageArrayList.get(position).getViewType()){
            case 1:
                return LAYOUT_ONE_MESSAGE;
            default:
                return -1;
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case LAYOUT_ONE_CHAT:
                RecycleViewMessageBinding recycleViewMessageBinding = RecycleViewMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                MessageAdapter.MessageHolder holder = new MessageAdapter.MessageHolder(recycleViewMessageBinding);



                /*holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    private boolean isLongClick = false;
                    private final int MIN_CLICK_DURATION = 300;
                    private Handler handler = new Handler();

                    private Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            isLongClick = true;
                            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grey)); // arka plan rengi koyu gri olarak ayarlanıyor
                            showPopupMenu(holder.itemView);
                        }
                    };

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                isLongClick = false;
                                handler.postDelayed(runnable, MIN_CLICK_DURATION);
                                break;
                            case MotionEvent.ACTION_UP:
                                handler.removeCallbacks(runnable);
                                if (!isLongClick) {
                                    // Normal tıklama işlemleri burada yapılıyor
                                    int position = holder.getAdapterPosition();
                                    String mail = messageArrayList.get(position).mail;
                                    String name = messageArrayList.get(position).name;
                                    MessageFragmentDirections.ActionMessageFragmentToChatFragment action = MessageFragmentDirections.actionMessageFragmentToChatFragment("", "");
                                    action.setMail(mail);
                                    action.setName(name);
                                    Navigation.findNavController(v).navigate(action);
                                } else {
                                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.last)); // arka planın rengi daha açık gri olarak geri alınıyor
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                handler.removeCallbacks(runnable);
                                v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.last)); // arka planın rengi daha açık gri olarak geri alınıyor
                                break;
                            case MotionEvent.ACTION_MOVE:
                                // Eğer uzun tıklama sırasında parmağın hareketi tespit edilirse, arka planın rengi daha açık gri olarak geri alınıyor
                                if ((System.currentTimeMillis() - event.getDownTime()) > MIN_CLICK_DURATION) {
                                    handler.removeCallbacks(runnable);
                                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.last));
                                    showPopupMenu(holder.itemView);
                                    isLongClick = true;
                                }
                                break;
                        }
                        return true;
                    }

                    private void showPopupMenu(View v) {
                        PopupMenu popup = new PopupMenu(v.getContext(), v);
                        popup.inflate(R.menu.message_menu);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.deleteMessage:
                                        new AlertDialog.Builder(v.getContext())
                                                .setTitle("Mesajları Sil")
                                                .setMessage("Mesajları silmek istediğinize emin misiniz?")
                                                .setPositiveButton("Sil", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        int position = holder.getAdapterPosition();
                                                        String mail = messageArrayList.get(position).mail;
                                                        String myMail = user.getEmail();
                                                        DeleteMessage(v,mail,myMail);
                                                        messageArrayList.remove(position);
                                                        notifyItemRemoved(position);
                                                        notifyItemRangeChanged(position, messageArrayList.size());
                                                    }
                                                })
                                                .setNegativeButton("Çık", null).show();
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        popup.show();
                    }
                });*/
                return holder;
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
        switch (messageArrayList.get(position).getViewType()){
            case LAYOUT_ONE_CHAT:
                ImageView delete =((MessageHolder) holder).recycleViewMessageBinding.recycleviewDelete;
                String[] imageUrl = new String[1];
                //String[] mail = new String[1];
                //[] name = new String[1];
                String mail,name,date,message;
                mail = messageArrayList.get(position).mail;
                name = messageArrayList.get(position).name;
                //date = messageArrayList.get(position).date;
                //message = messageArrayList.get(position).message;
                firebaseFirestore.collection("users").document(mail)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    imageUrl[0] = documentSnapshot.getString("imageUrl");
                                }
                                //((MessageHolder) holder).recycleViewMessageBinding.recycleViewLastmesssage.setText(message);
                                //((MessageHolder) holder).recycleViewMessageBinding.recycleViewTime.setText(date);
                                ((MessageHolder) holder).recycleViewMessageBinding.recycleViewName.setText(name);
                                if(imageUrl[0].equals("image")){
                                    ImageView image = ((MessageHolder) holder).recycleViewMessageBinding.recycleViewProfilePhoto;
                                    image.setImageResource(R.drawable.user);
                                }else {
                                    Picasso.get().load(imageUrl[0]).into(((MessageHolder) holder).recycleViewMessageBinding.recycleViewProfilePhoto);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch(motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                int position = holder.getAdapterPosition();
                                String mail = messageArrayList.get(position).mail;
                                String name = messageArrayList.get(position).name;
                                MessageFragmentDirections.ActionMessageFragmentToChatFragment action = MessageFragmentDirections.actionMessageFragmentToChatFragment("", "");
                                action.setMail(mail);
                                action.setName(name);
                                Navigation.findNavController(view).navigate(action);
                                break;
                        }
                        return true;
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("Mesajı Sil")
                                .setMessage("Mesajı silmek istediğinize emin misiniz?")
                                .setPositiveButton("Sil", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        int position = holder.getAdapterPosition();
                                        String mail = messageArrayList.get(position).mail;
                                        String myMail = user.getEmail();
                                        DeleteMessage(holder.itemView,mail,myMail);
                                        messageArrayList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, messageArrayList.size());
                                    }
                                })
                                .setNegativeButton("Hayır", null).show();
                    }
                });
            break;
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }
    public class MessageHolder extends RecyclerView.ViewHolder{
        RecycleViewMessageBinding recycleViewMessageBinding;
        public MessageHolder(RecycleViewMessageBinding recycleViewMessageBinding) {
            super(recycleViewMessageBinding.getRoot());
            this.recycleViewMessageBinding = recycleViewMessageBinding;
        }
    }
    public void DeleteMessage(View view,String mail,String myMail){
        String[] messageTypes = {mail + ">" + myMail, myMail + ">" + mail};

        for(String messageType : messageTypes) {
            firebaseFirestore.collection("chats").document(messageType)
                    .collection(messageType).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete();
                            }
                            firebaseFirestore.collection("chats").document(messageType).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(view.getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        }
    }

}
