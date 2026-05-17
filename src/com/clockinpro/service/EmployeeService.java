package com.clockinpro.service;

import com.clockinpro.dao.EmployeeDAO;
import com.clockinpro.model.Employee;

public class EmployeeService {
    private EmployeeDAO employeeDAO;

    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
    }

    public boolean register(String name, String email, String password, double hourlyRate, String role) {
        Employee emp = new Employee(0, name, email, password, hourlyRate, role);
        return employeeDAO.registerEmployee(emp);
    }

    public Employee login(String email, String password) {
        return employeeDAO.login(email, password);
    }

    public Employee getEmployeeDetails(int id) {
        return employeeDAO.getEmployeeById(id);
    }

    public java.util.List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }
}
