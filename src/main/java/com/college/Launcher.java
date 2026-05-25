package com.college;

public class Launcher {
    public static void main(String[] args) {
        // Load environment variables
        com.college.utils.EnvLoader.load();

        MainFX.main(args);
    }
}
