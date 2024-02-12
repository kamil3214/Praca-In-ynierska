package com.example.RecieptScanner;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateCategory extends AppCompatActivity {

    EditText category_input, product_price_input, product_category_input, purchase_date_input;
    Button update_button, delete_button;
    String id, category_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updatecategory_layout);

        category_input = findViewById(R.id.category_input2);
        update_button = findViewById(R.id.update_button);
        delete_button = findViewById(R.id.delete_button);

        //First we call this
        getAndSetIntentData();

        //Set actionbar title after getAndSetIntentData method
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("");
        }

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //And only then we call this
                MyDatabaseHelper myDB = new MyDatabaseHelper(UpdateCategory.this);
                category_name = category_input.getText().toString().trim();
                if(category_input.getText().toString().trim().isEmpty()){
                    Toast.makeText(UpdateCategory.this, "Wprowadź nazwe", Toast.LENGTH_SHORT).show();
                } else {
                    myDB.updateCategories(id, category_name);
                    finish();
                }
            }
        });
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog();
            }
        });

    }

    void getAndSetIntentData(){
        if(getIntent().hasExtra("id") &&
                getIntent().hasExtra("product_category")){
            //Getting Data from Intent
            id = getIntent().getStringExtra("id");

            category_name = getIntent().getStringExtra("product_category");

            //Setting Intent Data
            category_input.setText(category_name);

            //Log.d("stev", title+" "+author+" "+pages);
        }else{
            Toast.makeText(this, "Brak danych", Toast.LENGTH_SHORT).show();
        }
    }

    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usunąć " + category_name + "?");
        builder.setMessage("Jesteś pewny, że chcesz usunąć kategorie " + category_name + "?");
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(UpdateCategory.this);
                myDB.deleteOneRowCategories(id);
                finish();
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