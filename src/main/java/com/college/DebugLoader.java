package com.college;

public class DebugLoader {
    public static void main(String[] args) {
        System.out.println("Debugging Class Loading...");

        try {
            System.out.println("Attempting to load PayrollManagementView class...");
            Class<?> c1 = Class.forName("com.college.fx.views.PayrollManagementView");
            System.out.println("Class found: " + c1.getName());

            System.out.println("Attempting to instantiate result...");
            Object instance = c1.getDeclaredConstructor().newInstance();
            System.out.println("Successfully instantiated " + instance.getClass().getName());

        } catch (Throwable e) {
            System.out.println("FAILED to load/instantiate PayrollManagementView: " + e);
            e.printStackTrace();
        }

        try {
            System.out.println("Attempting to load LeaveApprovalView class...");
            Class<?> c2 = Class.forName("com.college.fx.views.LeaveApprovalView");
            System.out.println("Class found: " + c2.getName());
            Object instance2 = c2.getDeclaredConstructor().newInstance();
            System.out.println("Successfully instantiated " + instance2.getClass().getName());
        } catch (Throwable e) {
            System.out.println("FAILED to load/instantiate LeaveApprovalView: " + e);
            e.printStackTrace();
        }
    }
}
