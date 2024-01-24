package com.example.pocketpal;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context context;
    private List<HelperClass> dataList;

    public WalletAdapter(Context context, List<HelperClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClass wallet = dataList.get(position);
        holder.walletName.setText(wallet.getWalletName());
        holder.walletBalance.setText(String.valueOf(wallet.getWalletBalance()));
        holder.walletCategory.setText(String.valueOf(wallet.getWalletCategory()));

        // Set the budgetCategoryIcon and budgetIconBg based on the category name
        String categoryName = wallet.getWalletCategory();
        int categoryImageRes = getCategoryImageResource(categoryName);
        int categoryBgColor = getCategoryBackgroundColor(categoryName);

        // Load category image using Glide into budgetCategoryIcon ImageView
        Glide.with(context).load(categoryImageRes).into(holder.budgetCategoryIcon);

        // Set background color for budgetIconBg ImageView
        holder.budgetIconBg.setBackgroundColor(categoryBgColor);
    }

    private int getCategoryImageResource(String categoryName) {
        switch (categoryName) {
            case "Food And Drinks":
                return R.drawable.img_food;
            case "Shopping":
                return R.drawable.img_shoppingbag;
            case "Transport":
                return R.drawable.img_car;
            case "Travel":
                return R.drawable.img_travel;
            case "Bills And Fees":
                return R.drawable.img_bills;
            case "Education":
                return R.drawable.img_education;
            case "Healthcare":
                return R.drawable.img_health;
            case "Hobbies":
                return R.drawable.img_sports;
            default:
                return R.drawable.img_question;
        }
    }

    private int getCategoryBackgroundColor(String categoryName) {
        Resources res = context.getResources();
        switch (categoryName) {
            case "Food And Drinks":
                return res.getColor(R.color.LightBlueColor);
            case "Shopping":
                return res.getColor(R.color.Orangecolor);
            case "Transport":
                return res.getColor(R.color.Purplecolor);
            case "Travel":
                return res.getColor(R.color.Greencolor);
            case "Bills And Fees":
                return res.getColor(R.color.Orangecolor);
            case "Education":
                return res.getColor(R.color.Greencolor);
            case "Healthcare":
                return res.getColor(R.color.LightBlueColor);
            case "Hobbies":
                return res.getColor(R.color.Purplecolor);
            default:
                // Return a default color for unknown categories
                return res.getColor(R.color.Greencolor);
        }
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<HelperClass> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    TextView walletName, walletBalance,walletCategory;
    CardView walletCard;
    ImageView budgetCategoryIcon, budgetIconBg;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        walletCard = itemView.findViewById(R.id.walletCard);
        walletName = itemView.findViewById(R.id.walletName);
        walletBalance = itemView.findViewById(R.id.walletBalance);
        budgetCategoryIcon = itemView.findViewById(R.id.budgetCategoryIcon);
        budgetIconBg = itemView.findViewById(R.id.budgetIconBg);
        walletCategory = itemView.findViewById(R.id.walletCategory);

        // Convert dp to pixels using TypedValue
        int iconSizeInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, itemView.getResources().getDisplayMetrics()
        );

        // Set the desired size for the budgetCategoryIcon ImageView
        ViewGroup.LayoutParams layoutParams = budgetCategoryIcon.getLayoutParams();
        layoutParams.width = iconSizeInPixels;
        layoutParams.height = iconSizeInPixels;
        budgetCategoryIcon.setLayoutParams(layoutParams);

        // Convert dp to pixels using TypedValue for budgetIconBg
        int bgSizeInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, itemView.getResources().getDisplayMetrics()
        );

        // Set the desired size for the budgetIconBg ImageView
        ViewGroup.LayoutParams bgLayoutParams = budgetIconBg.getLayoutParams();
        bgLayoutParams.width = bgSizeInPixels;
        bgLayoutParams.height = bgSizeInPixels;
        budgetIconBg.setLayoutParams(bgLayoutParams);

        // Set an OnClickListener on walletCard to display a toast message
        walletCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the toast message when the walletCard is clicked
                Toast.makeText(itemView.getContext(), "Click 'Edit My Budget' to edit your budget", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

    class WalletSpinnerAdapter extends ArrayAdapter<String> {
        private List<String> walletList;

        public WalletSpinnerAdapter(Context context, List<String> walletList) {
            super(context, android.R.layout.simple_spinner_item, walletList);
            this.walletList = walletList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText(walletList.get(position));
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText(walletList.get(position));
            return convertView;
        }


    }


