import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EntryForm extends JFrame {
    private JTextField txtInvoiceNo, txtInvoiceDate, txtDueDate, txtDiscount, txtTaxRate;
    private JComboBox<String> comboCustomer;
    private JTable tableItems;
    private DefaultTableModel tableModel;
    private JLabel lblSubtotal, lblTotal;
    private List<Integer> customerIds = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public EntryForm() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* ignored */ }

        setTitle("Invoice Entry Form");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title
        JLabel lblTitle = new JLabel("Create New Invoice", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout(10, 10));

        // Header Panel: Invoice Details
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Invoice Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("Invoice No:"), gbc);
        gbc.gridx = 1;
        txtInvoiceNo = new JTextField(15);
        headerPanel.add(txtInvoiceNo, gbc);

        gbc.gridx = 2;
        headerPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 3;
        comboCustomer = new JComboBox<>();
        headerPanel.add(comboCustomer, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("Invoice Date:"), gbc);
        gbc.gridx = 1;
        txtInvoiceDate = new JTextField("2026-05-01");
        headerPanel.add(txtInvoiceDate, gbc);

        gbc.gridx = 2;
        headerPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 3;
        txtDueDate = new JTextField("2026-06-01");
        headerPanel.add(txtDueDate, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        headerPanel.add(new JLabel("Discount (%):"), gbc);
        gbc.gridx = 1;
        txtDiscount = new JTextField("0");
        headerPanel.add(txtDiscount, gbc);

        gbc.gridx = 2;
        headerPanel.add(new JLabel("Tax Rate (%):"), gbc);
        gbc.gridx = 3;
        txtTaxRate = new JTextField("5.0");
        headerPanel.add(txtTaxRate, gbc);

        centerWrapper.add(headerPanel, BorderLayout.NORTH);

        // Center Panel: Line Items Table
        String[] columnNames = {"Product", "Description", "Qty/Hr", "Unit Price", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 4; // Total column is not editable
            }
        };
        tableItems = new JTable(tableModel);
        tableItems.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(tableItems);
        
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Line Items"));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddRow = new JButton("Add Item");
        btnAddRow.addActionListener(e -> tableModel.addRow(new Object[]{"", "", "1", "0.00", "0.00"}));
        JButton btnRemoveRow = new JButton("Remove Selected");
        btnRemoveRow.addActionListener(e -> {
            int row = tableItems.getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
        });
        tableActions.add(btnAddRow);
        tableActions.add(btnRemoveRow);
        tablePanel.add(tableActions, BorderLayout.SOUTH);

        centerWrapper.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Bottom Panel: Totals & Actions
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        totalsPanel.add(new JLabel("Subtotal:"));
        lblSubtotal = new JLabel("0.00");
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalsPanel.add(lblSubtotal);
        
        totalsPanel.add(new JLabel("Estimated Total:"));
        lblTotal = new JLabel("0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(0, 102, 204));
        totalsPanel.add(lblTotal);
        
        bottomPanel.add(totalsPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Invoice");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveInvoice());
        
        JButton btnInvoiceView = new JButton("Open Report Generator");
        btnInvoiceView.addActionListener(e -> {
            new Invoice().setVisible(true);
            dispose();
        });
        
        actionPanel.add(btnInvoiceView);
        actionPanel.add(btnSave);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Load data from DB and setup interactive table
        loadFormData();
        setupTableInteractivity();
    }

    private void setupTableInteractivity() {
        // Setup Combo Box for Product Column
        JComboBox<String> comboProduct = new JComboBox<>();
        for (Product p : productList) {
            comboProduct.addItem(p.name);
        }
        TableColumn productColumn = tableItems.getColumnModel().getColumn(0);
        productColumn.setCellEditor(new DefaultCellEditor(comboProduct));

        // Listen for changes in the table to auto-calculate
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // If Product changed, auto-fill price
                if (col == 0) {
                    String prodName = (String) tableModel.getValueAt(row, 0);
                    for (Product p : productList) {
                        if (p.name.equals(prodName)) {
                            tableModel.removeTableModelListener(tableModel.getTableModelListeners()[0]); // temporarily disable
                            tableModel.setValueAt(df.format(p.price), row, 3);
                            tableModel.addTableModelListener(tableModel.getTableModelListeners()[0]);
                            break;
                        }
                    }
                }
                
                // Recalculate row total
                if (col == 0 || col == 2 || col == 3) {
                    try {
                        double qty = Double.parseDouble(tableModel.getValueAt(row, 2).toString());
                        double price = Double.parseDouble(tableModel.getValueAt(row, 3).toString().replace(",", ""));
                        double total = qty * price;
                        
                        tableModel.removeTableModelListener(tableModel.getTableModelListeners()[0]);
                        tableModel.setValueAt(df.format(total), row, 4);
                        tableModel.addTableModelListener(tableModel.getTableModelListeners()[0]);
                        
                        updateGrandTotals();
                    } catch (Exception ex) {
                        // ignore parse errors
                    }
                }
            }
        });
    }

    private void updateGrandTotals() {
        double subtotal = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                double total = Double.parseDouble(tableModel.getValueAt(i, 4).toString().replace(",", ""));
                subtotal += total;
            } catch (Exception ex) { }
        }
        lblSubtotal.setText(df.format(subtotal));

        try {
            double disc = Double.parseDouble(txtDiscount.getText());
            double tax = Double.parseDouble(txtTaxRate.getText());
            double taxable = subtotal - (subtotal * (disc / 100));
            double grandTotal = taxable + (taxable * (tax / 100));
            lblTotal.setText(df.format(grandTotal));
        } catch (Exception ex) { }
    }

    private void loadFormData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT customerID, customerName FROM CUSTOMER");
            while (rs.next()) {
                customerIds.add(rs.getInt("customerID"));
                comboCustomer.addItem(rs.getString("customerName"));
            }

            rs = stmt.executeQuery("SELECT productID, productName, defaultPrice FROM PRODUCT");
            while (rs.next()) {
                productList.add(new Product(rs.getInt("productID"), rs.getString("productName"), rs.getDouble("defaultPrice")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void saveInvoice() {
        String invoiceNo = txtInvoiceNo.getText().trim();
        int customerIndex = comboCustomer.getSelectedIndex();
        if (invoiceNo.isEmpty() || customerIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please fill in Invoice No and select a Customer.");
            return;
        }

        int customerID = customerIds.get(customerIndex);
        String date = txtInvoiceDate.getText();
        String dueDate = txtDueDate.getText();
        double discount, taxRate;
        try {
            discount = Double.parseDouble(txtDiscount.getText());
            taxRate = Double.parseDouble(txtTaxRate.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid discount or tax rate.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            String sqlInvoice = "INSERT INTO INVOICE (invoiceNo, companyID, customerID, invoiceDate, dueDate, discount, taxRate) VALUES (?, 1, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInvoice)) {
                pstmt.setString(1, invoiceNo);
                pstmt.setInt(2, customerID);
                pstmt.setDate(3, Date.valueOf(date));
                pstmt.setDate(4, Date.valueOf(dueDate));
                pstmt.setDouble(5, discount);
                pstmt.setDouble(6, taxRate);
                pstmt.executeUpdate();
            }

            String sqlItem = "INSERT INTO LINE_ITEM (invoiceNo, productID, description, qtyHr, unitPrice) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlItem)) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String prodName = tableModel.getValueAt(i, 0).toString();
                    if (prodName.isEmpty()) continue;
                    
                    String desc = tableModel.getValueAt(i, 1).toString();
                    double qty = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
                    double price = Double.parseDouble(tableModel.getValueAt(i, 3).toString().replace(",", ""));

                    int prodID = 1;
                    for (Product p : productList) {
                        if (p.name.equals(prodName)) {
                            prodID = p.id;
                            break;
                        }
                    }

                    pstmt.setString(1, invoiceNo);
                    pstmt.setInt(2, prodID);
                    pstmt.setString(3, desc);
                    pstmt.setDouble(4, qty);
                    pstmt.setDouble(5, price);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Invoice saved successfully!");
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving invoice: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtInvoiceNo.setText("");
        txtInvoiceDate.setText("2026-05-01");
        txtDueDate.setText("2026-06-01");
        txtDiscount.setText("0");
        txtTaxRate.setText("5.0");
        tableModel.setRowCount(0);
        updateGrandTotals();
    }

    static class Product {
        int id;
        String name;
        double price;
        Product(int id, String name, double price) {
            this.id = id; this.name = name; this.price = price;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EntryForm().setVisible(true));
    }
}
