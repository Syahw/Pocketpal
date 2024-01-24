package com.example.pocketpal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class budgetFragment extends Fragment {

    RecyclerView recyclerView;
    ValueEventListener eventListener;
    FirebaseDatabase database;
    List<HelperClass> dataList;
    DatabaseReference reference;

    private String currentUserId; // To store the current user's ID

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public budgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment transactionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static transactionFragment newInstance(String param1, String param2) {
        transactionFragment fragment = new transactionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    ImageView imgCreateWallet,imgEditWallet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_budget, container, false);

        imgCreateWallet = rootView.findViewById(R.id.imgCreateWallet);
        imgEditWallet = rootView.findViewById(R.id.imgEditWallet);
        recyclerView = rootView.findViewById(R.id.walletRecycleView);
        TextView totalWealth = rootView.findViewById(R.id.tvTotalWealth);


        // Initialize Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }


        imgCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialogWallet();
            }
        });

        imgEditWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialogEditWallet();
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        WalletAdapter adapter = new WalletAdapter(requireContext(), dataList);
        recyclerView.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");

        eventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                double totalBalance = 0.0; // Change the type to double

                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    HelperClass helperClass = walletSnapshot.getValue(HelperClass.class);
                    dataList.add(helperClass);

                    // Change Integer.parseInt() to Double.parseDouble()
                    totalBalance += Double.parseDouble(String.valueOf(helperClass.getWalletBalance()));
                }

                adapter.updateData(dataList);
                dialog.dismiss();

                // Set the total balance to the totalWealth TextView
                totalWealth.setText("RM " + String.valueOf(totalBalance));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
        return rootView;
    }


    private void showBottomDialogWallet() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_wallet_layout);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        EditText WalletName = dialog.findViewById(R.id.etWalletName);
        EditText WalletBalance = dialog.findViewById(R.id.etWalletBalance);
        Button btnCategory = dialog.findViewById(R.id.btnCategory);
        Button createWallet = dialog.findViewById(R.id.btnCreateWallet);

        EditText etChooseCategory = dialog.findViewById(R.id.etChooseCategory);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomCategory(etChooseCategory, btnCategory);
            }
        });

        createWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");
                String walletID = reference.push().getKey(); // Generate a unique wallet ID
                String walletName = WalletName.getText().toString();
                Double walletBalance = 0.0;
                String walletBalanceStr = WalletBalance.getText().toString().trim();
                if (!TextUtils.isEmpty(walletBalanceStr)) {
                    walletBalance = Double.parseDouble(walletBalanceStr);
                }
                String walletCategory = etChooseCategory.getText().toString();

                HelperClass helperClass = new HelperClass(walletID, walletName, walletBalance, walletCategory);
                DatabaseReference userWalletsRef = database.getReference("users").child(currentUserId).child("wallets");
                userWalletsRef.child(walletID).setValue(helperClass)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Budget created successfully", Toast.LENGTH_SHORT).show();
                                    // Dismiss the dialog
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to create budget", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private void showBottomCategory(final TextView etChooseCategory, final Button btnCategory) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.category_layout);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        Button btnFood = dialog.findViewById(R.id.btnFood);
        Button btnShopping = dialog.findViewById(R.id.btnShopping);
        Button btnTransport = dialog.findViewById(R.id.btnTransport);
        Button btnTravel = dialog.findViewById(R.id.btnTravel);
        Button btnBills = dialog.findViewById(R.id.btnBills);
        Button btnEducation = dialog.findViewById(R.id.btnEducation);
        Button btnHealthcare = dialog.findViewById(R.id.btnHealthcare);
        Button btnSports = dialog.findViewById(R.id.btnSports);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Food And Drinks");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_food);
                }
                dialog.dismiss();
            }
        });

        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Shopping");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_shoppingbag);
                }
                dialog.dismiss();
            }
        });

        btnTransport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Transport");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_car);
                }
                dialog.dismiss();
            }
        });

        btnTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Travel");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_travel);
                }
                dialog.dismiss();
            }
        });

        btnBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Bills And Fees");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_bills);
                }
                dialog.dismiss();
            }
        });

        btnEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Education");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_education);
                }
                dialog.dismiss();
            }
        });

        btnHealthcare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Healthcare");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_health);
                }
                dialog.dismiss();
            }
        });

        btnSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etChooseCategory != null) {
                    etChooseCategory.setText("Hobbies");
                }
                if (btnCategory != null) {
                    btnCategory.setBackgroundResource(R.drawable.img_sports);
                }
                dialog.dismiss();
            }
        });

        dialog.show(); // Add this line to show the dialog
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    private Spinner spinner;
    private EditText etWalletName, etWalletBalance, etWalletCategory;
    private Button btnCategory2;

    private void showBottomDialogEditWallet() {
        final Dialog dialog = new Dialog(requireContext());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_wallet_layout);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        spinner = dialog.findViewById(R.id.spinWalletList);
        etWalletName = dialog.findViewById(R.id.etWalletName);
        etWalletBalance = dialog.findViewById(R.id.etWalletBalance);
        etWalletCategory = dialog.findViewById(R.id.etWalletCategory);
        btnCategory2 = dialog.findViewById(R.id.btnCategory3);
        TextView editWallet = dialog.findViewById(R.id.tvEditWallet2);
        TextView deleteWallet = dialog.findViewById(R.id.tvDeleteWallet);


        // Set an item click listener on the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle the selected item
                String selectedWallet = parent.getItemAtPosition(position).toString();
                retrieveWalletData(selectedWallet);
                Toast.makeText(requireContext(), "Selected Budget : " + selectedWallet, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(requireContext(), "Please select a budget" , Toast.LENGTH_SHORT).show();
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        getWalletListFromDatabase();

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Inside the editWallet.setOnClickListener
        editWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedWalletName = spinner.getSelectedItem().toString();
                String newWalletName = etWalletName.getText().toString().trim();
                String walletBalanceStr = etWalletBalance.getText().toString().trim();
                String walletCategory = etWalletCategory.getText().toString().trim();

                if (TextUtils.isEmpty(newWalletName)) {
                    etWalletName.setError("Budget name cannot be empty");
                    return;
                }

                // Parse the walletBalanceStr to Double
                final double finalDoubleBalance;
                try {
                    finalDoubleBalance = Double.parseDouble(walletBalanceStr);
                } catch (NumberFormatException e) {
                    // Handle the case where parsing the balance fails (e.g., invalid input)
                    etWalletBalance.setError("Invalid Budget balance");
                    return;
                }

                DatabaseReference walletTransactionsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallet_transactions").child(selectedWalletName);

                walletTransactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalTransactionAmount = 0.0;

                        for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                            double transactionAmount = transactionSnapshot.child("transactionAmount").getValue(Double.class);
                            totalTransactionAmount += transactionAmount;
                        }

                        // Calculate the updated wallet balance by deducting totalTransactionAmount
                        double updatedBalance = finalDoubleBalance - totalTransactionAmount;

                        DatabaseReference userWalletsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");
                        userWalletsRef.orderByChild("walletName").equalTo(selectedWalletName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                                        String walletKey = walletSnapshot.getKey();

                                        DatabaseReference updatedWalletRef = userWalletsRef.child(walletKey);
                                        updatedWalletRef.child("walletName").setValue(newWalletName);
                                        updatedWalletRef.child("walletBalance").setValue(updatedBalance); // Save as Double
                                        updatedWalletRef.child("walletCategory").setValue(walletCategory)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(requireContext(), "Failed to update budget name", Toast.LENGTH_SHORT).show();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Wallets_Page", "Error retrieving Budget data from database: " + databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Wallets_Page", "Error retrieving Budget transaction data from database: " + databaseError.getMessage());
                    }
                });
            }
        });


        deleteWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedWalletName = spinner.getSelectedItem().toString();

                // Get a reference to the "wallets" node in the Firebase Realtime Database
                DatabaseReference userWalletsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");

                // Query the database to find the wallet with the selected name
                userWalletsRef.orderByChild("walletName").equalTo(selectedWalletName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Iterate through the snapshots to find the correct wallet
                            for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                                // Get the key of the wallet to delete
                                String walletKey = walletSnapshot.getKey();

                                // Remove the wallet from the database
                                userWalletsRef.child(walletKey).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(requireContext(), "Budget deleted successfully", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(requireContext(), "Failed to delete Budget", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Wallet with the selected name was not found in the database
                            Toast.makeText(requireContext(), "Selected budget not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Wallets_Page", "Error retrieving budget data from database: " + databaseError.getMessage());
                    }
                });
            }
        });

        btnCategory2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomCategoryList();
            }
        });
    }
    private void showBottomCategoryList(){
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.category_layout);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        Button btnFood = dialog.findViewById(R.id.btnFood);
        Button btnShopping = dialog.findViewById(R.id.btnShopping);
        Button btnTransport = dialog.findViewById(R.id.btnTransport);
        Button btnTravel = dialog.findViewById(R.id.btnTravel);
        Button btnBills = dialog.findViewById(R.id.btnBills);
        Button btnEducation = dialog.findViewById(R.id.btnEducation);
        Button btnHealthcare = dialog.findViewById(R.id.btnHealthcare);
        Button btnSports = dialog.findViewById(R.id.btnSports);


        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Food And Drinks");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_food);
                }
                dialog.dismiss();
            }
        });

        btnShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Shopping");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_shoppingbag);
                }
                dialog.dismiss();
            }
        });

        btnTransport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Transport");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_car);
                }
                dialog.dismiss();
            }
        });

        btnTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Travel");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_travel);
                }
                dialog.dismiss();
            }
        });

        btnBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Bills And Fees");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_bills);
                }
                dialog.dismiss();
            }
        });

        btnEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Education");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_education);
                }
                dialog.dismiss();
            }
        });

        btnHealthcare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Healthcare");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_health);
                }
                dialog.dismiss();
            }
        });

        btnSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWalletCategory != null) {
                    etWalletCategory.setText("Hobbies");
                }
                if (btnCategory2 != null) {
                    btnCategory2.setBackgroundResource(R.drawable.img_sports);
                }
                dialog.dismiss();
            }
        });

        dialog.show(); // Add this line to show the dialog
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
    private void getWalletListFromDatabase() {
        DatabaseReference userWalletsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");

        userWalletsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> walletList = new ArrayList<>();

                for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                    String walletName = walletSnapshot.child("walletName").getValue(String.class);

                    if (walletName != null) {
                        walletList.add(walletName);
                    }
                }
                updateSpinnerAdapter(walletList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Wallets_Page", "Error retrieving wallet list from database: " + databaseError.getMessage());
            }
        });
    }
    private void retrieveWalletData(String selectedWalletName) {
        DatabaseReference userWalletsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallets");

        userWalletsRef.orderByChild("walletName").equalTo(selectedWalletName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                        String walletKey = walletSnapshot.getKey();
                        String walletName = walletSnapshot.child("walletName").getValue(String.class);
                        double walletBalance = walletSnapshot.child("walletBalance").getValue(Double.class);
                        String walletCategory = walletSnapshot.child("walletCategory").getValue(String.class);

                        DatabaseReference walletTransactionsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("wallet_transactions").child(selectedWalletName);

                        walletTransactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                double totalTransactionAmount = 0.0;

                                for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                                    HelperClass helperClass = transactionSnapshot.getValue(HelperClass.class);
                                    if (helperClass != null) {
                                        Double transactionAmount = helperClass.getTransactionAmount();
                                        if (transactionAmount != null) {
                                            totalTransactionAmount += transactionAmount;
                                        }
                                    }
                                }

                                // Calculate the updated balance by deducting totalTransactionAmount
                                double updatedBalance = walletBalance - totalTransactionAmount;

                                // Update the EditText field with the new balance
                                etWalletBalance.setText(String.valueOf(updatedBalance));

                                // Update the walletBalance in the database with the updated balance
                                userWalletsRef.child(walletKey).child("walletBalance").setValue(updatedBalance);

                                // Update the EditText fields with the retrieved wallet data
                                etWalletName.setText(walletName);
                                etWalletCategory.setText(walletCategory);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Wallets_Page", "Error retrieving wallet transaction data from database: " + databaseError.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Wallets_Page", "Error retrieving wallet data from database: " + databaseError.getMessage());
            }
        });
    }
    private void updateSpinnerAdapter(List<String> walletList) {
        // Create an ArrayAdapter using the updated wallet list and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, walletList);
        spinner.setAdapter(adapter);
    }

}