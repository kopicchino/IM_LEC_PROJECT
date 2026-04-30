import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class Invoice extends JFrame {
    private JComboBox<String> comboSearch;
    private JTextArea areaDetails;
    private JTable tableReport;
    private DefaultTableModel modelReport;
    private JLabel lblSubtotal, lblTax, lblDiscount, lblTotal;
    private DecimalFormat df = new DecimalFormat("$#,##0.00");

    public Invoice() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* ignored */ }

        setTitle("Invoice Report Generator");
        setSize(800, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Title
        JLabel lblTitle = new JLabel("Invoice Report", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center Container
        JPanel centerContainer = new JPanel(new BorderLayout(15, 15));

        // Top: Search Box
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        topPanel.add(new JLabel("Select Invoice No:"));
        
        comboSearch = new JComboBox<>();
        comboSearch.setPreferredSize(new Dimension(150, 25));
        topPanel.add(comboSearch);
        
        JButton btnLoad = new JButton("Generate Report");
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoad.setBackground(new Color(52, 152, 219));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.addActionListener(e -> {
            if (comboSearch.getSelectedItem() != null) {
                loadInvoice(comboSearch.getSelectedItem().toString());
            }
        });
        topPanel.add(btnLoad);
        
        centerContainer.add(topPanel, BorderLayout.NORTH);

        // Middle: Details and Table
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Report Output"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        areaDetails = new JTextArea(6, 30);
        areaDetails.setEditable(false);
        areaDetails.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaDetails.setBackground(new Color(245, 245, 245));
        areaDetails.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        reportPanel.add(areaDetails, BorderLayout.NORTH);

        String[] cols = {"Product", "Description", "Qty", "Unit Price", "Total"};
        modelReport = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableReport = new JTable(modelReport);
        tableReport.setRowHeight(25);
        tableReport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableReport.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Right align number columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableReport.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tableReport.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableReport.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        reportPanel.add(new JScrollPane(tableReport), BorderLayout.CENTER);

        // Bottom of Report: Totals
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 200, 10, 10));
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);

        summaryPanel.add(createAlignedLabel("Subtotal:", labelFont, SwingConstants.RIGHT));
        lblSubtotal = createAlignedLabel("$0.00", valueFont, SwingConstants.RIGHT);
        summaryPanel.add(lblSubtotal);

        summaryPanel.add(createAlignedLabel("Discount:", labelFont, SwingConstants.RIGHT));
        lblDiscount = createAlignedLabel("$0.00", valueFont, SwingConstants.RIGHT);
        lblDiscount.setForeground(new Color(192, 57, 43));
        summaryPanel.add(lblDiscount);

        summaryPanel.add(createAlignedLabel("Tax:", labelFont, SwingConstants.RIGHT));
        lblTax = createAlignedLabel("$0.00", valueFont, SwingConstants.RIGHT);
        summaryPanel.add(lblTax);

        summaryPanel.add(createAlignedLabel("Balance Due:", new Font("Segoe UI", Font.BOLD, 16), SwingConstants.RIGHT));
        lblTotal = createAlignedLabel("$0.00", new Font("Segoe UI", Font.BOLD, 18), SwingConstants.RIGHT);
        lblTotal.setForeground(new Color(39, 174, 96));
        summaryPanel.add(lblTotal);

        reportPanel.add(summaryPanel, BorderLayout.SOUTH);
        centerContainer.add(reportPanel, BorderLayout.CENTER);
        
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("Print Report");
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Sending to printer...", "Print", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton btnBack = new JButton("Back to Entry Form");
        btnBack.addActionListener(e -> {
            new EntryForm().setVisible(true);
            dispose();
        });
        
        footerPanel.add(btnPrint);
        footerPanel.add(btnBack);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        
        // Load Invoices into ComboBox
        loadInvoiceNumbers();
    }
    
    private JLabel createAlignedLabel(String text, Font font, int alignment) {
        JLabel lbl = new JLabel(text, alignment);
        lbl.setFont(font);
        return lbl;
    }

    private void loadInvoiceNumbers() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT invoiceNo FROM INVOICE ORDER BY invoiceDate DESC");
            while (rs.next()) {
                comboSearch.addItem(rs.getString("invoiceNo"));
            }
        } catch (SQLException e) {
            System.err.println("Could not load invoices: " + e.getMessage());
        }
    }

    private void loadInvoice(String invoiceNo) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Fetch Company, Customer and Invoice details
            String sqlHead = "SELECT i.*, c.companyName, c.address as cAddr, cust.customerName, cust.address as custAddr " +
                             "FROM INVOICE i " +
                             "JOIN COMPANY c ON i.companyID = c.companyID " +
                             "JOIN CUSTOMER cust ON i.customerID = cust.customerID " +
                             "WHERE i.invoiceNo = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sqlHead);
            pstmt.setString(1, invoiceNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-40s %s\n", "FROM: " + rs.getString("companyName"), "TO: " + rs.getString("customerName")));
                sb.append(String.format("%-40s %s\n\n", rs.getString("cAddr"), rs.getString("custAddr")));
                sb.append(String.format("Invoice Date: %-26s Due Date: %s\n", rs.getDate("invoiceDate"), rs.getDate("dueDate")));
                areaDetails.setText(sb.toString());

                double discPct = rs.getDouble("discount");
                double taxPct = rs.getDouble("taxRate");

                // 2. Fetch Line Items
                String sqlItems = "SELECT li.*, p.productName " +
                                  "FROM LINE_ITEM li " +
                                  "JOIN PRODUCT p ON li.productID = p.productID " +
                                  "WHERE li.invoiceNo = ?";
                pstmt = conn.prepareStatement(sqlItems);
                pstmt.setString(1, invoiceNo);
                ResultSet rsItems = pstmt.executeQuery();

                modelReport.setRowCount(0);
                double subtotal = 0;
                while (rsItems.next()) {
                    double qty = rsItems.getDouble("qtyHr");
                    double price = rsItems.getDouble("unitPrice");
                    double total = qty * price;
                    subtotal += total;
                    modelReport.addRow(new Object[]{
                        rsItems.getString("productName"),
                        rsItems.getString("description"),
                        qty,
                        df.format(price),
                        df.format(total)
                    });
                }

                // 3. Calculate Totals
                double discountAmt = subtotal * (discPct / 100);
                double taxableAmt = subtotal - discountAmt;
                double taxAmt = taxableAmt * (taxPct / 100);
                double balanceDue = taxableAmt + taxAmt;

                lblSubtotal.setText(df.format(subtotal));
                lblDiscount.setText("-" + df.format(discountAmt) + " (" + discPct + "%)");
                lblTax.setText(df.format(taxAmt) + " (" + taxPct + "%)");
                lblTotal.setText(df.format(balanceDue));

            } else {
                JOptionPane.showMessageDialog(this, "Invoice details could not be loaded.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Invoice().setVisible(true));
    }
}
