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
public class CustomAdapterCategories extends RecyclerView.Adapter<CustomAdapterCategories.MyViewHolder> {

    private Context context;
    private Activity activity;
    private ArrayList product_id, product_category;

    CustomAdapterCategories(Activity activity, Context context,ArrayList product_id, ArrayList product_category){
        this.activity = activity;
        this.context = context;
        this.product_id = product_id;
        this.product_category = product_category;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_rv_row, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.category_id_txt.setText(String.valueOf(product_id.get(position)));
        holder.category_txt.setText(String.valueOf(product_category.get(position)));

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateCategory.class);
                intent.putExtra("id", String.valueOf(product_id.get(position)));

                intent.putExtra("product_category", String.valueOf(product_category.get(position)));

                activity.startActivityForResult(intent, 1);
            }
        });


    }

    @Override
    public int getItemCount() {
        return product_id.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView category_id_txt, category_txt;
        LinearLayout mainLayout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            category_id_txt = itemView.findViewById(R.id.category_id_txt);
            category_txt = itemView.findViewById(R.id.category_txt);

            mainLayout = itemView.findViewById(R.id.mainLayout);
        }

    }

}
