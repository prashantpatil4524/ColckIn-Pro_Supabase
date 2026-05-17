package com.clockinpro.dao;

import com.clockinpro.model.Attendance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public boolean recordLogin(int employeeId, Timestamp loginTime) {
        String query = "INSERT INTO attendance (employee_id, login_time) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setTimestamp(2, loginTime);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording login: " + e.getMessage());
            return false;
        }
    }

    public Attendance getActiveLogin(int employeeId) {
        // PostgreSQL uses LIMIT instead of MySQL's same LIMIT — same syntax, no change needed
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Attendance att = new Attendance();
                att.setId(rs.getInt("id"));
                att.setEmployeeId(rs.getInt("employee_id"));
                att.setLoginTime(rs.getTimestamp("login_time"));
                return att;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active login: " + e.getMessage());
        }
        return null;
    }

    public boolean recordLogout(int attendanceId, Timestamp logoutTime, double totalHours) {
        String query = "UPDATE attendance SET logout_time = ?, total_hours = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, logoutTime);
            pstmt.setDouble(2, totalHours);
            pstmt.setInt(3, attendanceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error recording logout: " + e.getMessage());
            return false;
        }
    }

    public List<Attendance> getAttendanceByEmployee(int employeeId) {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY login_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Attendance att = new Attendance();
                att.setId(rs.getInt("id"));
                att.setEmployeeId(rs.getInt("employee_id"));
                att.setLoginTime(rs.getTimestamp("login_time"));
                att.setLogoutTime(rs.getTimestamp("logout_time"));
                att.setTotalHours(rs.getDouble("total_hours"));
                list.add(att);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance history: " + e.getMessage());
        }
        return list;
    }

    public double getTotalHoursByMonth(int employeeId, String monthYear) {
        // PostgreSQL equivalent of MySQL's DATE_FORMAT(login_time, '%Y-%m') = ?
        // Use TO_CHAR(login_time, 'YYYY-MM') = ?
        String query = "SELECT SUM(total_hours) as sum_hours FROM attendance " +
                       "WHERE employee_id = ? AND TO_CHAR(login_time, 'YYYY-MM') = ? AND logout_time IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            pstmt.setString(2, monthYear);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("sum_hours");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating monthly hours: " + e.getMessage());
        }
        return 0.0;
    }
}
