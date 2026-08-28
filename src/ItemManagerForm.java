import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ItemManagerForm extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField, locationField, reporterField;
    private JTextArea descArea;
    private JComboBox<String> categoryBox, statusBox, filterTypeBox, filterStatusBox;
    private JTextField searchField;
    private JButton foundToggle, lostToggle, submitBtn, deleteBtn;
    private String selectedType = "Found";
    private int selectedId = -1;

    public ItemManagerForm() {
        setTitle("Manage Lost & Found Items");
        setIconImage(new ImageIcon("assets/logo.png").getImage());
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(245, 246, 248));
        setLayout(new BorderLayout(15, 15));

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadItems();
    }

    public ItemManagerForm(String prefillSearch) {
        this();
        searchField.setText(prefillSearch);
        loadItems();
    }

    // ---------- FORM CARD (top) ----------

    private RoundedPanel buildFormCard() {
        RoundedPanel card = new RoundedPanel(Color.WHITE, 18);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Log New Item");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel subtitle = new JLabel("Submit a lost or found report");
        subtitle.setForeground(new Color(140, 140, 140));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        titleBox.add(title);
        titleBox.add(subtitle);

        JButton closeBtn = new JButton("✕");
        closeBtn.putClientProperty("JButton.buttonType", "borderless");
        closeBtn.setForeground(new Color(150, 150, 150));
        closeBtn.addActionListener(e -> {
            new DashboardForm().setVisible(true);
            dispose();
        });

        headerRow.add(titleBox, BorderLayout.WEST);
        headerRow.add(closeBtn, BorderLayout.EAST);

        JLabel typeLabel = sectionLabel("REPORT TYPE");

        foundToggle = new JButton("●  Found Item");
        lostToggle = new JButton("●  Lost Item");
        foundToggle.putClientProperty("JButton.buttonType", "roundRect");
        lostToggle.putClientProperty("JButton.buttonType", "roundRect");
        foundToggle.setFocusPainted(false);
        lostToggle.setFocusPainted(false);
        foundToggle.addActionListener(e -> { selectedType = "Found"; refreshToggleColors(); });
        lostToggle.addActionListener(e -> { selectedType = "Lost"; refreshToggleColors(); });

        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        togglePanel.setOpaque(false);
        togglePanel.add(foundToggle);
        togglePanel.add(lostToggle);
        togglePanel.setMaximumSize(new Dimension(600, 40));
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameField = styledField();
        locationField = styledField();
        reporterField = styledField();

        categoryBox = new JComboBox<>(new String[]{"Electronics", "Bags", "Keys", "Documents", "Clothing", "Jewelry", "Other"});
        categoryBox.setMaximumSize(new Dimension(600, 38));
        categoryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusBox = new JComboBox<>(new String[]{"Pending", "Returned"});
        statusBox.setMaximumSize(new Dimension(600, 38));
        statusBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(600, 70));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 15, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(600, 60));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(labeledField("ITEM NAME", nameField));
        row1.add(labeledField("LOCATION", locationField));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 15, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(600, 60));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(labeledField("REPORTED BY", reporterField));
        row2.add(labeledBox("CATEGORY", categoryBox));

        JPanel statusRow = labeledBox("STATUS", statusBox);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel descBox = new JPanel();
        descBox.setLayout(new BoxLayout(descBox, BoxLayout.Y_AXIS));
        descBox.setOpaque(false);
        descBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        descBox.add(sectionLabel("DESCRIPTION"));
        descBox.add(descScroll);

        submitBtn = new JButton("Submit Report");
        submitBtn.putClientProperty("JButton.buttonType", "roundRect");
        submitBtn.setBackground(new Color(37, 99, 235));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setMaximumSize(new Dimension(600, 42));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> submitForm());

        deleteBtn = new JButton("Delete this report");
        deleteBtn.putClientProperty("JButton.buttonType", "roundRect");
        deleteBtn.setBackground(new Color(254, 242, 242));
        deleteBtn.setForeground(new Color(220, 38, 38));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setMaximumSize(new Dimension(600, 38));
        deleteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteBtn.addActionListener(e -> deleteItem());

        card.add(headerRow);
        card.add(Box.createVerticalStrut(15));
        card.add(typeLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(togglePanel);
        card.add(Box.createVerticalStrut(15));
        card.add(row1);
        card.add(Box.createVerticalStrut(10));
        card.add(row2);
        card.add(Box.createVerticalStrut(10));
        card.add(statusRow);
        card.add(Box.createVerticalStrut(10));
        card.add(descBox);
        card.add(Box.createVerticalStrut(15));
        card.add(submitBtn);
        card.add(Box.createVerticalStrut(5));
        card.add(deleteBtn);

        refreshToggleColors();
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(130, 130, 130));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styledField() {
        JTextField field = new JTextField();
        field.putClientProperty("JTextField.arc", 12);
        field.setMaximumSize(new Dimension(300, 36));
        return field;
    }

    private JPanel labeledField(String label, JTextField field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.add(sectionLabel(label));
        box.add(field);
        return box;
    }

    private JPanel labeledBox(String label, JComboBox<String> box) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.add(sectionLabel(label));
        wrapper.add(box);
        return wrapper;
    }

    private void refreshToggleColors() {
        if (selectedType.equals("Found")) {
            foundToggle.setBackground(new Color(240, 253, 244));
            foundToggle.setForeground(new Color(21, 128, 61));
            lostToggle.setBackground(Color.WHITE);
            lostToggle.setForeground(new Color(120, 120, 120));
        } else {
            lostToggle.setBackground(new Color(254, 242, 242));
            lostToggle.setForeground(new Color(220, 38, 38));
            foundToggle.setBackground(Color.WHITE);
            foundToggle.setForeground(new Color(120, 120, 120));
        }
    }

    

    private RoundedPanel buildTablePanel() {
        RoundedPanel panel = new RoundedPanel(Color.WHITE, 18);
        panel.setLayout(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel filterRow = new JPanel();
        filterRow.setOpaque(false);

        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name...");
        filterTypeBox = new JComboBox<>(new String[]{"All", "Lost", "Found"});
        filterStatusBox = new JComboBox<>(new String[]{"All", "Pending", "Returned"});

        JButton searchBtn = new JButton("Search");
        searchBtn.putClientProperty("JButton.buttonType", "roundRect");
        searchBtn.setBackground(new Color(37, 99, 235));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.putClientProperty("JButton.buttonType", "roundRect");
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(37, 99, 235));
        backBtn.setFocusPainted(false);

        searchBtn.addActionListener(e -> loadItems());
        backBtn.addActionListener(e -> {
            new DashboardForm().setVisible(true);
            dispose();
        });

        filterRow.add(new JLabel("Search:"));
        filterRow.add(searchField);
        filterRow.add(new JLabel("Type:"));
        filterRow.add(filterTypeBox);
        filterRow.add(new JLabel("Status:"));
        filterRow.add(filterStatusBox);
        filterRow.add(searchBtn);
        filterRow.add(backBtn);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Description", "Category", "Location", "Type", "Status", "Reported By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ---------- DATA LOGIC ----------

    private void submitForm() {
        if (nameField.getText().trim().isEmpty() || locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item name and location are required.");
            return;
        }
        if (selectedId == -1) addItem(); else updateItem();
    }

    private void addItem() {
        String sql = "INSERT INTO items (item_name, description, category, location, item_type, status, date_reported, reported_by) VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, descArea.getText());
            ps.setString(3, (String) categoryBox.getSelectedItem());
            ps.setString(4, locationField.getText());
            ps.setString(5, selectedType);
            ps.setString(6, (String) statusBox.getSelectedItem());
            ps.setString(7, reporterField.getText());
            ps.executeUpdate();
            clearForm();
            loadItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateItem() {
        String sql = "UPDATE items SET item_name=?, description=?, category=?, location=?, item_type=?, status=?, reported_by=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, descArea.getText());
            ps.setString(3, (String) categoryBox.getSelectedItem());
            ps.setString(4, locationField.getText());
            ps.setString(5, selectedType);
            ps.setString(6, (String) statusBox.getSelectedItem());
            ps.setString(7, reporterField.getText());
            ps.setInt(8, selectedId);
            ps.executeUpdate();
            clearForm();
            loadItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteItem() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a report first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM items WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            clearForm();
            loadItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadItems() {
        tableModel.setRowCount(0);
        StringBuilder sql = new StringBuilder("SELECT * FROM items WHERE item_name LIKE ?");

        String type = (String) filterTypeBox.getSelectedItem();
        String status = (String) filterStatusBox.getSelectedItem();

        if (!"All".equals(type)) sql.append(" AND item_type='").append(type).append("'");
        if (!"All".equals(status)) sql.append(" AND status='").append(status).append("'");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, "%" + searchField.getText() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getString("item_type"),
                        rs.getString("status"),
                        rs.getString("reported_by")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedId = (int) tableModel.getValueAt(row, 0);
        nameField.setText((String) tableModel.getValueAt(row, 1));
        descArea.setText((String) tableModel.getValueAt(row, 2));
        categoryBox.setSelectedItem(tableModel.getValueAt(row, 3));
        locationField.setText((String) tableModel.getValueAt(row, 4));
        selectedType = (String) tableModel.getValueAt(row, 5);
        refreshToggleColors();
        statusBox.setSelectedItem(tableModel.getValueAt(row, 6));
        reporterField.setText((String) tableModel.getValueAt(row, 7));
        submitBtn.setText("Update Report");
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        descArea.setText("");
        categoryBox.setSelectedIndex(0);
        locationField.setText("");
        reporterField.setText("");
        statusBox.setSelectedIndex(0);
        selectedType = "Found";
        refreshToggleColors();
        table.clearSelection();
        submitBtn.setText("Submit Report");
    }
}