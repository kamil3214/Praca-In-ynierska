package com.example.RecieptScanner;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;

public class AddProducts extends AppCompatActivity {
    LayoutInflater layoutInflater;
    Button add_button;
    Button cancel_button;
    String selected_date;
    NumberFormat price_format = new DecimalFormat("#0.00");
    Button pickDateBtn;
    Button selectedDateTV;
    LinearLayout parent;
    MyDatabaseHelper myDB;
    ArrayList<String> categories = new ArrayList<>();
    String[] listItems = {};
    int number_of_items = 0;
    int include_discounts_val=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(new Locale("pl"));

        myDB = new MyDatabaseHelper(AddProducts.this);
        Cursor cursor = myDB.readAllDataCategories();
        if(cursor.getCount() == 0){
        }else{
            while (cursor.moveToNext()){
                categories.add(cursor.getString(1));

            }

        }

        include_discounts_val = myDB.getIncludeDiscoutsValue();

        setContentView(R.layout.addproducts_layout);
        ArrayList<String> product = getIntent().getStringArrayListExtra("product_array");
        ArrayList<String> price = getIntent().getStringArrayListExtra("price_array");
        for(int i=0; i<price.size();i++){
            price.set(i, price.get(i).replaceAll("([ABCcDEFG])",""));
        }

        if(include_discounts_val==1) {
            for (int i = 0; i < price.size(); i++) {
                if (price.get(i).contains("-")) {
                    price.set(i, "");
                    if (product.size() < i) {
                        product.set(i, "");
                    }
                }
            }
        }


        ListIterator<String> iter_product = product.listIterator();
        while(iter_product.hasNext()){
            if(iter_product.next().length()==0){
                iter_product.remove();
            }
        }

        ListIterator<String> iter_price = price.listIterator();
        while(iter_price.hasNext()){
            if(iter_price.next().length()==0){
                iter_price.remove();
            }
        }


        number_of_items = Math.min(product.size(), price.size());
        if(number_of_items==0)finish();

        add_button = findViewById(R.id.add_btn);
        cancel_button = findViewById(R.id.cancel_btn);

        parent = findViewById(R.id.formLayout);
        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        selectedDateTV = findViewById(R.id.idTVSelectedDate);
        selected_date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        selectedDateTV.setText(date);

        categories.add(0, "Brak kategorii");
        listItems = Arrays.copyOf(categories.toArray(), categories.size(), String[].class);


        int addCount = 0;
        //LinearLayout mLinearLayout = binding.linearLayoutId;
        for (int i = 0; i < number_of_items; i++) {
            LinearLayout newLayout = new LinearLayout(this);
            newLayout.setBackgroundColor(Color.rgb(104, 84, 164));

            //Product Edittext
            EditText product_edittext = new EditText(this);
            product_edittext.setWidth(convertDpToPixel(180,this));
            product_edittext.setText(product.get(i));
            LinearLayout.LayoutParams product_edittext_params = new LinearLayout.LayoutParams(convertDpToPixel(180,this), (int) LinearLayout.LayoutParams.WRAP_CONTENT);
            product_edittext_params.gravity = Gravity.CENTER_VERTICAL;
            product_edittext_params.setMargins(convertDpToPixel(5,this), convertDpToPixel(5,this), convertDpToPixel(5,this), convertDpToPixel(5,this));
            product_edittext.setLayoutParams(product_edittext_params);
            product_edittext.setBackgroundColor(Color.rgb(237, 235, 216));
            product_edittext.setGravity(Gravity.CENTER);
            product_edittext.setFilters(new InputFilter[] { new InputFilter.LengthFilter(36) });
            newLayout.addView(product_edittext);



            //Price Edittext
            EditText price_edittext = new EditText(this);
            //KeyListener keyListener = DigitsKeyListener.getInstance("0123456789.,");
            //price_edittext.setKeyListener(keyListener);
            price_edittext.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });
            price_edittext.setKeyListener(DigitsKeyListener.getInstance(true,true)); // decimals and positive/negative numbers.
            price_edittext.setText(price_format.format(Double.parseDouble(price.get(i))));

            LinearLayout.LayoutParams price_edittext_params = new LinearLayout.LayoutParams(convertDpToPixel(60,this), (int) LinearLayout.LayoutParams.WRAP_CONTENT);
            price_edittext_params.gravity = Gravity.CENTER_VERTICAL;
            price_edittext_params.setMargins(convertDpToPixel(0,this), convertDpToPixel(5,this), convertDpToPixel(5,this), convertDpToPixel(5,this));
            price_edittext.setGravity(Gravity.CENTER);

            price_edittext.setLayoutParams(price_edittext_params);

            newLayout.addView(price_edittext);

            Button category_btn = new Button(this);

            LinearLayout.LayoutParams category_btn_params = new LinearLayout.LayoutParams(convertDpToPixel(100,this), LinearLayout.LayoutParams.WRAP_CONTENT);
            category_btn_params.gravity = Gravity.CENTER_VERTICAL;
            category_btn_params.setMargins(convertDpToPixel(0,this), convertDpToPixel(5,this), convertDpToPixel(6,this), convertDpToPixel(5,this));

            category_btn.setLayoutParams(category_btn_params);
            category_btn.setHint("Kategoria");
            category_btn.setHintTextColor(Color.BLACK);
            newLayout.addView(category_btn);

            Button delete_button = new Button(this);
            LinearLayout.LayoutParams delete_btn_params = new LinearLayout.LayoutParams(convertDpToPixel(40,this), LinearLayout.LayoutParams.MATCH_PARENT);
            delete_btn_params.gravity = Gravity.CENTER_VERTICAL;
            delete_button.setLayoutParams(delete_btn_params);
            delete_button.setText("X");
            delete_button.setTextSize(convertDpToPixel(6,this));
            delete_button.setBackgroundColor(Color.RED);

            newLayout.addView(delete_button);

            if(i%2==0){
                product_edittext.setBackgroundColor(Color.rgb(255, 255, 255));
                price_edittext.setBackgroundColor(Color.rgb(255, 255, 255));
                category_btn.setBackgroundColor(Color.rgb(255, 255, 255));

            }else{
                product_edittext.setBackgroundColor(Color.rgb(255, 255, 255));
                price_edittext.setBackgroundColor(Color.rgb(255, 255, 255));
                category_btn.setBackgroundColor(Color.rgb(255, 255, 255));


            }

            parent.addView(newLayout);
            ((LinearLayout.MarginLayoutParams) newLayout.getLayoutParams()).setMargins(convertDpToPixel(5,this), convertDpToPixel(5,this), convertDpToPixel(5,this), convertDpToPixel(0,this));
            delete_button.setOnClickListener(v -> { // v is the button
                parent.removeView((ViewGroup) v.getParent());
            });


            category_btn.setOnClickListener(v -> { // v is the button
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddProducts.this);
                mBuilder.setTitle("Wybierz kategorie");
                mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listItems[i].equalsIgnoreCase("Brak kategorii")) {
                            category_btn.setText("");
                            dialogInterface.dismiss();
                        } else {
                            category_btn.setText(listItems[i]);
                            dialogInterface.dismiss();
                        }
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            });

        }

        add_button.setOnClickListener(v -> {
            for(int i=0;i<parent.getChildCount();i++){
                LinearLayout child = (LinearLayout) parent.getChildAt(i);
                EditText product_et = (EditText) child.getChildAt(0);
                EditText price_et = (EditText) child.getChildAt(1);
                Button category_btn = (Button) child.getChildAt(2);

                String price_temp = price_et.getText().toString().trim().replaceAll("[,]", ".");
                String[] parts = price_temp.split("[.]", 2);
                if(parts.length>1) {
                    price_temp = parts[0] + "." + parts[1].replaceAll("[.]", "");
                }
                String product_temp = product_et.getText().toString().trim();

                if(product_temp.isEmpty() && price_temp.isEmpty()){

                } else {
                    if (product_temp.isEmpty()) {
                        product_temp = "Bez nazwy";
                    }
                    if (price_temp.trim().isEmpty()) {
                        price_temp = "0";
                    }
                    double rounded_price = Double.parseDouble(price_temp.trim());
                    rounded_price = (double) Math.round(rounded_price * 100) / 100;
                    MyDatabaseHelper myDB = new MyDatabaseHelper(AddProducts.this);

                    myDB.addPurchase(product_temp,
                            String.valueOf(rounded_price),
                            category_btn.getText().toString().trim(),
                            selected_date);
                }

            }
            Toast.makeText(AddProducts.this, "Dodano", Toast.LENGTH_SHORT).show();
            finish();

        });

        cancel_button.setOnClickListener(v -> {
            finish();
        });

        selectedDateTV.setOnClickListener(new View.OnClickListener() {
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

                DateTimeFormatter f = DateTimeFormatter.ofPattern( "dd-MM-yyyy" ) ;
                LocalDate ld = LocalDate.parse( selectedDateTV.getText().toString() , f ) ;
                year = ld.getYear();
                month = ld.getMonthValue()-1;
                day = ld.getDayOfMonth();

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        AddProducts.this,
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
                                selectedDateTV.setText(day_temp + "-" + month_temp + "-" + year);
                                selected_date = year+ "-" + month_temp + "-" +day_temp;


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
    public static int convertDpToPixel(float dp, Context context){
        return Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
