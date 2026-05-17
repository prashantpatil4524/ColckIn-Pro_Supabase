package com.clockinpro.ui;

import com.clockinpro.service.EmployeeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private MainFrame mainFrame;
    private EmployeeService employeeService;

    private JTextField nameField, emailField, rateField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.employeeService = new EmployeeService();

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 244, 248));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 155, 160), 2),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 30, 10);
        formPanel.add(titleLabel, gbc);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = 1;

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(labelFont);
        gbc.gridy = 1; formPanel.add(nameLabel, gbc);
        nameField = new JTextField(20);
        nameField.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(nameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(emailLabel, gbc);
        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(emailField, gbc);

        JLabel pwdLabel = new JLabel("Password:");
        pwdLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(pwdLabel, gbc);
        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(passwordField, gbc);

        JLabel rateLabel = new JLabel("Hourly Rate ($):");
        rateLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(rateLabel, gbc);
        rateField = new JTextField(20);
        rateField.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(rateField, gbc);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(roleLabel, gbc);
        roleComboBox = new JComboBox<>(new String[]{"EMPLOYEE", "HR"});
        roleComboBox.setFont(fieldFont);
        roleComboBox.setBackground(Color.WHITE);
        gbc.gridx = 1; formPanel.add(roleComboBox, gbc);

        JButton registerButton = new JButton("Register");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(new Color(25, 135, 84)); // High-contrast green
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(200, 40));
        registerButton.addActionListener(e -> attemptRegister());
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(registerButton, gbc);

        JButton backButton = new JButton("Already have an account? Login");
        backButton.setFont(labelFont);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setForeground(new Color(0, 80, 160));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> mainFrame.showPanel("Login"));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 10, 10, 10);
        formPanel.add(backButton, gbc);

        add(formPanel);
    }

    private void attemptRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String rateStr = rateField.getText().trim();
        String role = roleComboBox.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double rate;
        try {
            rate = Double.parseDouble(rateStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid hourly rate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("HR".equalsIgnoreCase(role)) {
            String passkey = JOptionPane.showInputDialog(this, "Enter HR Passkey:");
            if (!"111".equals(passkey)) {
                JOptionPane.showMessageDialog(this, "Invalid HR Passkey. Registration cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean success = employeeService.register(name, email, password, rate, role);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showPanel("Login");
            nameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            rateField.setText("");
            roleComboBox.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Email might exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
