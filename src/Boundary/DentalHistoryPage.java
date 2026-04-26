package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;

public class DentalHistoryPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_BG       = new Color(15,  17,  20);
    private static final Color C_BANNER   = new Color(20,  22,  27);
    private static final Color C_BODY     = new Color(245, 242, 236);
    private static final Color C_GOLD     = new Color(188, 152, 90);
    private static final Color C_IVORY    = new Color(250, 247, 241);
    private static final Color C_CHARCOAL = new Color(35,  36,  40);
    private static final Color C_MUTED    = new Color(110, 106, 98);
    private static final Color C_CARD_BG  = new Color(237, 233, 224);
    private static final Color C_FIELD_BG = new Color(228, 223, 213);
    private static final Color C_TBL_HDR  = new Color(48,  42,  32);
    private static final Color C_TBL_ROW  = new Color(240, 236, 227);
    private static final Color C_TBL_ALT  = new Color(233, 228, 218);
    private static final Color C_SEL      = new Color(188, 152, 90, 80);
    private static final Color C_SUCCESS  = new Color(60,  130, 90);
    private static final Color C_INFO     = new Color(60,  100, 165);
    private static final Color C_WARNING  = new Color(170, 130, 50);

    private final int patientId;
    private final String patientName;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JLabel recordCountLabel;

    private float bannerAlpha = 0f;

    private static final String[] COLS = {"Date","Treatment Type","Description","X-Rays","Dentist","Notes"};

    public DentalHistoryPage(int patientId, String patientName) {
        super("Dental History — DentalCare");
        this.patientId   = patientId;
        this.patientName = patientName;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);

        root.add(buildBanner(), BorderLayout.NORTH);

        JPanel body = buildBody();
        root.add(body, BorderLayout.CENTER);

        // Fade in banner
        Timer fade = new Timer(16, e -> {
            bannerAlpha = Math.min(1f, bannerAlpha + 0.03f);
            root.getComponent(0).repaint();
            if (bannerAlpha >= 1f) ((Timer)e.getSource()).stop();
        });
        fade.start();

        loadDentalHistory();
    }

    // ── Banner (dark header with emblem inline) ───────────────────────────────
    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bannerAlpha));
                int w=getWidth(), h=getHeight();
                g2.setPaint(new GradientPaint(0,0,C_BANNER,w,h,C_BG));
                g2.fillRect(0,0,w,h);
                // Crosshatch
                g2.setColor(new Color(255,255,255,4)); g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h);
                for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
                // Bottom gold rule
                g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(30,h-1,w-30,h-1);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(0, 90));
        banner.setBorder(new EmptyBorder(0,28,0,28));

        // Left: mini emblem + brand
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        left.setOpaque(false);
        JPanel emblem = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=16;
                g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.8f)); g2.drawOval(cx-r-3,cy-r-3,r*2+6,r*2+6);
                g2.setColor(new Color(188,152,90,100)); g2.setStroke(new BasicStroke(1.1f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));
                g2.fillOval(cx-r,cy-r,r*2,r*2);
                int arm=7,th=3; g2.setColor(C_GOLD);
                g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2); g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(40,40);}
        };
        emblem.setOpaque(false);
        JPanel brandStack=new JPanel(); brandStack.setLayout(new BoxLayout(brandStack,BoxLayout.Y_AXIS)); brandStack.setOpaque(false); brandStack.setBorder(new EmptyBorder(0,12,0,0));
        JLabel bl=new JLabel("DentalCare"); bl.setFont(new Font("Georgia",Font.BOLD,17)); bl.setForeground(C_IVORY);
        JLabel sl=new JLabel("Patient Dental History"); sl.setFont(new Font("Georgia",Font.ITALIC,11)); sl.setForeground(C_GOLD);
        brandStack.add(bl); brandStack.add(sl);
        left.add(emblem); left.add(brandStack);

        // Right: patient info + KPI pills
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        right.setOpaque(false);

        // Patient pill
        JPanel patPill = makePill(patientName+" · ID "+patientId, C_GOLD);
        recordCountLabel = new JLabel("0 records");
        recordCountLabel.setFont(new Font("Georgia",Font.BOLD,12)); recordCountLabel.setForeground(C_GOLD);
        JPanel recPill = makePill("Records: 0", new Color(60,100,165));

        // 3 stat pills
        JPanel treatPill = makePill("Treatments: 5", C_SUCCESS);
        JPanel xrayPill  = makePill("X-Rays: 5", C_WARNING);
        JPanel visitPill = makePill("Visits: 5", new Color(130,90,170));

        right.add(patPill); right.add(treatPill); right.add(xrayPill); right.add(visitPill);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JPanel makePill(String text, Color c) {
        JPanel pill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),75)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20); g2.dispose();
            }
        };
        pill.setOpaque(false); pill.setBorder(new EmptyBorder(4,11,4,11));
        JLabel l=new JLabel(text); l.setFont(new Font("Georgia",Font.BOLD,11)); l.setForeground(c);
        pill.add(l); return pill;
    }

    // ── Body ────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(C_BODY);
        body.setBorder(new EmptyBorder(14,28,10,28));

        // Sub-header row: title + filter
        JPanel subHdr = new JPanel(new BorderLayout());
        subHdr.setOpaque(false); subHdr.setBorder(new EmptyBorder(0,0,10,0));
        JLabel tl=new JLabel("Treatment History");
        tl.setFont(new Font("Georgia",Font.BOLD,18)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel("Complete chronological record");
        sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false);
        ts.add(tl); ts.add(sl);
        subHdr.add(ts,BorderLayout.WEST);

        // Filter row
        JPanel fr=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); fr.setOpaque(false);
        JLabel fl=new JLabel("Filter:"); fl.setFont(new Font("Georgia",Font.BOLD,12)); fl.setForeground(C_CHARCOAL);
        JComboBox<String> fcb=new JComboBox<>(new String[]{"All Records","Last 30 Days","Last 6 Months","Last Year","Treatment Type"});
        fcb.setFont(new Font("Georgia",Font.PLAIN,12)); fcb.setBackground(C_FIELD_BG); fcb.setForeground(C_CHARCOAL);
        fcb.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
        JButton rb=mkBtn("Refresh",false,null); rb.addActionListener(e->loadDentalHistory());
        fr.add(fl); fr.add(fcb); fr.add(rb);
        subHdr.add(fr,BorderLayout.EAST);
        body.add(subHdr, BorderLayout.NORTH);

        // Table
        body.add(buildTablePanel(), BorderLayout.CENTER);

        // Button bar
        body.add(buildButtonBar(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Georgia",Font.PLAIN,12)); historyTable.setRowHeight(30); historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0,1)); historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setBackground(C_TBL_ROW); historyTable.setForeground(C_CHARCOAL); historyTable.setFillsViewportHeight(true);
        JTableHeader hdr=historyTable.getTableHeader();
        hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        historyTable.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,9,0,6));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    setForeground(col==0?C_GOLD:C_CHARCOAL); setHorizontalAlignment(col==0?CENTER:LEFT);}
                return this;}
        });
        int[]widths={90,130,210,110,120,240};
        for(int i=0;i<widths.length;i++)historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        historyTable.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)viewRecordDetails();}});
        JScrollPane sc=new JScrollPane(historyTable); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        JPanel w=new JPanel(new BorderLayout()); w.setOpaque(false); w.add(sc,BorderLayout.CENTER); return w;
    }

    private JPanel buildButtonBar() {
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,0,4,0));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        JButton bAdd=mkBtn("+ Add Record",true,null);
        JButton bXray=mkBtn("View X-Rays",false,C_INFO);
        JButton bPrint=mkBtn("Print History",false,C_WARNING);
        JButton bExport=mkBtn("Export",false,null);
        bAdd.addActionListener(e->addDentalRecord()); bXray.addActionListener(e->viewXrays());
        bPrint.addActionListener(e->printHistory()); bExport.addActionListener(e->exportHistory());
        lb.add(bAdd);lb.add(bXray);lb.add(bPrint);lb.add(bExport);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->dispose()); rb.add(back);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    // ── Widget factories ─────────────────────────────────────────────────────
    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}
        return btn;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadDentalHistory() {
        tableModel.setRowCount(0);
        Object[][]d={
            {"2024-01-15","Routine Cleaning","Professional dental cleaning and examination","Bitewing X-rays","Dr. Smith","Good oral health maintained"},
            {"2023-12-10","Filling","Composite filling on tooth #14","Periapical X-ray","Dr. Johnson","Small cavity treated successfully"},
            {"2023-11-05","Root Canal","Root canal treatment on tooth #7","Full mouth X-rays","Dr. Smith","Treatment completed in 2 sessions"},
            {"2023-09-20","Crown","Porcelain crown placement on tooth #3","Bitewing X-rays","Dr. Johnson","Crown fits perfectly"},
            {"2023-08-12","Consultation","Initial consultation and treatment planning","Panoramic X-ray","Dr. Smith","Treatment plan established"}
        };
        for(Object[]r:d)tableModel.addRow(r);
        if(recordCountLabel!=null)recordCountLabel.setText("Records: "+tableModel.getRowCount());
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void addDentalRecord(){JOptionPane.showMessageDialog(this,"Add New Treatment Record\n\nThis would open a form to add a new record.","Add Record",JOptionPane.INFORMATION_MESSAGE);}
    private void viewXrays(){int r=historyTable.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a record first.");return;}JOptionPane.showMessageDialog(this,"X-Ray viewer for: "+tableModel.getValueAt(r,0)+"\nType: "+tableModel.getValueAt(r,3));}
    private void printHistory(){JOptionPane.showMessageDialog(this,"Printing history for "+patientName+" ("+tableModel.getRowCount()+" records)");}
    private void exportHistory(){JOptionPane.showMessageDialog(this,"Export as: PDF / Excel / CSV");}
    private void viewRecordDetails(){int r=historyTable.getSelectedRow();if(r<0)return;JOptionPane.showMessageDialog(this,"Date: "+tableModel.getValueAt(r,0)+"\nTreatment: "+tableModel.getValueAt(r,1)+"\nDescription: "+tableModel.getValueAt(r,2)+"\nX-Rays: "+tableModel.getValueAt(r,3)+"\nDentist: "+tableModel.getValueAt(r,4)+"\nNotes: "+tableModel.getValueAt(r,5),"Treatment Details",JOptionPane.INFORMATION_MESSAGE);}

    public static void main(String[]args){SwingUtilities.invokeLater(()->new DentalHistoryPage(18,"Yarden Biton").setVisible(true));}
}