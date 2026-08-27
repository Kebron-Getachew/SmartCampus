import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DashboardForm extends JFrame {

    public DashboardForm() {
        setTitle("Smart Campus Lost & Found - Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 246, 248));
        setLayout(new BorderLayout(20, 20));

        add(buildTopBar(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        centerPanel.add(buildStatsRow());
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(buildRecentReportsPanel());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        topBar.setOpaque(false);

        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.X_AXIS));
        leftBox.setOpaque(false);

        ImageIcon logoIcon = new ImageIcon("assets/logo.png");
        Image scaled = logoIcon.getImage().getScaledInstance(36, 24, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel subtitle = new JLabel(getSubtitleText());
        subtitle.setForeground(new Color(120, 120, 120));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titleBox.add(title);
        titleBox.add(subtitle);

        leftBox.add(logoLabel);
        leftBox.add(titleBox);

        JPanel rightBox = new JPanel();
        rightBox.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search reports...");
        searchField.putClientProperty("JTextField.arc", 15);
        searchField.addActionListener(e -> {
            new ItemManagerForm(searchField.getText()).setVisible(true);
            dispose();
        });

        JButton logItemBtn = new JButton("+  Log Item");
        logItemBtn.putClientProperty("JButton.buttonType", "roundRect");
        logItemBtn.setBackground(new Color(37, 99, 235));
        logItemBtn.setForeground(Color.WHITE);
        logItemBtn.setFocusPainted(false);
        logItemBtn.addActionListener(e -> {
            new ItemManagerForm().setVisible(true);
            dispose();
        });

        rightBox.add(searchField);
        rightBox.add(logItemBtn);

        topBar.add(leftBox, BorderLayout.WEST);
        topBar.add(rightBox, BorderLayout.EAST);

        return topBar;
    }

    private String getSubtitleText() {
        int total = 0, today = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM items");
            if (rs1.next()) total = rs1.getInt(1);

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM items WHERE date_reported = CURDATE()");
            if (rs2.next()) today = rs2.getInt(1);
        } catch (SQLException e) {
            return "Unable to load stats";
        }
        return total + " items · " + today + " logged today";
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 15, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        int all = countQuery("SELECT COUNT(*) FROM items");
        int lost = countQuery("SELECT COUNT(*) FROM items WHERE item_type='Lost'");
        int found = countQuery("SELECT COUNT(*) FROM items WHERE item_type='Found'");
        int returned = countQuery("SELECT COUNT(*) FROM items WHERE status='Returned'");
        int pending = countQuery("SELECT COUNT(*) FROM items WHERE status='Pending'");

        row.add(buildStatCard("ALL ITEMS", String.valueOf(all), "tracked records",
                new Color(20, 20, 20), new Color(255, 255, 255)));
        row.add(buildStatCard("LOST", String.valueOf(lost), "unresolved",
                new Color(220, 38, 38), new Color(254, 242, 242)));
        row.add(buildStatCard("FOUND", String.valueOf(found), "awaiting claim",
                new Color(21, 128, 61), new Color(240, 253, 244)));
        row.add(buildStatCard("RETURNED", String.valueOf(returned), "resolved",
                new Color(75, 85, 99), new Color(243, 244, 246)));
        row.add(buildStatCard("PENDING", String.valueOf(pending), "needs action",
                new Color(180, 83, 9), new Color(255, 251, 235)));

        return row;
    }

    private int countQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            return 0;
        }
        return 0;
    }

    private RoundedPanel buildStatCard(String title, String value, String subtitle, Color valueColor, Color bgColor) {
        RoundedPanel card = new RoundedPanel(bgColor, 18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(new Color(130, 130, 130));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setForeground(valueColor);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLabel.setForeground(new Color(150, 150, 150));

        card.add(titleLabel);
        card.add(valueLabel);
        card.add(subLabel);

        return card;
    }

    private RoundedPanel buildRecentReportsPanel() {
        RoundedPanel panel = new RoundedPanel(Color.WHITE, 18);
        panel.setLayout(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel headerTitle = new JLabel("Recent Reports");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));

        JButton viewAllBtn = new JButton("View all →");
        viewAllBtn.putClientProperty("JButton.buttonType", "borderless");
        viewAllBtn.setForeground(new Color(37, 99, 235));
        viewAllBtn.addActionListener(e -> {
            new ItemManagerForm().setVisible(true);
            dispose();
        });

        header.add(headerTitle, BorderLayout.WEST);
        header.add(viewAllBtn, BorderLayout.EAST);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        String sql = "SELECT * FROM items ORDER BY date_reported DESC LIMIT 7";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            boolean any = false;
            while (rs.next()) {
                any = true;
                listPanel.add(buildReportRow(
                        rs.getString("item_name"),
                        rs.getString("location"),
                        rs.getString("item_type"),
                        rs.getString("status"),
                        rs.getDate("date_reported")
                ));
                listPanel.add(Box.createVerticalStrut(10));
            }
            if (!any) {
                JLabel empty = new JLabel("No reports yet. Click \"Log Item\" to add one.");
                empty.setForeground(new Color(150, 150, 150));
                listPanel.add(empty);
            }
        } catch (SQLException e) {
            listPanel.add(new JLabel("Error loading reports: " + e.getMessage()));
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildReportRow(String name, String location, String type, String status, java.sql.Date date) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.X_AXIS));
        leftBox.setOpaque(false);

        RoundedPanel iconBox = new RoundedPanel(new Color(243, 244, 246), 10);
        iconBox.setPreferredSize(new Dimension(36, 36));
        iconBox.setLayout(new GridBagLayout());
        JLabel iconLetter = new JLabel(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
        iconLetter.setFont(new Font("SansSerif", Font.BOLD, 14));
        iconBox.add(iconLetter);

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);
        textBox.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel locationLabel = new JLabel(location);
        locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        locationLabel.setForeground(new Color(140, 140, 140));

        textBox.add(nameLabel);
        textBox.add(locationLabel);

        leftBox.add(iconBox);
        leftBox.add(textBox);

        JPanel rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.X_AXIS));
        rightBox.setOpaque(false);

        JLabel dateLabel = new JLabel(date != null ? date.toString() : "");
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dateLabel.setForeground(new Color(150, 150, 150));
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        PillLabel badge;
        if ("Returned".equals(status)) {
            badge = new PillLabel("Returned", new Color(243, 244, 246), new Color(75, 85, 99));
        } else if ("Lost".equals(type)) {
            badge = new PillLabel("Lost", new Color(254, 242, 242), new Color(220, 38, 38));
        } else {
            badge = new PillLabel("Found", new Color(240, 253, 244), new Color(21, 128, 61));
        }

        rightBox.add(dateLabel);
        rightBox.add(badge);

        row.add(leftBox, BorderLayout.WEST);
        row.add(rightBox, BorderLayout.EAST);

        return row;
    }
}