package com.example.pocketpal;

public class HelperClass {

    private boolean isDateHeader;

    private double totalAmountForCategory;

    String username,gmail,password;
    String walletID,walletName,walletCategory;
    String transactionID;
    String transactionCategory;
    String transactionDate;
    String transactionNote;
    String transactionImage;
    Double transactionAmount,walletBalance;

    public boolean isDateHeader() {
        return isDateHeader;
    }

    public void setDateHeader(boolean dateHeader) {
        isDateHeader = dateHeader;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }
    public String getTransactionID() {
        return transactionID;
    }
    public String getTransactionCategory() {
        return transactionCategory;
    }
    public String getTransactionDate() {
        return transactionDate;
    }
    public String getTransactionNote() {
        return transactionNote;
    }
    public String getTransactionImage() {
        return transactionImage;
    }
    public Double getTransactionAmount() {
        return transactionAmount;
    }
    public double getTotalAmountForCategory() {return totalAmountForCategory;}

    public void setTransactionCategory(String transactionCategory) {
        this.transactionCategory = transactionCategory;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionNote(String transactionNote) {
        this.transactionNote = transactionNote;
    }

    public void setTransactionImage(String transactionImage) {
        this.transactionImage = transactionImage;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public void setTotalAmountForCategory(double totalAmountForCategory) {
        this.totalAmountForCategory = totalAmountForCategory;
    }


    // Constructor for updating transaction data
    public HelperClass(String transactionID, String transactionCategory, String transactionDate, String transactionNote, String transactionImage, Double transactionAmount) {
        this.transactionID = transactionID;
        this.transactionCategory = transactionCategory;
        this.transactionDate = transactionDate;
        this.transactionNote = transactionNote;
        this.transactionImage = transactionImage;
        this.transactionAmount = transactionAmount;
    }
    // Constructor for date header items
    public HelperClass(String transactionDate) {
        this.transactionDate = transactionDate;
        this.isDateHeader = true;
    }


    public String getWalletName() {
        return walletName;
    }
    public String getWalletID() {
        return walletID;
    }
    public String getWalletCategory() {
        return walletCategory;
    }

    public HelperClass(String walletID, String walletName, Double walletBalance, String walletCategory) {
        this.walletID = walletID;
        this.walletName = walletName;
        this.walletBalance = walletBalance;
        this.walletCategory = walletCategory;
    }

    public double getWalletBalance() {
        if (walletBalance == null) {
            return 0.0; // Return 0 if walletBalance is null
        }
        return walletBalance;
    }

    public void setWalletID(String walletID) {
        this.walletID = walletID;
    }
    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }
    public void setWalletCategory(String walletCategory) {
        this.walletCategory = walletCategory;
    }



    public HelperClass(String username, String gmail, String password) {
        this.username = username;
        this.gmail = gmail;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HelperClass() {
    }
}
