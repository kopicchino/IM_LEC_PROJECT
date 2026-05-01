import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class Invoice extends JFrame {
    private JComboBox<String> comboSearch;
    private JTable tableReport;
    private DefaultTableModel modelReport;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    // Header labels
    private JLabel lblCompanyName, lblCompanyAddress, lblCompanyWebsite, lblCompanyPhone;
    // Bill To labels
    private JLabel lblCustomerName, lblCustomerAddress, lblCustomerEmail, lblCustomerPhone;
    // Invoice meta
    private JLabel lblInvoiceNo, lblInvoiceDate, lblDueDate;
    // Totals
    private JLabel lblSubtotal, lblDiscount, lblSubtotalLessDiscount, lblTaxRate, lblTotalTax, lblBalanceDue;
    // Terms
    private JTextArea areaTerms;
    // Thank you
    private JLabel lblThankYou;

    public Invoice() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* ignored */ }

        setTitle("Invoice Report Generator");
        setSize(860, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(Color.WHITE);

        // ── Outer panel (white background, padded) ──────────────────────────
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // ── Search bar (top utility strip) ──────────────────────────────────
        JPanel searchStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchStrip.setBackground(new Color(240, 240, 240));
        searchStrip.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        searchStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        searchStrip.add(new JLabel("Invoice No:"));
        comboSearch = new JComboBox<>();
        comboSearch.setPreferredSize(new Dimension(140, 26));
        searchStrip.add(comboSearch);
        JButton btnGenerate = new JButton("Generate Report");
        styleButton(btnGenerate, new Color(41, 128, 185));
        btnGenerate.addActionListener(e -> {
            if (comboSearch.getSelectedItem() != null)
                loadInvoice(comboSearch.getSelectedItem().toString());
        });
        searchStrip.add(btnGenerate);

        JButton btnPrint = new JButton("Print");
        styleButton(btnPrint, new Color(100, 100, 100));
        btnPrint.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Sending to printer...", "Print", JOptionPane.INFORMATION_MESSAGE));
        searchStrip.add(btnPrint);

        JButton btnBack = new JButton("Back to Entry Form");
        styleButton(btnBack, new Color(150, 60, 40));
        btnBack.addActionListener(e -> { new EntryForm().setVisible(true); dispose(); });
        searchStrip.add(btnBack);

        root.add(searchStrip);
        root.add(Box.createVerticalStrut(12));

        // ── Invoice content panel ────────────────────────────────────────────
        JPanel invoicePanel = new JPanel(new BorderLayout());
        invoicePanel.setBackground(Color.WHITE);
        invoicePanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // ── Section 1: Company header + "INVOICE" title ──────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);

        // Left: company details
        JPanel companyBlock = new JPanel();
        companyBlock.setLayout(new BoxLayout(companyBlock, BoxLayout.Y_AXIS));
        companyBlock.setBackground(Color.WHITE);

        lblCompanyName = boldLabel("Company Name", 18);
        lblCompanyAddress = plainLabel("Company Address");
        lblCompanyWebsite = plainLabel("www.website.com");
        lblCompanyPhone = plainLabel("000-000-0000");

        companyBlock.add(lblCompanyName);
        companyBlock.add(Box.createVerticalStrut(2));
        companyBlock.add(lblCompanyAddress);
        companyBlock.add(lblCompanyWebsite);
        companyBlock.add(lblCompanyPhone);

        // Right: "INVOICE" title + logo placeholder
        JPanel invoiceTitleBlock = new JPanel(new BorderLayout(10, 0));
        invoiceTitleBlock.setBackground(Color.WHITE);

        JLabel lblInvoiceTitle = new JLabel("INVOICE", SwingConstants.RIGHT);
        lblInvoiceTitle.setFont(new Font("Georgia", Font.BOLD, 26));
        lblInvoiceTitle.setForeground(new Color(30, 30, 30));

        // Simple logo placeholder (grey shield shape drawn via component)
        JLabel logoPlaceholder = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Draw shield
                int[] xs = { w/2, w-6, w-6, w/2, 6, 6 };
                int[] ys = { 4, 14, h-20, h-4, h-20, 14 };
                g2.setColor(new Color(60, 60, 70));
                g2.fillPolygon(xs, ys, 6);
                g2.setColor(new Color(200, 160, 40));
                g2.setFont(new Font("Serif", Font.BOLD, 9));
                g2.setColor(Color.WHITE);
                g2.drawString("★★★★★", w/2 - 22, 28);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                g2.drawString("✦", w/2 - 7, h - 14);
            }
        };
        logoPlaceholder.setPreferredSize(new Dimension(70, 70));

        invoiceTitleBlock.add(lblInvoiceTitle, BorderLayout.NORTH);
        invoiceTitleBlock.add(logoPlaceholder, BorderLayout.EAST);

        headerRow.add(companyBlock, BorderLayout.WEST);
        headerRow.add(invoiceTitleBlock, BorderLayout.EAST);

        body.add(headerRow);
        body.add(Box.createVerticalStrut(20));

        // ── Divider ──────────────────────────────────────────────────────────
        body.add(hRule());
        body.add(Box.createVerticalStrut(14));

        // ── Section 2: Bill To (left) + Invoice Meta (right) ────────────────
        JPanel billingRow = new JPanel(new BorderLayout());
        billingRow.setBackground(Color.WHITE);

        // Left: Bill To
        JPanel billToBlock = new JPanel();
        billToBlock.setLayout(new BoxLayout(billToBlock, BoxLayout.Y_AXIS));
        billToBlock.setBackground(Color.WHITE);

        JLabel billToHeader = new JLabel("BILL TO");
        billToHeader.setFont(new Font("SansSerif", Font.BOLD, 10));
        billToHeader.setForeground(new Color(80, 80, 80));
        billToBlock.add(billToHeader);
        billToBlock.add(Box.createVerticalStrut(4));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setForeground(new Color(180, 180, 180));
        billToBlock.add(sep);
        billToBlock.add(Box.createVerticalStrut(6));

        lblCustomerName    = boldLabel("—", 13);
        lblCustomerAddress = plainLabel("—");
        lblCustomerEmail   = plainLabel("—");
        lblCustomerPhone   = plainLabel("—");

        billToBlock.add(lblCustomerName);
        billToBlock.add(lblCustomerAddress);
        billToBlock.add(lblCustomerEmail);
        billToBlock.add(lblCustomerPhone);

        // Right: Invoice meta table
        JPanel metaBlock = new JPanel(new GridLayout(3, 2, 6, 4));
        metaBlock.setBackground(Color.WHITE);

        Font metaKeyFont = new Font("SansSerif", Font.BOLD, 11);
        Font metaValFont = new Font("SansSerif", Font.PLAIN, 11);

        metaBlock.add(rightLabel("Invoice No:", metaKeyFont));
        lblInvoiceNo   = rightLabel("—", metaValFont); metaBlock.add(lblInvoiceNo);
        metaBlock.add(rightLabel("Invoice Date:", metaKeyFont));
        lblInvoiceDate = rightLabel("—", metaValFont); metaBlock.add(lblInvoiceDate);
        metaBlock.add(rightLabel("Due Date:", metaKeyFont));
        lblDueDate     = rightLabel("—", metaValFont); metaBlock.add(lblDueDate);

        billingRow.add(billToBlock, BorderLayout.WEST);
        billingRow.add(metaBlock, BorderLayout.EAST);

        body.add(billingRow);
        body.add(Box.createVerticalStrut(18));

        // ── Section 3: Line Items Table ──────────────────────────────────────
        String[] cols = { "DESCRIPTION", "QTY/ HR", "UNIT PRICE", "TOTAL" };
        modelReport = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableReport = new JTable(modelReport);
        tableReport.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tableReport.setRowHeight(24);
        tableReport.setShowGrid(true);
        tableReport.setGridColor(new Color(220, 220, 220));
        tableReport.setBackground(Color.WHITE);
        tableReport.setSelectionBackground(new Color(210, 230, 250));

        // Header style
        JTableHeader th = tableReport.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBackground(Color.WHITE);
        th.setForeground(new Color(30, 30, 30));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(30, 30, 30)));

        // Right-align numeric columns
        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(JLabel.RIGHT);
        tableReport.getColumnModel().getColumn(1).setCellRenderer(rightR);
        tableReport.getColumnModel().getColumn(2).setCellRenderer(rightR);
        tableReport.getColumnModel().getColumn(3).setCellRenderer(rightR);

        // Column widths
        tableReport.getColumnModel().getColumn(0).setPreferredWidth(320);
        tableReport.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableReport.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableReport.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Fixed height scroll pane (10 visible rows)
        JScrollPane tableScroll = new JScrollPane(tableReport);
        tableScroll.setPreferredSize(new Dimension(800, 240));
        tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        body.add(tableScroll);
        body.add(Box.createVerticalStrut(14));

        // ── Section 4: Thank You (left) + Totals (right) ─────────────────────
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Color.WHITE);

        // Left: thank you + terms
        JPanel leftBottom = new JPanel();
        leftBottom.setLayout(new BoxLayout(leftBottom, BoxLayout.Y_AXIS));
        leftBottom.setBackground(Color.WHITE);

        lblThankYou = new JLabel("Thank you for your business!");
        lblThankYou.setFont(new Font("Georgia", Font.ITALIC, 13));
        lblThankYou.setForeground(new Color(80, 80, 80));
        leftBottom.add(lblThankYou);

        leftBottom.add(Box.createVerticalStrut(14));

        JLabel termsHeader = new JLabel("Terms & Instructions");
        termsHeader.setFont(new Font("SansSerif", Font.BOLD, 11));
        leftBottom.add(termsHeader);

        areaTerms = new JTextArea(3, 28);
        areaTerms.setEditable(false);
        areaTerms.setFont(new Font("SansSerif", Font.PLAIN, 11));
        areaTerms.setBackground(Color.WHITE);
        areaTerms.setLineWrap(true);
        areaTerms.setWrapStyleWord(true);
        areaTerms.setBorder(null);
        leftBottom.add(areaTerms);

        // Right: totals grid
        JPanel totalsPanel = new JPanel(new GridBagLayout());
        totalsPanel.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 10, 2, 4);

        Font totKeyFont  = new Font("SansSerif", Font.BOLD, 11);
        Font totValFont  = new Font("SansSerif", Font.PLAIN, 11);
        Font balKeyFont  = new Font("SansSerif", Font.BOLD, 14);
        Font balValFont  = new Font("SansSerif", Font.BOLD, 14);

        lblSubtotal              = rightLabel("—", totValFont);
        lblDiscount              = rightLabel("—", totValFont);
        lblSubtotalLessDiscount  = rightLabel("—", totValFont);
        lblTaxRate               = rightLabel("—", totValFont);
        lblTotalTax              = rightLabel("—", totValFont);
        lblBalanceDue            = rightLabel("—", balValFont);
        lblBalanceDue.setForeground(new Color(20, 20, 20));

        addTotalsRow(totalsPanel, gc, 0, "SUBTOTAL",              lblSubtotal,             totKeyFont);
        addTotalsRow(totalsPanel, gc, 1, "DISCOUNT",              lblDiscount,             totKeyFont);
        addTotalsRow(totalsPanel, gc, 2, "SUBTOTAL LESS DISCOUNT",lblSubtotalLessDiscount, totKeyFont);
        addTotalsRow(totalsPanel, gc, 3, "TAX RATE",              lblTaxRate,              totKeyFont);
        addTotalsRow(totalsPanel, gc, 4, "TOTAL TAX",             lblTotalTax,             totKeyFont);

        // Divider row
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 10, 4, 4);
        JSeparator totalSep = new JSeparator();
        totalSep.setForeground(new Color(30, 30, 30));
        totalsPanel.add(totalSep, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        // Balance Due row (larger)
        gc.insets = new Insets(4, 10, 2, 4);
        gc.gridx = 0; gc.gridy = 6; gc.anchor = GridBagConstraints.WEST;
        JLabel balKey = new JLabel("Balance Due", SwingConstants.LEFT);
        balKey.setFont(balKeyFont);
        totalsPanel.add(balKey, gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.EAST;
        totalsPanel.add(lblBalanceDue, gc);

        bottomRow.add(leftBottom, BorderLayout.WEST);
        bottomRow.add(totalsPanel, BorderLayout.EAST);

        body.add(bottomRow);

        invoicePanel.add(body, BorderLayout.CENTER);
        root.add(invoicePanel);

        JScrollPane outerScroll = new JScrollPane(root);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);
        outerScroll.setBorder(null);
        add(outerScroll);

        loadInvoiceNumbers();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addTotalsRow(JPanel p, GridBagConstraints gc, int row, String key, JLabel val, Font kf) {
        gc.gridx = 0; gc.gridy = row; gc.anchor = GridBagConstraints.WEST;
        JLabel lk = new JLabel(key, SwingConstants.LEFT);
        lk.setFont(kf);
        p.add(lk, gc);
        gc.gridx = 1; gc.anchor = GridBagConstraints.EAST;
        p.add(val, gc);
    }

    private JLabel boldLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, size));
        return l;
    }

    private JLabel plainLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JLabel rightLabel(String text, Font f) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(f);
        return l;
    }

    private JSeparator hRule() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        s.setForeground(new Color(180, 180, 180));
        return s;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
    }

    // ── Data Loading ─────────────────────────────────────────────────────────

    private void loadInvoiceNumbers() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT invoiceNo FROM INVOICE ORDER BY invoiceDate DESC")) {
            while (rs.next()) comboSearch.addItem(rs.getString("invoiceNo"));
        } catch (SQLException e) {
            System.err.println("Could not load invoices: " + e.getMessage());
        }
    }

    private void loadInvoice(String invoiceNo) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sqlHead =
                "SELECT i.*, c.companyName, c.address AS cAddr, c.website, c.phone AS cPhone, " +
                "       cust.customerName, cust.address AS custAddr, cust.email, cust.phone AS custPhone, " +
                "       i.terms " +
                "FROM INVOICE i " +
                "JOIN COMPANY c ON i.companyID = c.companyID " +
                "JOIN CUSTOMER cust ON i.customerID = cust.customerID " +
                "WHERE i.invoiceNo = ?";

            PreparedStatement pstmt = conn.prepareStatement(sqlHead);
            int invoiceId;
            try { invoiceId = Integer.parseInt(invoiceNo); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Invoice Number format."); return;
            }
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Invoice not found."); return;
            }

            // Company header
            lblCompanyName.setText(rs.getString("companyName"));
            lblCompanyAddress.setText(rs.getString("cAddr"));
            safeSet(lblCompanyWebsite, rs, "website");
            safeSet(lblCompanyPhone, rs, "cPhone");

            // Customer bill-to
            lblCustomerName.setText(rs.getString("customerName"));
            lblCustomerAddress.setText(rs.getString("custAddr"));
            safeSet(lblCustomerEmail, rs, "email");
            safeSet(lblCustomerPhone, rs, "custPhone");

            // Invoice meta
            lblInvoiceNo.setText("#INV" + String.format("%04d", invoiceId));
            lblInvoiceDate.setText(rs.getDate("invoiceDate").toString());
            lblDueDate.setText(rs.getDate("dueDate").toString());

            // Terms
            try { areaTerms.setText(rs.getString("terms")); }
            catch (SQLException ignored) { areaTerms.setText(""); }

            double discPct = rs.getDouble("discount");
            double taxPct  = rs.getDouble("taxRate");

            // Line items
            String sqlItems =
                "SELECT li.*, p.productName " +
                "FROM LINE_ITEM li " +
                "JOIN PRODUCT p ON li.productID = p.productID " +
                "WHERE li.invoiceNo = ?";
            PreparedStatement ps2 = conn.prepareStatement(sqlItems);
            ps2.setInt(1, invoiceId);
            ResultSet rsItems = ps2.executeQuery();

            modelReport.setRowCount(0);
            double subtotal = 0;
            while (rsItems.next()) {
                double qty   = rsItems.getDouble("qtyHr");
                double price = rsItems.getDouble("unitPrice");
                double total = qty * price;
                subtotal += total;
                modelReport.addRow(new Object[]{
                    rsItems.getString("productName") +
                        (rsItems.getString("description") != null && !rsItems.getString("description").isEmpty()
                            ? " — " + rsItems.getString("description") : ""),
                    df.format(qty),
                    df.format(price),
                    df.format(total)
                });
            }

            // Totals
            double discAmt   = subtotal * (discPct / 100.0);
            double afterDisc = subtotal - discAmt;
            double taxAmt    = afterDisc * (taxPct / 100.0);
            double balance   = afterDisc + taxAmt;

            lblSubtotal.setText(df.format(subtotal));
            lblDiscount.setText(df.format(discAmt));
            lblSubtotalLessDiscount.setText(df.format(afterDisc));
            lblTaxRate.setText(String.format("%.2f%%", taxPct));
            lblTotalTax.setText(df.format(taxAmt));
            lblBalanceDue.setText("$" + df.format(balance));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void safeSet(JLabel lbl, ResultSet rs, String col) {
        try { String v = rs.getString(col); lbl.setText(v != null ? v : ""); }
        catch (SQLException ignored) { lbl.setText(""); }
    }

    public void setSelectedInvoice(String invoiceNo) {
        comboSearch.setSelectedItem(invoiceNo);
        if (comboSearch.getSelectedItem() == null || !comboSearch.getSelectedItem().toString().equals(invoiceNo)) {
            comboSearch.addItem(invoiceNo);
            comboSearch.setSelectedItem(invoiceNo);
        }
        loadInvoice(invoiceNo);
    }

    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();
        SwingUtilities.invokeLater(() -> new Invoice().setVisible(true));
    }
}