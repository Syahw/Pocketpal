package com.example.pocketpal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ThemedSpinnerAdapter;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link spendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class spendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public spendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment spendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static spendsFragment newInstance(String param1, String param2) {
        spendsFragment fragment = new spendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    TextView tvSpend;
    RecyclerView recyclerView;
    List<HelperClass> dataList;
    DatabaseReference databaseReference;
    Spinner spinner;
    SearchView searchView;
    MyAdapter adapter;
    private String selectedWallet; // Variable to store the selected wallet name
    private String currentUserId; // To store the current user's ID

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_spends, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        tvSpend = rootView.findViewById(R.id.tvSpend);
        searchView = rootView.findViewById(R.id.searchBar);
        spinner = rootView.findViewById(R.id.spinner);
        searchView.clearFocus();

        // Initialize Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Fetch the wallets from Firebase
        fetchWalletsFromFirebase();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize the RecyclerView
        setupRecyclerView();

        dataList = new ArrayList<>();
        adapter = new MyAdapter(requireContext(), dataList);
        recyclerView.setAdapter(adapter);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected wallet name from the spinner
                Object selectedItem = parent.getItemAtPosition(position);
                if (selectedItem != null) {
                    selectedWallet = selectedItem.toString();
                    // Call the method to fetch transactions for the selected wallet
                    fetchTransactionsForWallet(selectedWallet);
                } else {
                }
                dialog.dismiss();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case where nothing is selected (optional, based on your logic)
            }
        });


        return rootView;
    }

    public void searchList(String text) {
        if (text == null || dataList == null) {
            Toast.makeText(requireContext(), "No Transactions Found", Toast.LENGTH_SHORT).show();
            return;
        }

        String searchText = text.toLowerCase().trim();
        // Clear the list if the search query is empty
        if (searchText.isEmpty()) {
            adapter.searchDataList((ArrayList<HelperClass>) dataList);
            return;
        }

        ArrayList<HelperClass> searchResults = new ArrayList<>();
        for (HelperClass helperClass : dataList) {
            if (helperClass.getTransactionNote() != null && helperClass.getTransactionNote().toLowerCase().contains(searchText)) {
                searchResults.add(helperClass);
            }
        }

        adapter.searchDataList(searchResults);
    }

    private void setupRecyclerView() {
        // Add a null check for dataList before setting the adapter
        if (dataList != null) {
            MyAdapter adapter = new MyAdapter(requireContext(), dataList);
            recyclerView.setAdapter(adapter);
        }
    }

    private void fetchWalletsFromFirebase() {
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("wallets");

        walletsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> walletList = new ArrayList<>();

                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    String walletName = walletSnapshot.child("walletName").getValue(String.class);
                    walletList.add(walletName);
                }

                // Call the method to populate the spinner with the retrieved wallets
                populateSpinner(walletList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error here if needed
            }
        });
    }

    private void populateSpinner(List<String> walletList) {
        // Check if the fragment is attached to a context
        if (!isAdded() || requireContext() == null) {
            // Fragment is not attached or context is null, return early
            return;
        }

        // Create a custom adapter for the spinner
        WalletSpinnerAdapter spinnerAdapter = new WalletSpinnerAdapter(requireContext(), walletList);

        // Set the custom adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
    }


    private void fetchTransactionsForWallet(String walletName) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Query the transactions for the selected wallet
        Query walletQuery = userReference.child("wallets").orderByChild("walletName").equalTo(walletName);

        walletQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                Double totalSpend = 0.0; // Variable to store the total spend

                if (snapshot.exists()) {
                    // Store the transactions in a temporary list to group them by date
                    List<HelperClass> tempList = new ArrayList<>();
                    for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                        if (walletSnapshot.child("walletTransactions").exists()) {
                            for (DataSnapshot transactionSnapshot : walletSnapshot.child("walletTransactions").getChildren()) {
                                HelperClass helperClass = transactionSnapshot.getValue(HelperClass.class);
                                helperClass.setTransactionID(transactionSnapshot.getKey());
                                tempList.add(helperClass);
                                totalSpend += helperClass.getTransactionAmount();
                            }
                        }
                    }

                    // Sort the tempList based on the transaction date in descending order (latest to oldest)
                    Collections.sort(tempList, new Comparator<HelperClass>() {
                        @Override
                        public int compare(HelperClass o1, HelperClass o2) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            try {
                                Date date1 = sdf.parse(o1.getTransactionDate());
                                Date date2 = sdf.parse(o2.getTransactionDate());
                                return date2.compareTo(date1); // Compare in reverse order (latest to oldest)
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });


                    // Now add the date headers to the dataList
                    String currentDate = "";
                    SimpleDateFormat sdfFormatted = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    for (HelperClass transaction : tempList) {
                        String transactionDate = transaction.getTransactionDate();
                        try {
                            Date date = sdfFormatted.parse(transactionDate);
                            String formattedDate = sdfFormatted.format(date);
                            if (!formattedDate.equals(currentDate)) {
                                // Add the date header item to the dataList
                                HelperClass dateHeaderItem = new HelperClass(formattedDate);
                                dataList.add(dateHeaderItem);
                                currentDate = formattedDate;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Add the transaction item to the dataList
                        dataList.add(transaction);
                    }

                    // Set the total spend to the tvSpend TextView
                    tvSpend.setText("RM " + String.valueOf(totalSpend));

                    // Notify the adapter of the changes to the sorted list
                    adapter.notifyDataSetChanged();
                } else {
                    // No transactions found, display a message
                    // (You can handle this case by showing a TextView or a message on the screen)
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
            }
        });
    }
}
