package com.college;

import com.college.utils.MigrationRunner;

public class TriggerMigration {
    public static void main(String[] args) {
        System.out.println("Triggering Migrations...");
        try {
            MigrationRunner.runMigrations();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
