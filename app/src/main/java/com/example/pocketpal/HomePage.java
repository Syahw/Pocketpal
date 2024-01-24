package com.example.pocketpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomePage extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Bottom NavBar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this,  R.id.fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // Handle bottom navigation item selection
            if (item.getItemId() == R.id.homeFragment) {
                // Check if the user is already on the homeFragment
                if (navController.getCurrentDestination().getId() == R.id.homeFragment) {
                    return true;
                }
                if (!navController.popBackStack(R.id.homeFragment, false)) {
                    navController.navigate(R.id.homeFragment);
                }
                return true;
            } else if (item.getItemId() == R.id.budgetFragment) {
                if (navController.getCurrentDestination().getId() == R.id.budgetFragment) {
                    return true;
                }
                navController.navigate(R.id.budgetFragment);
                return true;
            } else if (item.getItemId() == R.id.transactionFragment) {
                if (navController.getCurrentDestination().getId() == R.id.transactionFragment) {
                    return true;
                }
                navController.navigate(R.id.transactionFragment);
                return true;
            } else if (item.getItemId() == R.id.spendsFragment) {
                if (navController.getCurrentDestination().getId() == R.id.spendsFragment) {
                    return true;
                }
                navController.navigate(R.id.spendsFragment);
                return true;
            } else if (item.getItemId() == R.id.moreFragment) {
                if (navController.getCurrentDestination().getId() == R.id.moreFragment) {
                    return true;
                }
                if (!navController.popBackStack(R.id.moreFragment, false)) {
                    // If the home fragment is not on the back stack, navigate to it
                    navController.navigate(R.id.moreFragment);
                }
                return true;
            }
            // Add other menu item cases if applicable
            return false;
        });
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String currentDateString = sdf.format(calendar.getTime());

        TextView selectDate = findViewById(R.id.tvSelectDate);
        selectDate.setText(currentDateString);
    }

}

