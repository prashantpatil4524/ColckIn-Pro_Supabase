package com.clockinpro.ui;

import com.clockinpro.model.Employee;
import com.clockinpro.service.EmployeeService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private EmployeeService employeeService;

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.employeeService = new EmployeeService();

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 244, 248)); // Crisp high-contrast light background

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 155, 160), 2),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome to ClockInPro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 30, 10);
        formPanel.add(titleLabel, gbc);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = 1;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        gbc.gridy = 1;
        formPanel.add(emailLabel, gbc);
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        JLabel pwdLabel = new JLabel("Password:");
        pwdLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(pwdLabel, gbc);
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 95, 200)); // Darker high-contrast blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.addActionListener(e -> attemptLogin());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(loginButton, gbc);

        JButton registerButton = new JButton("Don't have an account? Register");
        registerButton.setFont(labelFont);
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setForeground(new Color(0, 80, 160)); // Darker link text
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> mainFrame.showPanel("Register"));

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 10, 10);
        formPanel.add(registerButton, gbc);

        add(formPanel);
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Employee emp = employeeService.login(email, password);
        if (emp != null) {
            if ("HR".equalsIgnoreCase(emp.getRole()) || "ADMIN".equalsIgnoreCase(emp.getRole())) {
                String passkey = JOptionPane.showInputDialog(this, "Enter HR Passkey to continue:");
                if (!"111".equals(passkey)) {
                    JOptionPane.showMessageDialog(this, "Invalid HR Passkey. Login cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            if ("HR".equalsIgnoreCase(emp.getRole()) || "ADMIN".equalsIgnoreCase(emp.getRole())) {
                if (mainFrame.getAdminDashboardPanel() != null) {
                    mainFrame.getAdminDashboardPanel().setCurrentEmployee(emp);
                    mainFrame.showPanel("AdminDashboard");
                } else {
                    JOptionPane.showMessageDialog(this, "Dashboard not initialized yet.");
                }
            } else {
                mainFrame.getEmployeeDashboardPanel().setCurrentEmployee(emp);
                mainFrame.showPanel("EmployeeDashboard");
            }
            
            // clear fields
            emailField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
