package com.clockinpro.service;

import com.clockinpro.dao.AttendanceDAO;
import com.clockinpro.dao.EmployeeDAO;
import com.clockinpro.dao.PayrollDAO;
import com.clockinpro.model.Employee;
import com.clockinpro.model.Payroll;
import java.util.List;

public class PayrollService {
    private AttendanceDAO attendanceDAO;
    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;

    public PayrollService() {
        this.attendanceDAO = new AttendanceDAO();
        this.payrollDAO = new PayrollDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    public String generatePayroll(int employeeId, String monthYear) {
        double totalHours = attendanceDAO.getTotalHoursByMonth(employeeId, monthYear);
        if (totalHours <= 0) {
            return "No completed work hours found for the month of " + monthYear;
        }

        Employee emp = employeeDAO.getEmployeeById(employeeId);
        double totalSalary = totalHours * emp.getHourlyRate();

        Payroll payroll = new Payroll();
        payroll.setEmployeeId(employeeId);
        payroll.setMonth(monthYear);
        payroll.setTotalHours(totalHours);
        payroll.setTotalSalary(totalSalary);

        boolean success = payrollDAO.savePayroll(payroll);
        if (success) {
            return String.format("Payroll generated successfully for %s! Total Hours: %.2f, Total Salary: $%.2f", 
                              monthYear, totalHours, totalSalary);
        } else {
            return "Failed to generate payroll.";
        }
    }

    public List<Payroll> getMonthlyReports(int employeeId) {
        return payrollDAO.getPayrollByEmployee(employeeId);
    }

    public List<Payroll> getAllPayrolls() {
        return payrollDAO.getAllPayrolls();
    }

    public String generatePaySlipText(int payrollId) {
        Payroll p = payrollDAO.getPayrollById(payrollId);
        if (p == null) return "Payroll record not found.";

        Employee emp = employeeDAO.getEmployeeById(p.getEmployeeId());
        if (emp == null) return "Employee record not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("          CLOCKINPRO - PAY SLIP           \n");
        sb.append("==========================================\n");
        sb.append("Employee Name: ").append(emp.getName()).append("\n");
        sb.append("Employee ID:   ").append(emp.getId()).append("\n");
        sb.append("Role:          ").append(emp.getRole()).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("Pay Period:    ").append(p.getMonth()).append("\n");
        sb.append("Hourly Rate:   $").append(String.format("%.2f", emp.getHourlyRate())).append("\n");
        sb.append("Total Hours:   ").append(String.format("%.2f", p.getTotalHours())).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("GROSS SALARY:  $").append(String.format("%.2f", p.getTotalSalary())).append("\n");
        sb.append("==========================================\n");
        sb.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("==========================================\n");

        return sb.toString();
    }
}
