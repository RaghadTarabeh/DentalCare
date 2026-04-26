package Boundary;

import Entity.Staff;
import Entity.Appointment;
import Control.AppointmentController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.Timer;

public class DentistUpcomingAppointments extends JFrame {

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
    private static final Color C_WARNING  = new Color(170, 130, 50);
    private static final Color C_DANGER   = new Color(150, 65,  55);
    private static final Color C_INFO     = new Color(60,  100, 165);

    private final Staff currentDentist;
    private JTable table;
    private DefaultTableModel tableModel;
    private java.util.List<Appointment> appointments = new ArrayList<>();
    private JLabel lblTotal, lblToday, lblScheduled, lblUrgent;
    private JButton btnView, btnComplete, btnReschedule, btnNotes;
    private float bannerAlpha = 0f;

    private static final String[] COLS = {"Date","Time","Patient","Treatment","Status","Priority"};

    public DentistUpcomingAppointments(Staff dentist) {
        this.currentDentist = dentist;
        setTitle("My Appointments — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);
        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);

        Timer fade=new Timer(16,e->{bannerAlpha=Math.min(1f,bannerAlpha+0.03f);root.getComponent(0).repaint();if(bannerAlpha>=1f)((Timer)e.getSource()).stop();});
        fade.start();

        loadAppointments();
    }

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
        banner.setOpaque(false); banner.setPreferredSize(new Dimension(0,86)); banner.setBorder(new EmptyBorder(0,28,0,28));

        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false);
        left.add(makeEmblem());
        JPanel bs=new JPanel(); bs.setLayout(new BoxLayout(bs,BoxLayout.Y_AXIS)); bs.setOpaque(false); bs.setBorder(new EmptyBorder(0,12,0,0));
        JLabel bl=new JLabel("My Appointments"); bl.setFont(new Font("Georgia",Font.BOLD,18)); bl.setForeground(C_IVORY);
        JLabel sl=new JLabel("Dr. "+currentDentist.getFirstName()+" "+currentDentist.getLastName()+" · Schedule Overview"); sl.setFont(new Font("Georgia",Font.ITALIC,11)); sl.setForeground(C_GOLD);
        bs.add(bl); bs.add(sl); left.add(bs);

        // KPI pills in banner
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        lblTotal    =new JLabel("0"); lblToday=new JLabel("0"); lblScheduled=new JLabel("0"); lblUrgent=new JLabel("0");
        right.add(makeLabelPill("Total",     lblTotal,    C_GOLD));
        right.add(makeLabelPill("Today",     lblToday,    C_INFO));
        right.add(makeLabelPill("Scheduled", lblScheduled,C_SUCCESS));
        right.add(makeLabelPill("Urgent",    lblUrgent,   C_DANGER));

        banner.add(left,BorderLayout.WEST); banner.add(right,BorderLayout.EAST);
        return banner;
    }

    private JPanel makeLabelPill(String label, JLabel val, Color c) {
        JPanel pill=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),75));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g2.dispose();}};
        pill.setOpaque(false); pill.setLayout(new BoxLayout(pill,BoxLayout.X_AXIS)); pill.setBorder(new EmptyBorder(4,10,4,10));
        JLabel lbl=new JLabel(label+" "); lbl.setFont(new Font("Georgia",Font.ITALIC,11)); lbl.setForeground(new Color(c.getRed(),c.getGreen(),c.getBlue(),180));
        val.setFont(new Font("Georgia",Font.BOLD,13)); val.setForeground(c);
        pill.add(lbl); pill.add(val); return pill;
    }

    private JPanel buildBody() {
        JPanel body=new JPanel(new BorderLayout()); body.setBackground(C_BODY); body.setBorder(new EmptyBorder(14,28,10,28));

        JPanel subHdr=new JPanel(new BorderLayout()); subHdr.setOpaque(false); subHdr.setBorder(new EmptyBorder(0,0,10,0));
        JLabel tl=new JLabel("Appointment Schedule"); tl.setFont(new Font("Georgia",Font.BOLD,17)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel(new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH).format(new Date())); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); ts.add(sl);
        subHdr.add(ts,BorderLayout.WEST);
        JPanel fr=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); fr.setOpaque(false);
        JComboBox<String> view=new JComboBox<>(new String[]{"All Upcoming","Today Only","This Week","Next 7 Days","Next 30 Days"});
        view.setFont(new Font("Georgia",Font.PLAIN,12)); view.setBackground(C_FIELD_BG); view.setForeground(C_CHARCOAL); view.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
        JButton rb=mkBtn("Refresh",false,null); rb.addActionListener(e->loadAppointments());
        fr.add(view); fr.add(rb); subHdr.add(fr,BorderLayout.EAST);
        body.add(subHdr,BorderLayout.NORTH);
        body.add(buildTablePanel(),BorderLayout.CENTER);
        body.add(buildButtonBar(),BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildTablePanel() {
        tableModel=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,12)); table.setRowHeight(30); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,9,0,6));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==0||col==1){setForeground(C_GOLD);setHorizontalAlignment(CENTER);}
                    else if(col==4&&v!=null){String s=v.toString();if(s.contains("Confirm")||s.contains("Schedule"))setForeground(C_SUCCESS);else if(s.contains("Urgent")||s.contains("Emerg"))setForeground(C_DANGER);else if(s.contains("Cancel"))setForeground(C_DANGER);else setForeground(C_CHARCOAL);setHorizontalAlignment(LEFT);}
                    else if(col==5&&v!=null){setForeground(v.toString().contains("High")||v.toString().contains("Urgent")?C_DANGER:C_CHARCOAL);setHorizontalAlignment(LEFT);}
                    else{setForeground(C_CHARCOAL);setHorizontalAlignment(LEFT);}
                } return this;}
        });
        int[]widths={88,62,155,200,125,115}; for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e->{ if(!e.getValueIsAdjusting())updateButtons(); });
        table.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)viewDetails();}});
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        JPanel w=new JPanel(new BorderLayout()); w.setOpaque(false); w.add(sc,BorderLayout.CENTER); return w;
    }

    private JPanel buildButtonBar(){
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,0,4,0));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        btnView     =mkBtn("View Details",false,C_INFO);
        btnNotes    =mkBtn("Add Notes",false,C_WARNING);
        btnReschedule=mkBtn("Reschedule",false,null);
        btnComplete =mkBtn("Mark Complete",true,null);
        btnView.addActionListener(e->viewDetails()); btnNotes.addActionListener(e->addNotes());
        btnReschedule.addActionListener(e->reschedule()); btnComplete.addActionListener(e->markComplete());
        lb.add(btnView);lb.add(btnNotes);lb.add(btnReschedule);lb.add(btnComplete);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton back=mkBtn("← Dashboard",false,null); back.addActionListener(e->dispose()); rb.add(back);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST);
        updateButtons(); return bar;
    }

    private void loadAppointments(){
        tableModel.setRowCount(0);
        // Sample data
        Object[][]d={
            {"20/07/2025","09:00","John Smith",   "Routine Cleaning",   "Scheduled","Normal"},
            {"20/07/2025","10:30","Sarah Johnson","Root Canal",          "Urgent",   "High"},
            {"21/07/2025","14:00","Mike Davis",   "Crown Fitting",       "Scheduled","Normal"},
            {"22/07/2025","11:15","Emma Wilson",  "Cavity Filling",      "Scheduled","Normal"},
            {"22/07/2025","16:30","David Brown",  "Emergency Treatment", "Urgent",   "High"},
            {"23/07/2025","13:00","Lisa Anderson","Teeth Whitening",     "Confirmed","Normal"},
            {"23/07/2025","15:45","Robert Taylor","Dental Checkup",      "Scheduled","Normal"}
        };
        for(Object[]r:d)tableModel.addRow(r);
        int tot=tableModel.getRowCount(),today=2,sched=5,urg=2;
        if(lblTotal!=null){lblTotal.setText(String.valueOf(tot));lblToday.setText(String.valueOf(today));lblScheduled.setText(String.valueOf(sched));lblUrgent.setText(String.valueOf(urg));}
        updateButtons();
    }

    private void updateButtons(){
        boolean has=table.getSelectedRow()>=0;
        if(btnView!=null){btnView.setEnabled(has);btnNotes.setEnabled(has);btnReschedule.setEnabled(has);btnComplete.setEnabled(has);}
    }
    private void viewDetails(){int r=table.getSelectedRow();if(r<0)return;JOptionPane.showMessageDialog(this,"Patient: "+tableModel.getValueAt(r,2)+"\nDate: "+tableModel.getValueAt(r,0)+" "+tableModel.getValueAt(r,1)+"\nTreatment: "+tableModel.getValueAt(r,3)+"\nStatus: "+tableModel.getValueAt(r,4),"Appointment Details",JOptionPane.INFORMATION_MESSAGE);}
    private void addNotes(){int r=table.getSelectedRow();if(r<0)return;JOptionPane.showMessageDialog(this,"Add clinical notes for: "+tableModel.getValueAt(r,2),"Add Notes",JOptionPane.INFORMATION_MESSAGE);}
    private void reschedule(){int r=table.getSelectedRow();if(r<0)return;JOptionPane.showMessageDialog(this,"Reschedule appointment for: "+tableModel.getValueAt(r,2),"Reschedule",JOptionPane.INFORMATION_MESSAGE);}
    private void markComplete(){int r=table.getSelectedRow();if(r<0)return;int ok=JOptionPane.showConfirmDialog(this,"Mark appointment for "+tableModel.getValueAt(r,2)+" as completed?","Confirm",JOptionPane.YES_NO_OPTION);if(ok==JOptionPane.YES_OPTION){tableModel.setValueAt("Completed",r,4);JOptionPane.showMessageDialog(this,"Appointment marked as completed.");}}

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JPanel makeEmblem(){JPanel e=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int cx=getWidth()/2,cy=getHeight()/2,r=16;g2.setColor(new Color(188,152,90,60));g2.setStroke(new BasicStroke(0.8f));g2.drawOval(cx-r-3,cy-r-3,r*2+6,r*2+6);g2.setColor(new Color(188,152,90,100));g2.setStroke(new BasicStroke(1.1f));g2.drawOval(cx-r,cy-r,r*2,r*2);g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));g2.fillOval(cx-r,cy-r,r*2,r*2);int arm=7,th=3;g2.setColor(C_GOLD);g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2);g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);g2.dispose();}@Override public Dimension getPreferredSize(){return new Dimension(40,40);}};e.setOpaque(false);return e;}
    private JButton mkBtn(String text,boolean primary,Color accent){JButton btn=new JButton(text);btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));btn.setFocusPainted(false);btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));btn.setOpaque(true);Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}return btn;}
}