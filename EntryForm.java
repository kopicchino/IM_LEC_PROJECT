import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;

public class EntryForm extends JFrame {

    private static final Color C_HEADER_BG    = new Color(15, 39, 68);
    //private static final Color C_HEADER_BADGE = new Color(29, 111, 184);
    private static final Color C_BADGE_TEXT   = new Color(122, 196, 248);
    private static final Color C_PAGE_BG      = new Color(245, 246, 248);
    private static final Color C_CARD_BG      = Color.WHITE;
    private static final Color C_CARD_HEADER  = new Color(249, 250, 251);
    private static final Color C_BORDER       = new Color(226, 228, 233);
    private static final Color C_LABEL        = new Color(30, 48, 58);
    private static final Color C_SECTION_TITLE = new Color(10, 20, 40);
    private static final Color C_TABLE_HEADER = new Color(15, 39, 68);
    private static final Color C_TABLE_HDRFG  = new Color(147, 197, 253);
    private static final Color C_BTN_PRIMARY  = new Color(15, 39, 68);
    private static final Color C_BTN_ADD      = new Color(29, 111, 184);
    private static final Color C_BTN_ADD_FG   = Color.WHITE;
    private static final Color C_FOOTER_BG    = new Color(249, 250, 251);
    private static final Color C_REQUIRED     = new Color(226, 75, 74);
    private static final Color C_TOTAL_AMT    = new Color(29, 111, 184);
    private static final Color C_ROW_SEL      = new Color(219, 234, 254);
    private static final Color C_ROW_ALT      = new Color(250, 251, 252);

    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SECTION  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 12);
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_TOTAL_LG = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField txtCompName, txtCompWebsite, txtCompCity, txtCompZip;
    private JTextField txtCompPhone, txtCompStreet, txtCompState;

    private JTextField txtCustName, txtCustPhone, txtCustCity, txtCustZip;
    private JTextField txtCustEmail, txtCustStreet, txtCustState;

    private JTextField txtInvDate, txtDueDate, txtSubtotal, txtTaxRate;

    private JTable tableItems;
    private DefaultTableModel tableModel;

    private JTextField txtItemDesc, txtItemQty, txtItemPrice;

    private JLabel lblTotalSubtotal, lblTotalTax, lblTotalGrand, lblTaxRateLabel;

    private final DecimalFormat df = new DecimalFormat("0.00");

    public EntryForm() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        setTitle("Invoice Entry Form");
        setSize(980, 860);
        setMinimumSize(new Dimension(820, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_PAGE_BG);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = buildContent();
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(C_PAGE_BG);
        root.add(scroll, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);
        add(root);
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(C_HEADER_BG);
        hdr.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JPanel logoMark = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BADGE_TEXT.darker());
                g2.fillRoundRect(0, 0, 34, 34, 8, 8);
                g2.setColor(C_BADGE_TEXT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(8, 10, 20, 10);
                g2.drawLine(8, 15, 26, 15);
                g2.drawLine(8, 20, 26, 20);
                g2.drawLine(8, 25, 16, 25);
            }
        };
        logoMark.setPreferredSize(new Dimension(34, 34));
        logoMark.setOpaque(false);
        left.add(logoMark);
        left.add(Box.createHorizontalStrut(12));

        JLabel title = new JLabel("Invoice Entry");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        left.add(title);

        JLabel badge = new JLabel("NEW INVOICE");
        badge.setFont(new Font("Consolas", Font.BOLD, 10));
        badge.setForeground(C_BADGE_TEXT);
        badge.setBorder(new CompoundBorder(
            new LineBorder(new Color(122, 196, 248, 60), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        badge.setOpaque(true);
        badge.setBackground(new Color(122, 196, 248, 30));

        hdr.add(left, BorderLayout.WEST);
        hdr.add(badge, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildContent() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_PAGE_BG);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        body.add(buildSectionCard("COMPANY INFORMATION", new Color(219, 234, 254), new Color(29, 78, 216),
                buildCompanyFields()));
        body.add(Box.createRigidArea(new Dimension(0, 14)));

        body.add(buildSectionCard("CUSTOMER INFORMATION", new Color(220, 252, 231), new Color(21, 128, 61),
                buildCustomerFields()));
        body.add(Box.createRigidArea(new Dimension(0, 14)));

        body.add(buildSectionCard("BILLING DETAILS", new Color(254, 243, 199), new Color(180, 83, 9),
                buildBillingFields()));
        body.add(Box.createRigidArea(new Dimension(0, 14)));

        body.add(buildItemsCard());
        return body;
    }

    private JPanel buildSectionCard(String title, Color iconBg, Color iconFg, JPanel fields) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

        JPanel cardHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        cardHdr.setBackground(C_CARD_HEADER);
        cardHdr.setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, 20, 20, 5, 5);
                g2.setColor(iconFg);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String t = title.substring(0, 1);
                g2.drawString(t, (20 - fm.stringWidth(t)) / 2, (20 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        dot.setPreferredSize(new Dimension(20, 20));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(C_SECTION_TITLE);

        cardHdr.add(dot);
        cardHdr.add(lbl);

        fields.setBorder(new EmptyBorder(16, 18, 16, 18));
        fields.setBackground(C_CARD_BG);

        card.add(cardHdr, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCompanyFields() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        txtCompName    = styledField("e.g. Acme Corporation");
        txtCompPhone   = styledField("e.g. +1 (555) 000-0000");
        txtCompWebsite = styledField("e.g. https://company.com");
        txtCompStreet  = styledField("e.g. 123 Main St");
        txtCompCity    = styledField("e.g. San Francisco");
        txtCompState   = styledField("e.g. CA");
        txtCompZip     = styledField("e.g. 94102");

        addField(p, "COMPANY NAME", true,  txtCompName,    0, 0, g);
        addField(p, "PHONE",        false, txtCompPhone,   2, 0, g);
        addField(p, "WEBSITE",      false, txtCompWebsite, 0, 1, g);
        addField(p, "STREET",       false, txtCompStreet,  2, 1, g);
        addField(p, "CITY",         false, txtCompCity,    0, 2, g);
        addField(p, "STATE",        false, txtCompState,   2, 2, g);
        addField(p, "ZIP CODE",     false, txtCompZip,     0, 3, g);
        return p;
    }

    private JPanel buildCustomerFields() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        txtCustName   = styledField("e.g. Jane Smith");
        txtCustEmail  = styledField("e.g. jane@example.com");
        txtCustPhone  = styledField("e.g. +1 (555) 000-0000");
        txtCustStreet = styledField("e.g. 456 Oak Ave");
        txtCustCity   = styledField("e.g. New York");
        txtCustState  = styledField("e.g. NY");
        txtCustZip    = styledField("e.g. 10001");

        addField(p, "CUSTOMER NAME", true,  txtCustName,   0, 0, g);
        addField(p, "EMAIL",         false, txtCustEmail,  2, 0, g);
        addField(p, "PHONE",         false, txtCustPhone,  0, 1, g);
        addField(p, "STREET",        false, txtCustStreet, 2, 1, g);
        addField(p, "CITY",          false, txtCustCity,   0, 2, g);
        addField(p, "STATE",         false, txtCustState,  2, 2, g);
        addField(p, "ZIP CODE",      false, txtCustZip,    0, 3, g);
        return p;
    }

    private JPanel buildBillingFields() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        txtInvDate = styledField("YYYY-MM-DD");
        txtInvDate.setText("2026-05-01");

        txtDueDate = styledField("YYYY-MM-DD");
        txtDueDate.setText("2026-05-15");

        txtSubtotal = styledField(null);
        txtSubtotal.setText("0.00");
        txtSubtotal.setEditable(false);
        txtSubtotal.setBackground(C_CARD_HEADER);
        txtSubtotal.setFont(FONT_MONO);

        txtTaxRate = styledField(null);
        txtTaxRate.setText("5.0");
        txtTaxRate.setFont(FONT_MONO);
        txtTaxRate.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        addField(p, "INVOICE DATE (YYYY-MM-DD)", true, txtInvDate,  0, 0, g);
        addField(p, "DUE DATE (YYYY-MM-DD)",     true, txtDueDate,  2, 0, g);
        addField(p, "SUBTOTAL LESS DISCOUNT",    true, txtSubtotal, 0, 1, g);
        addField(p, "TAX RATE (%)",              true, txtTaxRate,  2, 1, g);
        return p;
    }

    private JPanel buildItemsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cardHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        cardHdr.setBackground(C_CARD_HEADER);
        cardHdr.setBorder(new MatteBorder(0, 0, 1, 0, C_BORDER));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(243, 232, 255));
                g2.fillRoundRect(0, 0, 20, 20, 5, 5);
                g2.setColor(new Color(124, 58, 237));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                String t = "I";
                g2.drawString(t, (20 - fm.stringWidth(t)) / 2, (20 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        dot.setPreferredSize(new Dimension(20, 20));
        dot.setOpaque(false);

        JLabel lbl = new JLabel("INVOICE ITEMS");
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(C_SECTION_TITLE);
        cardHdr.add(dot);
        cardHdr.add(lbl);
        card.add(cardHdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_CARD_BG);
        body.setBorder(new EmptyBorder(16, 18, 16, 18));

        String[] cols = {"Description", "Qty / Hr", "Unit Price", "Amount"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableItems = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(C_ROW_SEL);
                    c.setForeground(new Color(15, 39, 68));
                } else {
                    c.setBackground(row % 2 == 0 ? C_CARD_BG : C_ROW_ALT);
                    c.setForeground(new Color(10, 20, 38));
                }
                return c;
            }
        };
        tableItems.setRowHeight(32);
        tableItems.setFont(FONT_FIELD);
        tableItems.setShowHorizontalLines(true);
        tableItems.setShowVerticalLines(false);
        tableItems.setGridColor(C_BORDER);
        tableItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableItems.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader th = tableItems.getTableHeader();
        th.setBackground(C_TABLE_HEADER);
        th.setForeground(C_TABLE_HDRFG);
        th.setFont(new Font("Consolas", Font.BOLD, 11));
        th.setPreferredSize(new Dimension(th.getWidth(), 36));
        th.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) th.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        DefaultTableCellRenderer rightMono = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(JLabel.RIGHT); setFont(FONT_MONO); }
        };
        tableItems.getColumnModel().getColumn(1).setCellRenderer(rightMono);
        tableItems.getColumnModel().getColumn(2).setCellRenderer(rightMono);
        tableItems.getColumnModel().getColumn(3).setCellRenderer(rightMono);

        tableItems.getColumnModel().getColumn(0).setPreferredWidth(380);
        tableItems.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableItems.getColumnModel().getColumn(2).setPreferredWidth(110);
        tableItems.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane tScroll = new JScrollPane(tableItems);
        tScroll.setBorder(new LineBorder(C_BORDER, 1, true));
        tScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 160));
        tScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        tScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        body.add(tScroll);
        body.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel addRow = new JPanel(new GridBagLayout());
        addRow.setBackground(C_CARD_BG);
        addRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints ag = new GridBagConstraints();
        ag.fill = GridBagConstraints.HORIZONTAL;
        ag.insets = new Insets(0, 0, 0, 10);
        ag.gridy = 0;

        txtItemDesc  = styledField("Enter description");
        txtItemQty   = styledField("0");
        txtItemQty.setFont(FONT_MONO);
        txtItemPrice = styledField("0.00");
        txtItemPrice.setFont(FONT_MONO);

        JPanel descGroup  = labeledInput("DESCRIPTION", txtItemDesc);
        JPanel qtyGroup   = labeledInput("QTY / HR",    txtItemQty);
        JPanel priceGroup = labeledInput("UNIT PRICE",  txtItemPrice);

        ag.gridx = 0; ag.weightx = 3.0; addRow.add(descGroup, ag);
        ag.gridx = 1; ag.weightx = 1.0; addRow.add(qtyGroup, ag);
        ag.gridx = 2; ag.weightx = 1.0; addRow.add(priceGroup, ag);

        JPanel btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.Y_AXIS));
        btnGroup.setOpaque(false);

        JLabel spacer = new JLabel(" ");
        spacer.setFont(FONT_LABEL);
        spacer.setBorder(new EmptyBorder(0, 0, 4, 0));
        btnGroup.add(spacer);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        JButton btnAdd = primaryButton("+ Add", C_BTN_ADD, C_BTN_ADD_FG);
        btnAdd.addActionListener(e -> addLineItem());
        btnRow.add(btnAdd);

        JButton btnRemove = outlineButton("Remove Selected");
        btnRemove.addActionListener(e -> {
            int row = tableItems.getSelectedRow();
            if (row >= 0) {
                tableModel.removeRow(row);
                updateSubtotal();
                updateTotals();
            }
        });
        btnRow.add(btnRemove);

        btnGroup.add(btnRow);

        ag.gridx = 3; ag.weightx = 0;
        ag.insets = new Insets(0, 0, 0, 0);
        addRow.add(btnGroup, ag);

        addRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        body.add(addRow);
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel totalsPanel = new JPanel(new BorderLayout());
        totalsPanel.setOpaque(false);
        totalsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalsPanel.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        totalsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel totalsRight = new JPanel();
        totalsRight.setLayout(new BoxLayout(totalsRight, BoxLayout.Y_AXIS));
        totalsRight.setOpaque(false);
        totalsRight.setBorder(new EmptyBorder(12, 0, 4, 0));

        lblTotalSubtotal = monoLabel("$0.00");
        lblTotalTax      = monoLabel("$0.00");
        lblTaxRateLabel  = new JLabel("Tax (5.0%)");
        lblTaxRateLabel.setFont(FONT_LABEL);
        lblTaxRateLabel.setForeground(C_LABEL);
        lblTotalGrand = monoLabel("$0.00");
        lblTotalGrand.setFont(FONT_TOTAL_LG);
        lblTotalGrand.setForeground(C_TOTAL_AMT);

        totalsRight.add(totalRow("Subtotal", lblTotalSubtotal));
        totalsRight.add(Box.createRigidArea(new Dimension(0, 5)));
        totalsRight.add(totalRow(lblTaxRateLabel, lblTotalTax));
        totalsRight.add(Box.createRigidArea(new Dimension(0, 8)));

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        totalsRight.add(sep);
        totalsRight.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel grandLbl = new JLabel("Total Due");
        grandLbl.setFont(FONT_TOTAL_LG);
        grandLbl.setForeground(C_SECTION_TITLE);
        totalsRight.add(totalRow(grandLbl, lblTotalGrand));

        totalsPanel.add(totalsRight, BorderLayout.EAST);
        body.add(totalsPanel);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 4, 5, 16);
        g.anchor = GridBagConstraints.NORTHWEST;
        return g;
    }

    private void addField(JPanel panel, String labelText, boolean required,
                          JTextField field, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y; gbc.weightx = 0;
        JPanel lp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lp.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_LABEL);
        lp.add(lbl);
        if (required) {
            JLabel req = new JLabel(" *");
            req.setFont(FONT_LABEL);
            req.setForeground(C_REQUIRED);
            lp.add(req);
        }
        panel.add(lp, gbc);
        gbc.gridx = x + 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && placeholder != null && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(180, 185, 195));
                    g2.setFont(FONT_FIELD);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 1);
                }
            }
        };
        f.setFont(FONT_FIELD);
        f.setForeground(new Color(25, 35, 50));
        f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, 34));
        f.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(new Color(29, 111, 184), 1, true),
                    new EmptyBorder(0, 10, 0, 10)
                ));
                f.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(C_BORDER, 1, true),
                    new EmptyBorder(0, 10, 0, 10)
                ));
                f.repaint();
            }
        });
        return f;
    }

    private JPanel labeledInput(String labelText, JTextField field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_LABEL);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(field);
        return p;
    }

    private JButton primaryButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                             getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setPreferredSize(new Dimension(80, 34));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton outlineButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(254, 242, 242));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
                g2.setColor(getModel().isRollover() ? new Color(226, 75, 74) : C_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
                g2.setColor(getModel().isRollover() ? new Color(226, 75, 74) : C_SECTION_TITLE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setFont(FONT_BTN);
        b.setPreferredSize(new Dimension(140, 34));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel monoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_MONO);
        l.setForeground(C_SECTION_TITLE);
        l.setHorizontalAlignment(JLabel.RIGHT);
        return l;
    }

    private JPanel totalRow(String labelText, JLabel valueLabel) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_LABEL);
        return totalRow(lbl, valueLabel);
    }

    private JPanel totalRow(JLabel labelComp, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 0, 0));
        valueLabel.setPreferredSize(new Dimension(100, 20));
        valueLabel.setHorizontalAlignment(JLabel.RIGHT);
        row.add(labelComp, BorderLayout.WEST);
        row.add(Box.createHorizontalStrut(48), BorderLayout.CENTER);
        row.add(valueLabel, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(340, 24));
        return row;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(C_FOOTER_BG);
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel hint = new JLabel("Fields marked * are required");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(70, 80, 100));
        footer.add(hint, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton btnClear = outlineButton("Clear Form");
        btnClear.setPreferredSize(new Dimension(110, 36));
        btnClear.addActionListener(e -> clearForm());
        btns.add(btnClear);

        JButton btnSave = primaryButton("Save & Generate Invoice", C_BTN_PRIMARY, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(210, 36));
        btnSave.setFont(FONT_BTN);
        btnSave.addActionListener(e -> saveInvoice());
        btns.add(btnSave);

        footer.add(btns, BorderLayout.EAST);
        return footer;
    }

    private void addLineItem() {
        String desc     = txtItemDesc.getText().trim();
        String qtyStr   = txtItemQty.getText().trim();
        String priceStr = txtItemPrice.getText().trim();

        if (desc.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Description, Qty/Hr, and Unit Price.");
            return;
        }
        try {
            double qty   = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            if (qty <= 0) throw new NumberFormatException("Qty must be > 0");

            double amount = qty * price;
            tableModel.addRow(new Object[]{desc, df.format(qty), df.format(price), df.format(amount)});

            txtItemDesc.setText("");
            txtItemQty.setText("");
            txtItemPrice.setText("");
            txtItemDesc.requestFocus();
            updateSubtotal();
            updateTotals();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Qty and Unit Price.");
        }
    }

    private void updateSubtotal() {
        double sub = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double qty   = Double.parseDouble(tableModel.getValueAt(i, 1).toString().replace(",", ""));
            double price = Double.parseDouble(tableModel.getValueAt(i, 2).toString().replace(",", ""));
            sub += qty * price;
        }
        txtSubtotal.setText(df.format(sub));
    }

    private void updateTotals() {
        double sub = 0;
        try { sub = Double.parseDouble(txtSubtotal.getText().replace(",", "")); }
        catch (NumberFormatException ignored) {}

        double taxRate = 0;
        try { taxRate = Double.parseDouble(txtTaxRate.getText().trim()); }
        catch (NumberFormatException ignored) {}

        double tax   = sub * taxRate / 100.0;
        double total = sub + tax;

        lblTotalSubtotal.setText("$" + df.format(sub));
        lblTotalTax.setText("$" + df.format(tax));
        lblTotalGrand.setText("$" + df.format(total));
        lblTaxRateLabel.setText("Tax (" + df.format(taxRate) + "%)");
    }

    private void clearForm() {
        for (JTextField f : new JTextField[]{
                txtCompName, txtCompWebsite, txtCompCity, txtCompZip,
                txtCompPhone, txtCompStreet, txtCompState,
                txtCustName, txtCustPhone, txtCustCity, txtCustZip,
                txtCustEmail, txtCustStreet, txtCustState,
                txtItemDesc, txtItemQty, txtItemPrice}) {
            f.setText("");
        }
        txtInvDate.setText("2026-05-01");
        txtDueDate.setText("2026-05-15");
        txtTaxRate.setText("5.0");
        txtSubtotal.setText("0.00");
        tableModel.setRowCount(0);
        updateTotals();
    }

    private void saveInvoice() {
        if (txtCompName.getText().trim().isEmpty() || txtCustName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Company Name and Customer Name are required.");
            return;
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one line item.");
            return;
        }

        String invDateStr = txtInvDate.getText().trim();
        String dueDateStr = txtDueDate.getText().trim();
        Date invDate, dueDate;
        double taxRate;
        try {
            invDate  = Date.valueOf(invDateStr);
            dueDate  = dueDateStr.isEmpty() ? null : Date.valueOf(dueDateStr);
            taxRate  = txtTaxRate.getText().trim().isEmpty() ? 0
                       : Double.parseDouble(txtTaxRate.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date (use YYYY-MM-DD) or tax rate.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            int companyId = -1;
            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO COMPANY (companyName, website, address, phone, termsInstructions) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, txtCompName.getText().trim());
                pst.setString(2, txtCompWebsite.getText().trim());
                pst.setString(3, buildAddress(txtCompStreet, txtCompCity, txtCompState, txtCompZip));
                pst.setString(4, txtCompPhone.getText().trim());
                pst.setString(5, "Thank you for your business!");
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) companyId = rs.getInt(1);
            }

            int customerId = -1;
            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO CUSTOMER (customerName, address, email, phone) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, txtCustName.getText().trim());
                pst.setString(2, buildAddress(txtCustStreet, txtCustCity, txtCustState, txtCustZip));
                pst.setString(3, txtCustEmail.getText().trim());
                pst.setString(4, txtCustPhone.getText().trim());
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) customerId = rs.getInt(1);
            }

            int invoiceId = -1;
            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO INVOICE (companyID, customerID, invoiceDate, dueDate, discount, taxRate) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, companyId);
                pst.setInt(2, customerId);
                pst.setDate(3, invDate);
                pst.setDate(4, dueDate);
                pst.setDouble(5, 0.0);
                pst.setDouble(6, taxRate);
                pst.executeUpdate();
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) invoiceId = rs.getInt(1);
            }

            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO LINE_ITEM (invoiceNo, productID, description, qtyHr, unitPrice) VALUES (?,1,?,?,?)")) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String desc  = tableModel.getValueAt(i, 0).toString();
                    double qty   = Double.parseDouble(tableModel.getValueAt(i, 1).toString().replace(",", ""));
                    double price = Double.parseDouble(tableModel.getValueAt(i, 2).toString().replace(",", ""));
                    pst.setInt(1, invoiceId);
                    pst.setString(2, desc);
                    pst.setDouble(3, qty);
                    pst.setDouble(4, price);
                    pst.addBatch();
                }
                pst.executeBatch();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Invoice #" + invoiceId + " generated successfully!");

            Invoice reportGenerator = new Invoice();
            reportGenerator.setVisible(true);
            reportGenerator.setSelectedInvoice(String.valueOf(invoiceId));
            this.dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private String buildAddress(JTextField street, JTextField city, JTextField state, JTextField zip) {
        return String.format("%s, %s, %s %s",
                street.getText().trim(), city.getText().trim(),
                state.getText().trim(), zip.getText().trim())
                .replaceAll("^[,\\s]+|[,\\s]+$", "").trim();
    }

    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();
        SwingUtilities.invokeLater(() -> new EntryForm().setVisible(true));
    }
}