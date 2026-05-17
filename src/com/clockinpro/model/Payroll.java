package com.clockinpro.model;

public class Payroll {
    private int id;
    private int employeeId;
    private String month; // format yyyy-MM
    private double totalHours;
    private double totalSalary;

    public Payroll() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }
}
