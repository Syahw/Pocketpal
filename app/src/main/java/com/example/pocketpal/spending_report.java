package com.example.pocketpal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class spending_report extends AppCompatActivity {

    private Spinner spinnerMonth;
    private ImageView imgClose;
    private String currentUserId;
    private BarChart barChart;
    private PieChart pieChart;

    private List<Integer> customColors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spending_report);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        imgClose = findViewById(R.id.imgClose3);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);



        // Get the current user ID from Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {

        }

        // Get a list of all months available in a year
        List<String> monthsList = getAllMonthsInYear();
        customColors = getCustomColors(8); // Change 8 to the desired number of colors


        // Create an ArrayAdapter with the list of months
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthsList);

        // Set the dropdown layout for the spinner
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter for the month spinner
        spinnerMonth.setAdapter(adapter);

        // Find the index of the current month in the monthsList
        int currentMonthIndex = Calendar.getInstance().get(Calendar.MONTH);

        // Set the spinner selection to the current month
        spinnerMonth.setSelection(currentMonthIndex);

        // Retrieve the wallets from Firebase and update the spinnerSelectBudget adapter
        getWalletListFromDatabase();

        // Set OnClickListener for the imgClose ImageView
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Call this method when the selected month from the spinner changes
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = parent.getItemAtPosition(position).toString();
                // Call a method here to retrieve and display the transactions for the selected month
                displayTransactionsForSelectedMonth(selectedMonth,customColors);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Wallets_Page", "Error retrieving wallet list from database: " + databaseError.getMessage());
            }
        });
    }

    private List<String> getAllMonthsInYear() {
        List<String> monthsList = new ArrayList<>();

        // Create a Calendar instance to get the current year
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a SimpleDateFormat to format the month names
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

        // Loop through all months and add them to the list
        for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
            calendar.set(currentYear, month, 1); // Set the calendar to the first day of the month
            String monthName = monthFormat.format(calendar.getTime());
            monthsList.add(monthName);
        }

        return monthsList;
    }

    private List<Integer> getCustomColors(int size) {

        // Create a list to store custom colors
        List<Integer> colors = new ArrayList<>();

        // Add colors to the list (You can add more colors here or modify as needed)
        colors.add(ContextCompat.getColor(this, R.color.LightRed));
        colors.add(ContextCompat.getColor(this, R.color.Purple));
        colors.add(ContextCompat.getColor(this, R.color.LightBlue));
        colors.add(ContextCompat.getColor(this, R.color.DarkBlue));
        colors.add(ContextCompat.getColor(this, R.color.LightGreen));
        colors.add(ContextCompat.getColor(this, R.color.Yellow));
        colors.add(ContextCompat.getColor(this, R.color.Orange));
        colors.add(ContextCompat.getColor(this, R.color.LightPurple));

        // If the required size is larger than the available colors, repeat the colors
        while (colors.size() < size) {
            colors.addAll(colors);
        }

        // Return the list with the required number of colors
        return colors.subList(0, size);
    }

    private void displayTransactionsForSelectedMonth(String selectedMonth, List<Integer> customColors) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUserId).child("wallets");

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> categoryLabels = new ArrayList<>(); // To store category labels with data

                for (DataSnapshot walletSnapshot : dataSnapshot.getChildren()) {
                    if (walletSnapshot.child("walletTransactions").exists()) {
                        DataSnapshot walletTransactionsSnapshot = walletSnapshot.child("walletTransactions");
                        for (DataSnapshot transactionSnapshot : walletTransactionsSnapshot.getChildren()) {
                            String transactionMonth = getMonthFromDate(transactionSnapshot.child("transactionDate").getValue(String.class));

                            if (selectedMonth.equalsIgnoreCase(transactionMonth)) {
                                String category = transactionSnapshot.child("transactionCategory").getValue(String.class);
                                int amount = transactionSnapshot.child("transactionAmount").getValue(Integer.class);

                                // Check if the category label already exists in the list
                                int categoryIndex = categoryLabels.indexOf(category);
                                if (categoryIndex == -1) {
                                    // Category label not found, add it to the list
                                    categoryLabels.add(category);
                                    // Add a new BarEntry for this category with the associated amount
                                    entries.add(new BarEntry(entries.size(), amount));
                                } else {
                                    // Category label already exists, update the existing BarEntry with the amount
                                    BarEntry existingEntry = entries.get(categoryIndex);
                                    existingEntry.setY(existingEntry.getY() + amount);
                                }
                            }
                        }
                    }
                }
                displayPieChart(entries, categoryLabels, customColors);

                if (entries.isEmpty() || categoryLabels.isEmpty()) {
                    // Handle the case when there are no transactions for the selected month or data is invalid
                    pieChart.clear();
                    pieChart.setNoDataText("No data available for this month");
                    pieChart.invalidate();

                    barChart.clear();
                    barChart.setNoDataText("No data available for this month");
                    barChart.invalidate();
                    return;
                }

                // Create a BarDataSet with the entries and a label for the legend
                BarDataSet dataSet = new BarDataSet(entries, "Transaction Categories");
                dataSet.setColors(customColors);

                // Set the custom renderer to the barChart
                RoundedBarChartRenderer roundedBarChartRenderer = new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler());
                barChart.setRenderer(roundedBarChartRenderer);

                // Create a BarData with the dataSet
                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f); // Set the width of the bars

                // Customize the X-axis
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(categoryLabels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setDrawGridLines(false);
                xAxis.setDrawLabels(!categoryLabels.isEmpty()); // Hide labels if there are no categories with data

                // Customize the Y-axis
                YAxis yAxisLeft = barChart.getAxisLeft();
                yAxisLeft.setGranularity(1f);
                YAxis yAxisRight = barChart.getAxisRight();
                yAxisRight.setEnabled(false); // Disable the right Y-axis
                yAxisLeft.setDrawGridLines(false); // Disable Y-axis gridlines
                yAxisLeft.setGranularityEnabled(true);

                // Set the custom ValueFormatter for the data set (to display "RM" before the values)
                dataSet.setValueFormatter(new MyValueFormatter());

                // Set the barData to the chart
                barChart.setData(barData);
                barChart.getDescription().setEnabled(false);
                // Set the value text size here (change the value as needed)
                dataSet.setValueTextSize(11f);
                barChart.invalidate(); // Refresh the chart

                barChart.setDragEnabled(true);
                barChart.setScaleEnabled(false); // Disable scaling to only allow horizontal scrolling
                barChart.setPinchZoom(false);
                barChart.setVisibleXRangeMaximum(4); // Adjust this value based on the number of bars you want to show initially
                displayPieChart(entries, categoryLabels,customColors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("spending_report", "Error retrieving transactions from database: " + databaseError.getMessage());
            }
        });
    }

    private String getMonthFromDate(String date) {
        // Create a SimpleDateFormat to parse the date string
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try {
            // Parse the date string to a Date object
            Date parsedDate = inputFormat.parse(date);

            // Create a Calendar instance and set it to the parsed date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            // Get the month from the Calendar and format it as MMMM (e.g., "July")
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            String monthName = outputFormat.format(calendar.getTime());

            return monthName;
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the parse exception (optional) and return an empty string or an error message
            return "";
        }
    }


    private void displayPieChart(List<BarEntry> entries, List<String> categoryLabels, List<Integer> customColors) {
        if (entries.isEmpty() || categoryLabels.isEmpty() || entries.size() != categoryLabels.size()) {
            pieChart.clear();
            pieChart.setNoDataText("No data available for this month");
            pieChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.Redcolor));
            pieChart.invalidate();
            return;
        }

        float totalSpending = 0f;
        for (BarEntry entry : entries) {
            totalSpending += entry.getY();
        }

        if (totalSpending == 0f) {
            pieChart.clear();
            pieChart.setNoDataText("No data available for this month");
            pieChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.Redcolor));
            pieChart.invalidate();
            return;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        for (int i = 0; i < categoryLabels.size(); i++) {
            String category = categoryLabels.get(i);
            BarEntry entry = entries.get(i);
            float percentage = (entry.getY() / totalSpending) * 100;
            pieEntries.add(new PieEntry(percentage, category));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Category Percentages");
        pieDataSet.setColors(customColors); // Use custom colors here
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentageFormatter());
        pieData.setValueTextSize(10f);
        pieData.setValueTextColor(Color.WHITE);

        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        Legend legend = pieChart.getLegend();
        List<LegendEntry> legendEntries = new ArrayList<>();

        for (int i = 0; i < categoryLabels.size(); i++) {
            String category = categoryLabels.get(i);
            LegendEntry legendEntry = new LegendEntry();
            legendEntry.label = category;
            // Ensure that the index doesn't exceed the available colors in customColors array
            int colorIndex = i % customColors.size();
            legendEntry.formColor = customColors.get(colorIndex);
            legendEntry.form = Legend.LegendForm.CIRCLE;
            legendEntry.formLineWidth =5f;
            legendEntry.formLineDashEffect = null;
            legendEntry.formSize = 14f;
            legendEntries.add(legendEntry);
        }
        legend.setEntries(legendEntries);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setFormToTextSpace(8f); // Space between form and text in pixels
        legend.setCustom(legendEntries);
    }
}
    class MyValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        // Add "RM" before the value and return the formatted string
        return "RM " + value;
    }
}

class PercentageFormatter extends ValueFormatter {
    private DecimalFormat format;

    public PercentageFormatter() {
        // Format the value with three decimal places
        format = new DecimalFormat("##.#");
    }

    @Override
    public String getFormattedValue(float value) {
        // Round up the value to three decimal places
        double roundedValue = Math.ceil(value * 1000.0) / 1000.0;

        // Add "%" after the formatted value and return the string
        return format.format(roundedValue) + "%";
    }
}

 class RoundedBarChartRenderer extends BarChartRenderer {

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

     @Override
     protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
         Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

         mShadowPaint.setColor(dataSet.getBarShadowColor());

         float phaseX = mAnimator.getPhaseX();
         float phaseY = mAnimator.getPhaseY();

         // draw the bar shadow before the values
         if (mChart.isDrawBarShadowEnabled()) {
             mShadowPaint.setColor(dataSet.getBarShadowColor());

             BarBuffer buffer = mBarBuffers[index];

             float barWidth = mChart.getBarData().getBarWidth() / 2f;

             for (int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4) {
                 float left = buffer.buffer[j];
                 float right = buffer.buffer[j + 2];
                 float top = buffer.buffer[j + 1];
                 float bottom = buffer.buffer[j + 3];

                 mBarRect.set(left + barWidth, top, right - barWidth, bottom);

                 c.drawRoundRect(mBarRect, 10f, 10f, mShadowPaint);
             }
         }

         // initialize the buffer
         BarBuffer buffer = mBarBuffers[index];
         buffer.setPhases(phaseX, phaseY);
         buffer.setDataSet(index);
         buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
         buffer.setBarWidth(mChart.getBarData().getBarWidth());

         buffer.feed(dataSet);

         trans.pointValuesToPixel(buffer.buffer);

         // draw the bars
         for (int j = 0; j < buffer.buffer.length * phaseX; j += 4) {
             float left = buffer.buffer[j];
             float right = buffer.buffer[j + 2];
             float top = buffer.buffer[j + 1];
             float bottom = buffer.buffer[j + 3];

             mBarRect.set(left, top, right, bottom);

             // Set the color for the bar based on the data set's color
             mRenderPaint.setColor(dataSet.getColor(j / 4));

             c.drawRoundRect(mBarRect, 10f, 10f, mRenderPaint);
         }
     }

 }




