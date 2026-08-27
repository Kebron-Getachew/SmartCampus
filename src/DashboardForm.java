import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DashboardForm extends JFrame {
    private JLabel lostLabel, foundLabel, returnedLabel, pendingLabel;

    public DashboardForm() {
        setTitle("Dashboard - Smart Campus Lost & Found");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        lostLabel = new JLabel();
        foundLabel = new JLabel();
        returnedLabel = new JLabel();
        pendingLabel = new JLabel();

        Font f = new Font("SansSerif", Font.BOLD, 16);
        lostLabel.setFont(f);
        foundLabel.setFont(f);
        returnedLabel.setFont(f);
        pendingLabel.setFont(f);

        statsPanel.add(lostLabel);
        statsPanel.add(foundLabel);
        statsPanel.add(returnedLabel);
        statsPanel.add(pendingLabel);

        JButton manageBtn = new JButton("Manage Items");
        JButton logoutBtn = new JButton("Logout");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(manageBtn);
        buttonPanel.add(logoutBtn);

        add(statsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        manageBtn.addActionListener(e -> {
            new ItemManagerForm().setVisible(true);
            dispose();
        });

        logoutBtn.addActionListener(e -> {
            new LoginForm().setVisible(true);
            dispose();
        });

        loadStats();
    }

    private void loadStats() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM items WHERE item_type='Lost'");
            rs1.next();
            lostLabel.setText("Total Lost Items: " + rs1.getInt(1));

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM items WHERE item_type='Found'");
            rs2.next();
            foundLabel.setText("Total Found Items: " + rs2.getInt(1));

            ResultSet rs3 = st.executeQuery("SELECT COUNT(*) FROM items WHERE status='Returned'");
            rs3.next();
            returnedLabel.setText("Returned Items: " + rs3.getInt(1));

            ResultSet rs4 = st.executeQuery("SELECT COUNT(*) FROM items WHERE status='Pending'");
            rs4.next();
            pendingLabel.setText("Pending Items: " + rs4.getInt(1));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading stats: " + e.getMessage());
        }
    }
}