package com.example.RecieptScanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddCategory extends AppCompatActivity {

    EditText category_input ;
    Button add_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcategory_layout);

        category_input = findViewById(R.id.category_input);

        add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(AddCategory.this);
                if(category_input.getText().toString().trim().length()==0){
                    Toast.makeText(AddCategory.this, "Wprowadź nazwę kategorii", Toast.LENGTH_SHORT).show();
                } else if (category_input.getText().toString().trim().equalsIgnoreCase("Wszystko")
                        || category_input.getText().toString().trim().equalsIgnoreCase("Brak kategorii")  ) {
                    Toast.makeText(AddCategory.this, "Zabroniona nazwa kategorii", Toast.LENGTH_SHORT).show();
                } else {
                    myDB.addCategory(category_input.getText().toString().trim());
                    setResult(1);
                    finish();
                }

            }
        });
    }
}
