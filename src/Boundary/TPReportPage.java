package Boundary;

import Entity.Staff;
import Control.TPReportLauncher;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import javax.swing.Timer;

public class TPReportPage extends JFrame {

    private static final Color C_BG       = new Color(15,  17,  20);
    private static final Color C_BANNER   = new Color(20,  22,  27);
    private static final Color C_BODY     = new Color(245, 242, 236);
    private static final Color C_GOLD     = new Color(188, 152, 90);
    private static final Color C_IVORY    = new Color(250, 247, 241);
    private static final Color C_CHARCOAL = new Color(35,  36,  40);
    private static final Color C_MUTED    = new Color(110, 106, 98);
    private static final Color C_CARD_BG  = new Color(237, 233, 224);
    private static final Color C_FIELD_BG = new Color(228, 223, 213);
    private static final Color C_INFO     = new Color(60,  100, 165);
    private static final Color C_SUCCESS  = new Color(60,  130, 90);

    private JComboBox<Staff> cbDentist;
    private JButton btnGenerate, btnExport;
    private Staff preselectedDentist;
    private float bannerAlpha = 0f;

    public TPReportPage(Staff dentist) {
        this.preselectedDentist = dentist;
        build();
        if (preselectedDentist != null) preselectDentist();
        else loadDentists();
    }

    public TPReportPage() {
        this.preselectedDentist = null;
        build();
        loadDentists();
    }

    private void build() {
        setTitle("Treatment Progress Report — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);
        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);

        Timer fade = new Timer(16, e -> { bannerAlpha=Math.min(1f,bannerAlpha+0.03f); root.getComponent(0).repaint(); if(bannerAlpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
    }

    // ── Banner ───────────────────────────────────────────────────────────────
    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,bannerAlpha));
                int w=getWidth(),h=getHeight();
                g2.setPaint(new GradientPaint(0,0,C_BANNER,w,h,C_BG)); g2.fillRect(0,0,w,h);
                g2.setColor(new Color(255,255,255,4)); g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
                g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(30,h-1,w-30,h-1); g2.dispose();
            }
        };
        banner.setOpaque(false); banner.setPreferredSize(new Dimension(0,82)); banner.setBorder(new EmptyBorder(0,26,0,26));

        // Left: emblem + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false);
        left.add(makeEmblem());
        JPanel bs=new JPanel(); bs.setLayout(new BoxLayout(bs,BoxLayout.Y_AXIS)); bs.setOpaque(false); bs.setBorder(new EmptyBorder(0,12,0,0));
        JLabel bl=new JLabel("Treatment Progress Report"); bl.setFont(new Font("Georgia",Font.BOLD,18)); bl.setForeground(C_IVORY);
        JLabel sl=new JLabel(preselectedDentist!=null
            ? "Dr. "+preselectedDentist.getFirstName()+" "+preselectedDentist.getLastName()
            : "Select a dentist to generate the report");
        sl.setFont(new Font("Georgia",Font.ITALIC,11)); sl.setForeground(C_GOLD);
        bs.add(bl); bs.add(sl); left.add(bs);

        // Right: status pill
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); right.setOpaque(false);
        if(preselectedDentist!=null){
            right.add(makePill("Staff ID: "+preselectedDentist.getStaffID(), C_GOLD));
            right.add(makePill("Dentist", C_SUCCESS));
        } else {
            right.add(makePill("All Dentists", C_INFO));
        }

        banner.add(left,BorderLayout.WEST); banner.add(right,BorderLayout.EAST);
        return banner;
    }

    // ── Body ────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout()); body.setBackground(C_BODY); body.setBorder(new EmptyBorder(22,36,18,36));

        // Central card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose();
            }
        };
        card.setOpaque(false); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(28,32,28,32));

        if (preselectedDentist == null) {
            // Dentist selector row
            JLabel lbl=new JLabel("Select Dentist"); lbl.setFont(new Font("Georgia",Font.BOLD,14)); lbl.setForeground(C_CHARCOAL); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            cbDentist=new JComboBox<>(); cbDentist.setFont(new Font("Georgia",Font.PLAIN,13)); cbDentist.setBackground(C_FIELD_BG); cbDentist.setForeground(C_CHARCOAL);
            cbDentist.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1)); cbDentist.setPreferredSize(new Dimension(320,34)); cbDentist.setMaximumSize(new Dimension(360,34));
            cbDentist.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lbl); card.add(Box.createRigidArea(new Dimension(0,10))); card.add(cbDentist);
            card.add(Box.createRigidArea(new Dimension(0,10)));
            // Gold rule
            card.add(makeRule());
            card.add(Box.createRigidArea(new Dimension(0,16)));
        } else {
            cbDentist=new JComboBox<>(); cbDentist.setVisible(false);
            // Dentist info display
            JLabel forLbl=new JLabel("Generating report for"); forLbl.setFont(new Font("Georgia",Font.ITALIC,13)); forLbl.setForeground(C_MUTED); forLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel nameLbl=new JLabel("Dr. "+preselectedDentist.getFirstName()+" "+preselectedDentist.getLastName()); nameLbl.setFont(new Font("Georgia",Font.BOLD,22)); nameLbl.setForeground(C_CHARCOAL); nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel specLbl=new JLabel("Staff ID: "+preselectedDentist.getStaffID()); specLbl.setFont(new Font("Georgia",Font.ITALIC,12)); specLbl.setForeground(C_GOLD); specLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(forLbl); card.add(Box.createRigidArea(new Dimension(0,6))); card.add(nameLbl); card.add(Box.createRigidArea(new Dimension(0,4))); card.add(specLbl);
            card.add(Box.createRigidArea(new Dimension(0,16))); card.add(makeRule()); card.add(Box.createRigidArea(new Dimension(0,16)));
        }

        // Report info cards row (3 mini info pills)
        JPanel infoRow=new JPanel(new FlowLayout(FlowLayout.CENTER,12,0)); infoRow.setOpaque(false); infoRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoRow.add(makeInfoCard("Treatment Plans","Completed & in-progress",C_SUCCESS));
        infoRow.add(makeInfoCard("Progress Tracking","Per patient overview",C_INFO));
        infoRow.add(makeInfoCard("PDF Export","Save to file",C_GOLD));
        card.add(infoRow); card.add(Box.createRigidArea(new Dimension(0,22)));

        // Buttons
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,14,0)); btnRow.setOpaque(false); btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGenerate=mkBtn("Generate Report",true,null);
        btnExport  =mkBtn("Export to PDF",  false,C_INFO);
        btnGenerate.addActionListener(e->generateReport());
        btnExport.addActionListener(e->exportToPDF());
        btnRow.add(btnGenerate); btnRow.add(btnExport);
        card.add(btnRow);

        body.add(card,BorderLayout.CENTER);

        // Button bar bottom
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bar.setLayout(new FlowLayout(FlowLayout.RIGHT,8,0)); bar.setOpaque(false); bar.setBorder(new EmptyBorder(10,0,0,0));
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->dispose()); bar.add(back);
        body.add(bar,BorderLayout.SOUTH);
        return body;
    }

    private JPanel makeRule(){JPanel r=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};r.setOpaque(false);r.setMaximumSize(new Dimension(Integer.MAX_VALUE,8));r.setAlignmentX(Component.CENTER_ALIGNMENT);return r;}

    private JPanel makeInfoCard(String title,String sub,Color c){
        JPanel card=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),18));g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),60));g2.setStroke(new BasicStroke(0.7f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        card.setOpaque(false); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(10,14,10,14));
        JLabel tl=new JLabel(title); tl.setFont(new Font("Georgia",Font.BOLD,12)); tl.setForeground(c); tl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sl=new JLabel(sub); sl.setFont(new Font("Georgia",Font.ITALIC,10)); sl.setForeground(C_MUTED); sl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tl); card.add(Box.createRigidArea(new Dimension(0,3))); card.add(sl); return card;
    }

    // ── Logic ────────────────────────────────────────────────────────────────
    private void loadDentists(){
        if(preselectedDentist!=null)return;
        try{
            List<Staff> dentists=TPReportLauncher.getAllDentists();
            cbDentist.removeAllItems();
            for(Staff d:dentists)cbDentist.addItem(d);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Error loading dentists: "+e.getMessage(),"DB Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void preselectDentist(){
        if(preselectedDentist!=null)setTitle("Treatment Progress — Dr. "+preselectedDentist.getFirstName()+" "+preselectedDentist.getLastName());
    }

    public void setPreselectedDentist(Staff dentist){this.preselectedDentist=dentist;if(cbDentist!=null&&dentist!=null)preselectDentist();}

    private Staff getSelectedDentist(){return preselectedDentist!=null?preselectedDentist:(Staff)cbDentist.getSelectedItem();}

    private void generateReport(){
        Staff d=getSelectedDentist();
        if(d==null){JOptionPane.showMessageDialog(this,"Please select a dentist first.","",JOptionPane.WARNING_MESSAGE);return;}
        try{
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); btnGenerate.setEnabled(false); btnGenerate.setText("Generating…");
            TPReportLauncher.generateTreatmentProgressReport(d);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Error generating report: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
        finally{btnGenerate.setEnabled(true);btnGenerate.setText("Generate Report");setCursor(Cursor.getDefaultCursor());}
    }

    private void exportToPDF(){
        Staff d=getSelectedDentist();
        if(d==null){JOptionPane.showMessageDialog(this,"Please select a dentist first.","",JOptionPane.WARNING_MESSAGE);return;}
        JFileChooser fc=new JFileChooser();
        fc.setDialogTitle("Save Report as PDF");
        fc.setSelectedFile(new java.io.File("TreatmentReport_Dr"+d.getLastName()+"_"+new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date())+".pdf"));
        if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            try{
                String path=fc.getSelectedFile().getAbsolutePath();
                if(!path.toLowerCase().endsWith(".pdf"))path+=".pdf";
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); btnExport.setEnabled(false); btnExport.setText("Exporting…");
                TPReportLauncher.exportReportToPDF(d,path);
                JOptionPane.showMessageDialog(this,"Exported successfully to:\n"+path,"Done",JOptionPane.INFORMATION_MESSAGE);
            }catch(Exception e){JOptionPane.showMessageDialog(this,"Export error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
            finally{btnExport.setEnabled(true);btnExport.setText("Export to PDF");setCursor(Cursor.getDefaultCursor());}
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JPanel makeEmblem(){JPanel e=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int cx=getWidth()/2,cy=getHeight()/2,r=16;g2.setColor(new Color(188,152,90,60));g2.setStroke(new BasicStroke(0.8f));g2.drawOval(cx-r-3,cy-r-3,r*2+6,r*2+6);g2.setColor(new Color(188,152,90,100));g2.setStroke(new BasicStroke(1.1f));g2.drawOval(cx-r,cy-r,r*2,r*2);g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));g2.fillOval(cx-r,cy-r,r*2,r*2);int arm=7,th=3;g2.setColor(C_GOLD);g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2);g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);g2.dispose();}@Override public Dimension getPreferredSize(){return new Dimension(40,40);}};e.setOpaque(false);return e;}
    private JPanel makePill(String text,Color c){JPanel p=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),75));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g2.dispose();}};p.setOpaque(false);p.setBorder(new EmptyBorder(4,11,4,11));JLabel l=new JLabel(text);l.setFont(new Font("Georgia",Font.BOLD,11));l.setForeground(c);p.add(l);return p;}
    private JButton mkBtn(String text,boolean primary,Color accent){JButton btn=new JButton(text);btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,13));btn.setFocusPainted(false);btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));btn.setOpaque(true);Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(8,16,8,16)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(8,16,8,16)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(8,16,8,16)));}});}return btn;}
}