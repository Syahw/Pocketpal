package com.example.pocketpal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    public MyAdapter(Context context, List<HelperClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    private List<HelperClass> dataList;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(headerView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list, parent, false);
            return new TransactionViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HelperClass item = dataList.get(position);
        if (holder instanceof DateHeaderViewHolder) {
            DateHeaderViewHolder dateHeaderViewHolder = (DateHeaderViewHolder) holder;
            dateHeaderViewHolder.bindDateHeader(item.getTransactionDate());
            // Calculate the total amount for each date header before binding
            calculateTotalAmountForDateHeaders();

            // Bind the total amount for the category
            if (item.isDateHeader()) {
                double totalAmountForCategory = item.getTotalAmountForCategory();
                dateHeaderViewHolder.bindTotalAmountForCategory(totalAmountForCategory);
            }
        } else if (holder instanceof TransactionViewHolder) {
            TransactionViewHolder transactionViewHolder = (TransactionViewHolder) holder;
            transactionViewHolder.bindTransaction(item);
        }
    }

    private void calculateTotalAmountForDateHeaders() {
        // Iterate through the dataList and calculate the total amount for each date header
        for (int i = 0; i < dataList.size(); i++) {
            HelperClass item = dataList.get(i);

            if (item.isDateHeader()) {
                double totalAmountForDate = 0;
                String currentDate = item.getTransactionDate();

                // Iterate through the transactions under this date header and sum their amounts
                for (int j = i + 1; j < dataList.size(); j++) {
                    HelperClass transaction = dataList.get(j);
                    if (!transaction.isDateHeader() && transaction.getTransactionDate().equals(currentDate)) {
                        totalAmountForDate += transaction.getTransactionAmount();
                    } else {
                        // Stop summing when a new date header is encountered
                        break;
                    }
                }

                // Update the total amount for this date header
                item.setTotalAmountForCategory(totalAmountForDate);
            }
        }
    }



    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        HelperClass item = dataList.get(position);
        return item.isDateHeader() ? TYPE_DATE_HEADER : TYPE_TRANSACTION;
    }

    public void searchDataList(ArrayList<HelperClass> searchList) {
        dataList = searchList;
        notifyDataSetChanged();
    }
}
class DateHeaderViewHolder extends RecyclerView.ViewHolder {
    private TextView tvDateHeader;
    private TextView tvTotalAmountForCategory;

    public DateHeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        tvTotalAmountForCategory = itemView.findViewById(R.id.categoryPrice);
    }

    public void bindDateHeader(String date) {
        tvDateHeader.setText(date);
    }

    public void bindTotalAmountForCategory(double totalAmountForCategory) {
        tvTotalAmountForCategory.setText("Total: RM" + totalAmountForCategory); // Customize the format as needed
    }
}


class TransactionViewHolder extends RecyclerView.ViewHolder {
    ImageView transactionImage;
    TextView transactionCategoryName, transactionPrice;
    CardView transactionCard;

    public TransactionViewHolder(@NonNull View itemView) {
        super(itemView);
        transactionImage = itemView.findViewById(R.id.transactionCategory);
        transactionCard = itemView.findViewById(R.id.transactionCard);
        transactionCategoryName = itemView.findViewById(R.id.transactionCategoryName);
        transactionPrice = itemView.findViewById(R.id.transactionPrice);
    }

    public void bindTransaction(HelperClass transaction) {
        Glide.with(itemView).load(transaction.getTransactionImage()).into(transactionImage);
        transactionCategoryName.setText(transaction.getTransactionNote());
        transactionPrice.setText(String.valueOf(transaction.getTransactionAmount()));
        transactionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(), TransactionDetails.class);
                intent.putExtra("Image", transaction.getTransactionImage());
                intent.putExtra("Category", transaction.getTransactionCategory());
                intent.putExtra("Price", transaction.getTransactionAmount());
                intent.putExtra("Date", transaction.getTransactionDate());
                intent.putExtra("Note", transaction.getTransactionNote());
                intent.putExtra("Key", transaction.getTransactionID());
                itemView.getContext().startActivity(intent);
            }
        });
    }
}
