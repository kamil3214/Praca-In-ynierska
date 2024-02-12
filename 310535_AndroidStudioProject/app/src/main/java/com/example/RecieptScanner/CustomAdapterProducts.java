package com.example.RecieptScanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterProducts extends RecyclerView.Adapter<CustomAdapterProducts.MyViewHolder> {

    private Context context;
    private Activity activity;
    private ArrayList product_id,product_price, product_name, product_category, purchase_date;

    CustomAdapterProducts(Activity activity, Context context, ArrayList product_id, ArrayList product_price, ArrayList product_name, ArrayList product_category,
                          ArrayList purchase_date){
        this.activity = activity;
        this.context = context;
        this.product_id = product_id;
        this.product_price = product_price;
        this.product_name = product_name;
        this.product_category = product_category;
        this.purchase_date = purchase_date;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.purchase_rv_row, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.purchase_id_txt.setText(String.valueOf(product_id.get(position)));
        holder.product_price_txt.setText(String.valueOf(product_price.get(position)).replaceAll("[.]", ","));
        holder.product_name_txt.setText(String.valueOf(product_name.get(position)));
        holder.product_category_txt.setText(String.valueOf(product_category.get(position)));
        holder.purchase_date_txt.setText(String.valueOf(purchase_date.get(position)));

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateProduct.class);
                intent.putExtra("id", String.valueOf(product_id.get(position)));
                intent.putExtra("product_name", String.valueOf(product_name.get(position)));
                intent.putExtra("product_price", String.valueOf(product_price.get(position)));
                intent.putExtra("product_category", String.valueOf(product_category.get(position)));
                intent.putExtra("purchase_date", String.valueOf(purchase_date.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });


    }

    @Override
    public int getItemCount() {
        return product_price.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView purchase_id_txt, product_price_txt, product_name_txt, product_category_txt, purchase_date_txt;
        LinearLayout mainLayout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            purchase_id_txt = itemView.findViewById(R.id.purchase_id_txt);
            product_price_txt = itemView.findViewById(R.id.product_price_txt);
            product_name_txt = itemView.findViewById(R.id.product_name_txt);
            product_category_txt = itemView.findViewById(R.id.product_category_txt);
            purchase_date_txt = itemView.findViewById(R.id.purchase_date_txt);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }

    }

}
