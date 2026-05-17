package com.clockinpro.service;

import com.clockinpro.dao.AttendanceDAO;
import com.clockinpro.model.Attendance;
import java.sql.Timestamp;
import java.util.List;

public class AttendanceService {
    private AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    public String clockIn(int employeeId) {
        Attendance current = attendanceDAO.getActiveLogin(employeeId);
        if (current != null) {
            return "Error: You are already clocked in! Please clock out first.";
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean success = attendanceDAO.recordLogin(employeeId, now);
        if (success) {
            return "Success: Clocked in at " + now;
        } else {
            return "Error during clock-in. Please try again.";
        }
    }

    public String clockOut(int employeeId) {
        Attendance current = attendanceDAO.getActiveLogin(employeeId);
        if (current == null) {
            return "Error: You have not clocked in yet!";
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        long msDiff = now.getTime() - current.getLoginTime().getTime();
        double hours = msDiff / (1000.0 * 60 * 60);

        boolean success = attendanceDAO.recordLogout(current.getId(), now, hours);
        if (success) {
            return String.format("Success: Clocked out at %s. Session hours: %.2f", now, hours);
        } else {
            return "Error during clock-out. Please try again.";
        }
    }

    public List<Attendance> getWorkHoursHistory(int employeeId) {
        return attendanceDAO.getAttendanceByEmployee(employeeId);
    }

    public Attendance getActiveSession(int employeeId) {
        return attendanceDAO.getActiveLogin(employeeId);
    }
}
