package Boundary;

import Entity.Patient;
import Entity.Invoice;
import Control.BillingController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Timer;

public class BillingPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_LEFT_TOP  = new Color(15,  17,  20);
    private static final Color C_LEFT_BOT  = new Color(26,  29,  34);
    private static final Color C_RIGHT     = new Color(245, 242, 236);
    private static final Color C_GOLD      = new Color(188, 152, 90);
    private static final Color C_IVORY     = new Color(250, 247, 241);
    private static final Color C_CHARCOAL  = new Color(35,  36,  40);
    private static final Color C_MUTED     = new Color(110, 106, 98);
    private static final Color C_CARD_BG   = new Color(237, 233, 224);
    private static final Color C_TBL_HDR   = new Color(48,  42,  32);
    private static final Color C_TBL_ROW   = new Color(240, 236, 227);
    private static final Color C_TBL_ALT   = new Color(233, 228, 218);
    private static final Color C_SEL       = new Color(188, 152, 90, 80);
    private static final Color C_PAID      = new Color(60,  130, 90);
    private static final Color C_PENDING   = new Color(170, 130, 50);
    private static final Color C_OVERDUE   = new Color(160, 60,  50);
    private static final Color C_REFUNDED  = new Color(60,  100, 160);

    private final Patient currentPatient;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblOwed, lblPaid, lblRefunds;
    private JButton btnView, btnPay, btnHistory, btnReceipt, btnBack;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] COLS = {"Invoice #","Date","Description","Amount","Status","Due Date"};

    public BillingPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("Payment & Billing — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        // Left painted panel
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(270, 0); }
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

        // Right
        JPanel right = buildRight();
        right.setPreferredSize(new Dimension(828, 660));
        root.add(right, BorderLayout.EAST);

        // Timers
        Timer fade = new Timer(16, e -> { alpha = Math.min(1f, alpha+0.025f); left.repaint(); if (alpha>=1f) ((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadInvoices();
        updateSummary();
        updateButtons();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx = w/2, cy = h/2 - 65;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,135,new float[]{0f,1f},new Color[]{new Color(188,152,90,22),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r = 52f + pulse*2f;
        paintEmblem(g2, cx, cy, r);

        // Brand
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,19));
        FontMetrics fm = g2.getFontMetrics(); String br = "DentalCare";
        g2.drawString(br, cx-fm.stringWidth(br)/2, cy+(int)r+30);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry = cy+(int)r+42; g2.drawLine(cx-58,ry,cx+58,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm = g2.getFontMetrics();
        String sub = "Payment & Billing";
        g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // Patient box
        int bx=cx-80, by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(160,153,140)); g2.setFont(new Font("Georgia",Font.ITALIC,10)); fm=g2.getFontMetrics();
        String fl="Billing for"; g2.drawString(fl, cx-fm.stringWidth(fl)/2, by+14);
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,13)); fm=g2.getFontMetrics();
        String name=currentPatient.getFullName(); while(fm.stringWidth(name)>148&&name.length()>4)name=name.substring(0,name.length()-4)+"…";
        g2.drawString(name, cx-fm.stringWidth(name)/2, by+31);
        g2.setColor(new Color(90,86,80)); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String id="ID: "+currentPatient.getPatientID(); g2.drawString(id, cx-fm.stringWidth(id)/2, by+46);

        // 3 KPI mini cards
        Color[]kc = {C_OVERDUE, C_PAID, C_REFUNDED};
        String[]kl = {"Outstanding","Total Paid","Refunds"};
        String[]kv = {lblOwed==null?"$0":lblOwed.getText(), lblPaid==null?"$0":lblPaid.getText(), lblRefunds==null?"$0":lblRefunds.getText()};
        int cw=74, ch=38, startX=cx-cw-4, ky=by+62;
        for(int i=0;i<3;i++){
            int kx=startX+i*(cw+4);
            g2.setColor(new Color(kc[i].getRed(),kc[i].getGreen(),kc[i].getBlue(),22)); g2.fillRoundRect(kx,ky,cw,ch,5,5);
            g2.setColor(new Color(kc[i].getRed(),kc[i].getGreen(),kc[i].getBlue(),65)); g2.setStroke(new BasicStroke(0.6f)); g2.drawRoundRect(kx,ky,cw,ch,5,5);
            g2.setColor(kc[i]); g2.setFont(new Font("Georgia",Font.BOLD,10)); fm=g2.getFontMetrics();
            String v=kv[i]; if(fm.stringWidth(v)>cw-4){v=v.substring(0,Math.min(v.length(),7))+"…";}
            g2.drawString(v, kx+(cw-fm.stringWidth(v))/2, ky+18);
            g2.setColor(new Color(120,114,104)); g2.setFont(new Font("Georgia",Font.ITALIC,8)); fm=g2.getFontMetrics();
            g2.drawString(kl[i], kx+(cw-fm.stringWidth(kl[i]))/2, ky+30);
        }

        // Payment methods note
        int ny=ky+ch+12;
        g2.setColor(new Color(65,62,58)); g2.setFont(new Font("Georgia",Font.ITALIC,9)); fm=g2.getFontMetrics();
        String pm="Cards · Transfer · Cash"; g2.drawString(pm, cx-fm.stringWidth(pm)/2, ny);
        g2.drawString("Secure & encrypted", cx-fm.stringWidth("Secure & encrypted")/2, ny+12);

        // Copyright
        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188,152,90,40)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));
        g2.setColor(new Color(188,152,90,85)); g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},
                new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)}));
        g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        float arm=r*.42f,th=r*.18f; g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx-th/2,cy-arm,th,arm*2,3,3));
        g2.fill(new RoundRectangle2D.Float(cx-arm,cy-th/2,arm*2,th,3,3));
        g2.setColor(new Color(188,152,90,95)); g2.setStroke(new BasicStroke(1f));
        for(int i=0;i<12;i++){double a=i*Math.PI/6-Math.PI/2;float in=r+3,out=r+(i%3==0?10:7);
            g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),(int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));}
        g2.setColor(new Color(188,152,90,120)); float dd=r*.63f,dr=r*.07f;
        for(int i=0;i<4;i++){double a=Math.PI/4+i*Math.PI/2;
            g2.fill(new Ellipse2D.Float((float)(cx+Math.cos(a)*dd)-dr,(float)(cy+Math.sin(a)*dd)-dr,dr*2,dr*2));}
    }

    // ── Right panel ──────────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar
        JPanel top = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(26,getHeight()-1,getWidth()-26,getHeight()-1); g2.dispose(); }
        };
        top.setOpaque(false); top.setBorder(new EmptyBorder(14,26,11,26));
        JLabel tl=new JLabel("Payment & Billing"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel(currentPatient.getFullName()+" · ID "+currentPatient.getPatientID()); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false);
        ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts,BorderLayout.WEST);

        // Summary pills top-right
        JPanel pills=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); pills.setOpaque(false);
        lblOwed   =new JLabel("$0"); lblPaid=new JLabel("$0"); lblRefunds=new JLabel("$0");
        pills.add(makePill("Outstanding",lblOwed,C_OVERDUE));
        pills.add(makePill("Paid",lblPaid,C_PAID));
        pills.add(makePill("Refunds",lblRefunds,C_REFUNDED));
        top.add(pills,BorderLayout.EAST);
        p.add(top,BorderLayout.NORTH);

        // Table
        p.add(buildTablePanel(),BorderLayout.CENTER);

        // Button bar
        p.add(buildButtonBar(),BorderLayout.SOUTH);
        return p;
    }

    private JPanel makePill(String label, JLabel val, Color c) {
        JPanel pill=new JPanel(){@Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
            g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),75)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); g2.dispose();}};
        pill.setLayout(new BoxLayout(pill,BoxLayout.X_AXIS)); pill.setOpaque(false); pill.setBorder(new EmptyBorder(3,10,3,10));
        JLabel lbl=new JLabel(label+" "); lbl.setFont(new Font("Georgia",Font.ITALIC,11)); lbl.setForeground(C_MUTED);
        val.setFont(new Font("Georgia",Font.BOLD,12)); val.setForeground(c);
        pill.add(lbl); pill.add(val); return pill;
    }

    private JPanel buildTablePanel() {
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.setBorder(new EmptyBorder(8,26,6,26));
        tableModel=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,12)); table.setRowHeight(32); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,9,0,6));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==4&&v!=null){String s=v.toString();
                        if(s.equals("Paid"))setForeground(C_PAID);else if(s.equals("Pending"))setForeground(C_PENDING);
                        else if(s.equals("Overdue"))setForeground(C_OVERDUE);else if(s.equals("Refunded"))setForeground(C_REFUNDED);
                        else setForeground(C_CHARCOAL);}
                    else if(col==3){setForeground(C_GOLD);setHorizontalAlignment(RIGHT);}
                    else{setForeground(col==0?C_GOLD:C_CHARCOAL);setHorizontalAlignment(col==0?CENTER:LEFT);}
                } return this;}
        });
        int[]widths={100,80,210,90,80,90}; for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting())updateButtons(); });
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    private JPanel buildButtonBar() {
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(26,0,getWidth()-26,0); g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,26,12,26));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); lb.setOpaque(false);
        btnView    =mkBtn("View Details",    false, null);
        btnPay     =mkBtn("Make Payment",    true,  null);
        btnHistory =mkBtn("Payment History", false, C_REFUNDED);
        btnReceipt =mkBtn("Download Receipt",false, null);
        btnView.addActionListener(this::viewDetails); btnPay.addActionListener(this::makePayment);
        btnHistory.addActionListener(this::viewHistory); btnReceipt.addActionListener(this::downloadReceipt);
        lb.add(btnView);lb.add(btnPay);lb.add(btnHistory);lb.add(btnReceipt);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        btnBack=mkBtn("← Dashboard",false,null); btnBack.addActionListener(e->dispose()); rb.add(btnBack);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    private JButton mkBtn(String text, boolean primary, Color accent) {
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,16,6,16)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,14,6,14)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));}});}
        return btn;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadInvoices() {
        tableModel.setRowCount(0);
        NumberFormat cf=NumberFormat.getCurrencyInstance();
        tableModel.addRow(new Object[]{"INV-2025-001","15/01/2025","Routine Cleaning & Checkup",cf.format(150.00),"Paid","15/02/2025"});
        tableModel.addRow(new Object[]{"INV-2025-002","20/01/2025","Cavity Filling — Upper Molar",cf.format(320.00),"Pending","20/02/2025"});
        tableModel.addRow(new Object[]{"INV-2024-045","10/12/2024","Orthodontic Treatment — Phase 1",cf.format(1200.00),"Paid","10/01/2025"});
        tableModel.addRow(new Object[]{"INV-2024-046","15/12/2024","Dental X-Rays & Consultation",cf.format(85.00),"Paid","15/01/2025"});
        tableModel.addRow(new Object[]{"INV-2025-003","05/01/2025","Emergency Treatment",cf.format(450.00),"Overdue","05/02/2025"});
        tableModel.addRow(new Object[]{"INV-2024-040","20/11/2024","Cancelled Appointment Refund",cf.format(-200.00),"Refunded","N/A"});
    }

    private void updateSummary() {
        double owed=0,paid=0,refunds=0;
        for(int i=0;i<tableModel.getRowCount();i++){
            String st=(String)tableModel.getValueAt(i,4), am=(String)tableModel.getValueAt(i,3);
            double v=0; try{v=Double.parseDouble(am.replaceAll("[^\\d.-]",""));}catch(Exception ignored){}
            if("Pending".equals(st)||"Overdue".equals(st))owed+=v;
            else if("Paid".equals(st))paid+=v;
            else if("Refunded".equals(st)&&v<0)refunds+=Math.abs(v);
        }
        NumberFormat cf=NumberFormat.getCurrencyInstance();
        if(lblOwed!=null){lblOwed.setText(cf.format(owed));lblPaid.setText(cf.format(paid));lblRefunds.setText(cf.format(refunds));}
        repaint();
    }

    private void updateButtons(){
        int r=table.getSelectedRow(); boolean has=r>=0;
        btnView.setEnabled(has); btnReceipt.setEnabled(has);
        if(has){String st=(String)table.getValueAt(r,4);btnPay.setEnabled("Pending".equals(st)||"Overdue".equals(st));}
        else btnPay.setEnabled(false);
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void viewDetails(ActionEvent e){
        int r=table.getSelectedRow(); if(r<0)return;
        JOptionPane.showMessageDialog(this,
            "Invoice: "+table.getValueAt(r,0)+"\nDate: "+table.getValueAt(r,1)+"\nDescription: "+table.getValueAt(r,2)+
            "\nAmount: "+table.getValueAt(r,3)+"\nStatus: "+table.getValueAt(r,4)+"\nDue: "+table.getValueAt(r,5)+
            "\n\nPatient: "+currentPatient.getFullName()+" (ID: "+currentPatient.getPatientID()+")",
            "Invoice Details",JOptionPane.INFORMATION_MESSAGE);
    }

    private void makePayment(ActionEvent e){
        int r=table.getSelectedRow(); if(r<0)return;
        int ok=JOptionPane.showConfirmDialog(this,
            "Invoice: "+table.getValueAt(r,0)+"\nAmount: "+table.getValueAt(r,3)+"\n\nProceed to secure payment portal?",
            "Make Payment",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){
            JOptionPane.showMessageDialog(this,"Payment portal opened.\nAll transactions are encrypted and secure.","Payment",JOptionPane.INFORMATION_MESSAGE);
            table.setValueAt("Paid",r,4); updateSummary(); updateButtons();
        }
    }

    private void viewHistory(ActionEvent e){
        JOptionPane.showMessageDialog(this,
            "Payment History for "+currentPatient.getFullName()+"\n\n"+
            "Jan 2025:  INV-2025-001  $150.00  (Credit Card)\n"+
            "Dec 2024:  INV-2024-046  $85.00   (Cash)\n"+
            "Dec 2024:  INV-2024-045  $1,200.00 (Bank Transfer)\n"+
            "Nov 2024:  Refund        $200.00\n\n"+
            "Total Payments: $1,435.00 · Net: $1,235.00",
            "Payment History",JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadReceipt(ActionEvent e){
        int r=table.getSelectedRow(); if(r<0)return;
        String st=(String)table.getValueAt(r,4);
        if(!"Paid".equals(st)&&!"Refunded".equals(st)){
            JOptionPane.showMessageDialog(this,"Receipts are only available for paid or refunded invoices.","Unavailable",JOptionPane.WARNING_MESSAGE);return;}
        JOptionPane.showMessageDialog(this,
            "Receipt for "+table.getValueAt(r,0)+" is being prepared.\nA copy will be emailed to: "+currentPatient.getEmailAddress(),
            "Download Receipt",JOptionPane.INFORMATION_MESSAGE);
    }
}