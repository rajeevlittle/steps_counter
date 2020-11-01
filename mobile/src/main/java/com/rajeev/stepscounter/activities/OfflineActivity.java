package com.rajeev.stepscounter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rajeev.stepscounter.R;
import com.rajeev.stepscounter.utilities.NetworkUtils;


public class OfflineActivity extends AppCompatActivity {

    Button retryButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);


        retryButton = findViewById(R.id.connectionLostButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean connected = NetworkUtils.hasNetworkConnection(OfflineActivity.this);
                if (connected) {
                    Intent intent = new Intent(OfflineActivity.this, com.rajeev.stepscounter.activities.MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(OfflineActivity.this, (connected ? "Connection retrieved" : "Failed to connect"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}