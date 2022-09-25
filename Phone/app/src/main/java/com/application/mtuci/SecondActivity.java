package com.application.mtuci;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class SecondActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final LinearLayout bg = (LinearLayout) findViewById(R.id.linearLayout);
        bg.setOnTouchListener(new OnSwipeTouchListener(SecondActivity.this) {
            public void onSwipeRight() {
                onBackPressed();
            }
        });
        Button reg = (Button) findViewById(R.id.button3);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        Button login = (Button) findViewById(R.id.button4);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}