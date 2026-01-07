package com.college.tests;

import com.college.dao.EnhancedFeeDAO;

public class FinanceFeatureVerification {

    public static void main(String[] args) {
        System.out.println("Verifying Finance Dashboard Features...");
        EnhancedFeeDAO feeDAO = new EnhancedFeeDAO();
        boolean success = true;

        // 1. Verify getTodaysCollectionAmount
        try {
            double amount = feeDAO.getTodaysCollectionAmount();
            System.out.println("[PASS] Today's Collection Amount query runs. Result: " + amount);
        } catch (Exception e) {
            System.err.println("[FAIL] getTodaysCollectionAmount threw exception: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        // 2. Verify getTotalPendingAmount
        try {
            double pending = feeDAO.getTotalPendingAmount();
            System.out.println("[PASS] Total Pending Amount query runs. Result: " + pending);
        } catch (Exception e) {
            System.err.println("[FAIL] getTotalPendingAmount threw exception: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        // 3. Verify Payment Search
        try {
            var payments = feeDAO.searchPaymentHistory("");
            System.out.println("[PASS] searchPaymentHistory returns " + payments.size() + " records.");
        } catch (Exception e) {
            System.err.println("[FAIL] searchPaymentHistory threw exception: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }

        if (success) {
            System.out.println("\nVERIFICATION SUCCESSFUL: Finance DAO features are working.");
        } else {
            System.out.println("\nVERIFICATION FAILED");
            System.exit(1);
        }
    }
}
