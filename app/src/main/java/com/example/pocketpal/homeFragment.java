package com.example.pocketpal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public homeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Button btnMonth, btnWeek;
    private TextView seeAll, tvBalance,tvSpendingReport;
    private BarChart barChart;
    private RecyclerView recentSpendView;
    private ImageView imgBanner;
    private String currentUserId;
    private RecentSpendAdapter recentSpendAdapter;
    private Spinner spinner;
    private String selectedWalletName;
    private ArrayList<HelperClass> transactionsList = new ArrayList<>();
    private List<String> weeklyDates = new ArrayList<String>();
    private List<Double> weeklyAmounts = new ArrayList<>();
    private List<String> monthlyDates = new ArrayList<String>();
    private List<Double> monthlyAmounts = new ArrayList<>();

    private boolean showWeeklyData = true; // Flag to keep track of the current data view
    // Add a list of drawable resource IDs for the banner images
    private List<Integer> bannerImages = new ArrayList<>();
    // Index to keep track of the current image
    private int currentBannerImageIndex = 0;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_FIRST_TIME_LAUNCH = "first_time_launch";

    // Handler and Runnable for image banner animation
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the image in the banner
            updateBannerImage();

            // Schedule the next image change after 10 seconds
            bannerHandler.postDelayed(this, 6000); // 6 seconds in milliseconds
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        barChart = rootView.findViewById(R.id.chart);
        btnMonth = rootView.findViewById(R.id.btnMonth);
        btnWeek = rootView.findViewById(R.id.btnWeek);
        seeAll = rootView.findViewById(R.id.tvSeeAll);
        recentSpendView = rootView.findViewById(R.id.recentSpendView);
        spinner = rootView.findViewById(R.id.spinnerWalletList);
        tvBalance = rootView.findViewById(R.id.tvBalance);
        imgBanner = rootView.findViewById(R.id.imageViewBanner);
        tvSpendingReport = rootView.findViewById(R.id.tvSpendingReport);

        // Set the initial view to show weekly data
        showWeeklyData = true;
        btnWeek.setSelected(true); // Set the btnWeek as selected by default
        btnMonth.setSelected(false);

        // Check if it's the first time the user launches the app
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean firstTimeLaunch = preferences.getBoolean(PREF_FIRST_TIME_LAUNCH, true);

        if (firstTimeLaunch) {
            // Show the in-app tutorial
            showTutorial();
            // Mark that the tutorial has been shown
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_FIRST_TIME_LAUNCH, false);
            editor.apply();
        }

        // Initialize Firebase Authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move button1 to the back, making it appear behind button2
                btnMonth.bringToFront();
                // Change buttonMonth tint color
                btnMonth.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Greencolor)));
                // Reset buttonWeek tint color to default
                btnWeek.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                // Switch to weekly view
                showWeeklyData = false;
                btnWeek.setSelected(false);
                btnMonth.setSelected(true);

                updateBarChart();
            }
        });

        btnWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move button1 to the back, making it appear behind button2
                btnWeek.bringToFront();
                // Change buttonWeek tint color
                btnWeek.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Greencolor)));
                // Reset buttonMonth tint color to default
                btnMonth.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray)));

                // Switch to weekly view
                showWeeklyData = true;
                btnWeek.setSelected(true);
                btnMonth.setSelected(false);

                updateBarChart();
            }
        });

        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the spendsFragment using NavController
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_homeFragment_to_spendsFragment);
            }
        });

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        recentSpendView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Create a custom adapter for the RecyclerView
        recentSpendAdapter = new RecentSpendAdapter(transactionsList, requireContext());
        recentSpendView.setAdapter(recentSpendAdapter);
        // Get the recent transactions and update the transactionsList
        getRecentTransactions(selectedWalletName);
        fetchWalletsFromFirebase();

        // Set a listener for the spinner to get the selected wallet name
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected wallet name from the spinner
                selectedWalletName = parent.getItemAtPosition(position).toString();

                // Update the recent transactions based on the selected wallet
                getRecentTransactions(selectedWalletName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle if nothing is selected (optional)
            }
        });

        // Add the drawable resource IDs to the bannerImages list
        bannerImages.add(R.drawable.goals_banner);
        bannerImages.add(R.drawable.guide_banner);

        tvSpendingReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), spending_report.class);
                startActivity(intent);
            }
        });
        // Start the image banner animation
        startBannerAnimation();

        return rootView;
    }


    private void showTutorial() {
        // Set up the tutorial steps
        List<TapTarget> targets = new ArrayList<>();
        // Add tutorial targets for each view you want to highlight

        targets.add(TapTarget.forView(spinner, "Budget List", "Right here is your created budget lists.")
                .targetRadius(15)
                .outerCircleColor(R.color.Greencolor)
                .outerCircleAlpha(1.0f)
                .titleTextSize(24)
                .textColor(R.color.white)
                .descriptionTextSize(18)
                .descriptionTextColor(R.color.black)
                .cancelable(false)
                .transparentTarget(true)
        );

        targets.add(TapTarget.forView(btnMonth, "Monthly View", "Tap here to switch to monthly view.")
                .outerCircleColor(R.color.Greencolor)
                .outerCircleAlpha(1.0f)
                .titleTextSize(24)
                .textColor(R.color.white)
                .descriptionTextColor(R.color.black)
                .descriptionTextSize(18)
                .cancelable(false)
                .transparentTarget(true)
        );

        targets.add(TapTarget.forView(btnWeek, "Weekly View", "Tap here to switch to weekly view.")
                .outerCircleColor(R.color.Greencolor)
                .outerCircleAlpha(1.0f)
                .titleTextSize(24)
                .textColor(R.color.white)
                .descriptionTextSize(18)
                .descriptionTextColor(R.color.black)
                .cancelable(false)
                .transparentTarget(true)
        );

        targets.add(TapTarget.forView(tvSpendingReport, "See Spending Report", "Tap here to see transactions report.")
                .outerCircleColor(R.color.Greencolor)
                .outerCircleAlpha(1.0f)
                .titleTextSize(24)
                .targetRadius(20)
                .descriptionTextSize(18)
                .descriptionTextColor(R.color.black)
                .cancelable(false)
                .transparentTarget(true)
        );

        // Set up the tutorial sequence
        TapTargetSequence sequence = new TapTargetSequence(requireActivity())
                .targets(targets)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        // Optional: Callback when the tutorial sequence ends
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        // Optional: Callback after each step of the tutorial
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Optional: Callback if the tutorial is canceled
                    }
                });

        // Start the tutorial sequence
        sequence.start();
    }


        @Override
    public void onDestroyView() {
        // Remove the bannerRunnable from the handler when the fragment is destroyed
        bannerHandler.removeCallbacks(bannerRunnable);
        super.onDestroyView();
    }

    private void startBannerAnimation() {
        // Start the banner image animation
        bannerHandler.postDelayed(bannerRunnable, 5000); // 6 seconds in milliseconds
    }

    private void updateBannerImage() {
        // Get the current image resource ID from the list
        int currentBannerImageResource = bannerImages.get(currentBannerImageIndex);

        // Set the image in the ImageView
        imgBanner.setImageResource(currentBannerImageResource);

        // Add animation for the image change (optional)
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
        imgBanner.startAnimation(fadeIn);

        // Increment the index for the next image (loop back to 0 if reached the end)
        currentBannerImageIndex = (currentBannerImageIndex + 1) % bannerImages.size();
    }

    private void getRecentTransactions(String walletName) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("wallets");

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HelperClass> tempList = new ArrayList<>();
                for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                    if (walletSnapshot.child("walletName").exists() && walletSnapshot.child("walletName").getValue(String.class).equals(walletName)) {
                        // Found the selected wallet, now fetch its transactions
                        DataSnapshot walletTransactionsSnapshot = walletSnapshot.child("walletTransactions");
                        for (DataSnapshot transactionSnapshot : walletTransactionsSnapshot.getChildren()) {
                            HelperClass helperClass = transactionSnapshot.getValue(HelperClass.class);
                            helperClass.setTransactionID(transactionSnapshot.getKey());
                            tempList.add(helperClass);
                        }

                        calculateWeeklyData(tempList);
                        calculateMonthlyData(tempList);
                        break; // No need to continue searching once the selected wallet is found
                    }
                }

                updateBarChart();

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


                // Clear the transactionsList
                transactionsList.clear();

                // Add up to 3 transactions from Firebase to transactionsList
                int count = Math.min(tempList.size(), 4); // Limit to 3 items
                for (int i = 0; i < count; i++) {
                    transactionsList.add(tempList.get(i));
                }

                // Notify the adapter of the changes
                recentSpendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
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
        // Create a custom adapter for the spinner
        WalletSpinnerAdapter spinnerAdapter = new WalletSpinnerAdapter(requireContext(), walletList);

        // Set the custom adapter to the spinner
        spinner.setAdapter(spinnerAdapter);

        // Set a listener for the spinner to get the selected wallet name
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected wallet name from the spinner
                selectedWalletName = parent.getItemAtPosition(position).toString();

                // Update the recent transactions based on the selected wallet
                getRecentTransactions(selectedWalletName);

                // Update the balance for the selected wallet
                updateBalance(selectedWalletName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle if nothing is selected (optional)
            }
        });
    }

    private void updateBalance(String walletName) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("wallets");

        Query walletQuery = userReference.orderByChild("walletName").equalTo(walletName);
        walletQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Find the specific wallet by its name
                    for (DataSnapshot walletSnapshot : snapshot.getChildren()) {
                        Double initialBalance = walletSnapshot.child("walletBalance").getValue(Double.class);
                        double totalExpenses = 0;

                        DataSnapshot walletTransactionsSnapshot = walletSnapshot.child("walletTransactions");
                        for (DataSnapshot transactionSnapshot : walletTransactionsSnapshot.getChildren()) {
                            HelperClass helperClass = transactionSnapshot.getValue(HelperClass.class);
                            if (helperClass != null) {
                                Double transactionAmount = helperClass.getTransactionAmount();
                                if (transactionAmount != null) {
                                    totalExpenses += transactionAmount;
                                }
                            }
                        }

                        // Calculate the updated balance
                        double updatedBalance = initialBalance - totalExpenses;

                        // Update the tvBalance TextView with the updated balance
                        String balanceText = "RM " + String.valueOf(updatedBalance);
                        tvBalance.setText(balanceText);

                        // Set the text color based on the balance value
                        int color;
                        if (updatedBalance < 0) {
                            color = getResources().getColor(R.color.Redcolor); // Negative balance, set to red color
                            showBalanceNotification();
                        } else {
                            color = getResources().getColor(com.google.android.material.R.color.m3_default_color_secondary_text);
                        }
                        tvBalance.setTextColor(color);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }


    //NOTIFICATION FOR NEGATIVE BALANCE
    private void showBalanceNotification() {
        // Create the AlertDialog with a custom layout
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        View customLayout = getLayoutInflater().inflate(R.layout.custom_alert_reminder, null);
        alertDialogBuilder.setView(customLayout);
        // Add the OK button (optional, set to null to dismiss the dialog on click)
        alertDialogBuilder.setPositiveButton("OK", null);
        alertDialogBuilder.show();
    }
    private void updateBarChart() {
        if (showWeeklyData && weeklyDates.isEmpty()) {
            // Show "No Data Available For Current Week"
            showNoDataMessage("No Data Available For Current Week");
        } else if (!showWeeklyData && monthlyDates.isEmpty()) {
            // Show "No Data Available For Months"
            showNoDataMessage("No Data Available For Months");
        } else {
            // Update bar chart with the available data
            if (showWeeklyData) {
                updateBarChartWithData(weeklyDates, weeklyAmounts);
            } else {
                updateBarChartWithData(monthlyDates, monthlyAmounts);
            }
        }
    }

    private void showNoDataMessage(String message) {
        // Clear the bar chart data
        barChart.clear();

        // Show a message in the center of the bar chart with no data
        barChart.setNoDataText(message);
        barChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.Redcolor));
        barChart.invalidate();
    }


    private void calculateWeeklyData(List<HelperClass> tempList) {

        // List to hold the weekly data
        List<WeeklyData> weeklyDataList = new ArrayList<>();

        // Calculate the start and end dates of the current month
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.set(currentYear, currentMonth, 1); // Set to the first day of the month
        Date monthStartDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1); // Move to the first day of the next month
        calendar.add(Calendar.DATE, -1); // Move to the last day of the current month
        Date monthEndDate = calendar.getTime();

        // Iterate through the transactions and group them by week
        for (HelperClass transaction : tempList) {
            String transactionDateString = transaction.getTransactionDate(); // Get the date string from Firebase
            Date transactionDate = parseDateFromString(transactionDateString); // Parse the date string to Date object
            if (transactionDate != null && transactionDate.after(monthStartDate) && transactionDate.before(monthEndDate)) {
                String weekNumber = getWeekNumber(transactionDate);
                double transactionAmount = transaction.getTransactionAmount();
                int index = -1;
                for (int i = 0; i < weeklyDataList.size(); i++) {
                    if (weeklyDataList.get(i).weekNumber.equals(weekNumber)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    double existingAmount = weeklyDataList.get(index).amount;
                    weeklyDataList.get(index).amount = existingAmount + transactionAmount;
                } else {
                    weeklyDataList.add(new WeeklyData(weekNumber, transactionAmount));
                }
            }
        }

        // Sort the weeklyDataList using a custom comparator based on the week number
        Collections.sort(weeklyDataList, new Comparator<WeeklyData>() {
            @Override
            public int compare(WeeklyData data1, WeeklyData data2) {
                int weekNumber1 = Integer.parseInt(data1.weekNumber.substring(5));
                int weekNumber2 = Integer.parseInt(data2.weekNumber.substring(5));
                return Integer.compare(weekNumber1, weekNumber2);
            }
        });

        // Clear the old lists
        weeklyDates.clear();
        weeklyAmounts.clear();

        // Populate the sorted data back into the original lists
        for (WeeklyData data : weeklyDataList) {
            weeklyDates.add(data.weekNumber);
            weeklyAmounts.add(data.amount);
        }
    }

    private boolean isMonthPresent(String month) {
        return monthlyDates.contains(month);
    }
    private void calculateMonthlyData(List<HelperClass> tempList) {
        // List to hold the monthly data
        List<MonthlyData> monthlyDataList = new ArrayList<>();

        // Iterate through the transactions and group them by month
        for (HelperClass transaction : tempList) {
            String transactionDateString = transaction.getTransactionDate(); // Get the date string from Firebase
            Date transactionDate = parseDateFromString(transactionDateString); // Parse the date string to Date object
            if (transactionDate != null) {
                String month = getMonthName(transactionDate);
                double transactionAmount = transaction.getTransactionAmount();
                int index = -1;
                for (int i = 0; i < monthlyDataList.size(); i++) {
                    if (monthlyDataList.get(i).monthName.equals(month)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    double existingAmount = monthlyDataList.get(index).amount;
                    monthlyDataList.get(index).amount = existingAmount + transactionAmount;
                } else {
                    monthlyDataList.add(new MonthlyData(month, transactionAmount));
                }
            }
        }

        // Sort the monthlyDataList using a custom comparator based on the month name
        Collections.sort(monthlyDataList, new Comparator<MonthlyData>() {
            @Override
            public int compare(MonthlyData data1, MonthlyData data2) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                try {
                    Date date1 = dateFormat.parse(data1.monthName);
                    Date date2 = dateFormat.parse(data2.monthName);
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // Clear the old lists
        monthlyDates.clear();
        monthlyAmounts.clear();

        // Populate the sorted data back into the original lists
        for (MonthlyData data : monthlyDataList) {
            monthlyDates.add(data.monthName);
            monthlyAmounts.add(data.amount);
        }

        // Check if the current month is already present in the data
        String currentMonth = getMonthName(new Date());
        if (!isMonthPresent(currentMonth)) {
            monthlyDates.add(currentMonth);
            monthlyAmounts.add(0.0); // Add 0 amount for the current month (no transactions yet)
        }
    }

    private Date parseDateFromString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWeekNumber(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Special handling for the first three days of each month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (calendar.get(Calendar.DAY_OF_MONTH) <= 3 && daysInMonth >= 30) {
            return "Week 1"; // The first three days of the month will be considered as Week 1
        }

        int weekNumber = calendar.get(Calendar.WEEK_OF_MONTH);
        return "Week " + weekNumber;
    }



    private String getMonthName(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return monthFormat.format(date);
    }

    private void updateBarChartWithData(List<String> xLabels, List<Double> yValues) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < yValues.size(); i++) {
            barEntries.add(new BarEntry(i, yValues.get(i).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Transactions");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.Redcolor));

        // Set the value text size here (change the value as needed)
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new MyValueFormatter());

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        // Set the x-axis labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setLabelCount(xLabels.size());

        // Hide right Y-axis (optional)
        YAxis rightYAxis = barChart.getAxisRight();
        rightYAxis.setEnabled(false);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
        // Enable scrolling for the BarChart
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false); // Disable scaling to only allow horizontal scrolling
        barChart.setPinchZoom(false);
        barChart.setVisibleXRangeMaximum(5); // Adjust this value based on the number of bars you want to show initially
    }

    // Custom class to hold week number and corresponding amount
    private static class WeeklyData {
        private String weekNumber;
        private double amount;

        public WeeklyData(String weekNumber, double amount) {
            this.weekNumber = weekNumber;
            this.amount = amount;
        }
    }
    // Custom class to hold month name and corresponding amount
    private static class MonthlyData {
        private String monthName;
        private double amount;

        public MonthlyData(String monthName, double amount) {
            this.monthName = monthName;
            this.amount = amount;
        }
    }
}
