package com.appsians.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.appsians.strangers.R;
import com.appsians.strangers.databinding.ActivityCallBinding;
import com.appsians.strangers.models.InterfaceJava;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;
    String uniqeId = "";
    FirebaseAuth auth;
    String username  = "";
    String friendsUsername = "";
    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;

    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy ;

    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        friendsUsername = "";

        if(incoming.equalsIgnoreCase(friendsUsername))
            friendsUsername = incoming;

        setupWebView();

        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                CallJavaScriptFunction("javascript:toogleAudio(\"" + isAudio + "\"");

                if(isAudio){
                    binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
                } else {
                    binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });


        binding.vidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                CallJavaScriptFunction("javascript:toggleVideo(\"" + isAudio + "\")");

                if(isAudio){
                    binding.micBtn.setImageResource(R.drawable.btn_video_normal);
                } else {
                    binding.micBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        uniqeId = getUniqueId();

    }

    void setupWebView(){
        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
               // super.onPermissionRequest(request);
                request.grant(request.getResources());
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this),"Android");

        loadVideoCall();
        }

        public void loadVideoCall(){

        String filePath = "file:android_assets/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                 initializePeer();
            }
        });

    }




    void initializePeer(){
        uniqeId = getUniqueId();

        CallJavaScriptFunction("javascript:init(\"" + uniqeId + "\")" );

        if(createdBy.equalsIgnoreCase(username)){
            firebaseRef.child(username).child("connId").setValue(uniqeId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.controls.setVisibility(View.VISIBLE);
        } else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null){
                                         sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            },2000);
        }

    }

    public  void onPeerConnected(){
        isPeerConnected = true;
    }

    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "you are not connected,please check ur internet", Toast.LENGTH_SHORT).show();
            return;
        }

             listenConnId();
    }

    void listenConnId(){
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null)
                    return ;
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                CallJavaScriptFunction("javascript:startCall(\"" + connId + "\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void CallJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
            binding.webView.evaluateJavascript(function,null);
            }
        });
    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

}