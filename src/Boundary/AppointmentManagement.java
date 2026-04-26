package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class AppointmentManagement extends JFrame {

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
    private static final Color C_CONFIRM   = new Color(60,  130, 90);
    private static final Color C_PENDING   = new Color(170, 130, 50);
    private static final Color C_CANCEL    = new Color(155, 65,  55);
    private static final Color C_SUSPEND   = new Color(155, 110, 40);
    private static final Color C_URGENT    = new Color(175, 70,  60);
    private static final Color C_INFO      = new Color(60,  100, 165);
    private static final Color C_PAID      = new Color(60,  130, 90);
    private static final Color C_REFUND    = new Color(60,  100, 165);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] COLS = {
        "ID","Patient Name","Date","Time","Dentist","Treatment","Status","Priority","Sterilization","Payment"
    };

    public AppointmentManagement() {
        super("Appointment Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 660);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(240, 0); }
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
        right.setPreferredSize(new Dimension(958, 700));
        root.add(right, BorderLayout.EAST);

        Timer fade = new Timer(16, e -> { alpha = Math.min(1f,alpha+0.025f); left.repaint(); if(alpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadData();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-55;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,120,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r = 48f + pulse*2f;
        paintEmblem(g2, cx, cy, r);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,17));
        FontMetrics fm = g2.getFontMetrics(); String br="DentalCare";
        g2.drawString(br, cx-fm.stringWidth(br)/2, cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry=cy+(int)r+40; g2.drawLine(cx-55,ry,cx+55,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Appointment Mgmt."; g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // Count badge
        int count = tableModel==null?0:tableModel.getRowCount();
        int bx=cx-58,by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,116,42,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,116,42,6,6);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,20)); fm=g2.getFontMetrics();
        String cnt=String.valueOf(count); g2.drawString(cnt, cx-fm.stringWidth(cnt)/2, by+26);
        g2.setColor(new Color(130,124,112)); g2.setFont(new Font("Georgia",Font.ITALIC,10)); fm=g2.getFontMetrics();
        String cl=count==1?"appointment":"appointments"; g2.drawString(cl, cx-fm.stringWidth(cl)/2, by+39);

        // Status legend
        int ly=by+56;
        Color[]lc={C_CONFIRM,C_PENDING,C_SUSPEND,C_CANCEL,C_URGENT};
        String[]ll={"Confirmed","Pending","Suspended","Cancelled","Urgent"};
        for(int i=0;i<5;i++){
            int lyi=ly+i*17;
            g2.setColor(new Color(lc[i].getRed(),lc[i].getGreen(),lc[i].getBlue(),45)); g2.fillRoundRect(cx-58,lyi,116,14,4,4);
            g2.setColor(lc[i]); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
            g2.drawString(ll[i], cx-fm.stringWidth(ll[i])/2, lyi+10);
        }

        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188,152,90,40)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int)(cx-r-11),(int)(cy-r-11),(int)(r*2+22),(int)(r*2+22));
        g2.setColor(new Color(188,152,90,85)); g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)}));
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
                g2.drawLine(22,getHeight()-1,getWidth()-22,getHeight()-1); g2.dispose(); }
        };
        top.setOpaque(false); top.setBorder(new EmptyBorder(13,24,10,24));
        JLabel tl=new JLabel("Appointment Management"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel("Schedule, confirm, suspend and manage appointments"); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false);
        ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts,BorderLayout.WEST);
        // Search in top-right
        JPanel sr=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0)); sr.setOpaque(false);
        JLabel slbl=new JLabel("Patient:"); slbl.setFont(new Font("Georgia",Font.BOLD,12)); slbl.setForeground(C_CHARCOAL);
        searchField=new JTextField(14); styleField(searchField);
        JButton sb=mkBtn("Search",false,null); sb.addActionListener(e->searchAppointments()); searchField.addActionListener(e->searchAppointments());
        JButton cb=mkBtn("Clear",false,null); cb.addActionListener(e->{searchField.setText("");table.clearSelection();updateStatus();});
        sr.add(slbl); sr.add(searchField); sr.add(sb); sr.add(cb);
        top.add(sr,BorderLayout.EAST);
        p.add(top,BorderLayout.NORTH);

        // Table
        p.add(buildTablePanel(),BorderLayout.CENTER);

        // Button bar + status
        JPanel bottom=new JPanel(new BorderLayout()); bottom.setOpaque(false);
        bottom.add(buildButtonBar(),BorderLayout.NORTH);
        bottom.add(buildStatusBar(),BorderLayout.SOUTH);
        p.add(bottom,BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.setBorder(new EmptyBorder(8,24,5,24));
        tableModel=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,12)); table.setRowHeight(29); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,7,0,5));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{
                    setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==0){setForeground(C_GOLD);setHorizontalAlignment(CENTER);}
                    else if(col==6&&v!=null){String s=v.toString();
                        if(s.equals("Confirmed"))setForeground(C_CONFIRM);
                        else if(s.contains("Pending"))setForeground(C_PENDING);
                        else if(s.equals("Suspended"))setForeground(C_SUSPEND);
                        else if(s.equals("Cancelled"))setForeground(C_CANCEL);
                        else setForeground(C_INFO);
                        setHorizontalAlignment(LEFT);}
                    else if(col==7&&v!=null){setForeground("Urgent".equals(v.toString())?C_URGENT:C_INFO);setHorizontalAlignment(LEFT);}
                    else if(col==9&&v!=null){String s=v.toString();
                        if(s.equals("Paid"))setForeground(C_PAID);
                        else if(s.contains("Refund"))setForeground(C_REFUND);
                        else setForeground(C_PENDING);
                        setHorizontalAlignment(LEFT);}
                    else{setForeground(C_CHARCOAL);setHorizontalAlignment(LEFT);}
                } return this;}
        });
        int[]widths={46,130,88,62,105,120,112,72,95,88};
        for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    private JPanel buildButtonBar() {
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(22,0,getWidth()-22,0); g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(7,24,6,24));

        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); lb.setOpaque(false);
        JButton bSched=mkBtn("+ Schedule",true,null); JButton bUrgent=mkBtn("Urgent",false,C_URGENT);
        JButton bConf=mkBtn("Confirm",false,C_CONFIRM); JButton bSusp=mkBtn("Suspend",false,C_SUSPEND);
        JButton bCancel=mkBtn("Cancel",false,C_CANCEL);
        JButton bSteril=mkBtn("Sterilized",false,C_INFO); JButton bPay=mkBtn("Process Payment",false,C_PAID);
        bSched.addActionListener(e->scheduleAppointment("routine")); bUrgent.addActionListener(e->scheduleAppointment("urgent"));
        bConf.addActionListener(e->confirmAppointment()); bSusp.addActionListener(e->suspendAppointment());
        bCancel.addActionListener(e->cancelAppointment()); bSteril.addActionListener(e->markSterilized()); bPay.addActionListener(e->processPayment());
        lb.add(bSched);lb.add(bUrgent);lb.add(Box.createHorizontalStrut(6));lb.add(bConf);lb.add(bSusp);lb.add(bCancel);lb.add(Box.createHorizontalStrut(6));lb.add(bSteril);lb.add(bPay);

        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton bExport=mkBtn("Export JSON",false,new Color(60,100,165));
        bExport.addActionListener(e->exportToJson());
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->dispose());
        rb.add(bExport); rb.add(back);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    private JPanel buildStatusBar() {
        JPanel bar=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(188,152,90,30)); g2.setStroke(new BasicStroke(0.7f)); g2.drawLine(22,0,getWidth()-22,0); g2.dispose();}};
        bar.setOpaque(false); bar.setBorder(new EmptyBorder(5,24,10,24));
        statusLabel=new JLabel("Ready"); statusLabel.setFont(new Font("Georgia",Font.ITALIC,11)); statusLabel.setForeground(C_MUTED);
        JLabel copy=new JLabel("© 2025 DentalCare System"); copy.setFont(new Font("Georgia",Font.ITALIC,11)); copy.setForeground(new Color(0x3a3830));
        bar.add(statusLabel,BorderLayout.WEST); bar.add(copy,BorderLayout.EAST); return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,11));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(5,12,5,12)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(5,10,5,10)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(5,10,5,10)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(5,10,5,10)));}});}
        return btn;
    }

    private void styleField(JTextField f){f.setFont(new Font("Georgia",Font.PLAIN,12));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));}

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadData(){
        Object[][]d={
            {"001","John Smith",  "2025-07-20","09:00","Dr. Cohen","Routine Cleaning",  "Confirmed",           "Routine","Completed","Paid"},
            {"002","Sarah Johnson","2025-07-20","10:30","Dr. Bar",  "Root Canal",        "Pending Confirmation","Urgent", "Pending",  "Pending"},
            {"003","Mike Davis",  "2025-07-21","14:00","Dr. Levi", "Crown Fitting",     "Suspended",           "Routine","N/A",      "Pending Refund"},
            {"004","Emma Wilson", "2025-07-22","11:15","Dr. Cohen","Cavity Filling",    "Scheduled",           "Routine","Pending",  "Unpaid"},
            {"005","David Brown", "2025-07-22","16:30","Dr. Peretz","Emergency Treatment","Confirmed",          "Urgent", "Completed","Paid"},
            {"006","Lisa Anderson","2025-07-23","13:00","Dr. Cohen","Teeth Whitening",  "Confirmed",           "Routine","Completed","Paid"},
            {"007","Robert Taylor","2025-07-23","15:45","Dr. Bar",  "Dental Checkup",   "Scheduled",           "Routine","Pending",  "Unpaid"}
        };
        for(Object[]r:d)tableModel.addRow(r);
        updateStatus();
    }

    private void updateStatus(){
        int tot=tableModel.getRowCount(),conf=0,pend=0;
        for(int i=0;i<tot;i++){String s=(String)tableModel.getValueAt(i,6);if("Confirmed".equals(s))conf++;else if(s!=null&&s.contains("Pending"))pend++;}
        statusLabel.setText(String.format("Total: %d  ·  Confirmed: %d  ·  Pending: %d",tot,conf,pend));
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void scheduleAppointment(String priority){
        JOptionPane.showMessageDialog(this,"Scheduling "+(priority.equals("urgent")?"urgent (prioritized)":"routine")+" appointment.\nThis would open the scheduling dialog.","Schedule",JOptionPane.INFORMATION_MESSAGE);
    }
    private void confirmAppointment(){
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select an appointment.","",JOptionPane.WARNING_MESSAGE);return;}
        if("Confirmed".equals(tableModel.getValueAt(r,6))){JOptionPane.showMessageDialog(this,"Already confirmed.","",JOptionPane.INFORMATION_MESSAGE);return;}
        tableModel.setValueAt("Confirmed",r,6);updateStatus();JOptionPane.showMessageDialog(this,"Appointment confirmed. Confirmation email sent.","Confirmed",JOptionPane.INFORMATION_MESSAGE);
    }
    private void suspendAppointment(){
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select an appointment.","",JOptionPane.WARNING_MESSAGE);return;}
        int ok=JOptionPane.showConfirmDialog(this,"Suspend appointment for "+tableModel.getValueAt(r,1)+"?\n\nIf not resumed within 24h, will be auto-cancelled with refund.","Suspend",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){tableModel.setValueAt("Suspended",r,6);tableModel.setValueAt("Pending Refund",r,9);updateStatus();JOptionPane.showMessageDialog(this,"Appointment suspended. Auto-cancel in 24h if not resumed.","Suspended",JOptionPane.INFORMATION_MESSAGE);}
    }
    private void cancelAppointment(){
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select an appointment.","",JOptionPane.WARNING_MESSAGE);return;}
        int ok=JOptionPane.showConfirmDialog(this,"Cancel appointment for "+tableModel.getValueAt(r,1)+"?\nRefund will be processed automatically.","Cancel",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION){tableModel.setValueAt("Cancelled",r,6);tableModel.setValueAt("Refunded",r,9);updateStatus();JOptionPane.showMessageDialog(this,"Appointment cancelled. Refund processed.","Cancelled",JOptionPane.INFORMATION_MESSAGE);}
    }
    private void markSterilized(){
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select an appointment.","",JOptionPane.WARNING_MESSAGE);return;}
        if("Completed".equals(tableModel.getValueAt(r,8))){JOptionPane.showMessageDialog(this,"Sterilization already marked complete.","",JOptionPane.INFORMATION_MESSAGE);return;}
        tableModel.setValueAt("Completed",r,8);JOptionPane.showMessageDialog(this,"Equipment sterilization marked complete.","Done",JOptionPane.INFORMATION_MESSAGE);
    }
    private void processPayment(){
        int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select an appointment.","",JOptionPane.WARNING_MESSAGE);return;}
        if("Paid".equals(tableModel.getValueAt(r,9))){JOptionPane.showMessageDialog(this,"Payment already processed.","",JOptionPane.INFORMATION_MESSAGE);return;}
        int ok=JOptionPane.showConfirmDialog(this,"Process payment for "+tableModel.getValueAt(r,1)+"?","Payment",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){tableModel.setValueAt("Paid",r,9);JOptionPane.showMessageDialog(this,"Payment processed. Receipt sent to patient.","Paid",JOptionPane.INFORMATION_MESSAGE);}
    }
    private void searchAppointments(){
        String term=searchField.getText().toLowerCase().trim();if(term.isEmpty())return;
        for(int i=0;i<tableModel.getRowCount();i++){
            if(tableModel.getValueAt(i,1).toString().toLowerCase().contains(term)){
                table.setRowSelectionInterval(i,i);table.scrollRectToVisible(table.getCellRect(i,0,true));
                statusLabel.setText("Found: "+tableModel.getValueAt(i,1));return;
            }
        }
        JOptionPane.showMessageDialog(this,"No appointments found for: "+term);
        statusLabel.setText("No results for: "+term);
    }

    private void exportToJson() {
        if(tableModel.getRowCount()==0){
            JOptionPane.showMessageDialog(this,"No appointments to export.","Empty",JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build JSON string manually — no external library needed
        String exported = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for(int i=0;i<tableModel.getRowCount();i++){
            sb.append("  {\n");
            sb.append("    \"appointmentID\": \"").append(esc(tableModel.getValueAt(i,0))).append("\",\n");
            sb.append("    \"patientName\": \"").append(esc(tableModel.getValueAt(i,1))).append("\",\n");
            sb.append("    \"date\": \"").append(esc(tableModel.getValueAt(i,2))).append("\",\n");
            sb.append("    \"time\": \"").append(esc(tableModel.getValueAt(i,3))).append("\",\n");
            sb.append("    \"dentist\": \"").append(esc(tableModel.getValueAt(i,4))).append("\",\n");
            sb.append("    \"treatment\": \"").append(esc(tableModel.getValueAt(i,5))).append("\",\n");
            sb.append("    \"status\": \"").append(esc(tableModel.getValueAt(i,6))).append("\",\n");
            sb.append("    \"priority\": \"").append(esc(tableModel.getValueAt(i,7))).append("\",\n");
            sb.append("    \"sterilization\": \"").append(esc(tableModel.getValueAt(i,8))).append("\",\n");
            sb.append("    \"payment\": \"").append(esc(tableModel.getValueAt(i,9))).append("\",\n");
            sb.append("    \"exportedAt\": \"").append(exported).append("\"\n");
            sb.append("  }");
            if(i<tableModel.getRowCount()-1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");

        // Ask user where to save
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Appointments JSON");
        fc.setSelectedFile(new java.io.File("appointments_"+
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))+".json"));
        if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if(!path.toLowerCase().endsWith(".json")) path+=".json";

        try(FileWriter fw = new FileWriter(path)){
            fw.write(sb.toString());
            JOptionPane.showMessageDialog(this,
                "✅ Exported "+tableModel.getRowCount()+" appointments to:\n"+path,
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Exported "+tableModel.getRowCount()+" records to JSON");
        }catch(IOException ex){
            JOptionPane.showMessageDialog(this,"Export failed: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Escape quotes inside JSON string values */
    private String esc(Object v){
        if(v==null) return "";
        return v.toString().replace("\\","\\\\").replace("\"","\\\"");
    }

    public static void main(String[]args){SwingUtilities.invokeLater(()->new AppointmentManagement().setVisible(true));}
}