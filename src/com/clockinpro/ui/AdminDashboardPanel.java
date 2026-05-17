package com.clockinpro.ui;

import com.clockinpro.model.Employee;
import com.clockinpro.model.Payroll;
import com.clockinpro.service.EmployeeService;
import com.clockinpro.service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private EmployeeService employeeService;
    private PayrollService payrollService;
    private Employee currentEmployee;

    private JLabel welcomeLabel;
    private JLabel clockLabel;
    private JTable employeeTable;
    private JTable payrollTable;
    private JLabel expensesLabel;
    private Timer uiTimer;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.employeeService = new EmployeeService();
        this.payrollService = new PayrollService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        welcomeLabel = new JLabel("HR Management Dashboard", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(33, 37, 41));
        
        clockLabel = new JLabel("Loading clock...", SwingConstants.LEFT);
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        clockLabel.setForeground(new Color(108, 117, 125));
        
        titlePanel.add(welcomeLabel);
        titlePanel.add(clockLabel);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        logoutButton.addActionListener(e -> {
            if(uiTimer != null) uiTimer.stop();
            this.currentEmployee = null;
            mainFrame.showPanel("Login");
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Tab 1: Employees List
        JPanel employeesPanel = new JPanel(new BorderLayout(10, 10));
        employeesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        String[] empCols = {"ID", "Name", "Email", "Hourly Rate", "Role"};
        employeeTable = new JTable(new DefaultTableModel(empCols, 0));
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeTable.setRowHeight(32);
        employeeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        employeesPanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        tabbedPane.addTab("Employees Directory", employeesPanel);

        // Tab 2: Payroll Histories & Expenses
        JPanel payrollPanel = new JPanel(new BorderLayout(10, 10));
        payrollPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel statsCard = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsCard.setBackground(Color.WHITE);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        expensesLabel = new JLabel("Total Payroll Expenses: $0.00");
        expensesLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        expensesLabel.setForeground(new Color(13, 110, 253));
        statsCard.add(expensesLabel);
        payrollPanel.add(statsCard, BorderLayout.NORTH);

        String[] payCols = {"Payroll ID", "Employee ID", "Month", "Total Hours", "Total Salary"};
        payrollTable = new JTable(new DefaultTableModel(payCols, 0));
        payrollTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        payrollTable.setRowHeight(32);
        payrollTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        payrollPanel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);
        
        JButton viewSlipButton = new JButton("View Employee Pay Slip");
        viewSlipButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        viewSlipButton.setBackground(new Color(108, 117, 125));
        viewSlipButton.setForeground(Color.WHITE);
        viewSlipButton.addActionListener(e -> {
            int row = payrollTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a payroll record.");
                return;
            }
            int payrollId = (int) payrollTable.getValueAt(row, 0);
            String slip = payrollService.generatePaySlipText(payrollId);
            
            JTextArea textArea = new JTextArea(slip);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Digital Pay Slip", JOptionPane.PLAIN_MESSAGE);
        });
        payrollPanel.add(viewSlipButton, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Payroll Analytics", payrollPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Action Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton refreshButton = new JButton("Refresh Records");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setBackground(new Color(13, 110, 253));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(180, 45));
        refreshButton.addActionListener(e -> refreshData());
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        startClock();
    }

    private void startClock() {
        uiTimer = new Timer(1000, e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  HH:mm:ss");
            clockLabel.setText(now.format(dtf));
        });
        uiTimer.start();
    }

    public void setCurrentEmployee(Employee emp) {
        this.currentEmployee = emp;
        welcomeLabel.setText("HR Dashboard - Welcome, " + emp.getName());
        refreshData();
    }

    public void refreshData() {
        if (currentEmployee == null) return;

        // Refresh Employees Table
        List<Employee> employees = employeeService.getAllEmployees();
        DefaultTableModel empModel = (DefaultTableModel) employeeTable.getModel();
        empModel.setRowCount(0);

        for (Employee e : employees) {
            empModel.addRow(new Object[]{
                e.getId(),
                e.getName(),
                e.getEmail(),
                String.format("$%.2f", e.getHourlyRate()),
                e.getRole()
            });
        }

        // Refresh Payroll Table & Calculate Expenses
        List<Payroll> payrolls = payrollService.getAllPayrolls();
        DefaultTableModel payModel = (DefaultTableModel) payrollTable.getModel();
        payModel.setRowCount(0);

        double totalExpenses = 0.0;
        for (Payroll p : payrolls) {
            payModel.addRow(new Object[]{
                p.getId(),
                p.getEmployeeId(),
                p.getMonth(),
                String.format("%.2f", p.getTotalHours()),
                String.format("$%.2f", p.getTotalSalary())
            });
            totalExpenses += p.getTotalSalary();
        }

        expensesLabel.setText(String.format("Total Payroll Expenses: $%.2f", totalExpenses));
    }
}
