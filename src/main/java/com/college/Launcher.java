package com.college;

public class Launcher {
    public static void main(String[] args) {
        // Run Database Migrations
        com.college.utils.MigrationRunner.runMigrations();

        MainFX.main(args);
    }
}
