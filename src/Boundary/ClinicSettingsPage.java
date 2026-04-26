package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class ClinicSettingsPage extends JFrame {

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
    private static final Color C_SUCCESS   = new Color(60,  130, 90);
    private static final Color C_WARNING   = new Color(170, 130, 50);
    private static final Color C_DANGER    = new Color(155, 65,  55);
    private static final Color C_INFO      = new Color(60,  100, 165);

    // ── Operating Hours ───────────────────────────────────────────────────────
    private JComboBox<String>[] openCbs, closeCbs;
    private JCheckBox[] offDays;

    // ── System Preferences ───────────────────────────────────────────────────
    private JTextField txtClinicName, txtClinicPhone, txtClinicEmail;
    private JTextArea  txtAddress;
    private JComboBox<String> cbLanguage, cbTimeFormat, cbDateFormat;
    private JSlider sliderReminder;
    private JCheckBox chkEmail, chkSMS;

    // ── Permissions ───────────────────────────────────────────────────────────
    private JCheckBox chkSecBook, chkSecCancel, chkSecInv;
    private JCheckBox chkDentViewAll, chkDentEdit;
    private JCheckBox chkPatCancel, chkPatReschedule;

    // ── Backup ───────────────────────────────────────────────────────────────
    private JComboBox<String> cbBackupFreq;
    private JTextField txtBackupLoc;
    private JCheckBox chkAutoBackup;
    private JLabel lblLastBackup;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] TIME_SLOTS = {
        "Closed","07:00","07:30","08:00","08:30","09:00","09:30","10:00","10:30",
        "11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30",
        "15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30",
        "19:00","19:30","20:00","20:30","21:00","21:30","22:00"
    };
    private static final String[] DAYS = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

    @SuppressWarnings("unchecked")
    public ClinicSettingsPage() {
        setTitle("Clinic Settings — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialise arrays before building UI
        openCbs  = new JComboBox[7];
        closeCbs = new JComboBox[7];
        offDays  = new JCheckBox[7];
        for (int i=0;i<7;i++){
            openCbs[i]=makeCombo(TIME_SLOTS);
            closeCbs[i]=makeCombo(TIME_SLOTS);
            offDays[i]=makeCheck("Closed");
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        // Left panel
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(240,0); }
        };
        left.setBackground(C_LEFT_TOP);
        root.add(left, BorderLayout.WEST);

        JPanel div = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,30,new Color(188,152,90,0),0,getHeight()*.5f,new Color(188,152,90,150),false));
                g2.fillRect(0,0,1,getHeight()); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){ return new Dimension(1,0); }
        };
        div.setOpaque(false);
        root.add(div, BorderLayout.CENTER);

        JPanel right = buildRight();
        right.setPreferredSize(new Dimension(858, 660));
        root.add(right, BorderLayout.EAST);

        Timer fade=new Timer(16,e->{ alpha=Math.min(1f,alpha+0.025f); left.repaint(); if(alpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt=new Timer(30,e->{ pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadDefaults();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-55;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,120,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r=50f+pulse*2f;
        paintEmblem(g2,cx,cy,r);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,18));
        FontMetrics fm=g2.getFontMetrics(); String br="DentalCare";
        g2.drawString(br,cx-fm.stringWidth(br)/2,cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry=cy+(int)r+40; g2.drawLine(cx-55,ry,cx+55,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Clinic Settings"; g2.drawString(sub,cx-fm.stringWidth(sub)/2,ry+17);

        // 4 setting category pills
        Color[]cc={C_INFO,new Color(80,160,110),C_WARNING,C_DANGER};
        String[]cl={"Operating Hours","System Prefs","Permissions","Backup"};
        int py=ry+36;
        for(int i=0;i<4;i++){
            int lyi=py+i*17;
            g2.setColor(new Color(cc[i].getRed(),cc[i].getGreen(),cc[i].getBlue(),40)); g2.fillRoundRect(cx-58,lyi,116,14,4,4);
            g2.setColor(cc[i]); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
            g2.drawString(cl[i],cx-fm.stringWidth(cl[i])/2,lyi+10);
        }

        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy,cx-fm.stringWidth(copy)/2,h-16);
    }

    private void paintEmblem(Graphics2D g2,int cx,int cy,float r){
        g2.setColor(new Color(188,152,90,40));g2.setStroke(new BasicStroke(0.7f));g2.drawOval((int)(cx-r-11),(int)(cy-r-11),(int)(r*2+22),(int)(r*2+22));
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

    // ── Right panel ──────────────────────────────────────────────────────────
    private JPanel buildRight(){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar
        JPanel top=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(22,getHeight()-1,getWidth()-22,getHeight()-1);g2.dispose();}};
        top.setOpaque(false); top.setBorder(new EmptyBorder(13,24,10,24));
        JLabel tl=new JLabel("Clinic Settings"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel("Configure system preferences, hours, permissions and backups"); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts,BorderLayout.WEST); p.add(top,BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs=new JTabbedPane();
        tabs.setBackground(C_RIGHT); tabs.setOpaque(true);
        tabs.setFont(new Font("Georgia",Font.BOLD,13));
        tabs.addTab("Operating Hours",   buildHoursTab());
        tabs.addTab("System Preferences",buildPrefsTab());
        tabs.addTab("User Permissions",  buildPermsTab());
        tabs.addTab("Backup",            buildBackupTab());
        tabs.setBorder(new EmptyBorder(0,24,6,24));
        p.add(tabs,BorderLayout.CENTER);

        // Button bar
        p.add(buildButtonBar(),BorderLayout.SOUTH);
        return p;
    }

    // ── Tab: Operating Hours ─────────────────────────────────────────────────
    private JPanel buildHoursTab(){
        JPanel panel=new JPanel(); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(14,0,10,0));
        for(int i=0;i<7;i++){
            final int idx=i;
            JPanel row=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);g2.setColor(new Color(188,152,90,50));g2.setStroke(new BasicStroke(0.7f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);g2.dispose();}};
            row.setOpaque(false); row.setLayout(new FlowLayout(FlowLayout.LEFT,10,8));
            JLabel dl=new JLabel(DAYS[i]); dl.setFont(new Font("Georgia",Font.BOLD,13)); dl.setForeground(C_CHARCOAL); dl.setPreferredSize(new Dimension(90,20));
            JLabel ol=new JLabel("Open:");  ol.setFont(new Font("Georgia",Font.PLAIN,12)); ol.setForeground(C_MUTED);
            JLabel cl=new JLabel("Close:"); cl.setFont(new Font("Georgia",Font.PLAIN,12)); cl.setForeground(C_MUTED);
            row.add(dl); row.add(ol); row.add(openCbs[idx]); row.add(cl); row.add(closeCbs[idx]); row.add(offDays[idx]);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(row); panel.add(Box.createRigidArea(new Dimension(0,4)));
        }
        JScrollPane sc=new JScrollPane(panel); sc.setBorder(null); sc.getViewport().setBackground(C_RIGHT); sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    // ── Tab: System Preferences ──────────────────────────────────────────────
    private JPanel buildPrefsTab(){
        JPanel panel=new JPanel(); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(14,0,10,0));

        // Clinic info card
        panel.add(sectionTitle("Clinic Information"));
        panel.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel clinicCard=makeCard(); clinicCard.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(5,8,5,12); g.anchor=GridBagConstraints.WEST;
        txtClinicName=makeField("DentalCare Clinic"); txtClinicPhone=makeField("+972-4-123-4567"); txtClinicEmail=makeField("info@dentalcare.co.il");
        txtAddress=new JTextArea("123 Herzl Street\nHaifa, Israel 31000",2,20);
        txtAddress.setFont(new Font("Georgia",Font.PLAIN,12));txtAddress.setBackground(C_FIELD_BG);txtAddress.setForeground(C_CHARCOAL);txtAddress.setCaretColor(C_CHARCOAL);txtAddress.setLineWrap(true);txtAddress.setWrapStyleWord(true);txtAddress.setBorder(new EmptyBorder(5,7,5,7));
        addRow(clinicCard,g,0,"Clinic Name",txtClinicName,"Phone",txtClinicPhone);
        addRow(clinicCard,g,1,"Email",txtClinicEmail,"Address",new JScrollPane(txtAddress){{setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));}});
        panel.add(clinicCard); panel.add(Box.createRigidArea(new Dimension(0,12)));

        // System config card
        panel.add(sectionTitle("System Configuration"));
        panel.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel sysCard=makeCard(); sysCard.setLayout(new GridBagLayout());
        cbLanguage=makeCombo(new String[]{"English","Hebrew","Arabic"});
        cbTimeFormat=makeCombo(new String[]{"24 Hour","12 Hour AM/PM"});
        cbDateFormat=makeCombo(new String[]{"DD/MM/YYYY","MM/DD/YYYY","YYYY-MM-DD"});
        addRow(sysCard,g,0,"Language",cbLanguage,"Time Format",cbTimeFormat);
        g.gridx=0;g.gridy=1;sysCard.add(makeLbl("Date Format"),g);g.gridx=1;sysCard.add(cbDateFormat,g);
        panel.add(sysCard); panel.add(Box.createRigidArea(new Dimension(0,12)));

        // Notification card
        panel.add(sectionTitle("Notifications"));
        panel.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel notCard=makeCard(); notCard.setLayout(new GridBagLayout());
        sliderReminder=new JSlider(1,72,24); sliderReminder.setMajorTickSpacing(12); sliderReminder.setPaintTicks(true); sliderReminder.setPaintLabels(true); sliderReminder.setOpaque(false);
        chkEmail=makeCheck("Send Email Reminders"); chkSMS=makeCheck("Send SMS Reminders");
        g.gridx=0;g.gridy=0;notCard.add(makeLbl("Reminder Hours"),g);g.gridx=1;notCard.add(sliderReminder,g);
        g.gridx=0;g.gridy=1;g.gridwidth=2;notCard.add(chkEmail,g);g.gridy=2;notCard.add(chkSMS,g);g.gridwidth=1;
        panel.add(notCard);

        JScrollPane sc=new JScrollPane(panel); sc.setBorder(null); sc.getViewport().setBackground(C_RIGHT); sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    // ── Tab: Permissions ─────────────────────────────────────────────────────
    private JPanel buildPermsTab(){
        JPanel panel=new JPanel(); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(14,0,10,0));
        chkSecBook=makeCheck("Can book appointments"); chkSecCancel=makeCheck("Can cancel appointments"); chkSecInv=makeCheck("Can manage inventory");
        chkDentViewAll=makeCheck("Can view all patient records"); chkDentEdit=makeCheck("Can edit own profile");
        chkPatCancel=makeCheck("Can cancel own appointments"); chkPatReschedule=makeCheck("Can reschedule appointments");

        String[]sections={"Secretary Permissions","Dentist Permissions","Patient Permissions"};
        JCheckBox[][]perms={{chkSecBook,chkSecCancel,chkSecInv},{chkDentViewAll,chkDentEdit},{chkPatCancel,chkPatReschedule}};
        for(int s=0;s<3;s++){
            panel.add(sectionTitle(sections[s]));
            panel.add(Box.createRigidArea(new Dimension(0,6)));
            JPanel card=makeCard(); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
            for(JCheckBox cb:perms[s]){card.add(cb);card.add(Box.createRigidArea(new Dimension(0,6)));}
            panel.add(card); panel.add(Box.createRigidArea(new Dimension(0,12)));
        }
        JScrollPane sc=new JScrollPane(panel); sc.setBorder(null); sc.getViewport().setBackground(C_RIGHT); sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    // ── Tab: Backup ──────────────────────────────────────────────────────────
    private JPanel buildBackupTab(){
        JPanel panel=new JPanel(); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(14,0,10,0));
        panel.add(sectionTitle("Backup Configuration"));
        panel.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel card=makeCard(); card.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,8,7,12); g.anchor=GridBagConstraints.WEST;
        cbBackupFreq=makeCombo(new String[]{"Daily","Weekly","Monthly","Manual Only"});
        txtBackupLoc=makeField("C:\\DentalCare\\Backups\\");
        chkAutoBackup=makeCheck("Enable automatic backups");
        lblLastBackup=makeLbl("Last backup: Never");
        g.gridx=0;g.gridy=0;card.add(makeLbl("Backup Frequency"),g);g.gridx=1;card.add(cbBackupFreq,g);
        g.gridx=0;g.gridy=1;card.add(makeLbl("Backup Location"),g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1.0;card.add(txtBackupLoc,g);g.fill=GridBagConstraints.NONE;g.weightx=0;
        g.gridx=0;g.gridy=2;g.gridwidth=2;card.add(chkAutoBackup,g);
        g.gridy=3;card.add(lblLastBackup,g);g.gridwidth=1;
        panel.add(card);
        JScrollPane sc=new JScrollPane(panel); sc.setBorder(null); sc.getViewport().setBackground(C_RIGHT);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    // ── Button bar ───────────────────────────────────────────────────────────
    private JPanel buildButtonBar(){
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(22,0,getWidth()-22,0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(7,24,12,24));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        JButton bSave=mkBtn("Save Settings",true,null);
        JButton bReset=mkBtn("Restore Defaults",false,C_WARNING);
        JButton bBackup=mkBtn("Backup Now",false,C_INFO);
        JButton bEmail=mkBtn("Test Email",false,null);
        bSave.addActionListener(e->JOptionPane.showMessageDialog(this,"All settings saved successfully.","Saved",JOptionPane.INFORMATION_MESSAGE));
        bReset.addActionListener(e->{int ok=JOptionPane.showConfirmDialog(this,"Restore all settings to defaults?","Confirm",JOptionPane.YES_NO_OPTION);if(ok==JOptionPane.YES_OPTION){loadDefaults();JOptionPane.showMessageDialog(this,"Defaults restored.","Done",JOptionPane.INFORMATION_MESSAGE);}});
        bBackup.addActionListener(e->{lblLastBackup.setText("Last backup: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));JOptionPane.showMessageDialog(this,"Backup completed successfully.","Done",JOptionPane.INFORMATION_MESSAGE);});
        bEmail.addActionListener(e->JOptionPane.showMessageDialog(this,"Test email sent to: "+txtClinicEmail.getText(),"Email Test",JOptionPane.INFORMATION_MESSAGE));
        lb.add(bSave);lb.add(bReset);lb.add(bBackup);lb.add(bEmail);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->{dispose();try{new ClinicManagerPage().setVisible(true);}catch(Exception ignored){}});
        rb.add(back); bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void addRow(JPanel p,GridBagConstraints g,int row,String l1,JComponent f1,String l2,JComponent f2){
        g.gridx=0;g.gridy=row;p.add(makeLbl(l1),g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.4;p.add(f1,g);
        g.fill=GridBagConstraints.NONE;g.weightx=0;g.gridx=2;p.add(makeLbl(l2),g);g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.4;p.add(f2,g);
        g.fill=GridBagConstraints.NONE;g.weightx=0;
    }

    private JPanel sectionTitle(String text){
        JPanel w=new JPanel(); w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS)); w.setOpaque(false); w.setAlignmentX(Component.LEFT_ALIGNMENT); w.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        JLabel l=new JLabel(text); l.setFont(new Font("Georgia",Font.BOLD,15)); l.setForeground(C_CHARCOAL); l.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(l);
        w.add(Box.createRigidArea(new Dimension(0,3)));
        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setMaximumSize(new Dimension(Integer.MAX_VALUE,5)); rule.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(rule); return w;
    }

    private JPanel makeCard(){
        JPanel c=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        c.setOpaque(false); c.setBorder(new EmptyBorder(12,16,12,16)); c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE)); return c;
    }

    private JLabel makeLbl(String t){JLabel l=new JLabel(t);l.setFont(new Font("Georgia",Font.BOLD,13));l.setForeground(C_CHARCOAL);return l;}
    private JTextField makeField(String v){JTextField f=new JTextField(v,16);f.setFont(new Font("Georgia",Font.PLAIN,12));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));return f;}
    @SuppressWarnings("unchecked")
    private JComboBox<String> makeCombo(String[]items){JComboBox<String> cb=new JComboBox<>(items);cb.setFont(new Font("Georgia",Font.PLAIN,12));cb.setBackground(C_FIELD_BG);cb.setForeground(C_CHARCOAL);cb.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));return cb;}
    private JCheckBox makeCheck(String t){JCheckBox cb=new JCheckBox(t);cb.setFont(new Font("Georgia",Font.PLAIN,13));cb.setForeground(C_CHARCOAL);cb.setBackground(C_CARD_BG);cb.setOpaque(false);cb.setFocusPainted(false);return cb;}

    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}
        return btn;
    }

    private void loadDefaults(){
        // Hours
        String[]opens={"08:00","08:00","08:00","08:00","08:00","Closed","Closed"};
        String[]closes={"18:00","18:00","18:00","18:00","16:00","Closed","Closed"};
        for(int i=0;i<7;i++){
            openCbs[i].setSelectedItem(opens[i]);
            closeCbs[i].setSelectedItem(closes[i]);
            offDays[i].setSelected("Closed".equals(opens[i]));
        }
        // Prefs
        if(cbLanguage!=null)cbLanguage.setSelectedItem("English");
        if(cbTimeFormat!=null)cbTimeFormat.setSelectedItem("24 Hour");
        if(cbDateFormat!=null)cbDateFormat.setSelectedItem("DD/MM/YYYY");
        if(chkEmail!=null)chkEmail.setSelected(true);
        if(chkSMS!=null)chkSMS.setSelected(false);
        // Permissions
        if(chkSecBook!=null){chkSecBook.setSelected(true);chkSecCancel.setSelected(true);chkSecInv.setSelected(false);}
        if(chkDentViewAll!=null){chkDentViewAll.setSelected(false);chkDentEdit.setSelected(true);}
        if(chkPatCancel!=null){chkPatCancel.setSelected(true);chkPatReschedule.setSelected(true);}
        // Backup
        if(cbBackupFreq!=null){cbBackupFreq.setSelectedItem("Weekly");chkAutoBackup.setSelected(true);
            lblLastBackup.setText("Last backup: "+LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));}
    }

    public static void main(String[]args){SwingUtilities.invokeLater(()->new ClinicSettingsPage().setVisible(true));}
}