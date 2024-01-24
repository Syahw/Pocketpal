package com.example.pocketpal;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link moreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class moreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private int tapCount = 0;
    private final int TRIPLE_TAP_TIMEOUT = 300; // milliseconds
    private Handler tapHandler = new Handler(Looper.getMainLooper());

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private GestureDetector gestureDetector;



    public moreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment moreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static moreFragment newInstance(String param1, String param2) {
        moreFragment fragment = new moreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    TextView UserGmail, SignOut, tvAbout,myBudgets,tvHelp, tvMore;
    ImageView imgUser;
    Switch switchNotify;

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
        View rootView = inflater.inflate(R.layout.fragment_more, container, false);
        UserGmail = rootView.findViewById(R.id.tvUserGmail);
        SignOut = rootView.findViewById(R.id.tvSignOut);
        myBudgets = rootView.findViewById(R.id.tvMyBudgets);
        imgUser = rootView.findViewById(R.id.imgUser);
        tvAbout = rootView.findViewById(R.id.tvAbout);
        tvHelp = rootView.findViewById(R.id.tvHelp);
        tvMore = rootView.findViewById(R.id.tvTitleMore);
        showUserData();

        // Initialize GoogleSignInOptions and GoogleSignInClient using the Activity context
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(getActivity(), gso);

        myBudgets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the spendsFragment using NavController
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_moreFragment_to_budgetFragment);
            }

        });
        // Get the hosting Activity
        HomePage activity = (HomePage) requireActivity();

        // Check if a user is signed in
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        if (acct != null) {
            String personEmail = acct.getEmail();
            Uri personPhotoUrl = acct.getPhotoUrl();
            Picasso.get().load(personPhotoUrl).into(imgUser);
            UserGmail.setText(personEmail);
        }

        // Create a GestureDetector to detect triple tap
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                navigateToDevelopersInfo();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
        });

        // Set the triple tap listener on tvMore TextView
        tvMore.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                tapCount++;
                tapHandler.removeCallbacksAndMessages(null);
                tapHandler.postDelayed(() -> {
                    if (tapCount == 3) {
                        navigateToDevelopersInfo();
                    }
                    tapCount = 0;
                }, TRIPLE_TAP_TIMEOUT);
            }
            return true;
        });



        // Set click listener for sign out
        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        tvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), about_page.class);
                startActivity(intent);
            }
        });

        tvHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), Help.class);
                startActivity(intent);
            }
        });
        return rootView;
    }
    private void signOut() {
        // Check if the user is signed in with Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if (account != null) {
            // Signed in with Google, sign out from Google
            gsc.signOut().addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    navigateToMain();
                }
            });
        } else {
            // Not signed in with Google, perform sign out
            signOutNonGoogleUser();
        }
    }
    private void navigateToDevelopersInfo() {
        Intent intent = new Intent(requireContext(), DevelopersInfo.class);
        startActivity(intent);
    }
    private void signOutNonGoogleUser(){
        FirebaseAuth.getInstance().signOut();
        navigateToMain();
    }


    void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showUserData() {
        // Get the currently signed-in user from Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in with Firebase, retrieve the email
            String userGmail = currentUser.getEmail();
            UserGmail.setText(userGmail);
        } else {
            // User is not signed in with Firebase, handle the case accordingly
            // For example, show a default email or prompt the user to sign in
            UserGmail.setText("Not signed in");
        }
    }


}
