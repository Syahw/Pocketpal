package com.example.pocketpal;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class RecentSpendAdapter extends RecyclerView.Adapter<RecentSpendAdapter.ViewHolder> {
    private List<HelperClass> transactionsList; // Change the data type here
    private Context context; // Add a Context variable

    public RecentSpendAdapter(List<HelperClass> transactionsList, Context context) { // Change the parameter type here
        this.transactionsList = transactionsList;
        this.context = context; // Initialize the Context variable
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_spend_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClass transaction = transactionsList.get(position); // Change the data type here
        // Bind the transaction data to the ViewHolder views
        holder.bindTransaction(transaction);
        // Get the HelperClass object for the current position
        HelperClass currentCategory = transactionsList.get(position);
        // Set the category image based on the transaction category name
        int categoryImageResource = getCategoryImageResource(currentCategory.getTransactionCategory());
        holder.transactionIcon.setImageResource(categoryImageResource);
        int categoryBackgroundColor = getCategoryBackgroundColor(transaction.getTransactionCategory());
        holder.transactionIconBg.setBackgroundColor(categoryBackgroundColor);
    }


    @Override
    public int getItemCount() {
        // Limit the number of items to 3
        return Math.min(transactionsList.size(), 4);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView transactionCategory, transactionPrice,transactionDate;
        private ImageView transactionIcon,transactionIconBg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionCategory = itemView.findViewById(R.id.transactionCategoryName);
            transactionPrice = itemView.findViewById(R.id.transactionPrice);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);
            transactionIconBg = itemView.findViewById(R.id.transactionIconBg);
            transactionDate = itemView.findViewById(R.id.transactionDate);

        }

        public void bindTransaction(HelperClass transaction) {
            transactionCategory.setText(transaction.getTransactionNote());
            transactionPrice.setText(String.valueOf(transaction.getTransactionAmount()));
            transactionDate.setText(String.valueOf(transaction.getTransactionDate()));
            // Set other data from the HelperClass object if needed
        }
    }
        // Method to map the transaction category names to their corresponding drawable resources
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

}
