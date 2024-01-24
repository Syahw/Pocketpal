package com.example.pocketpal;

import android.app.Activity;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link transactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class transactionFragment extends Fragment implements SelectWalletAdapter.OnWalletClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public transactionFragment() {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    private TextView selectWallet, selectCategory, selectDate, addPhoto,tvAddTransaction;
    private ImageView imgPhoto,imgCategory;
    private EditText etRM, etNote;
    private Button AddTransaction;
    private String imageUrl;
    private Uri uri;
    private String currentUserId;

    ActivityResultLauncher<Intent> activityResultLauncher; // Declare as a class member variable


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_transaction, container, false);
        selectCategory = rootView.findViewById(R.id.tvSelectCategory);
        selectDate = rootView.findViewById(R.id.tvSelectDate);
        selectWallet = rootView.findViewById(R.id.tvSelectWallet);
        addPhoto = rootView.findViewById(R.id.tvAddPhoto);
        imgPhoto = rootView.findViewById(R.id.imgPhoto);
        imgCategory = rootView.findViewById(R.id.imgCategory);
        AddTransaction = rootView.findViewById(R.id.btnAddTransaction);
        etRM = rootView.findViewById(R.id.etRM);
        etNote = rootView.findViewById(R.id.etWriteNote);




        // Initialize Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        selectWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSelectWallet();
            }
        });


        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(requireFragmentManager(), "date picker");

            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialogPhoto();
            }
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            imgPhoto.setImageURI(uri);
                        } else {
                            Toast.makeText(requireContext()
                                    , "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        AddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transactionNote = etNote.getText().toString().trim();
                String selectedDate = selectDate.getText().toString().trim();

                if (selectedDate.equals("Select Date")) {
                    // Show a toast message if the date is not selected
                    Toast.makeText(requireContext(), "Please Select Date", Toast.LENGTH_SHORT).show();
                } else if (transactionNote.isEmpty()) {
                    // Show a toast message if the transaction note is empty
                    Toast.makeText(requireContext(), "Please enter a transaction name", Toast.LENGTH_SHORT).show();
                } else {
                    saveData();
                }
            }
        });
        return rootView;

    }


    @Override
    public void onWalletClick(HelperClass wallet, Dialog dialog) {
        String selectedWalletName = wallet.getWalletName();
        String selectedWalletCategory = wallet.getWalletCategory();

        selectWallet.setText(selectedWalletName);
        selectCategory.setText(selectedWalletCategory);

        // Change the background and icon based on the wallet category
        switch (selectedWalletCategory) {
            case "Food And Drinks":
                changeToBlueColor();
                imgCategory.setImageResource(R.drawable.img_food);
                break;
            case "Shopping":
                changetoOrangeColor();
                imgCategory.setImageResource(R.drawable.img_shoppingbag);
                break;
            case "Transport":
                changetoPurpleColor();
                imgCategory.setImageResource(R.drawable.img_car);
                break;
            case "Travel":
                changetoGreenColor();
                imgCategory.setImageResource(R.drawable.img_travel);
                break;
            case "Bills And Fees":
                changetoOrangeColor();
                imgCategory.setImageResource(R.drawable.img_bills);
                break;
            case "Education":
                changetoGreenColor();
                imgCategory.setImageResource(R.drawable.img_education);
                break;
            case "Healthcare":
                changeToBlueColor();
                imgCategory.setImageResource(R.drawable.img_health);
                break;
            case "Hobbies":
                changetoPurpleColor();
                imgCategory.setImageResource(R.drawable.img_sports);
                break;
            default:
                // Set default background and icon here if needed
                break;
        }
        dialog.dismiss(); // Dismiss the dialog here

    }

    private void showBottomSelectWallet() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.select_wallet_layout);

        ImageView imgClose = dialog.findViewById(R.id.imgClose2);
        RecyclerView recyclerView = dialog.findViewById(R.id.recycleViewWallet);
        TextView emptyMessageText = dialog.findViewById(R.id.tvEmptyMessage);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Fetch the user's wallets from Firebase
        DatabaseReference walletsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("wallets");
        List<HelperClass> walletList = new ArrayList<>();
        SelectWalletAdapter adapter = new SelectWalletAdapter(requireContext(), walletList, dialog, (SelectWalletAdapter.OnWalletClickListener) this);
        recyclerView.setAdapter(adapter);

        walletsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                walletList.clear();

                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    HelperClass helperClass = walletSnapshot.getValue(HelperClass.class);
                    walletList.add(helperClass);

                }
                adapter.updateData(walletList);

                // Show/hide the empty message TextView based on the list size
                if (walletList.isEmpty()) {
                    emptyMessageText.setVisibility(View.VISIBLE);
                } else {
                    emptyMessageText.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error here if needed
            }


        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void showBottomDialogPhoto() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_photo_layout);
        TextView tvCancel = dialog.findViewById(R.id.tvCancel);
        TextView tvTakePhoto = dialog.findViewById(R.id.tvTakePhoto);
        TextView tvChooseFromLibrary = dialog.findViewById(R.id.tvChooseFromLibrary);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        tvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        tvChooseFromLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //  ------ ADD PHOTO FUNCTIONS ------
    private static final int CAMERA_REQUEST_CODE = 123;
    private static final int GALLERY_REQUEST_CODE = 456;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private void openCamera() {
        // Check for camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, open the camera
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, open the camera
                launchCamera();
            } else {
                // Permission denied. You can show a message to the user or take appropriate action.
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        activityResultLauncher.launch(photoPicker);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Handle the captured photo here
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");

            // Convert the Bitmap to a URI and store it in the 'uri' variable
            uri = getImageUri(requireContext(), photo);

            // Resize the photo if needed
            int desiredWidth = 100; // Set your desired width for the photo
            int desiredHeight = 100; // Set your desired height for the photo
            Bitmap resizedPhoto = Bitmap.createScaledBitmap(photo, desiredWidth, desiredHeight, false);

            // Create a Drawable from the resized photo
            Drawable drawable = new BitmapDrawable(getResources(), resizedPhoto);

            // Set the drawable as the compound drawable of the ImageView
            imgPhoto.setImageDrawable(drawable);
        }
        else if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                imgPhoto.setImageURI(selectedImageUri);
            }
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    // FOR STORING DATA IN DATABASE
    public void saveData() {
        String amountText = etRM.getText().toString().trim();
        if (uri != null && !amountText.isEmpty()) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("TransactionImage")
                    .child(uri.getLastPathSegment());

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setCancelable(false);
            builder.setView(R.layout.progress_layout);
            AlertDialog dialog = builder.create();
            dialog.show();

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isComplete());
                    Uri urlImage = uriTask.getResult();
                    imageUrl = urlImage.toString();
                    uploadData();
                    dialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                }
            });
        } else {
            // Display Toast messages for error cases
            if (uri == null) {
                Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
            }

            if (amountText.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a Valid Amount", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadData() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Get the selected wallet from the TextView
        String selectedWalletName = selectWallet.getText().toString();

        // Query the wallets node to find the selected wallet
        Query walletQuery = userReference.child("wallets").orderByChild("walletName").equalTo(selectedWalletName);

        walletQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get the wallet ID of the selected wallet
                    String walletId = snapshot.getChildren().iterator().next().getKey();

                    // Now, you have the wallet ID, use it to add the transaction under the wallet's "transactions" node
                    String transactionID = userReference.child("wallets").child(walletId).child("walletTransactions").push().getKey(); // Generate a unique transaction ID
                    String transactionCategory = selectCategory.getText().toString();
                    String transactionDate = selectDate.getText().toString();
                    String transactionNote = etNote.getText().toString();
                    Double transactionAmount = Double.valueOf(etRM.getText().toString());

                    HelperClass helperClass = new HelperClass(transactionID, transactionCategory, transactionDate, transactionNote, imageUrl, transactionAmount);

                    userReference.child("wallets").child(walletId).child("walletTransactions").child(transactionID).setValue(helperClass)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Transaction has been saved", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(requireContext(), "Selected Budget not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
            }
        });
    }


    // METHOD OF CHANGING THE BACKGROUND TRANSACTION COLOR
    public void changeToBlueColor() {
        if (getView() != null) {
            TextView imgBg = getView().findViewById(R.id.imgBg);
            imgBg.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_transaction_lightblue));
        }
    }
    public void changetoGreenColor() {
        if (getView() != null) {
            TextView imgBg = getView().findViewById(R.id.imgBg);
            imgBg.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_transaction_green));
        }
    }
    public void changetoPurpleColor() {
        if (getView() != null) {
            TextView imgBg = getView().findViewById(R.id.imgBg);
            imgBg.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_transaction_purple));
        }
    }
    public void changetoOrangeColor() {
        if (getView() != null) {
            TextView imgBg = getView().findViewById(R.id.imgBg);
            imgBg.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_transaction_orange));
        }
    }

}


