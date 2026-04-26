package Boundary;

import Entity.Patient;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class PatientPage extends JFrame {

    private static final Color C_HERO_TOP = new Color(26,28,32);
    private static final Color C_HERO_BOT = new Color(15,17,20);
    private static final Color C_BODY     = new Color(245,242,236);
    private static final Color C_GOLD     = new Color(188,152,90);
    private static final Color C_IVORY    = new Color(250,247,241);
    private static final Color C_CHARCOAL = new Color(35,36,40);
    private static final Color C_MUTED    = new Color(110,106,98);
    private static final Color C_CARD_BG  = new Color(237,233,224);

    private static final Color[] ACCENTS = {
        new Color(91,143,168), new Color(90,158,122), new Color(188,152,90),
        new Color(138,114,184), new Color(78,158,168), new Color(184,106,106),
    };

    private final Patient currentPatient;
    private float heroAlpha = 0f;

    public PatientPage() {
        JOptionPane.showMessageDialog(this,"No patient information provided.","Error",JOptionPane.ERROR_MESSAGE);
        dispose(); currentPatient=null;
    }

    public PatientPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("DentalCare — Patient Portal");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1060, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_HERO_BOT);
        setContentPane(root);
        root.add(buildHero(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        Timer fade = new Timer(16, e -> {
            heroAlpha = Math.min(1f,heroAlpha+0.03f);
            root.getComponent(0).repaint();
            if(heroAlpha>=1f)((Timer)e.getSource()).stop();
        });
        fade.start();
    }

    // ── Hero ─────────────────────────────────────────────────────────────────
    private JPanel buildHero() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,heroAlpha));
                int w=getWidth(),h=getHeight();
                g2.setPaint(new GradientPaint(0,0,C_HERO_TOP,w,h,C_HERO_BOT)); g2.fillRect(0,0,w,h);
                g2.setColor(new Color(255,255,255,13)); g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h);
                for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(28,h-1,w-28,h-1);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setPreferredSize(new Dimension(0,140));
        hero.setBorder(new EmptyBorder(0,28,0,28));

        // Top micro bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false); topBar.setBorder(new EmptyBorder(10,0,0,0));
        topBar.setPreferredSize(new Dimension(0,20));
        JLabel brandLbl=new JLabel("DentalCare  ·  Patient Portal");
        brandLbl.setFont(new Font("Georgia",Font.ITALIC,10)); brandLbl.setForeground(new Color(188,152,90,85));
        String dateStr=LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",java.util.Locale.ENGLISH));
        String timeStr=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLbl=new JLabel(dateStr+"  ·  "+timeStr);
        timeLbl.setFont(new Font("Georgia",Font.ITALIC,10)); timeLbl.setForeground(new Color(80,76,70));
        topBar.add(brandLbl,BorderLayout.WEST); topBar.add(timeLbl,BorderLayout.EAST);

        // Main content row — use GridBagLayout so pills are NEVER squeezed
        JPanel mainRow = new JPanel(new GridBagLayout());
        mainRow.setOpaque(false); mainRow.setBorder(new EmptyBorder(10,0,12,0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.NONE;
        gc.insets = new Insets(0,0,0,0);

        // Avatar — fixed width
        JPanel avatar = buildAvatar();
        gc.gridx=0; gc.gridy=0; gc.weightx=0;
        mainRow.add(avatar, gc);

        // Name block — takes remaining space but never pushes pills
        JPanel nameBlock = buildNameBlock();
        gc.gridx=1; gc.gridy=0; gc.weightx=1.0; gc.fill=GridBagConstraints.HORIZONTAL;
        mainRow.add(nameBlock, gc);
        gc.fill=GridBagConstraints.NONE; gc.weightx=0;

        // 4 pills — each fixed size, right-aligned, GUARANTEED space
        Color[]pc={new Color(188,152,90),new Color(60,100,165),new Color(60,130,90),new Color(138,114,184)};
        String[]pv={String.valueOf(currentPatient.getAge()),"3","2","₪320"};
        String[]pl={"Age","Appts","Plans","Due"};
        for(int i=0;i<4;i++){
            gc.gridx=2+i; gc.gridy=0; gc.insets=new Insets(0,i==0?16:6,0,0);
            mainRow.add(makeStatPill(pv[i],pl[i],pc[i]), gc);
        }

        hero.add(topBar,  BorderLayout.NORTH);
        hero.add(mainRow, BorderLayout.CENTER);
        return hero;
    }

    private JPanel buildAvatar() {
        return new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=32;
                g2.setColor(new Color(188,152,90,22)); g2.drawOval(cx-r-5,cy-r-5,(r+5)*2,(r+5)*2);
                g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},
                    new Color[]{new Color(188,152,90,30),new Color(188,152,90,12)}));
                g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(new Color(188,152,90,80)); g2.setStroke(new BasicStroke(1.1f));
                g2.drawOval(cx-r,cy-r,r*2,r*2);
                String ini=getInitials(); g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,22));
                FontMetrics fm=g2.getFontMetrics(); g2.drawString(ini,cx-fm.stringWidth(ini)/2,cy+fm.getAscent()/2-1);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(74,74);}
            @Override public Dimension getMinimumSize(){return new Dimension(74,74);}
            @Override public Dimension getMaximumSize(){return new Dimension(74,74);}
        };
    }

    private JPanel buildNameBlock() {
        JPanel nb=new JPanel(); nb.setLayout(new BoxLayout(nb,BoxLayout.Y_AXIS)); nb.setOpaque(false); nb.setBorder(new EmptyBorder(2,16,0,0));

        JLabel welcomeLbl=new JLabel("Welcome back,");
        welcomeLbl.setFont(new Font("Georgia",Font.ITALIC,12)); welcomeLbl.setForeground(new Color(106,101,96));

        JLabel nameLbl=new JLabel(currentPatient.getFullName());
        nameLbl.setFont(new Font("Georgia",Font.BOLD,24)); nameLbl.setForeground(C_IVORY);

        // Badge
        JPanel badge=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,130,90,22)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(60,130,90,80)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.setColor(new Color(80,155,105)); g2.fillOval(8,getHeight()/2-3,6,6);
                g2.setColor(new Color(80,155,105)); g2.setFont(new Font("Georgia",Font.BOLD,10));
                String t="Active Patient  ·  ID: "+currentPatient.getPatientID();
                FontMetrics fm=g2.getFontMetrics(); g2.drawString(t,18,getHeight()/2+4); g2.dispose();
            }
        };
        badge.setOpaque(false);
        FontMetrics fm2=badge.getFontMetrics(new Font("Georgia",Font.BOLD,10));
        String t2="Active Patient  ·  ID: "+currentPatient.getPatientID();
        Dimension bd=new Dimension(fm2.stringWidth(t2)+28,22);
        badge.setPreferredSize(bd); badge.setMaximumSize(bd);

        nb.add(welcomeLbl); nb.add(Box.createRigidArea(new Dimension(0,2)));
        nb.add(nameLbl);    nb.add(Box.createRigidArea(new Dimension(0,6)));
        nb.add(badge);
        return nb;
    }

    private JPanel makeStatPill(String val, String label, Color c) {
        JPanel pill=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),60)); g2.setStroke(new BasicStroke(0.7f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); g2.dispose();
            }
        };
        pill.setOpaque(false); pill.setLayout(new BoxLayout(pill,BoxLayout.Y_AXIS)); pill.setBorder(new EmptyBorder(8,16,8,16));
        Dimension ps=new Dimension(80,54);
        pill.setPreferredSize(ps); pill.setMinimumSize(ps); pill.setMaximumSize(ps);
        JLabel vl=new JLabel(val); vl.setFont(new Font("Georgia",Font.BOLD,16)); vl.setForeground(c); vl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ll=new JLabel(label); ll.setFont(new Font("Georgia",Font.ITALIC,10)); ll.setForeground(new Color(c.getRed(),c.getGreen(),c.getBlue(),160)); ll.setAlignmentX(Component.CENTER_ALIGNMENT);
        pill.add(vl); pill.add(Box.createRigidArea(new Dimension(0,2))); pill.add(ll);
        return pill;
    }

    private String getInitials(){
        String fn=currentPatient.getFirstName(),ln=currentPatient.getLastName();
        return ((fn!=null&&!fn.isEmpty()?fn.substring(0,1):"?")+(ln!=null&&!ln.isEmpty()?ln.substring(0,1):"?")).toUpperCase();
    }

    // ── Body ─────────────────────────────────────────────────────────────────
    private JPanel buildBody(){
        JPanel body=new JPanel(new BorderLayout()); body.setBackground(C_BODY);

        JPanel secRow=new JPanel(new BorderLayout()); secRow.setBackground(C_BODY); secRow.setBorder(new EmptyBorder(10,28,6,28));
        JLabel secLbl=new JLabel("Your services"); secLbl.setFont(new Font("Georgia",Font.ITALIC,12)); secLbl.setForeground(C_MUTED);
        JPanel secRule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.7f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        secRule.setOpaque(false); secRule.setBorder(new EmptyBorder(0,10,0,0));
        secRow.add(secLbl,BorderLayout.WEST); secRow.add(secRule,BorderLayout.CENTER);
        body.add(secRow,BorderLayout.NORTH);

        JPanel listWrap=new JPanel(); listWrap.setLayout(new BoxLayout(listWrap,BoxLayout.Y_AXIS));
        listWrap.setBackground(C_BODY); listWrap.setBorder(new EmptyBorder(0,20,8,20));

        String[][]services={
            {"Patient Profile",   "View and update your personal & insurance information"},
            {"Book Appointment",  "Schedule your next dental visit at a convenient time"},
            {"My Appointments",   "View, manage and track all your upcoming appointments"},
            {"Treatment Plans",   "Track your ongoing dental treatments and progress"},
            {"Payment & Billing", "View invoices, outstanding balance and payment history"},
            {"Medical History",   "Access your complete dental and medical health records"},
        };
        int[]iconTypes={4,2,0,3,5,1};
        Runnable[]actions={this::openPatientProfile,this::openAppointmentBooking,this::openMyAppointments,this::openTreatmentPlans,this::openBilling,this::openMedicalHistory};

        for(int i=0;i<6;i++){
            listWrap.add(buildServiceRow(services[i][0],iconTypes[i],services[i][1],ACCENTS[i],actions[i]));
            if(i<5)listWrap.add(Box.createRigidArea(new Dimension(0,6)));
        }
        body.add(listWrap,BorderLayout.CENTER);
        body.add(buildFooter(),BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildServiceRow(String title,int iconType,String desc,Color accent,Runnable action){
        JPanel row=new JPanel(){
            boolean hover=false;
            {setLayout(new BorderLayout());setBackground(C_CARD_BG);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){hover=true;repaint();}
                @Override public void mouseExited(MouseEvent e) {hover=false;repaint();}
                @Override public void mouseClicked(MouseEvent e){action.run();}
             });}
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                g2.setColor(hover?new Color(228,222,210):C_CARD_BG); g2.fillRoundRect(0,0,w,h,9,9);
                g2.setColor(hover?new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),90):new Color(188,152,90,35));
                g2.setStroke(new BasicStroke(hover?1f:0.7f)); g2.drawRoundRect(0,0,w-1,h-1,9,9);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),hover?180:80)); g2.fillRoundRect(0,0,4,h,3,3);
                g2.dispose(); super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(Integer.MAX_VALUE,60));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconWrap=new JPanel(new GridBagLayout()); iconWrap.setOpaque(false); iconWrap.setPreferredSize(new Dimension(60,60));
        JPanel iconBubble=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=17;
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18)); g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),55)); g2.setStroke(new BasicStroke(0.7f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(accent); g2.setStroke(new BasicStroke(1.4f)); int ir=9;
                switch(iconType){
                    case 0:g2.drawOval(cx-ir,cy-ir,ir*2,ir*2);break;
                    case 1:g2.drawRoundRect(cx-ir,cy-ir,ir*2,ir*2,3,3);g2.setStroke(new BasicStroke(1.2f));g2.drawLine(cx-4,cy,cx+4,cy);g2.drawLine(cx,cy-4,cx,cy+4);break;
                    case 2:g2.drawOval(cx-ir,cy-ir,ir*2,ir*2);g2.fillOval(cx-3,cy-3,7,7);break;
                    case 3:g2.drawPolygon(new int[]{cx,cx+ir,cx,cx-ir},new int[]{cy-ir,cy,cy+ir,cy},4);break;
                    case 4:g2.drawOval(cx-4,cy-ir,9,9);g2.drawArc(cx-ir,cy+1,ir*2,10,0,180);break;
                    case 5:g2.drawRoundRect(cx-6,cy-ir,12,ir*2,2,2);g2.setStroke(new BasicStroke(1.1f));g2.drawLine(cx-4,cy-4,cx+4,cy-4);g2.drawLine(cx-4,cy,cx+4,cy);g2.drawLine(cx-4,cy+4,cx+2,cy+4);break;
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(38,38);}
        };
        iconBubble.setOpaque(false);
        iconWrap.add(iconBubble);

        JPanel textBlock=new JPanel(); textBlock.setLayout(new BoxLayout(textBlock,BoxLayout.Y_AXIS)); textBlock.setOpaque(false); textBlock.setBorder(new EmptyBorder(0,2,0,0));
        JLabel tl=new JLabel(title); tl.setFont(new Font("Georgia",Font.BOLD,14)); tl.setForeground(C_CHARCOAL);
        JLabel dl=new JLabel(desc);   dl.setFont(new Font("Georgia",Font.PLAIN,11)); dl.setForeground(C_MUTED);
        textBlock.add(tl); textBlock.add(Box.createRigidArea(new Dimension(0,2))); textBlock.add(dl);

        JLabel chev=new JLabel("›"); chev.setFont(new Font("Georgia",Font.PLAIN,22)); chev.setForeground(new Color(188,152,90,80)); chev.setBorder(new EmptyBorder(0,0,0,16));

        row.add(iconWrap, BorderLayout.WEST);
        row.add(textBlock,BorderLayout.CENTER);
        row.add(chev,     BorderLayout.EAST);
        return row;
    }

    private JPanel buildFooter(){
        JPanel bar=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,40));g2.setStroke(new BasicStroke(0.7f));g2.drawLine(24,0,getWidth()-24,0);g2.dispose();}};
        bar.setOpaque(false); bar.setBorder(new EmptyBorder(7,28,10,28));
        JLabel copy=new JLabel("Patient ID: "+currentPatient.getPatientID()+"  ·  © 2025 DentalCare System");
        copy.setFont(new Font("Georgia",Font.ITALIC,10)); copy.setForeground(C_MUTED);
        JButton back=new JButton("← Back to Main Menu"); back.setFont(new Font("Georgia",Font.PLAIN,11)); back.setForeground(C_MUTED); back.setBackground(new Color(228,223,213));
        back.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,70),1),new EmptyBorder(5,13,5,13)));
        back.setFocusPainted(false); back.setOpaque(true); back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){back.setForeground(C_CHARCOAL);back.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(5,13,5,13)));}@Override public void mouseExited(MouseEvent e){back.setForeground(C_MUTED);back.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,70),1),new EmptyBorder(5,13,5,13)));}});
        back.addActionListener(e->{dispose();try{MainMenu.showMainMenu();}catch(Exception ex){System.exit(0);}});
        bar.add(copy,BorderLayout.WEST); bar.add(back,BorderLayout.EAST);
        return bar;
    }

    private void navigateTo(java.util.function.Supplier<JFrame> factory,String name){
        this.setVisible(false);
        try{JFrame p=factory.get();p.setVisible(true);p.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){PatientPage.this.setVisible(true);}});}
        catch(Exception e){JOptionPane.showMessageDialog(this,name+" — coming soon!","Info",JOptionPane.INFORMATION_MESSAGE);this.setVisible(true);}
    }
    private void openPatientProfile()     {navigateTo(()->new PatientProfilePage(currentPatient),"Patient Profile");}
    private void openAppointmentBooking() {navigateTo(()->new AppointmentBookingPage(currentPatient),"Book Appointment");}
    private void openMyAppointments()     {navigateTo(()->new MyAppointmentsPage(currentPatient),"My Appointments");}
    private void openTreatmentPlans()     {navigateTo(()->new TreatmentPlansPage(currentPatient),"Treatment Plans");}
    private void openBilling()            {navigateTo(()->new BillingPage(currentPatient),"Billing");}
    private void openMedicalHistory()     {navigateTo(()->new MedicalHistoryPage(currentPatient),"Medical History");}

    public static void main(String[]args){
        SwingUtilities.invokeLater(()->{
            try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
            new PatientPage(new Patient(1,"Yarden","Katz","050-123-4567","yarden@email.com",28)).setVisible(true);
        });
    }
}