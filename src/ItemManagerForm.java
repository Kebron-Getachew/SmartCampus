import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ItemManagerForm extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField nameField, descField, categoryField, locationField, reporterField;
    private JComboBox<String> typeBox, statusBox, filterTypeBox, filterStatusBox;
    private JTextField searchField;
    private int selectedId = -1;

    public ItemManagerForm(String prefillSearch) {
    this();
    searchField.setText(prefillSearch);
    loadItems();
}

    public ItemManagerForm() {
        setTitle("Manage Lost & Found Items");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 6, 5, 5));
        nameField = new JTextField();
        descField = new JTextField();
        categoryField = new JTextField();
        locationField = new JTextField();
        reporterField = new JTextField();
        typeBox = new JComboBox<>(new String[]{"Lost", "Found"});
        statusBox = new JComboBox<>(new String[]{"Pending", "Returned"});

        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeBox);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);
        formPanel.add(new JLabel("Reported By:"));
        formPanel.add(reporterField);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        JPanel actionPanel = new JPanel();
        actionPanel.add(addBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(clearBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(actionPanel, BorderLayout.SOUTH);

        searchField = new JTextField(15);
        filterTypeBox = new JComboBox<>(new String[]{"All", "Lost", "Found"});
        filterStatusBox = new JComboBox<>(new String[]{"All", "Pending", "Returned"});
        JButton searchBtn = new JButton("Search");
        JButton backBtn = new JButton("Back to Dashboard");

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Type:"));
        searchPanel.add(filterTypeBox);
        searchPanel.add(new JLabel("Status:"));
        searchPanel.add(filterStatusBox);
        searchPanel.add(searchBtn);
        searchPanel.add(backBtn);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Description", "Category", "Location", "Type", "Status", "Reported By"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());
        clearBtn.addActionListener(e -> clearForm());
        searchBtn.addActionListener(e -> loadItems());
        backBtn.addActionListener(e -> {
            new DashboardForm().setVisible(true);
            dispose();
        });

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        loadItems();
    }

    private void addItem() {
        String sql = "INSERT INTO items (item_name, description, category, location, item_type, status, date_reported, reported_by) VALUES (?, ?, ?, ?, ?, ?, CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, descField.getText());
            ps.setString(3, categoryField.getText());
            ps.setString(4, locationField.getText());
            ps.setString(5, (String) typeBox.getSelectedItem());
            ps.setString(6, (String) statusBox.getSelectedItem());
            ps.setString(7, reporterField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Item added.");
            clearForm();
            loadItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateItem() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        String sql = "UPDATE items SET item_name=?, description=?, category=?, location=?, item_type=?, status=?, reported_by=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, descField.getText());
            ps.setString(3, categoryField.getText());
            ps.setString(4, locationField.getText());
            ps.setString(5, (String) typeBox.getSelectedItem());
            ps.setString(6, (String) statusBox.getSelectedItem());
            ps.setString(7, reporterField.getText());
            ps.setInt(8, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Item updated.");
            clearForm();
            loadItems();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteItem() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM items WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Item deleted.");
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
        descField.setText((String) tableModel.getValueAt(row, 2));
        categoryField.setText((String) tableModel.getValueAt(row, 3));
        locationField.setText((String) tableModel.getValueAt(row, 4));
        typeBox.setSelectedItem(tableModel.getValueAt(row, 5));
        statusBox.setSelectedItem(tableModel.getValueAt(row, 6));
        reporterField.setText((String) tableModel.getValueAt(row, 7));
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        descField.setText("");
        categoryField.setText("");
        locationField.setText("");
        reporterField.setText("");
        typeBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        table.clearSelection();
    }
}