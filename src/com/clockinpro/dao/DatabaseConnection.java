package com.clockinpro.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // -------------------------------------------------------
    // SUPABASE CONNECTION SETTINGS
    // Replace the values below with your Supabase project info
    // Found at: Supabase Dashboard → Settings → Database
    // -------------------------------------------------------

    private static final String HOST     = "db.zqjlrctwksoxbamjzrzk.supabase.co"; // e.g. db.abcxyz.supabase.co
    private static final String PORT     = "5432";
    private static final String DATABASE = "postgres";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "25062004@Patil"; // Set during project creation

    // Full JDBC URL for Supabase (PostgreSQL)
    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE + "?sslmode=require";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found! Add postgresql-42.7.5.jar to your classpath.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Database Connection Failed! Check your Supabase credentials.");
            e.printStackTrace();
            return null;
        }
    }
}
