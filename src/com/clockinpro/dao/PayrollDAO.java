package com.clockinpro.dao;

import com.clockinpro.model.Payroll;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    public boolean savePayroll(Payroll payroll) {
        // PostgreSQL uses ON CONFLICT for upsert — cleaner than check+insert/update
        String upsertQuery = "INSERT INTO payroll (employee_id, month, total_hours, total_salary) " +
                             "VALUES (?, ?, ?, ?) " +
                             "ON CONFLICT (employee_id, month) " +
                             "DO UPDATE SET total_hours = EXCLUDED.total_hours, total_salary = EXCLUDED.total_salary";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(upsertQuery)) {
            pstmt.setInt(1, payroll.getEmployeeId());
            pstmt.setString(2, payroll.getMonth());
            pstmt.setDouble(3, payroll.getTotalHours());
            pstmt.setDouble(4, payroll.getTotalSalary());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving payroll: " + e.getMessage());
            return false;
        }
    }

    public List<Payroll> getPayrollByEmployee(int employeeId) {
        List<Payroll> list = new ArrayList<>();
        String query = "SELECT * FROM payroll WHERE employee_id = ? ORDER BY month DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payroll p = new Payroll();
                p.setId(rs.getInt("id"));
                p.setEmployeeId(rs.getInt("employee_id"));
                p.setMonth(rs.getString("month"));
                p.setTotalHours(rs.getDouble("total_hours"));
                p.setTotalSalary(rs.getDouble("total_salary"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll details: " + e.getMessage());
        }
        return list;
    }

    public List<Payroll> getAllPayrolls() {
        List<Payroll> list = new ArrayList<>();
        String query = "SELECT * FROM payroll ORDER BY month DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Payroll p = new Payroll();
                p.setId(rs.getInt("id"));
                p.setEmployeeId(rs.getInt("employee_id"));
                p.setMonth(rs.getString("month"));
                p.setTotalHours(rs.getDouble("total_hours"));
                p.setTotalSalary(rs.getDouble("total_salary"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all payrolls: " + e.getMessage());
        }
        return list;
    }

    public Payroll getPayrollById(int payrollId) {
        String query = "SELECT * FROM payroll WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, payrollId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Payroll p = new Payroll();
                p.setId(rs.getInt("id"));
                p.setEmployeeId(rs.getInt("employee_id"));
                p.setMonth(rs.getString("month"));
                p.setTotalHours(rs.getDouble("total_hours"));
                p.setTotalSalary(rs.getDouble("total_salary"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll by ID: " + e.getMessage());
        }
        return null;
    }
}
