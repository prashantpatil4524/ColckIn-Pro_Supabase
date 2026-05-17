package com.clockinpro.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private EmployeeDashboardPanel employeeDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;

    public MainFrame() {
        setTitle("ClockInPro - Work Hours & Payroll Tracker");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        employeeDashboardPanel = new EmployeeDashboardPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);

        // Add panels to card layout
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(registerPanel, "Register");
        mainPanel.add(employeeDashboardPanel, "EmployeeDashboard");
        mainPanel.add(adminDashboardPanel, "AdminDashboard");

        add(mainPanel);

        // Initially show Login
        showPanel("Login");
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        if (panelName.equals("EmployeeDashboard")) {
            employeeDashboardPanel.refreshData();
        } else if (panelName.equals("AdminDashboard")) {
            adminDashboardPanel.refreshData();
        }
    }

    public EmployeeDashboardPanel getEmployeeDashboardPanel() {
        return employeeDashboardPanel;
    }

    public AdminDashboardPanel getAdminDashboardPanel() {
        return adminDashboardPanel;
    }

    public static void main(String[] args) {
        try {
            // Use Nimbus Look and Feel for modern UI and proper color rendering
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
