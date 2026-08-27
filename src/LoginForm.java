import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Smart Campus Lost & Found - Login");
        setSize(400, 480);
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

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JLabel subLabel = new JLabel("Log in to Smart Campus Lost & Found");
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

        JButton loginBtn = new JButton("Log In");
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.setBackground(new Color(37, 99, 235));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setMaximumSize(new Dimension(300, 40));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerBtn = new JButton("Create an account");
        registerBtn.putClientProperty("JButton.buttonType", "borderless");
        registerBtn.setForeground(new Color(37, 99, 235));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logoLabel);
        card.add(titleLabel);
        card.add(subLabel);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(registerBtn);

        add(card);

        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> {
            new RegisterForm().setVisible(true);
            dispose();
        });
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && org.mindrot.jbcrypt.BCrypt.checkpw(password, rs.getString("password"))) {
                new DashboardForm().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}