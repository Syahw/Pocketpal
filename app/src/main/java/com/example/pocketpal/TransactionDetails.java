package com.example.pocketpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TextView transactionCategory, transactionPrice, transactionDate, transactionNote;
    ImageView transactionImage, closeDetails, imgDate;
    FloatingActionButton btnDelete, btnEdit;
    String key = "";
    String imageUrl = "";
    // Firebase Authentication instance
    private FirebaseAuth mAuth;
    private String currentUserId;
    // Assuming you have the currentUserId available in your current context

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        // Initialize TextView and ImageView elements
        transactionPrice = findViewById(R.id.etTransactionPrice);
        transactionDate = findViewById(R.id.etTransactionDate);
        transactionImage = findViewById(R.id.imgTransaction);
        transactionCategory = findViewById(R.id.tvTransactionCategory);
        transactionNote = findViewById(R.id.etTransactionNotes);
        closeDetails = findViewById(R.id.imgCloseDetails);
        btnDelete = findViewById(R.id.btnDelete);
        imgDate = findViewById(R.id.imgDate);
        btnEdit = findViewById(R.id.btnEdit);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        closeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Spend activity
                finish();
            }
        });
        // Get data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            String category = intent.getStringExtra("Category");
            double price = intent.getDoubleExtra("Price", 0);
            String date = intent.getStringExtra("Date");
            String image = intent.getStringExtra("Image");
            String note = intent.getStringExtra("Note");
            key = intent.getStringExtra("Key");
            imageUrl = intent.getStringExtra("Image");

            // Populate the TextViews and ImageView using the retrieved data
            transactionCategory.setText(category);
            transactionPrice.setText(String.valueOf(price));
            transactionDate.setText(date);
            transactionNote.setText(note);
            Glide.with(this).load(image).into(transactionImage);

        }

        // Initialize Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // The user is authenticated, enable delete and edit buttons
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);

            // Set the currentUserId to the ID of the authenticated user
            currentUserId = currentUser.getUid();

            // Check if the current transaction belongs to the authenticated user
            if (currentUserId.equals(currentUserId)) {
                // Allow delete and edit actions
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTransactionData();
                    }
                });

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateTransactionData();
                    }
                });
            } else {
                // If the transaction doesn't belong to the user, disable delete and edit buttons
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        } else {
            // The user is not authenticated, hide delete and edit buttons or show a message to prompt sign-in
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
        }

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateTransactionData();
                    }
                });


        imgDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });



    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        String currentDateString = sdf.format(calendar.getTime());

        EditText transactionDate = findViewById(R.id.etTransactionDate);
        transactionDate.setText(currentDateString);
    }

    private void updateTransactionData() {
        String category = transactionCategory.getText().toString();
        String priceStr = transactionPrice.getText().toString();
        String date = transactionDate.getText().toString();
        String note = transactionNote.getText().toString();

        // Validation (you can add more validation as per your requirements)
        if (category.isEmpty() || priceStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        HelperClass updatedTransaction = new HelperClass(key, category, date, note, imageUrl, price);

        // Get a reference to the "wallets" node under the current user
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("wallets");

        // Call the recursive function to update the transaction in the correct wallet
        updateTransactionInWallet(walletsRef, updatedTransaction);
    }

    private void updateTransactionInWallet(DatabaseReference walletsRef, HelperClass updatedTransaction) {
        // Start listening for changes to the "wallets" node
        walletsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the wallets
                for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                    String walletKey = walletSnapshot.getKey();

                    // Get a reference to the "walletTransactions" node of this wallet
                    DatabaseReference walletTransactionsRef = walletsRef
                            .child(walletKey)
                            .child("walletTransactions")
                            .child(key);

                    // Check if the transaction exists in this wallet
                    walletTransactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Transaction found in this wallet, update the data
                                walletTransactionsRef.setValue(updatedTransaction)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(TransactionDetails.this, "Transaction has been updated", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(TransactionDetails.this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                return; // Exit the loop once the transaction is updated
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle the error if the data retrieval is canceled or fails
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the data retrieval is canceled or fails
            }
        });
    }



    private void deleteTransactionData() {
        // Get a reference to the "wallets" node under the current user
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("wallets");

        // Find the wallet that contains the transaction to be deleted
        walletsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the wallets
                for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                    String walletKey = walletSnapshot.getKey();
                    // Now that we have the walletKey, call the deleteTransactionData() method
                    deleteTransactionData(currentUserId, walletKey, key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the data retrieval is canceled or fails
            }
        });
    }


    private void deleteTransactionData(String currentUserId, String walletID, String transactionId) {
        // Get a reference to the specific transaction node in the Firebase Realtime Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("wallets")
                .child(walletID)
                .child("walletTransactions")
                .child(transactionId);

        // Check if the transaction exists in the current wallet
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Delete the transaction from the database
                    reference.removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(TransactionDetails.this, "Transaction has been deleted", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                    startActivity(intent);

                                    // Update the wallet balance after the transaction is deleted
                                    updateWalletBalance(currentUserId, walletID);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TransactionDetails.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the data retrieval is canceled or fails
            }
        });
    }

    private void updateWalletBalance(String currentUserId, String walletID) {
        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("wallets")
                .child(walletID);

        // Query all wallet transactions
        walletRef.child("walletTransactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double balance = 0.0;

                for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                    HelperClass transaction = transactionSnapshot.getValue(HelperClass.class);
                    if (transaction != null) {
                        balance += transaction.getTransactionAmount();
                    }
                }

                // Update the balance in the wallet node
                walletRef.child("balance").setValue(balance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if the data retrieval is canceled or fails
            }
        });
    }



}

