package Boundary;

import Entity.Patient;
import Entity.TreatmentPlan;
import Control.TreatmentController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.Timer;

public class TreatmentPlansPage extends JFrame {

    private static final Color C_LEFT_TOP  = new Color(15, 17, 20);
    private static final Color C_LEFT_BOT  = new Color(26, 29, 34);
    private static final Color C_RIGHT     = new Color(245, 242, 236);
    private static final Color C_GOLD      = new Color(188, 152, 90);
    private static final Color C_IVORY     = new Color(250, 247, 241);
    private static final Color C_CHARCOAL  = new Color(35, 36, 40);
    private static final Color C_MUTED     = new Color(110, 106, 98);
    private static final Color C_CARD_BG   = new Color(237, 233, 224);
    private static final Color C_TBL_HDR   = new Color(48, 42, 32);
    private static final Color C_TBL_ROW   = new Color(240, 236, 227);
    private static final Color C_TBL_ALT   = new Color(233, 228, 218);
    private static final Color C_ACTIVE    = new Color(60, 130, 90);
    private static final Color C_COMPLETE  = new Color(60, 110, 160);
    private static final Color C_CANCELLED = new Color(150, 80, 70);
    private static final Color C_SEL       = new Color(188, 152, 90, 80);

    private final Patient currentPatient;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnDetails, btnInvoices, btnBack;
    private JLabel lblActive, lblCompleted, lblCost;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] COLS = {"Plan ID","Start Date","Est. Completion","Status","Progress","Total Cost"};

    public TreatmentPlansPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("Treatment Plans — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

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

        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,30,new Color(188,152,90,0),0,getHeight()*.5f,new Color(188,152,90,150),false);
                g2.setPaint(gp); g2.fillRect(0,0,1,getHeight()); g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(1,0); }
        };
        div.setOpaque(false);
        root.add(div, BorderLayout.CENTER);

        JPanel right = buildRight();
        right.setPreferredSize(new Dimension(818, 660));
        root.add(right, BorderLayout.EAST);

        Timer fade = new Timer(16, e -> { alpha = Math.min(1f,alpha+0.025f); left.repaint(); if(alpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadPlans();
        updateSummary();
        updateButtons();
    }

    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-60;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,130,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
        float r=50f+pulse*2f; paintEmblem(g2,cx,cy,r);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,19)); FontMetrics fm=g2.getFontMetrics();
        String br="DentalCare"; g2.drawString(br,cx-fm.stringWidth(br)/2,cy+(int)r+30);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f)); int ry=cy+(int)r+42;
        g2.drawLine(cx-58,ry,cx+58,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Treatment Plans"; g2.drawString(sub,cx-fm.stringWidth(sub)/2,ry+17);

        // Patient box
        int bx=cx-80,by=ry+35;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(160,153,140)); g2.setFont(new Font("Georgia",Font.ITALIC,10)); fm=g2.getFontMetrics();
        String fl="Plans for"; g2.drawString(fl,cx-fm.stringWidth(fl)/2,by+14);
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,13)); fm=g2.getFontMetrics();
        String name=currentPatient.getFullName(); while(fm.stringWidth(name)>148&&name.length()>4)name=name.substring(0,name.length()-4)+"…";
        g2.drawString(name,cx-fm.stringWidth(name)/2,by+31);
        g2.setColor(new Color(90,86,80)); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String id="ID: "+currentPatient.getPatientID(); g2.drawString(id,cx-fm.stringWidth(id)/2,by+46);

        // Stats row
        int sy=by+66;
        String[] labels={"Active","Done","Cost"}; Color[] cols={C_ACTIVE,C_COMPLETE,C_GOLD};
        String[] vals={lblActive==null?"0":lblActive.getText(), lblCompleted==null?"0":lblCompleted.getText(), lblCost==null?"$0":lblCost.getText()};
        int sw=50; int startX=cx-sw-6;
        for(int i=0;i<3;i++){
            int sx2=startX+i*(sw+6);
            g2.setColor(new Color(cols[i].getRed(),cols[i].getGreen(),cols[i].getBlue(),28));
            g2.fillRoundRect(sx2,sy,sw,40,5,5);
            g2.setColor(new Color(cols[i].getRed(),cols[i].getGreen(),cols[i].getBlue(),70));
            g2.setStroke(new BasicStroke(0.6f)); g2.drawRoundRect(sx2,sy,sw,40,5,5);
            g2.setColor(cols[i]); g2.setFont(new Font("Georgia",Font.BOLD,i==2?11:16)); fm=g2.getFontMetrics();
            String v=vals[i]; g2.drawString(v,sx2+(sw-fm.stringWidth(v))/2,sy+22);
            g2.setColor(new Color(120,114,104)); g2.setFont(new Font("Georgia",Font.ITALIC,9)); fm=g2.getFontMetrics();
            g2.drawString(labels[i],sx2+(sw-fm.stringWidth(labels[i]))/2,sy+35);
        }

        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy,cx-fm.stringWidth(copy)/2,h-16);
    }

    private void paintEmblem(Graphics2D g2,int cx,int cy,float r){
        g2.setColor(new Color(188,152,90,40));g2.setStroke(new BasicStroke(0.7f));g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));
        g2.setColor(new Color(188,152,90,85));g2.setStroke(new BasicStroke(1.1f));g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)}));
        g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        float arm=r*.42f,th=r*.18f; g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx-th/2,cy-arm,th,arm*2,3,3)); g2.fill(new RoundRectangle2D.Float(cx-arm,cy-th/2,arm*2,th,3,3));
        g2.setColor(new Color(188,152,90,95));g2.setStroke(new BasicStroke(1f));
        for(int i=0;i<12;i++){double a=i*Math.PI/6-Math.PI/2;float in=r+3,out=r+(i%3==0?10:7);g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),(int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));}
        g2.setColor(new Color(188,152,90,120)); float dd=r*.63f,dr=r*.07f;
        for(int i=0;i<4;i++){double a=Math.PI/4+i*Math.PI/2;g2.fill(new Ellipse2D.Float((float)(cx+Math.cos(a)*dd)-dr,(float)(cy+Math.sin(a)*dd)-dr,dr*2,dr*2));}
    }

    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar
        JPanel top = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(28,getHeight()-1,getWidth()-28,getHeight()-1); g2.dispose(); }
        };
        top.setOpaque(false); top.setBorder(new EmptyBorder(16,26,12,26));
        JLabel tl=new JLabel("Treatment Plans"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel(currentPatient.getFullName()+" · ID "+currentPatient.getPatientID()); sl.setFont(new Font("Georgia",Font.ITALIC,13)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts,BorderLayout.WEST);

        // Summary pills
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); pills.setOpaque(false);
        lblActive=new JLabel("0"); lblCompleted=new JLabel("0"); lblCost=new JLabel("$0");
        pills.add(makePill("Active",lblActive,C_ACTIVE)); pills.add(makePill("Completed",lblCompleted,C_COMPLETE)); pills.add(makePill("Total Cost",lblCost,C_GOLD));
        top.add(pills,BorderLayout.EAST); p.add(top,BorderLayout.NORTH);

        // Table
        p.add(buildTablePanel(),BorderLayout.CENTER);

        // Button bar
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(28,0,getWidth()-28,0); g2.dispose(); }
        };
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(9,26,13,26));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); lb.setOpaque(false);
        btnDetails=makeBtn("View Details",false,null); btnInvoices=makeBtn("Related Invoices",false,C_COMPLETE);
        btnDetails.addActionListener(this::viewDetails); btnInvoices.addActionListener(this::viewInvoices);
        lb.add(btnDetails); lb.add(btnInvoices);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        btnBack=makeBtn("← Dashboard",false,null); btnBack.addActionListener(e->dispose());
        rb.add(btnBack); bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST);
        p.add(bar,BorderLayout.SOUTH); return p;
    }

    private JPanel makePill(String label, JLabel val, Color c) {
        JPanel pill=new JPanel() { @Override protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20); g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),80)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); g2.dispose(); } };
        pill.setLayout(new BoxLayout(pill,BoxLayout.X_AXIS)); pill.setOpaque(false); pill.setBorder(new EmptyBorder(3,10,3,10));
        JLabel lbl=new JLabel(label+" "); lbl.setFont(new Font("Georgia",Font.ITALIC,11)); lbl.setForeground(C_MUTED);
        val.setFont(new Font("Georgia",Font.BOLD,13)); val.setForeground(c);
        pill.add(lbl); pill.add(val); return pill;
    }

    private JPanel buildTablePanel() {
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.setBorder(new EmptyBorder(8,26,6,26));
        tableModel=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,13)); table.setRowHeight(34); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,13)); hdr.setBackground(C_TBL_HDR); hdr.setForeground(new Color(215,185,120));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,10,0,6));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==3&&v!=null){String s=v.toString();if(s.equals("Active"))setForeground(C_ACTIVE);else if(s.equals("Completed"))setForeground(C_COMPLETE);else if(s.equals("Cancelled"))setForeground(C_CANCELLED);else setForeground(C_CHARCOAL);}
                    else if(col==4&&v!=null){setForeground(C_ACTIVE);}
                    else if(col==5){setForeground(C_GOLD);setHorizontalAlignment(RIGHT);}
                    else setForeground(C_CHARCOAL);
                } if(col!=5)setHorizontalAlignment(col==0?CENTER:LEFT); return this;}
        });
        int[]widths={70,100,120,90,80,100}; for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting())updateButtons(); });
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    private JButton makeBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,16,6,16)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border.equals(new Color(188,152,90,100))?C_GOLD:border,1),new EmptyBorder(6,14,6,14)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));}});}
        return btn;
    }

    private void loadPlans() {
        tableModel.setRowCount(0);
        NumberFormat cf=NumberFormat.getCurrencyInstance();
        try {
            List<TreatmentPlan> plans=TreatmentController.getPatientTreatmentPlans(currentPatient.getPatientID());
            for(TreatmentPlan p:plans) tableModel.addRow(new Object[]{"TP"+String.format("%03d",p.getTreatmentPlanID()),p.getStartDate(),p.getEstimatedCompletionDate(),p.getStatus(),progressFor(p.getStatus()),cf.format(p.getTotalCost())});
        } catch(Exception e) { tableModel.addRow(new Object[]{"Error","","","","",e.getMessage()}); }
    }

    private String progressFor(String status){ switch(status){case "Completed":return "100%";case "Cancelled":return "0%";default:return "50%";} }

    private void updateSummary() {
        int active=0,done=0; double cost=0;
        for(int i=0;i<tableModel.getRowCount();i++){
            String st=(String)tableModel.getValueAt(i,3); String cs=(String)tableModel.getValueAt(i,5);
            double c=0; try{c=Double.parseDouble(cs.replaceAll("[^\\d.]",""));}catch(Exception ignored){}
            if("Active".equals(st)){active++;cost+=c;} else if("Completed".equals(st)){done++;cost+=c;}
        }
        if(lblActive!=null){lblActive.setText(String.valueOf(active));lblCompleted.setText(String.valueOf(done));lblCost.setText(NumberFormat.getCurrencyInstance().format(cost));}
    }

    private void updateButtons() { boolean sel=table.getSelectedRow()>=0; btnDetails.setEnabled(sel); btnInvoices.setEnabled(sel); }

    private void viewDetails(ActionEvent e) {
        int row=table.getSelectedRow(); if(row<0)return;
        JOptionPane.showMessageDialog(this,"Plan: "+tableModel.getValueAt(row,0)+"\nStatus: "+tableModel.getValueAt(row,3)+"\nProgress: "+tableModel.getValueAt(row,4)+"\nCost: "+tableModel.getValueAt(row,5),"Treatment Details",JOptionPane.INFORMATION_MESSAGE);
    }
    private void viewInvoices(ActionEvent e) {
        int row=table.getSelectedRow(); if(row<0)return;
        JOptionPane.showMessageDialog(this,"Invoices for plan "+tableModel.getValueAt(row,0)+" will be shown here.\nView full details in Payment & Billing.","Related Invoices",JOptionPane.INFORMATION_MESSAGE);
    }
}