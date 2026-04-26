package Boundary;

import Control.FinancialController;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public class FinancialReportsPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_LEFT_TOP  = new Color(15,  17,  20);
    private static final Color C_LEFT_BOT  = new Color(26,  29,  34);
    private static final Color C_RIGHT     = new Color(245, 242, 236);
    private static final Color C_GOLD      = new Color(188, 152, 90);
    private static final Color C_IVORY     = new Color(250, 247, 241);
    private static final Color C_CHARCOAL  = new Color(35,  36,  40);
    private static final Color C_MUTED     = new Color(110, 106, 98);
    private static final Color C_CARD_BG   = new Color(237, 233, 224);
    private static final Color C_FIELD_BG  = new Color(228, 223, 213);
    private static final Color C_TBL_HDR   = new Color(48,  42,  32);
    private static final Color C_TBL_ROW   = new Color(240, 236, 227);
    private static final Color C_TBL_ALT   = new Color(233, 228, 218);
    private static final Color C_SEL       = new Color(188, 152, 90, 80);
    private static final Color C_REVENUE   = new Color(60,  130, 90);
    private static final Color C_EXPENSE   = new Color(160, 90,  60);
    private static final Color C_PROFIT    = new Color(60,  100, 160);
    private static final Color C_MARGIN    = new Color(188, 152, 90);

    private final DecimalFormat cf = new DecimalFormat("₪#,##0.00");
    private final DecimalFormat pf = new DecimalFormat("#0.0%");

    private JTable revenueTable, expenseTable;
    private DefaultTableModel revenueModel, expenseModel;
    private JComboBox<String> cbPeriod;
    private JLabel lblRevenue, lblExpenses, lblProfit, lblMargin;
    private JButton btnRefresh, btnBack;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    public FinancialReportsPage() {
        setTitle("Financial Reports — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        // Left panel
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(280, 0); }
        };
        left.setBackground(C_LEFT_TOP);
        root.add(left, BorderLayout.WEST);

        // Divider
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,30,new Color(188,152,90,0),0,getHeight()*.5f,new Color(188,152,90,150),false));
                g2.fillRect(0,0,1,getHeight()); g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(1,0); }
        };
        div.setOpaque(false);
        root.add(div, BorderLayout.CENTER);

        // Right panel
        JPanel right = buildRight();
        right.setPreferredSize(new Dimension(828, 660));
        root.add(right, BorderLayout.EAST);

        // Timers
        Timer fade = new Timer(16, e -> { alpha = Math.min(1f, alpha + 0.025f); left.repaint(); if (alpha >= 1f) ((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse += 0.04f * pDir; if (pulse > 1f) { pulse = 1f; pDir = -1; } if (pulse < -1f) { pulse = -1f; pDir = 1; } left.repaint(); });
        pt.start();

        loadData();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx = w/2, cy = h/2 - 70;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,140,new float[]{0f,1f},new Color[]{new Color(188,152,90,22),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for (int x=0;x<w;x+=22) g2.drawLine(x,0,x,h);
        for (int y=0;y<h;y+=22) g2.drawLine(0,y,w,y);

        float r = 52f + pulse * 2f;
        paintEmblem(g2, cx, cy, r);

        // Brand
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,19));
        FontMetrics fm = g2.getFontMetrics(); String br = "DentalCare";
        g2.drawString(br, cx-fm.stringWidth(br)/2, cy+(int)r+30);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry = cy+(int)r+42; g2.drawLine(cx-60,ry,cx+60,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm = g2.getFontMetrics();
        String sub = "Financial Reports"; g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // KPI cards — 4 mini cards stacked
        int[][] kpiColors = {{60,130,90},{160,90,60},{60,100,160},{188,152,90}};
        String[] kpiLabels = {"Revenue","Expenses","Net Profit","Margin"};
        String[] kpiVals = {
            lblRevenue==null?"₪0":lblRevenue.getText(),
            lblExpenses==null?"₪0":lblExpenses.getText(),
            lblProfit==null?"₪0":lblProfit.getText(),
            lblMargin==null?"0%":lblMargin.getText()
        };
        int cardW = 160, cardH = 34, cardX = cx - cardW/2, startY = ry + 32;
        for (int i = 0; i < 4; i++) {
            Color c = new Color(kpiColors[i][0], kpiColors[i][1], kpiColors[i][2]);
            int cardY = startY + i * (cardH + 6);
            g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22));
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 5, 5);
            g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),70));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, 5, 5);

            g2.setColor(new Color(140,134,122)); g2.setFont(new Font("Georgia",Font.ITALIC,10));
            fm = g2.getFontMetrics(); g2.drawString(kpiLabels[i], cardX+10, cardY+14);

            g2.setColor(c); g2.setFont(new Font("Georgia",Font.BOLD,12));
            fm = g2.getFontMetrics(); String val = kpiVals[i];
            g2.drawString(val, cardX+cardW-fm.stringWidth(val)-8, cardY+24);
        }

        // Period label
        String period = cbPeriod == null ? "" : "Period: "+(String)cbPeriod.getSelectedItem();
        g2.setColor(new Color(80,76,70)); g2.setFont(new Font("Georgia",Font.ITALIC,10));
        fm = g2.getFontMetrics(); g2.drawString(period, cx-fm.stringWidth(period)/2, startY+4*(cardH+6)+10);

        // Copyright
        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10));
        fm = g2.getFontMetrics(); String copy = "© DentalCare System";
        g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188,152,90,40)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));
        g2.setColor(new Color(188,152,90,85)); g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},
                new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)}));
        g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        float arm=r*.42f, th=r*.18f; g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx-th/2,cy-arm,th,arm*2,3,3));
        g2.fill(new RoundRectangle2D.Float(cx-arm,cy-th/2,arm*2,th,3,3));
        g2.setColor(new Color(188,152,90,95)); g2.setStroke(new BasicStroke(1f));
        for (int i=0;i<12;i++) {
            double a=i*Math.PI/6-Math.PI/2; float in=r+3,out=r+(i%3==0?10:7);
            g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),(int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));
        }
        g2.setColor(new Color(188,152,90,120)); float dd=r*.63f,dr=r*.07f;
        for (int i=0;i<4;i++) {
            double a=Math.PI/4+i*Math.PI/2;
            g2.fill(new Ellipse2D.Float((float)(cx+Math.cos(a)*dd)-dr,(float)(cy+Math.sin(a)*dd)-dr,dr*2,dr*2));
        }
    }

    // ── Right panel ──────────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar
        JPanel top = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(24,getHeight()-1,getWidth()-24,getHeight()-1); g2.dispose();
            }
        };
        top.setOpaque(false); top.setBorder(new EmptyBorder(14,26,11,26));

        JLabel tl = new JLabel("Financial Reports & Analytics");
        tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl = new JLabel("Revenue, expenses & profitability overview");
        sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts = new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false);
        ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts, BorderLayout.WEST);

        // Period selector
        JPanel pr = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); pr.setOpaque(false);
        JLabel pl = new JLabel("Period:"); pl.setFont(new Font("Georgia",Font.BOLD,12)); pl.setForeground(C_CHARCOAL);
        cbPeriod = new JComboBox<>(new String[]{"This Month","Last Month","Last 3 Months","Last 6 Months","This Year","Last Year"});
        cbPeriod.setFont(new Font("Georgia",Font.PLAIN,12)); cbPeriod.setBackground(C_FIELD_BG); cbPeriod.setForeground(C_CHARCOAL);
        cbPeriod.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
        cbPeriod.addActionListener(e -> loadData());
        pr.add(pl); pr.add(cbPeriod);
        top.add(pr, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // Summary row + tables
        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS)); body.setBackground(C_RIGHT);
        body.setBorder(new EmptyBorder(10,26,8,26));
        body.add(buildSummaryRow());
        body.add(Box.createRigidArea(new Dimension(0,14)));
        body.add(buildTablesRow());
        p.add(body, BorderLayout.CENTER);

        // Button bar
        p.add(buildButtonBar(), BorderLayout.SOUTH);
        return p;
    }

    // ── Summary row ──────────────────────────────────────────────────────────
    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1,4,12,0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,64));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] titles = {"Total Revenue","Total Expenses","Net Profit","Profit Margin"};
        Color[] colors   = {C_REVENUE, C_EXPENSE, C_PROFIT, C_MARGIN};
        String[] defaults= {"₪0.00","₪0.00","₪0.00","0.0%"};
        JLabel[] refs    = new JLabel[4];

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JPanel card = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(C_CARD_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(new Color(colors[idx].getRed(),colors[idx].getGreen(),colors[idx].getBlue(),55));
                    g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    // Left accent stripe
                    g2.setColor(new Color(colors[idx].getRed(),colors[idx].getGreen(),colors[idx].getBlue(),140));
                    g2.fillRoundRect(0,0,3,getHeight(),2,2); g2.dispose();
                }
            };
            card.setOpaque(false); card.setLayout(new BorderLayout()); card.setBorder(new EmptyBorder(8,14,8,12));
            JLabel lbl = new JLabel(titles[i]); lbl.setFont(new Font("Georgia",Font.ITALIC,11)); lbl.setForeground(C_MUTED);
            JLabel val = new JLabel(defaults[i]); val.setFont(new Font("Georgia",Font.BOLD,18)); val.setForeground(colors[i]);
            refs[i] = val;
            JPanel vp = new JPanel(new BorderLayout()); vp.setOpaque(false); vp.add(lbl,BorderLayout.NORTH); vp.add(val,BorderLayout.CENTER);
            card.add(vp, BorderLayout.CENTER); row.add(card);
        }
        lblRevenue  = refs[0]; lblExpenses = refs[1]; lblProfit = refs[2]; lblMargin = refs[3];
        return row;
    }

    // ── Tables row ───────────────────────────────────────────────────────────
    private JPanel buildTablesRow() {
        JPanel row = new JPanel(new GridLayout(1,2,14,0));
        row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Revenue table
        revenueModel = new DefaultTableModel(new String[]{"Service","Qty","Rate","Total"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        revenueTable = buildTable(revenueModel, C_REVENUE);
        row.add(buildTableCard("Revenue Breakdown", revenueTable, C_REVENUE));

        // Expense table
        expenseModel = new DefaultTableModel(new String[]{"Category","Description","Amount","Date"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        expenseTable = buildTable(expenseModel, C_EXPENSE);
        row.add(buildTableCard("Expense Breakdown", expenseTable, C_EXPENSE));

        return row;
    }

    private JTable buildTable(DefaultTableModel model, Color accent) {
        JTable t = new JTable(model);
        t.setFont(new Font("Georgia",Font.PLAIN,12)); t.setRowHeight(28); t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setBackground(C_TBL_ROW); t.setForeground(C_CHARCOAL); t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader hdr = t.getTableHeader();
        hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR); hdr.setForeground(new Color(215,185,120));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean foc,int row,int col) {
                super.getTableCellRendererComponent(tbl,v,sel,foc,row,col);
                setBorder(new EmptyBorder(0,8,0,6));
                if (sel) { setBackground(C_SEL); setForeground(C_CHARCOAL); }
                else {
                    setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    // Last col gets accent color (Total / Amount)
                    if (col==tbl.getColumnCount()-1) { setForeground(accent); setHorizontalAlignment(RIGHT); }
                    else { setForeground(C_CHARCOAL); setHorizontalAlignment(LEFT); }
                }
                return this;
            }
        });
        return t;
    }

    private JPanel buildTableCard(String title, JTable t, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),50));
                g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(12,14,12,14));

        JPanel titleRow = new JPanel(new BorderLayout()); titleRow.setOpaque(false); titleRow.setBorder(new EmptyBorder(0,0,8,0));
        JLabel lbl = new JLabel(title); lbl.setFont(new Font("Georgia",Font.BOLD,14)); lbl.setForeground(accent);
        JPanel rule = new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(0,8));
        titleRow.add(lbl,BorderLayout.NORTH); titleRow.add(rule,BorderLayout.SOUTH);
        card.add(titleRow, BorderLayout.NORTH);

        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createLineBorder(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),50),1));
        sc.getViewport().setBackground(C_TBL_ROW);
        card.add(sc, BorderLayout.CENTER);
        return card;
    }

    // ── Button bar ───────────────────────────────────────────────────────────
    private JPanel buildButtonBar() {
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(26,0,getWidth()-26,0); g2.dispose();
            }
        };
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,26,12,26));

        JPanel lb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lb.setOpaque(false);

        btnRefresh = mkBtn("Refresh Data", false, null);
        btnRefresh.addActionListener(e -> {
            loadData();
            JOptionPane.showMessageDialog(
                    this,
                    "Data refreshed for: " + cbPeriod.getSelectedItem(),
                    "Refreshed",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        lb.add(btnRefresh);
        JPanel rb = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        btnBack = mkBtn("← Dashboard", false, null);
        btnBack.addActionListener(e -> goBack());
        rb.add(btnBack);

        bar.add(lb, BorderLayout.WEST); bar.add(rb, BorderLayout.EAST);
        return bar;
    }

    private JButton mkBtn(String text, boolean primary, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        if (primary) {
            btn.setBackground(new Color(35,27,12)); btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(7,18,7,18)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});
        } else {
            btn.setBackground(C_CARD_BG); btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(7,16,7,16)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(7,16,7,16)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(7,16,7,16)));}});
        }
        return btn;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadData() {
        if (revenueModel == null) return;
        revenueModel.setRowCount(0); expenseModel.setRowCount(0);
        String period = cbPeriod == null ? "This Month" : (String)cbPeriod.getSelectedItem();
        try {
            List<FinancialController.RevenueData> rev = FinancialController.getRevenueData(period);
            if (rev.isEmpty()) revenueModel.addRow(new Object[]{"No data","0","₪0","₪0"});
            else for (var r : rev) revenueModel.addRow(new Object[]{r.serviceName,r.quantity,cf.format(r.rate),cf.format(r.total)});
            List<FinancialController.ExpenseData> exp = FinancialController.getExpenseData(period);
            if (exp.isEmpty()) expenseModel.addRow(new Object[]{"No data","—","₪0",LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))});
            else for (var e : exp) expenseModel.addRow(new Object[]{e.category,e.description,cf.format(e.amount),e.date});
        } catch (Exception e) {
            revenueModel.addRow(new Object[]{"Error","","",""+e.getMessage()});
        }
        updateSummary();
        repaint(); // refresh left panel KPI cards
    }

    private void updateSummary() {
        double rev = calcTotal(revenueModel,3), exp = calcTotal(expenseModel,2);
        double profit = rev - exp, margin = rev > 0 ? profit/rev : 0;
        if (lblRevenue  != null) lblRevenue.setText(cf.format(rev));
        if (lblExpenses != null) lblExpenses.setText(cf.format(exp));
        if (lblProfit   != null) { lblProfit.setText(cf.format(profit)); lblProfit.setForeground(profit>=0?C_PROFIT:C_EXPENSE); }
        if (lblMargin   != null) lblMargin.setText(pf.format(margin));
        repaint();
    }

    private double calcTotal(DefaultTableModel m, int col) {
        double t=0;
        for (int i=0;i<m.getRowCount();i++) try{t+=Double.parseDouble(m.getValueAt(i,col).toString().replace("₪","").replace(",",""));}catch(Exception ignored){}
        return t;
    }

    // ── Export ───────────────────────────────────────────────────────────────
    private void exportReport() {
        JDialog loading = createLoadingDialog(); loading.setVisible(true);
        new SwingWorker<Void,Void>(){
            protected Void doInBackground() throws Exception { generateJasperReport(); return null; }
            protected void done() { loading.dispose(); try{get();}catch(Exception e){JOptionPane.showMessageDialog(FinancialReportsPage.this,"Export failed: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);} }
        }.execute();
    }

    private void generateJasperReport() throws Exception {
        String path="/Blank_A4.jasper";
        if (!new java.io.File(path).exists()){JOptionPane.showMessageDialog(this,"Report file not found: "+path,"Missing",JOptionPane.WARNING_MESSAGE);return;}
        JasperReport jr=(JasperReport)JRLoader.loadObjectFromFile(path);
        Map<String,Object> params=new HashMap<>(); params.put("GeneratedBy","Clinic Manager"); params.put("ReportTitle","Financial Report");
        Connection conn=DriverManager.getConnection("jdbc:ucanaccess://database/dentalcare.accdb","","");
        JasperPrint jp=JasperFillManager.fillReport(jr,params,conn); conn.close();
        JasperViewer v=new JasperViewer(jp,false); v.setTitle("Financial Report"); v.setVisible(true);
    }

    private JDialog createLoadingDialog() {
        JDialog dlg=new JDialog(this,"Generating...",true); dlg.setSize(280,130); dlg.setLocationRelativeTo(this);
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_RIGHT); p.setBorder(new EmptyBorder(24,28,24,28));
        JLabel l=new JLabel("Generating report…"); l.setFont(new Font("Georgia",Font.ITALIC,13)); l.setForeground(C_CHARCOAL);
        JProgressBar pb=new JProgressBar(); pb.setIndeterminate(true); pb.setForeground(C_GOLD); pb.setBackground(C_FIELD_BG);
        p.add(l,BorderLayout.NORTH); p.add(Box.createRigidArea(new Dimension(0,12)),BorderLayout.CENTER); p.add(pb,BorderLayout.SOUTH);
        dlg.add(p); return dlg;
    }

    private void goBack() { dispose(); try{new ClinicManagerPage().setVisible(true);}catch(Exception ignored){} }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinancialReportsPage().setVisible(true));
    }
}