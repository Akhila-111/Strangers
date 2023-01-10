package com.appsians.strangers.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.appsians.strangers.R;
import com.appsians.strangers.databinding.ActivityConnectingBinding;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_connecting);
    }
}