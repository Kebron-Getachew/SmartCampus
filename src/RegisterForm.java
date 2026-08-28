import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmField;

    public RegisterForm() {
        setTitle("Smart Campus Lost & Found - Register");
        setIconImage(new ImageIcon("assets/logo.png").getImage());
        setSize(400, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 246, 248));
        setLayout(new GridBagLayout());

        RoundedPanel card = new RoundedPanel(Color.WHITE, 25);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        ImageIcon logoIcon = new ImageIcon("assets/logo.png");
        Image scaled = logoIcon.getImage().getScaledInstance(90, 60, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JLabel subLabel = new JLabel("Join Smart Campus Lost & Found");
        subLabel.setForeground(new Color(120, 120, 120));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        usernameField = new JTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Username");
        usernameField.putClientProperty("JTextField.arc", 15);
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Password");
        passwordField.putClientProperty("JTextField.arc", 15);
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmField = new JPasswordField();
        confirmField.putClientProperty("JTextField.placeholderText", "Confirm Password");
        confirmField.putClientProperty("JTextField.arc", 15);
        confirmField.setMaximumSize(new Dimension(300, 40));
        confirmField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerBtn = new JButton("Create Account");
        registerBtn.putClientProperty("JButton.buttonType", "roundRect");
        registerBtn.setBackground(new Color(37, 99, 235));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setMaximumSize(new Dimension(300, 40));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backBtn = new JButton("Back to Login");
        backBtn.putClientProperty("JButton.buttonType", "borderless");
        backBtn.setForeground(new Color(37, 99, 235));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logoLabel);
        card.add(titleLabel);
        card.add(subLabel);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(20));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backBtn);

        add(card);

        registerBtn.addActionListener(e -> register());
        backBtn.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            new LoginForm().setVisible(true);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage() + "\n(Username might already exist)");
        }
    }
}