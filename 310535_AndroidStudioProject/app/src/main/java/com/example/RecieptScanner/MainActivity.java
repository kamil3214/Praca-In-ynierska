package com.example.RecieptScanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity {

    TextRecognizer textRecognizer;
    FloatingActionButton camera_scan_btn, gallery_scan_btn, categories_btn, product_add_btn, settings_btn, purchases_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_layout);
        camera_scan_btn = findViewById(R.id.camera_scan_btn);
        gallery_scan_btn = findViewById(R.id.gallery_scan_btn);
        categories_btn = findViewById(R.id.categories_btn);
        product_add_btn = findViewById(R.id.product_add_btn);
        purchases_btn = findViewById(R.id.purchases_btn);
        settings_btn = findViewById(R.id.setting_btn);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        camera_scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getApplicationContext(), ProcessReciept.class);
                intent.putExtra("key", "camera");
                MainActivity.this.startActivity(intent);

            }
        });

        gallery_scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ProcessReciept.class);
                intent.putExtra("key", "gallery");
                MainActivity.this.startActivity(intent);

            }
        });

        purchases_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent browse  = new Intent(getApplicationContext(), BrowsePurchases.class);
                MainActivity.this.startActivity(browse);

            }
        });

        product_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent browse  = new Intent(getApplicationContext(), AddSingleProduct.class);
                MainActivity.this.startActivity(browse);

            }
        });

        categories_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent browse  = new Intent(getApplicationContext(), BrowseCategories.class);
                MainActivity.this.startActivity(browse);

            }
        });

        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent browse  = new Intent(getApplicationContext(), Settings.class);
                MainActivity.this.startActivity(browse);

            }
        });

    }

}




