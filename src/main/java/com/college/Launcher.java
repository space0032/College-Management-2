package com.college;

public class Launcher {
    public static void main(String[] args) {
        // Load environment variables
        com.college.utils.EnvLoader.load();
        
        // Run Database Migrations
        com.college.utils.MigrationRunner.runMigrations();

        MainFX.main(args);
    }
}
