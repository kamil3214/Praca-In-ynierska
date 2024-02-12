package com.example.RecieptScanner;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Settings extends AppCompatActivity {

    MyDatabaseHelper myDB;
    Switch includeDiscoutsSwitch;
    FloatingActionButton delete_purchases;
    FloatingActionButton delete_categories;
    boolean switch_value = false;
    int temp_switch_value = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        delete_purchases = findViewById(R.id.delete_purchases_btn);
        delete_categories = findViewById(R.id.delete_categories_btn);

        delete_purchases.setOnClickListener(v -> {
            deleteData_confirmDialog();
        });

        delete_categories.setOnClickListener(v -> {
            deleteCategories_confirmDialog();
        });

        includeDiscoutsSwitch = findViewById(R.id.includeDiscoutsSwitch);
        myDB = new MyDatabaseHelper(Settings.this);
        temp_switch_value = myDB.getIncludeDiscoutsValue();
        if(temp_switch_value==0){
            switch_value = false;
        } else if (temp_switch_value==1) {
            switch_value = true;
        }

        includeDiscoutsSwitch.setChecked(switch_value);
        includeDiscoutsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    myDB.updateIncludeDiscoutsValue(1);
                } else {
                    myDB.updateIncludeDiscoutsValue(0);
                }
            }
        });

    }

    void deleteData_confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usunąć wszystkie zakupy?");
        builder.setMessage("Jesteś pewny że chcesz usunąć wszystkie zakupy?");
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(Settings.this);
                myDB.deleteAllData();
                //Refresh Activity

            }
        });
        builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    void deleteCategories_confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usunąć wszystkie kategorie?");
        builder.setMessage("Jesteś pewny że chcesz usunąć wszystkie kategorie?");
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(Settings.this);
                myDB.deleteAllCategories();
                //Refresh Activity

            }
        });
        builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }
}
