package com.clockinpro.ui;

import com.clockinpro.model.Attendance;
import com.clockinpro.model.Employee;
import com.clockinpro.model.Payroll;
import com.clockinpro.service.AttendanceService;
import com.clockinpro.service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeDashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private AttendanceService attendanceService;
    private PayrollService payrollService;
    private Employee currentEmployee;

    private JLabel welcomeLabel;
    private JLabel clockLabel;
    private JLabel statusLabel;
    private JLabel durationLabel;
    private JTable hoursTable;
    private JTable payrollTable;
    private Timer uiTimer;

    public EmployeeDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.attendanceService = new AttendanceService();
        this.payrollService = new PayrollService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 244, 248));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        welcomeLabel = new JLabel("Welcome!", SwingConstants.LEFT);
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
            stopTimers();
            this.currentEmployee = null;
            mainFrame.showPanel("Login");
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center configuration
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        // Panel 1: Actions & Hours
        JPanel attendancePanel = new JPanel(new BorderLayout(15, 15));
        attendancePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Status Card
        JPanel statusCard = new JPanel(new GridLayout(1, 2, 10, 0));
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        statusLabel = new JLabel("Status: Offline");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        durationLabel = new JLabel("Duration: 00:00:00");
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        durationLabel.setForeground(new Color(13, 110, 253));
        
        statusCard.add(statusLabel);
        statusCard.add(durationLabel);
        
        JPanel topActions = new JPanel(new BorderLayout(10, 10));
        topActions.setOpaque(false);
        topActions.add(statusCard, BorderLayout.NORTH);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        actionButtons.setOpaque(false);
        
        JButton clockInButton = new JButton("Clock In");
        clockInButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        clockInButton.setBackground(new Color(25, 135, 84));
        clockInButton.setForeground(Color.WHITE);
        clockInButton.setPreferredSize(new Dimension(180, 50));
        clockInButton.addActionListener(e -> {
            String msg = attendanceService.clockIn(currentEmployee.getId());
            JOptionPane.showMessageDialog(this, msg);
            refreshData();
        });

        JButton clockOutButton = new JButton("Clock Out");
        clockOutButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        clockOutButton.setBackground(new Color(180, 40, 50));
        clockOutButton.setForeground(Color.WHITE);
        clockOutButton.setPreferredSize(new Dimension(180, 50));
        clockOutButton.addActionListener(e -> {
            String msg = attendanceService.clockOut(currentEmployee.getId());
            JOptionPane.showMessageDialog(this, msg);
            refreshData();
        });

        actionButtons.add(clockInButton);
        actionButtons.add(clockOutButton);
        topActions.add(actionButtons, BorderLayout.CENTER);
        
        attendancePanel.add(topActions, BorderLayout.NORTH);

        String[] hoursCols = {"Login Time", "Logout Time", "Hours"};
        hoursTable = new JTable(new DefaultTableModel(hoursCols, 0));
        hoursTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursTable.setRowHeight(32);
        hoursTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        attendancePanel.add(new JScrollPane(hoursTable), BorderLayout.CENTER);
        tabbedPane.addTab("Attendance & Hours", attendancePanel);

        // Panel 2: Payroll
        JPanel payrollPanel = new JPanel(new BorderLayout(10, 10));
        payrollPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel generatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generatePanel.setOpaque(false);
        
        JLabel monthLabel = new JLabel("Month (YYYY-MM):");
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generatePanel.add(monthLabel);
        
        JTextField monthField = new JTextField(10);
        monthField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        generatePanel.add(monthField);
        
        JButton generateButton = new JButton("Generate Payroll");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateButton.setBackground(new Color(13, 110, 253));
        generateButton.setForeground(Color.WHITE);
        generateButton.addActionListener(e -> {
            String mo = monthField.getText().trim();
            if(!mo.matches("\\d{4}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Invalid format. Use YYYY-MM.");
                return;
            }
            String result = payrollService.generatePayroll(currentEmployee.getId(), mo);
            JOptionPane.showMessageDialog(this, result);
            refreshData();
        });
        generatePanel.add(generateButton);
        payrollPanel.add(generatePanel, BorderLayout.NORTH);

        String[] payCols = {"ID", "Month", "Total Hours", "Total Salary"};
        payrollTable = new JTable(new DefaultTableModel(payCols, 0));
        payrollTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        payrollTable.setRowHeight(32);
        payrollTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        payrollPanel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);
        
        JButton viewSlipButton = new JButton("View Digital Pay Slip");
        viewSlipButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        viewSlipButton.setBackground(new Color(108, 117, 125));
        viewSlipButton.setForeground(Color.WHITE);
        viewSlipButton.addActionListener(e -> {
            int row = payrollTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a payroll record from the table.");
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
        
        tabbedPane.addTab("Payroll Reports", payrollPanel);

        add(tabbedPane, BorderLayout.CENTER);
        
        startTimers();
    }

    private void startTimers() {
        uiTimer = new Timer(1000, e -> {
            // Update Real-time Clock
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  HH:mm:ss");
            clockLabel.setText(now.format(dtf));
            
            // Update Duration if clocked in
            if (currentEmployee != null) {
                Attendance active = attendanceService.getActiveSession(currentEmployee.getId());
                if (active != null) {
                    statusLabel.setText("Status: WORKING");
                    statusLabel.setForeground(new Color(25, 135, 84));
                    
                    long ms = System.currentTimeMillis() - active.getLoginTime().getTime();
                    long sec = (ms / 1000) % 60;
                    long min = (ms / (1000 * 60)) % 60;
                    long hr = (ms / (1000 * 60 * 60));
                    durationLabel.setText(String.format("Duration: %02d:%02d:%02d", hr, min, sec));
                } else {
                    statusLabel.setText("Status: OFFLINE");
                    statusLabel.setForeground(new Color(108, 117, 125));
                    durationLabel.setText("Duration: 00:00:00");
                }
            }
        });
        uiTimer.start();
    }

    private void stopTimers() {
        if (uiTimer != null) uiTimer.stop();
    }

    public void setCurrentEmployee(Employee emp) {
        this.currentEmployee = emp;
        welcomeLabel.setText("Welcome, " + emp.getName());
        refreshData();
    }

    public void refreshData() {
        if (currentEmployee == null) return;

        // Refresh Hours Table
        List<Attendance> records = attendanceService.getWorkHoursHistory(currentEmployee.getId());
        DefaultTableModel hoursModel = (DefaultTableModel) hoursTable.getModel();
        hoursModel.setRowCount(0);
        for (Attendance att : records) {
            String logout = (att.getLogoutTime() != null) ? att.getLogoutTime().toString() : "Current";
            hoursModel.addRow(new Object[]{
                att.getLoginTime().toString(),
                logout,
                String.format("%.2f", att.getTotalHours())
            });
        }

        // Refresh Payroll Table
        List<Payroll> reports = payrollService.getMonthlyReports(currentEmployee.getId());
        DefaultTableModel payrollModel = (DefaultTableModel) payrollTable.getModel();
        payrollModel.setRowCount(0);
        for (Payroll p : reports) {
            payrollModel.addRow(new Object[]{
                p.getId(),
                p.getMonth(),
                String.format("%.2f", p.getTotalHours()),
                String.format("$%.2f", p.getTotalSalary())
            });
        }
    }
}
