package com.example.RecieptScanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BrowsePurchases extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView no_data;
    double sum = 0;
    int sort_options_val = 0;
    SearchView searchView;
    Button category_btn, sort_btn;
    TextView cumulative_price;
    NumberFormat price_format = new DecimalFormat("#0.00");
    final String VISIBLE_FORMAT = "dd-MM-yyyy";
    final String HIDDEN_FORMAT = "yyyy-MM-dd";
    String date_start = "0001-01-01";
    String date_end = "9999-01-01";
    Button chose_date_start, chose_date_end;
    MyDatabaseHelper myDB;
    ArrayList<String> product_id ,product_price, product_name, product_category, purchase_date;
    CustomAdapterProducts customAdapter;
    ArrayList<String> categories = new ArrayList<>();
    String[] listItems = {};
    String[] sortOptions = {"Data ▼", "Data ▲", "Cena ▼", "Cena ▲", "Nazwa ▼", "Nazwa ▲" };
    Context context_this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browsepurchases_layout);
        Locale.setDefault(new Locale("pl"));

        recyclerView = findViewById(R.id.recyclerView);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);
        category_btn = findViewById(R.id.chosen_category);
        cumulative_price = findViewById(R.id.cumulative);
        sort_btn = findViewById(R.id.sort_option_btn);
        chose_date_start = findViewById(R.id.chose_date_start);
        chose_date_end = findViewById(R.id.chose_date_end);
        category_btn.setText("Wszystko");
        searchView = findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });



        myDB = new MyDatabaseHelper(BrowsePurchases.this);
        product_id = new ArrayList<>();
        product_price = new ArrayList<>();
        product_name = new ArrayList<>();
        product_category = new ArrayList<>();
        purchase_date = new ArrayList<>();

        storeDataInArrays(date_start,date_end, sort_options_val);

        customAdapter = new CustomAdapterProducts(BrowsePurchases.this,this,product_id, product_price, product_name, product_category,
                purchase_date);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(BrowsePurchases.this));


        myDB = new MyDatabaseHelper(BrowsePurchases.this);
        Cursor cursor = myDB.readAllDataCategories();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                categories.add(cursor.getString(1));

            }

        }

        sort_btn.setText("Sortuj: "+sortOptions[0]);

        chose_date_start.setText("Od: "+ myDB.getMinDate());
        chose_date_end.setText("Do: " +myDB.getMaxDate());
        //category_btn.setText("Wszystko");

        categories.add(0, "Brak kategorii");
        categories.add(0, "Wszystko");
        listItems = Arrays.copyOf(
                categories.toArray(), categories.size(), String[].class);


        sum = 0;
        for(int i = 0; i < product_price.size(); i++) {
            sum += Double.parseDouble(product_price.get(i));
        }
        cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");






        category_btn.setOnClickListener(v -> { // v is the button
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(BrowsePurchases.this);
            mBuilder.setTitle("Wybierz kategorie");
            mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    category_btn.setText(listItems[i]);
                    dialogInterface.dismiss();


                    if(category_btn.getText().toString().trim().equals("Wszystko")){
                        storeDataInArrays(date_start, date_end, sort_options_val);
                    } else if (category_btn.getText().toString().trim().equals("Brak kategorii")) {
                        storeDataInArraysWithCategory("", date_start, date_end,sort_options_val);
                    } else {
                        storeDataInArraysWithCategory(category_btn.getText().toString().trim(), date_start, date_end,sort_options_val);
                    }

                    customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id, product_price, product_name, product_category,
                            purchase_date);
                    recyclerView.setAdapter(customAdapter);

                    sum = 0;
                    for(int k = 0; k < product_price.size(); k++) {
                        sum += Double.parseDouble(product_price.get(k));
                    }
                    cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");
                    searchView.setQuery("", false);
                    searchView.clearFocus();


                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        sort_btn.setOnClickListener(v -> { // v is the button
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(BrowsePurchases.this);
            mBuilder.setTitle("Wybierz kategorie");
            mBuilder.setSingleChoiceItems(sortOptions, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sort_btn.setText("Sortuj: " + sortOptions[i]);
                    dialogInterface.dismiss();
                    sort_options_val = i;
                    if(category_btn.getText().toString().equals("Wszystko")){
                        storeDataInArrays(date_start,date_end,sort_options_val);
                    } else if (category_btn.getText().toString().trim().equals("Brak kategorii")) {
                        storeDataInArraysWithCategory("", date_start, date_end, sort_options_val);
                    } else {
                        storeDataInArraysWithCategory(category_btn.getText().toString().trim(), date_start, date_end,sort_options_val);
                    }

                    customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id, product_price, product_name, product_category,
                            purchase_date);
                    recyclerView.setAdapter(customAdapter);

                    searchView.setQuery("", false);
                    searchView.clearFocus();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });


        chose_date_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                if(!chose_date_start.getText().toString().equals("Od: Data od")){
                    DateTimeFormatter f = DateTimeFormatter.ofPattern( "dd-MM-yyyy" ) ;
                    LocalDate ld = LocalDate.parse( chose_date_start.getText().toString().replaceAll("[OoDd:]", "").trim() , f ) ;
                    year = ld.getYear();
                    month = ld.getMonthValue()-1;
                    day = ld.getDayOfMonth();
                }

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        BrowsePurchases.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                String day_temp ;
                                String month_temp;
                                if (dayOfMonth<10){
                                    day_temp = "0" +dayOfMonth;
                                } else {
                                    day_temp=String.valueOf(dayOfMonth);
                                }

                                if (monthOfYear<9){
                                    month_temp = "0" +(monthOfYear + 1);
                                } else {
                                    month_temp=String.valueOf((monthOfYear + 1));
                                }
                                chose_date_start.setText("Od: "+day_temp + "-" + month_temp + "-" + year);
                                date_start = year+ "-" + month_temp + "-" +day_temp;

                                if(category_btn.getText().toString().trim().equals("Wszystko")){
                                    storeDataInArrays(date_start, date_end,sort_options_val);
                                } else if (category_btn.getText().toString().trim().equals("Brak kategorii")) {
                                    storeDataInArraysWithCategory("", date_start, date_end, sort_options_val);
                                } else {
                                    storeDataInArraysWithCategory(category_btn.getText().toString().trim(), date_start, date_end,sort_options_val);
                                }

                                customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id, product_price, product_name, product_category,
                                        purchase_date);
                                recyclerView.setAdapter(customAdapter);

                                sum = 0;
                                for(int k = 0; k < product_price.size(); k++) {
                                    sum += Double.parseDouble(product_price.get(k));
                                }
                                cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");
                                searchView.setQuery("", false);
                                searchView.clearFocus();


                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        chose_date_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                if(!chose_date_end.getText().toString().equals("Do: Data do")){
                    DateTimeFormatter f = DateTimeFormatter.ofPattern( "dd-MM-yyyy" ) ;
                    LocalDate ld = LocalDate.parse( chose_date_end.getText().toString().replaceAll("[OoDd:]", "").trim() , f ) ;
                    year = ld.getYear();
                    month = ld.getMonthValue()-1;
                    day = ld.getDayOfMonth();
                }

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        BrowsePurchases.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                String day_temp ;
                                String month_temp;
                                if (dayOfMonth<10){
                                    day_temp = "0" +dayOfMonth;
                                } else {
                                    day_temp=String.valueOf(dayOfMonth);
                                }

                                if (monthOfYear<9){
                                    month_temp = "0" +(monthOfYear + 1);
                                } else {
                                    month_temp=String.valueOf((monthOfYear + 1));
                                }
                                chose_date_end.setText("Do: "+day_temp + "-" + month_temp + "-" + year);
                                date_end = year+ "-" + month_temp + "-" +day_temp;

                                if(category_btn.getText().toString().trim().equals("Wszystko")){
                                    storeDataInArrays(date_start, date_end,sort_options_val);
                                } else if (category_btn.getText().toString().trim().equals("Brak kategorii")) {
                                    storeDataInArraysWithCategory("", date_start, date_end,sort_options_val);
                                } else {
                                    storeDataInArraysWithCategory(category_btn.getText().toString().trim(), date_start, date_end,sort_options_val);
                                }
                                customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id, product_price, product_name, product_category,
                                        purchase_date);
                                recyclerView.setAdapter(customAdapter);

                                sum = 0;
                                for(int k = 0; k < product_price.size(); k++) {
                                    sum += Double.parseDouble(product_price.get(k));
                                }
                                cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");
                                searchView.setQuery("", false);
                                searchView.clearFocus();


                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

    }

    private void filterList(String newText) {

        ArrayList<String> product_id_searched = new ArrayList<>();
        ArrayList<String> product_price_searched = new ArrayList<>();
        ArrayList<String> product_name_searched = new ArrayList<>();
        ArrayList<String> product_category_searched = new ArrayList<>();
        ArrayList<String> purchase_date_searched = new ArrayList<>();

        for (int i = 0; i<product_name.size(); i++){
            if(product_name.get(i).toLowerCase().contains((newText.toLowerCase()))){
                product_id_searched.add(product_id.get(i));
                product_price_searched.add(product_price.get(i));
                product_name_searched.add(product_name.get(i));
                product_category_searched.add(product_category.get(i));
                purchase_date_searched.add(purchase_date.get(i));

            }
        }

        customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id_searched, product_price_searched, product_name_searched, product_category_searched,
                purchase_date);
        recyclerView.setAdapter(customAdapter);
        sum = 0;
        for(int k = 0; k < product_price_searched.size(); k++) {
            sum += Double.parseDouble(product_price_searched.get(k));
        }
        cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(category_btn.getText().toString().trim().equals("Wszystko")){
                storeDataInArrays(date_start, date_end, sort_options_val);
            } else if (category_btn.getText().toString().trim().equals("Brak kategorii")) {
                storeDataInArraysWithCategory("", date_start, date_end,sort_options_val);
            } else {
                storeDataInArraysWithCategory(category_btn.getText().toString().trim(), date_start, date_end,sort_options_val);
            }
            customAdapter = new CustomAdapterProducts(BrowsePurchases.this,context_this,product_id, product_price, product_name, product_category,
                    purchase_date);
            recyclerView.setAdapter(customAdapter);

            sum = 0;
            for(int k = 0; k < product_price.size(); k++) {
                sum += Double.parseDouble(product_price.get(k));
            }
            cumulative_price.setText(price_format.format(sum).replaceAll("[.]",",")+"zł");
        }
    }

    void storeDataInArrays(String date_start, String date_end, int sort_option){
        product_id = new ArrayList<>();
        product_price = new ArrayList<>();
        product_name = new ArrayList<>();
        product_category = new ArrayList<>();
        purchase_date = new ArrayList<>();
        Cursor cursor = myDB.readAllData(date_start, date_end, sort_option);
        if(cursor.getCount() == 0){
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                product_id.add(cursor.getString(0));
                product_name.add(cursor.getString(1));
                product_price.add(price_format.format(Double.parseDouble(cursor.getString(2))).replaceAll("[,]","."));
                product_category.add(cursor.getString(3));
                String newDateString;

                SimpleDateFormat sdf = new SimpleDateFormat(HIDDEN_FORMAT);
                Date d;
                try {
                    d = sdf.parse(cursor.getString(4));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                sdf.applyPattern(VISIBLE_FORMAT);
                newDateString = sdf.format(d);
                purchase_date.add(newDateString);
            }
            empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
        }
    }

    void storeDataInArraysWithCategory(String category, String date_start, String date_end, int sort_option){
        product_id = new ArrayList<>();
        product_price = new ArrayList<>();
        product_name = new ArrayList<>();
        product_category = new ArrayList<>();
        purchase_date = new ArrayList<>();
        Cursor cursor = myDB.readAllDataWithCategory(category, date_start, date_end, sort_option);
        if(cursor.getCount() == 0){
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                product_id.add(cursor.getString(0));
                product_name.add(cursor.getString(1));
                product_price.add(price_format.format(Double.parseDouble(cursor.getString(2))).replaceAll(",","."));
                product_category.add(cursor.getString(3));
                String newDateString;

                SimpleDateFormat sdf = new SimpleDateFormat(HIDDEN_FORMAT);
                Date d;
                try {
                    d = sdf.parse(cursor.getString(4));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                sdf.applyPattern(VISIBLE_FORMAT);
                newDateString = sdf.format(d);
                purchase_date.add(newDateString);
            }
            empty_imageview.setVisibility(View.GONE);
            no_data.setVisibility(View.GONE);
        }
    }
}