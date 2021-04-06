package com.example.singletonplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    MySingleton singleton = MySingleton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLifecycle().addObserver(singleton);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView text = findViewById(R.id.text);
        text.setText(singleton.res);
    }

    @Override
    protected void onDestroy() {
        singleton.releaseRes();
        super.onDestroy();
    }

    static class MySingleton extends SingletonPlus {
        private String res;
        private static MySingleton instance;

        public static MySingleton getInstance() {
            if (instance == null) {
                synchronized (SingletonPlus.class) {
                    if (instance == null) {
                        instance = new MySingleton();
                    }
                }
            }
            return instance;
        }

        @Override
        public void initRes() {
            super.initRes();
            res += "a";
        }

        @Override
        public void releaseRes() {
            super.releaseRes();
            res = "";
        }
    }
}