package com.clockinpro.dao;

import com.clockinpro.model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {

    public boolean registerEmployee(Employee employee) {
        String query = "INSERT INTO employees (name, email, password, hourly_rate, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, employee.getName());
                pstmt.setString(2, employee.getEmail());
                pstmt.setString(3, employee.getPassword());
                pstmt.setDouble(4, employee.getHourlyRate());
                pstmt.setString(5, employee.getRole() != null ? employee.getRole() : "EMPLOYEE");
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("=== REGISTER ERROR ===");
            System.err.println("Message : " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrCode : " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    public Employee login(String email, String password) {
        String query = "SELECT * FROM employees WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("=== LOGIN ERROR: Connection is null ===");
                return null;
            }

            // --- DEBUG: Check if email exists at all (ignoring password) ---
            try (PreparedStatement debugStmt = conn.prepareStatement("SELECT id, email, password, role FROM employees WHERE email = ?")) {
                debugStmt.setString(1, email);
                ResultSet debugRs = debugStmt.executeQuery();
                if (debugRs.next()) {
                    String dbPassword = debugRs.getString("password");
                    System.out.println("DEBUG: Found user with email: " + email);
                    System.out.println("DEBUG: DB password = [" + dbPassword + "]");
                    System.out.println("DEBUG: Input password = [" + password + "]");
                    System.out.println("DEBUG: Passwords match? " + dbPassword.equals(password));
                } else {
                    System.err.println("DEBUG: NO user found with email: " + email);
                    // Check total row count to see if RLS is blocking
                    try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM employees")) {
                        ResultSet countRs = countStmt.executeQuery();
                        if (countRs.next()) {
                            System.out.println("DEBUG: Total employees visible = " + countRs.getInt(1));
                        }
                    }
                }
            }
            // --- END DEBUG ---

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                System.out.println("Trying login with email: " + email);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setName(rs.getString("name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setPassword(rs.getString("password"));
                    emp.setHourlyRate(rs.getDouble("hourly_rate"));
                    emp.setRole(rs.getString("role"));
                    System.out.println("Login success for: " + emp.getName());
                    return emp;
                } else {
                    System.err.println("No matching user found for email: " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("=== LOGIN ERROR ===");
            System.err.println("Message : " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        return null;
    }

    public Employee getEmployeeById(int id) {
        String query = "SELECT * FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setName(rs.getString("name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setPassword(rs.getString("password"));
                    emp.setHourlyRate(rs.getDouble("hourly_rate"));
                    emp.setRole(rs.getString("role"));
                    return emp;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee: " + e.getMessage());
        }
        return null;
    }

    public java.util.List<Employee> getAllEmployees() {
        java.util.List<Employee> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM employees";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return list;
            try (PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setName(rs.getString("name"));
                    emp.setEmail(rs.getString("email"));
                    emp.setHourlyRate(rs.getDouble("hourly_rate"));
                    emp.setRole(rs.getString("role"));
                    list.add(emp);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all employees: " + e.getMessage());
        }
        return list;
    }
}
