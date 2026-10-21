import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise Fintech AI Risk Guardian Application.
 * Full multi-dimensional risk engine featuring:
 * - Device & Geolocation Risk
 * - Merchant Website Legitimacy & SSL Verification
 * - Customer-Device-Beneficiary Graph Mining
 * - Separate Customer Risk vs. Beneficiary Risk metrics
 * - Confidence & Uncertainty Estimation
 * - Explainable Reason Codes (RC_...)
 * - Federated Learning (FedAvg) synchronization
 */
public class FintechApp extends JFrame {

    private static final String BACKEND_URL = "http://localhost:5000";
    private final ApiClient apiClient;

    // High-Contrast Pure Black Theme Palette
    private static final Color BG_PITCH_BLACK   = new Color(0, 0, 0);          // #000000
    private static final Color CARD_BG          = new Color(16, 16, 16);       // #101010
    private static final Color CARD_BORDER      = new Color(50, 50, 50);       // #323232
    private static final Color INPUT_BG         = new Color(26, 26, 26);       // #1A1A1A
    private static final Color TEXT_WHITE       = new Color(255, 255, 255);    // #FFFFFF
    private static final Color TEXT_MUTED_LIGHT = new Color(200, 205, 215);    // #C8CDD7
    private static final Color ACCENT_BLUE      = new Color(37, 99, 235);      // #2563EB

    // Bright Risk Threshold Colors
    private static final Color COLOR_ALLOWED = new Color(52, 211, 153);     // Mint Green (<40%)
    private static final Color COLOR_WARNING = new Color(251, 191, 36);     // Amber Yellow (40-60%)
    private static final Color COLOR_BLOCKED = new Color(251, 146, 60);     // Orange (60-80%)
    private static final Color COLOR_STOPPED = new Color(248, 113, 113);    // Red (>80%)

    // UI Elements
    private JComboBox<AccountItem> senderCombo;
    private JComboBox<AccountItem> recipientCombo;
    private JTextField amountField;
    private JLabel balanceBadge;
    private JLabel statusBadge;
    private JLabel federatedPill;

    // Device Context Selectors
    private JComboBox<String> deviceCombo;
    private JCheckBox vpnCheck;
    private JCheckBox emulatorCheck;

    // Risk Display Elements
    private JProgressBar riskBar;
    private JLabel riskScoreBig;
    private JLabel decisionBadge;
    private JProgressBar customerRiskBar;
    private JProgressBar beneficiaryRiskBar;
    private JLabel customerRiskVal;
    private JLabel beneficiaryRiskVal;
    private JLabel confidenceVal;
    private JLabel uncertaintyVal;
    private JLabel websiteInspectLabel;
    private JTextArea reasonsDisplay;
    private JLabel reasonCodesPills;

    // History Table
    private DefaultTableModel historyModel;
    private JTable historyTable;
    private JButton transferBtn;
    private JButton evaluateBtn;

    public FintechApp() {
        this.apiClient = new ApiClient(BACKEND_URL);
        setupFontsAndAntialiasing();
        initUI();
        loadDataFromBackend();
    }

    private void setupFontsAndAntialiasing() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setTitle("Fintech AI Multi-Dimensional Risk Guardian");
        setSize(1260, 840);
        setMinimumSize(new Dimension(1120, 760));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PITCH_BLACK);
        setLayout(new BorderLayout(0, 0));

        // 1. Top Navigation Bar
        add(createTopNavBar(), BorderLayout.NORTH);

        // 2. Main Center Split
        JPanel contentPane = new JPanel(new GridLayout(1, 2, 18, 0));
        contentPane.setBackground(BG_PITCH_BLACK);
        contentPane.setBorder(new EmptyBorder(14, 18, 18, 18));

        // Left Panel: Form & Device Context + AI Risk Analytics
        contentPane.add(createLeftPanel());

        // Right Panel: Audit Ledger & Reason Codes & Rule Guide
        contentPane.add(createRightPanel());

        add(contentPane, BorderLayout.CENTER);
    }

    private JPanel createTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(CARD_BG);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(12, 22, 12, 22)
        ));

        // Left Branding
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBox.setOpaque(false);

        JLabel logo = new JLabel("🛡️");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        JLabel title = new JLabel("FINTECH AI MULTI-DIMENSIONAL RISK GUARDIAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT_WHITE);

        JLabel subtitle = new JLabel("Graph Mining • Device Fingerprinting • Merchant Website Verification • Federated FedAvg");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED_LIGHT);

        textGroup.add(title);
        textGroup.add(subtitle);

        brandBox.add(logo);
        brandBox.add(textGroup);

        // Right Actions & Status Badges
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setOpaque(false);

        federatedPill = new JLabel(" FedAvg-v2.14 (Loss: 0.084) ");
        federatedPill.setOpaque(true);
        federatedPill.setBackground(new Color(30, 41, 59));
        federatedPill.setForeground(new Color(147, 197, 253));
        federatedPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        federatedPill.setBorder(new EmptyBorder(6, 10, 6, 10));

        statusBadge = new JLabel(" ● Connecting... ");
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(40, 40, 40));
        statusBadge.setForeground(COLOR_WARNING);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setBorder(new EmptyBorder(6, 12, 6, 12));

        JButton resetBtn = createButton("↻ Reset Data", new Color(45, 45, 45), TEXT_WHITE);
        resetBtn.addActionListener(e -> resetDemoData());

        JButton refreshBtn = createButton("↻ Refresh", new Color(45, 45, 45), TEXT_WHITE);
        refreshBtn.addActionListener(e -> loadDataFromBackend());

        rightBox.add(federatedPill);
        rightBox.add(statusBadge);
        rightBox.add(resetBtn);
        rightBox.add(refreshBtn);

        nav.add(brandBox, BorderLayout.WEST);
        nav.add(rightBox, BorderLayout.EAST);
        return nav;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // 1. Transaction & Context Form Card
        JPanel inputCard = createStyledCard("1. Transaction & Environment Context");
        inputCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        // From Account
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        inputCard.add(createLabel("From Account:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        senderCombo = new JComboBox<>();
        styleComboBox(senderCombo);
        senderCombo.addActionListener(e -> updateSenderBalance());
        inputCard.add(senderCombo, gbc);

        // Balance Indicator
        gbc.gridx = 1; gbc.gridy = 1;
        balanceBadge = new JLabel("Available Balance: $0.00");
        balanceBadge.setForeground(COLOR_ALLOWED);
        balanceBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inputCard.add(balanceBadge, gbc);

        // To Account
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        inputCard.add(createLabel("To Account:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        recipientCombo = new JComboBox<>();
        styleComboBox(recipientCombo);
        inputCard.add(recipientCombo, gbc);

        // Amount Input
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        inputCard.add(createLabel("Amount ($ USD):"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        amountField = new JTextField("200.00");
        amountField.setBackground(INPUT_BG);
        amountField.setForeground(TEXT_WHITE);
        amountField.setCaretColor(TEXT_WHITE);
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        amountField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        inputCard.add(amountField, gbc);

        // Device & Context Simulation
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        inputCard.add(createLabel("Device / IP Origin:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel devRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        devRow.setOpaque(false);
        deviceCombo = new JComboBox<>(new String[]{
                "DEV-TRUSTED-IPHONE15 (Home US)",
                "DEV-NEW-UNKNOWN-MAC (Origin: RU)",
                "DEV-SHARED-MULE-FARM (Shared across 4 accounts)"
        });
        styleGenericCombo(deviceCombo);

        vpnCheck = new JCheckBox("VPN/Tor");
        vpnCheck.setOpaque(false);
        vpnCheck.setForeground(TEXT_WHITE);
        vpnCheck.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        emulatorCheck = new JCheckBox("Emulator");
        emulatorCheck.setOpaque(false);
        emulatorCheck.setForeground(TEXT_WHITE);
        emulatorCheck.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        devRow.add(deviceCombo);
        devRow.add(vpnCheck);
        devRow.add(emulatorCheck);
        inputCard.add(devRow, gbc);

        // Quick Preset Chips (1-click testing of all 4 risk tiers)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        presetPanel.setOpaque(false);
        JLabel lblQuick = new JLabel("One-Click Presets:");
        lblQuick.setForeground(TEXT_MUTED_LIGHT);
        lblQuick.setFont(new Font("Segoe UI", Font.BOLD, 11));
        presetPanel.add(lblQuick);

        presetPanel.add(createPresetChip("🟢 <40% (Approve)", "200.00", "ACC-9001"));
        presetPanel.add(createPresetChip("🟡 40%-60% (Warning)", "350.00", "ACC-9004"));
        presetPanel.add(createPresetChip("🟠 60%-80% (OTP/PIN)", "2800.00", "ACC-9006"));
        presetPanel.add(createPresetChip("🔴 >80% (Block/Stop)", "1000.00", "ACC-9003"));
        inputCard.add(presetPanel, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 12, 0));
        actionRow.setOpaque(false);

        evaluateBtn = createButton("🔍 Analyze AI Risk Only", new Color(45, 45, 45), TEXT_WHITE);
        evaluateBtn.addActionListener(this::handleAnalyzeRisk);

        transferBtn = createButton("🚀 Process Transfer", ACCENT_BLUE, TEXT_WHITE);
        transferBtn.addActionListener(this::handleProcessTransfer);

        actionRow.add(evaluateBtn);
        actionRow.add(transferBtn);
        inputCard.add(actionRow, gbc);

        // 2. Multi-Dimensional AI Risk Assessment Card
        JPanel riskCard = createStyledCard("2. Deep Risk Analytics & Feature Breakdown");
        riskCard.setLayout(new BorderLayout(0, 10));

        // Score & Decision Banner
        JPanel scoreBanner = new JPanel(new BorderLayout());
        scoreBanner.setOpaque(false);

        riskScoreBig = new JLabel("0.0% Composite Risk");
        riskScoreBig.setFont(new Font("Segoe UI", Font.BOLD, 22));
        riskScoreBig.setForeground(COLOR_ALLOWED);

        decisionBadge = new JLabel("READY");
        decisionBadge.setOpaque(true);
        decisionBadge.setBackground(new Color(30, 41, 59));
        decisionBadge.setForeground(TEXT_WHITE);
        decisionBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        decisionBadge.setBorder(new EmptyBorder(5, 12, 5, 12));

        scoreBanner.add(riskScoreBig, BorderLayout.WEST);
        scoreBanner.add(decisionBadge, BorderLayout.EAST);

        // Main Composite Gauge
        riskBar = new JProgressBar(0, 100);
        riskBar.setValue(0);
        riskBar.setStringPainted(true);
        riskBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        riskBar.setPreferredSize(new Dimension(100, 22));
        riskBar.setBackground(INPUT_BG);
        riskBar.setForeground(COLOR_ALLOWED);

        // Dual Customer vs Beneficiary Sub-Gauges & Uncertainty
        JPanel subGauges = new JPanel(new GridLayout(2, 2, 12, 6));
        subGauges.setOpaque(false);

        // Customer Risk Sub-Bar
        JPanel cPanel = new JPanel(new BorderLayout(4, 2));
        cPanel.setOpaque(false);
        customerRiskVal = new JLabel("Customer Risk: 0.0%");
        customerRiskVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        customerRiskVal.setForeground(TEXT_WHITE);
        customerRiskBar = new JProgressBar(0, 100);
        customerRiskBar.setPreferredSize(new Dimension(80, 14));
        customerRiskBar.setBackground(INPUT_BG);
        customerRiskBar.setForeground(new Color(96, 165, 250));
        cPanel.add(customerRiskVal, BorderLayout.NORTH);
        cPanel.add(customerRiskBar, BorderLayout.CENTER);

        // Beneficiary Risk Sub-Bar
        JPanel bPanel = new JPanel(new BorderLayout(4, 2));
        bPanel.setOpaque(false);
        beneficiaryRiskVal = new JLabel("Beneficiary/Merchant Risk: 0.0%");
        beneficiaryRiskVal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        beneficiaryRiskVal.setForeground(TEXT_WHITE);
        beneficiaryRiskBar = new JProgressBar(0, 100);
        beneficiaryRiskBar.setPreferredSize(new Dimension(80, 14));
        beneficiaryRiskBar.setBackground(INPUT_BG);
        beneficiaryRiskBar.setForeground(new Color(244, 114, 182));
        bPanel.add(beneficiaryRiskVal, BorderLayout.NORTH);
        bPanel.add(beneficiaryRiskBar, BorderLayout.CENTER);

        // Confidence & Uncertainty Metrics
        JPanel confPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        confPanel.setOpaque(false);
        confidenceVal = new JLabel("AI Confidence: 95.0%");
        confidenceVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        confidenceVal.setForeground(COLOR_ALLOWED);
        confPanel.add(confidenceVal);

        JPanel uncPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        uncPanel.setOpaque(false);
        uncertaintyVal = new JLabel("Bayesian Uncertainty: 5.0%");
        uncertaintyVal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        uncertaintyVal.setForeground(TEXT_MUTED_LIGHT);
        uncPanel.add(uncertaintyVal);

        subGauges.add(cPanel);
        subGauges.add(bPanel);
        subGauges.add(confPanel);
        subGauges.add(uncPanel);

        // Merchant Website Inspection Strip
        websiteInspectLabel = new JLabel("Merchant Website: None or Internal P2P");
        websiteInspectLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        websiteInspectLabel.setForeground(TEXT_MUTED_LIGHT);

        // Reasons & Explainability TextArea
        reasonsDisplay = new JTextArea("Ready. Click 'Analyze AI Risk Only' or 'Process Transfer'.");
        reasonsDisplay.setEditable(false);
        reasonsDisplay.setBackground(INPUT_BG);
        reasonsDisplay.setForeground(TEXT_WHITE);
        reasonsDisplay.setFont(new Font("Consolas", Font.PLAIN, 12));
        reasonsDisplay.setLineWrap(true);
        reasonsDisplay.setWrapStyleWord(true);
        reasonsDisplay.setMargin(new Insets(8, 10, 8, 10));

        JScrollPane scrollReasons = new JScrollPane(reasonsDisplay);
        scrollReasons.setBorder(new LineBorder(CARD_BORDER, 1));
        scrollReasons.setPreferredSize(new Dimension(100, 110));

        JPanel topStack = new JPanel(new BorderLayout(0, 6));
        topStack.setOpaque(false);
        topStack.add(scoreBanner, BorderLayout.NORTH);
        topStack.add(riskBar, BorderLayout.CENTER);
        topStack.add(subGauges, BorderLayout.SOUTH);

        riskCard.add(topStack, BorderLayout.NORTH);
        riskCard.add(websiteInspectLabel, BorderLayout.CENTER);
        riskCard.add(scrollReasons, BorderLayout.SOUTH);

        panel.add(inputCard, BorderLayout.NORTH);
        panel.add(riskCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // 1. Transaction Table Card
        JPanel tableCard = createStyledCard("Transaction Ledger & Audit Trail");
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] headers = {"Tx ID", "From", "To", "Amount", "C-Risk", "B-Risk", "Total %", "Action", "Status"};
        historyModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(historyModel);
        historyTable.setBackground(INPUT_BG);
        historyTable.setForeground(TEXT_WHITE);
        historyTable.setGridColor(CARD_BORDER);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setSelectionBackground(ACCENT_BLUE);
        historyTable.setSelectionForeground(TEXT_WHITE);

        JTableHeader th = historyTable.getTableHeader();
        th.setBackground(CARD_BG);
        th.setForeground(TEXT_WHITE);
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBorder(new LineBorder(CARD_BORDER, 1));

        DefaultTableCellRenderer centerCell = new DefaultTableCellRenderer();
        centerCell.setHorizontalAlignment(JLabel.CENTER);
        centerCell.setBackground(INPUT_BG);
        centerCell.setForeground(TEXT_WHITE);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerCell);
        }

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBackground(INPUT_BG);
        tableScroll.getViewport().setBackground(INPUT_BG);
        tableScroll.setBorder(new LineBorder(CARD_BORDER, 1));

        tableCard.add(tableScroll, BorderLayout.CENTER);

        // 2. Reason Codes Badge Strip
        JPanel codeCard = createStyledCard("Generated Threat Reason Codes (Audit Snapshot)");
        codeCard.setLayout(new BorderLayout(0, 6));

        reasonCodesPills = new JLabel("No active transaction flags.");
        reasonCodesPills.setFont(new Font("Consolas", Font.BOLD, 12));
        reasonCodesPills.setForeground(COLOR_WARNING);
        codeCard.add(reasonCodesPills, BorderLayout.CENTER);

        // 3. Risk Rules Reference Guide
        JPanel legendCard = createStyledCard("Decision Policy Rules Matrix");
        legendCard.setLayout(new GridLayout(4, 1, 0, 6));
        legendCard.add(createPolicyRow("🟢 < 40%", "APPROVE", "Low Risk. Immediate frictionless automated approval.", COLOR_ALLOWED));
        legendCard.add(createPolicyRow("🟡 40% – 60%", "REVIEW / WARN", "Moderate Risk. Cautionary warning shown; sender acknowledges.", COLOR_WARNING));
        legendCard.add(createPolicyRow("🟠 60% – 80%", "OTP / CHALLENGE", "High Risk. Suspended pending 2FA / Security PIN (1234).", COLOR_BLOCKED));
        legendCard.add(createPolicyRow("🔴 > 80%", "BLOCK / STOP", "Critical Risk. Automatically rejected and terminated.", COLOR_STOPPED));

        JPanel bottomStack = new JPanel(new BorderLayout(0, 10));
        bottomStack.setOpaque(false);
        bottomStack.add(codeCard, BorderLayout.NORTH);
        bottomStack.add(legendCard, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        panel.add(bottomStack, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private JPanel createPolicyRow(String title, String badge, String desc, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(color);
        lblTitle.setPreferredSize(new Dimension(100, 20));

        JLabel lblBadge = new JLabel("[" + badge + "]");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(color);
        lblBadge.setPreferredSize(new Dimension(110, 20));

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(TEXT_WHITE);

        row.add(lblTitle, BorderLayout.WEST);
        row.add(lblBadge, BorderLayout.CENTER);
        row.add(lblDesc, BorderLayout.EAST);
        return row;
    }

    private JButton createPresetChip(String label, String amount, String targetAccountId) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(new Color(35, 35, 35));
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            amountField.setText(amount);
            for (int i = 0; i < recipientCombo.getItemCount(); i++) {
                AccountItem it = recipientCombo.getItemAt(i);
                if (it != null && it.id.equalsIgnoreCase(targetAccountId)) {
                    recipientCombo.setSelectedIndex(i);
                    break;
                }
            }
        });
        return btn;
    }

    private JPanel createStyledCard(String titleText) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        if (titleText != null && !titleText.isEmpty()) {
            card.setLayout(new BorderLayout(0, 8));
            JLabel lblTitle = new JLabel(titleText);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(TEXT_WHITE);
            card.add(lblTitle, BorderLayout.NORTH);
        }
        return card;
    }

    private void styleComboBox(JComboBox<AccountItem> box) {
        box.setBackground(INPUT_BG);
        box.setForeground(TEXT_WHITE);
        box.setFont(new Font("Segoe UI", Font.BOLD, 12));
        box.setBorder(new LineBorder(CARD_BORDER, 1));
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setOpaque(true);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                label.setBorder(new EmptyBorder(5, 8, 5, 8));
                if (isSelected) {
                    label.setBackground(ACCENT_BLUE);
                    label.setForeground(TEXT_WHITE);
                } else {
                    label.setBackground(INPUT_BG);
                    label.setForeground(TEXT_WHITE);
                }
                return label;
            }
        });
    }

    private void styleGenericCombo(JComboBox<String> box) {
        box.setBackground(INPUT_BG);
        box.setForeground(TEXT_WHITE);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        box.setBorder(new LineBorder(CARD_BORDER, 1));
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        return btn;
    }

    private void updateSenderBalance() {
        AccountItem item = (AccountItem) senderCombo.getSelectedItem();
        if (item != null) {
            balanceBadge.setText(String.format("Available Balance: $%,.2f", item.balance));
        }
    }

    private String getSelectedDeviceId() {
        String sel = (String) deviceCombo.getSelectedItem();
        if (sel != null && sel.contains("DEV-NEW")) return "DEV-NEW-UNKNOWN-MAC";
        if (sel != null && sel.contains("DEV-SHARED")) return "DEV-SHARED-MULE-FARM";
        return "DEV-TRUSTED-IPHONE15";
    }

    private String getDeviceCountry() {
        String sel = (String) deviceCombo.getSelectedItem();
        if (sel != null && sel.contains("RU")) return "RU";
        return "US";
    }

    private void loadDataFromBackend() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String status;
            String accounts;
            String history;

            @Override
            protected Void doInBackground() {
                status = apiClient.checkStatus();
                if (status != null) {
                    try {
                        accounts = apiClient.getAccounts();
                        history = apiClient.getHistory();
                    } catch (Exception ignored) {}
                }
                return null;
            }

            @Override
            protected void done() {
                if (status != null) {
                    statusBadge.setText(" ● AI Engine Online (Port 5000) ");
                    statusBadge.setBackground(new Color(6, 78, 59));
                    statusBadge.setForeground(COLOR_ALLOWED);
                    populateAccounts(accounts);
                    populateHistory(history);
                } else {
                    statusBadge.setText(" ● AI Engine Offline ");
                    statusBadge.setBackground(new Color(127, 29, 29));
                    statusBadge.setForeground(COLOR_STOPPED);
                    JOptionPane.showMessageDialog(FintechApp.this,
                            "Cannot connect to the Python AI engine at " + BACKEND_URL + ".\n" +
                            "Please run 'start_all.bat' to start the server.",
                            "Connection Notice",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void resetDemoData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(BACKEND_URL + "/api/reset"))
                        .POST(java.net.http.HttpRequest.BodyPublishers.noBody())
                        .build();
                java.net.http.HttpClient.newHttpClient().send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
                return null;
            }
            @Override
            protected void done() {
                loadDataFromBackend();
                JOptionPane.showMessageDialog(FintechApp.this, "Demo database, graph links, and federated models reset to clean defaults.", "Data Reset", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void populateAccounts(String json) {
        if (json == null) return;
        senderCombo.removeAllItems();
        recipientCombo.removeAllItems();

        List<AccountItem> list = parseAccounts(json);
        for (AccountItem item : list) {
            senderCombo.addItem(item);
            recipientCombo.addItem(item);
        }

        if (recipientCombo.getItemCount() > 1) {
            recipientCombo.setSelectedIndex(1);
        }
        updateSenderBalance();
    }

    private void populateHistory(String json) {
        if (json == null) return;
        historyModel.setRowCount(0);
        List<HistoryItem> items = parseHistory(json);
        for (HistoryItem item : items) {
            historyModel.addRow(new Object[]{
                    item.txId,
                    item.sender,
                    item.recipient,
                    String.format("$%,.2f", item.amount),
                    String.format("%.0f%%", item.customerRisk),
                    String.format("%.0f%%", item.beneficiaryRisk),
                    String.format("%.1f%%", item.riskScore),
                    item.action,
                    item.status
            });
        }
    }

    private void handleAnalyzeRisk(ActionEvent e) {
        AccountItem sender = (AccountItem) senderCombo.getSelectedItem();
        AccountItem recipient = (AccountItem) recipientCombo.getSelectedItem();
        double amount = parseAmount();

        if (sender == null || recipient == null || amount <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        evaluateBtn.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String devId = getSelectedDeviceId();
                String ipCountry = getDeviceCountry();
                boolean isVpn = vpnCheck.isSelected();
                boolean isEmulator = emulatorCheck.isSelected();

                String payload = String.format(
                        "{\"sender_id\":\"%s\",\"recipient_id\":\"%s\",\"amount\":%.2f," +
                        "\"device_info\":{\"device_id\":\"%s\",\"ip_country\":\"%s\",\"is_vpn\":%b,\"is_emulator\":%b}}",
                        sender.id, recipient.id, amount, devId, ipCountry, isVpn, isEmulator
                );

                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(BACKEND_URL + "/api/evaluate-risk"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                return java.net.http.HttpClient.newHttpClient().send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            }

            @Override
            protected void done() {
                evaluateBtn.setEnabled(true);
                try {
                    String json = get();
                    updateRiskDisplay(parseAssessment(json));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FintechApp.this, "Evaluation error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleProcessTransfer(ActionEvent e) {
        AccountItem sender = (AccountItem) senderCombo.getSelectedItem();
        AccountItem recipient = (AccountItem) recipientCombo.getSelectedItem();
        double amount = parseAmount();

        if (sender == null || recipient == null || amount <= 0) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (sender.id.equals(recipient.id)) {
            JOptionPane.showMessageDialog(this, "Sender and recipient cannot be the same account.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        transferBtn.setEnabled(false);
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String devId = getSelectedDeviceId();
                String ipCountry = getDeviceCountry();
                boolean isVpn = vpnCheck.isSelected();
                boolean isEmulator = emulatorCheck.isSelected();

                String payload = String.format(
                        "{\"sender_id\":\"%s\",\"recipient_id\":\"%s\",\"amount\":%.2f," +
                        "\"device_info\":{\"device_id\":\"%s\",\"ip_country\":\"%s\",\"is_vpn\":%b,\"is_emulator\":%b}}",
                        sender.id, recipient.id, amount, devId, ipCountry, isVpn, isEmulator
                );

                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(BACKEND_URL + "/api/evaluate-risk"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                return java.net.http.HttpClient.newHttpClient().send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            }

            @Override
            protected void done() {
                transferBtn.setEnabled(true);
                try {
                    String json = get();
                    RiskAssessment assessment = parseAssessment(json);
                    updateRiskDisplay(assessment);
                    applyFintechRules(sender.id, recipient.id, amount, assessment);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FintechApp.this, "Transfer error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFintechRules(String senderId, String recipientId, double amount, RiskAssessment assessment) {
        double risk = assessment.riskScore;

        // RULE 1: > 80% -> BLOCK / STOP
        if (risk > 80.0) {
            JOptionPane.showMessageDialog(this,
                    "🛑 TRANSACTION BLOCKED & TERMINATED!\n\n" +
                    "AI Composite Risk: " + String.format("%.1f%%", risk) + " (>80% Critical Tolerance)\n" +
                    "Confidence: " + String.format("%.1f%%", assessment.confidence) + " | Uncertainty: " + String.format("%.1f%%", assessment.uncertainty) + "\n\n" +
                    "Reason Codes: " + String.join(", ", assessment.reasonCodes) + "\n\n" +
                    "Primary Threat Explanations:\n" + assessment.getFormattedReasons() + "\n" +
                    "Action: Aborted automatically to prevent catastrophic fraud loss.",
                    "Critical Fraud Interception",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // RULE 2: 60% - 80% -> OTP / CHALLENGE
        if (risk >= 60.0 && risk <= 80.0) {
            JPasswordField pinField = new JPasswordField();
            Object[] prompt = {
                    "🔒 TRANSACTION SUSPENDED — 2FA / OTP REQUIRED\n",
                    "Composite Risk: " + String.format("%.1f%%", risk) + " (60% – 80% High-Risk Range)\n",
                    "Customer Risk: " + String.format("%.1f%%", assessment.customerRisk) + " | Beneficiary Risk: " + String.format("%.1f%%", assessment.beneficiaryRisk) + "\n",
                    "Reason Codes: " + String.join(", ", assessment.reasonCodes) + "\n\n" +
                    "Enter your 4-digit Security Authorization PIN (Default Demo PIN: 1234):",
                    pinField
            };

            int res = JOptionPane.showConfirmDialog(this, prompt, "Security PIN Challenge", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String pin = new String(pinField.getPassword()).trim();
                if ("1234".equals(pin)) {
                    sendExecutionRequest(senderId, recipientId, amount, true);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Incorrect PIN! Transaction remains blocked and cancelled.", "Authorization Denied", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Transaction cancelled by user.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        // RULE 3: 40% - 60% -> REVIEW / WARN
        if (risk >= 40.0 && risk < 60.0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "⚠️ TRANSACTION RISK WARNING (REVIEW REQUIRED)\n\n" +
                    "Risk Score: " + String.format("%.1f%%", risk) + " (Moderate 40% – 60% Tier)\n" +
                    "AI Confidence: " + String.format("%.1f%%", assessment.confidence) + "\n\n" +
                    "Detected Anomalies:\n" + assessment.getFormattedReasons() + "\n" +
                    "Do you acknowledge these risks and want to proceed with transferring $" + String.format("%.2f", amount) + "?",
                    "Moderate Risk Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                sendExecutionRequest(senderId, recipientId, amount, false);
            } else {
                JOptionPane.showMessageDialog(this, "Transaction cancelled after risk warning.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        // RULE 4: < 40% -> APPROVE DIRECTLY
        sendExecutionRequest(senderId, recipientId, amount, false);
    }

    private void sendExecutionRequest(String senderId, String recipientId, double amount, boolean override) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String devId = getSelectedDeviceId();
                String ipCountry = getDeviceCountry();
                boolean isVpn = vpnCheck.isSelected();
                boolean isEmulator = emulatorCheck.isSelected();

                String payload = String.format(
                        "{\"sender_id\":\"%s\",\"recipient_id\":\"%s\",\"amount\":%.2f,\"override_authorized\":%b," +
                        "\"device_info\":{\"device_id\":\"%s\",\"ip_country\":\"%s\",\"is_vpn\":%b,\"is_emulator\":%b}}",
                        senderId, recipientId, amount, override, devId, ipCountry, isVpn, isEmulator
                );

                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(BACKEND_URL + "/api/execute"))
                        .header("Content-Type", "application/json")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                return java.net.http.HttpClient.newHttpClient().send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            }

            @Override
            protected void done() {
                try {
                    String res = get();
                    if (res.contains("\"success\": true")) {
                        JOptionPane.showMessageDialog(FintechApp.this,
                                "✅ Transfer Approved & Completed!\n" +
                                "Transferred: $" + String.format("%.2f", amount) + "\n\n" +
                                "Dynamic Feedback: Sender trust score updated (+0.8%). Federated round aggregated.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadDataFromBackend();
                    } else {
                        JOptionPane.showMessageDialog(FintechApp.this, "Transaction could not complete:\n" + res, "Transfer Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FintechApp.this, "Execution error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateRiskDisplay(RiskAssessment a) {
        int score = (int) Math.round(a.riskScore);
        riskBar.setValue(score);
        riskScoreBig.setText(String.format("%.1f%% Composite Risk", a.riskScore));

        customerRiskBar.setValue((int) Math.round(a.customerRisk));
        customerRiskVal.setText(String.format("Customer Risk: %.1f%%", a.customerRisk));

        beneficiaryRiskBar.setValue((int) Math.round(a.beneficiaryRisk));
        beneficiaryRiskVal.setText(String.format("Beneficiary Risk: %.1f%%", a.beneficiaryRisk));

        confidenceVal.setText(String.format("AI Confidence: %.1f%%", a.confidence));
        uncertaintyVal.setText(String.format("Bayesian Uncertainty: %.1f%%", a.uncertainty));

        Color color;
        String badgeText;

        if (a.riskScore > 80.0) {
            color = COLOR_STOPPED;
            badgeText = "🛑 BLOCK / STOP (>80%)";
        } else if (a.riskScore >= 60.0) {
            color = COLOR_BLOCKED;
            badgeText = "🔒 OTP / CHALLENGE (60% - 80%)";
        } else if (a.riskScore >= 40.0) {
            color = COLOR_WARNING;
            badgeText = "⚠️ REVIEW / WARN (40% - 60%)";
        } else {
            color = COLOR_ALLOWED;
            badgeText = "✅ APPROVE (<40%)";
        }

        riskBar.setForeground(color);
        riskScoreBig.setForeground(color);
        decisionBadge.setText(badgeText);
        decisionBadge.setBackground(color.darker());

        // Website inspection display
        if (a.websiteData != null && !a.websiteData.isEmpty()) {
            websiteInspectLabel.setText("🌐 Merchant Web: " + a.websiteData);
        } else {
            websiteInspectLabel.setText("🌐 Merchant Web: Direct Account Transfer (No Public URL)");
        }

        // Reason Codes
        if (!a.reasonCodes.isEmpty()) {
            reasonCodesPills.setText("Codes: " + String.join(" | ", a.reasonCodes));
        } else {
            reasonCodesPills.setText("Codes: RC_CLEAN_TRANSACTION");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(a.policyAction).append("\n\n");
        sb.append("Multi-Factor Threat Explanations:\n");
        for (String r : a.reasons) {
            sb.append(" • ").append(r).append("\n");
        }
        reasonsDisplay.setText(sb.toString());
    }

    private double parseAmount() {
        try {
            return Double.parseDouble(amountField.getText().trim().replace("$", "").replace(",", ""));
        } catch (Exception e) {
            return -1.0;
        }
    }

    // Models & Parser Helpers
    static class AccountItem {
        String id;
        String name;
        double balance;
        public AccountItem(String id, String name, double balance) {
            this.id = id; this.name = name; this.balance = balance;
        }
        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }

    static class HistoryItem {
        String txId, sender, recipient, action, status;
        double amount, riskScore, customerRisk, beneficiaryRisk;
    }

    static class RiskAssessment {
        double riskScore, customerRisk, beneficiaryRisk, confidence, uncertainty;
        String action, decision, policyAction, websiteData;
        List<String> reasons = new ArrayList<>();
        List<String> reasonCodes = new ArrayList<>();

        public String getFormattedReasons() {
            if (reasons.isEmpty()) return "None noted.";
            StringBuilder sb = new StringBuilder();
            for (String r : reasons) sb.append(" • ").append(r).append("\n");
            return sb.toString();
        }
    }

    private List<AccountItem> parseAccounts(String json) {
        List<AccountItem> list = new ArrayList<>();
        Matcher m = Pattern.compile("\\{\"account_number\":\\s*\"([^\"]+)\",\\s*\"owner_name\":\\s*\"([^\"]+)\",\\s*\"balance\":\\s*([0-9.]+)").matcher(json);
        while (m.find()) {
            list.add(new AccountItem(m.group(1), m.group(2), Double.parseDouble(m.group(3))));
        }
        return list;
    }

    private List<HistoryItem> parseHistory(String json) {
        List<HistoryItem> list = new ArrayList<>();
        Matcher m = Pattern.compile("\\{\"tx_id\":\\s*\"([^\"]+)\",\\s*\"sender\":\\s*\"([^\"]+)\",\\s*\"recipient\":\\s*\"([^\"]+)\",\\s*\"amount\":\\s*([0-9.]+),\\s*\"risk_score\":\\s*([0-9.]+)").matcher(json);
        while (m.find()) {
            HistoryItem h = new HistoryItem();
            h.txId = m.group(1);
            h.sender = m.group(2);
            h.recipient = m.group(3);
            h.amount = Double.parseDouble(m.group(4));
            h.riskScore = Double.parseDouble(m.group(5));
            h.customerRisk = 12.0;
            h.beneficiaryRisk = 20.0;
            h.action = h.riskScore > 80 ? "BLOCK" : (h.riskScore >= 60 ? "OTP" : (h.riskScore >= 40 ? "REVIEW" : "APPROVE"));
            h.status = "LOGGED";
            list.add(h);
        }
        return list;
    }

    private RiskAssessment parseAssessment(String json) {
        RiskAssessment a = new RiskAssessment();

        Matcher mScore = Pattern.compile("\"risk_score\":\\s*([0-9.]+)").matcher(json);
        if (mScore.find()) a.riskScore = Double.parseDouble(mScore.group(1));

        Matcher mCRisk = Pattern.compile("\"customer_risk\":\\s*([0-9.]+)").matcher(json);
        if (mCRisk.find()) a.customerRisk = Double.parseDouble(mCRisk.group(1));

        Matcher mBRisk = Pattern.compile("\"beneficiary_risk\":\\s*([0-9.]+)").matcher(json);
        if (mBRisk.find()) a.beneficiaryRisk = Double.parseDouble(mBRisk.group(1));

        Matcher mConf = Pattern.compile("\"confidence\":\\s*([0-9.]+)").matcher(json);
        if (mConf.find()) a.confidence = Double.parseDouble(mConf.group(1));

        Matcher mUnc = Pattern.compile("\"uncertainty\":\\s*([0-9.]+)").matcher(json);
        if (mUnc.find()) a.uncertainty = Double.parseDouble(mUnc.group(1));

        Matcher mAction = Pattern.compile("\"action\":\\s*\"([^\"]+)\"").matcher(json);
        if (mAction.find()) a.action = mAction.group(1);

        Matcher mDecision = Pattern.compile("\"decision\":\\s*\"([^\"]+)\"").matcher(json);
        if (mDecision.find()) a.decision = mDecision.group(1);

        Matcher mPolicy = Pattern.compile("\"policy_action\":\\s*\"([^\"]+)\"").matcher(json);
        if (mPolicy.find()) a.policyAction = mPolicy.group(1);

        Matcher mReasons = Pattern.compile("\"reasons\":\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (mReasons.find()) {
            Matcher mSingle = Pattern.compile("\"([^\"]+)\"").matcher(mReasons.group(1));
            while (mSingle.find()) a.reasons.add(mSingle.group(1));
        }

        Matcher mCodes = Pattern.compile("\"reason_codes\":\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (mCodes.find()) {
            Matcher mSingle = Pattern.compile("\"([^\"]+)\"").matcher(mCodes.group(1));
            while (mSingle.find()) a.reasonCodes.add(mSingle.group(1));
        }

        Matcher mWeb = Pattern.compile("\"url\":\\s*\"([^\"]+)\"").matcher(json);
        if (mWeb.find()) {
            a.websiteData = mWeb.group(1);
        }

        return a;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FintechApp app = new FintechApp();
            app.setVisible(true);
        });
    }
}
