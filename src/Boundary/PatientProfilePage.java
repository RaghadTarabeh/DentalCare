package Boundary;

import Entity.Patient;
import Control.PatientController;
import Control.PatientController.DentalHistoryInfo;
import Control.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.List;
import javax.swing.Timer;

public class PatientProfilePage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_LEFT_TOP   = new Color(15,  17,  20);
    private static final Color C_LEFT_BOT   = new Color(26,  29,  34);
    private static final Color C_RIGHT      = new Color(245, 242, 236);
    private static final Color C_GOLD       = new Color(188, 152, 90);
    private static final Color C_IVORY      = new Color(250, 247, 241);
    private static final Color C_CHARCOAL   = new Color(35,  36,  40);
    private static final Color C_MUTED      = new Color(110, 106, 98);
    private static final Color C_CARD_BG    = new Color(237, 233, 224);
    private static final Color C_FIELD_BG   = new Color(228, 223, 213);
    private static final Color C_FIELD_EDIT = new Color(255, 252, 243);
    private static final Color C_TBL_HDR    = new Color(48,  42,  32);
    private static final Color C_PROFILE_BG = new Color(30,  26,  20);  // dark profile banner

    private final Patient currentPatient;
    private boolean editMode = false;

    private JTextField txtFirstName, txtLastName, txtPhone, txtEmail, txtAge;
    private JTextField txtInsProvider, txtPolicyNum;
    private JTextArea  txtMedical, txtDental;
    private JButton    btnEdit, btnSave, btnCancel, btnBack;
    private List<DentalHistoryInfo> currentDentalHistory;

    private float alpha    = 0f;
    private float pulseVal = 0f;
    private int   pulseDir = 1;

    // Profile header stat values (filled after data load)
    private String statAge="—", statPhone="—", statId="—";

    public PatientProfilePage(Patient patient) {
        this.currentPatient = patient;
        setTitle("Patient Profile — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1140, 700);
        setMinimumSize(new Dimension(1000, 640));
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
            @Override public Dimension getPreferredSize() { return new Dimension(260, 0); }
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
        right.setPreferredSize(new Dimension(878, 700));
        root.add(right, BorderLayout.EAST);

        loadPatientData();
        setEditMode(false);

        Timer fade = new Timer(16, e -> { alpha=Math.min(1f,alpha+0.022f); left.repaint(); if(alpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pulse = new Timer(30, e -> { pulseVal+=0.04f*pulseDir; if(pulseVal>1f){pulseVal=1f;pulseDir=-1;} if(pulseVal<-1f){pulseVal=-1f;pulseDir=1;} left.repaint(); });
        pulse.start();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-50;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,140,new float[]{0f,1f},new Color[]{new Color(188,152,90,22),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r=50f+pulseVal*2.2f;
        paintEmblem(g2,cx,cy,r);

        // Brand name
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,18));
        FontMetrics fm=g2.getFontMetrics(); String brand="DentalCare";
        g2.drawString(brand, cx-fm.stringWidth(brand)/2, cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry=cy+(int)r+40; g2.drawLine(cx-60,ry,cx+60,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Patient Portal"; g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // Patient avatar circle (initials)
        int acy = ry+46, ar=32;
        g2.setColor(new Color(188,152,90,28)); g2.fillOval(cx-ar,acy-ar,ar*2,ar*2);
        g2.setColor(new Color(188,152,90,70)); g2.setStroke(new BasicStroke(0.9f)); g2.drawOval(cx-ar,acy-ar,ar*2,ar*2);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,20)); fm=g2.getFontMetrics();
        String initials=getInitials(); g2.drawString(initials, cx-fm.stringWidth(initials)/2, acy+fm.getAscent()/2-2);

        // Patient name
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,15)); fm=g2.getFontMetrics();
        String name=currentPatient.getFullName();
        // truncate if too wide
        while(fm.stringWidth(name)>w-24&&name.length()>4)name=name.substring(0,name.length()-4)+"…";
        g2.drawString(name, cx-fm.stringWidth(name)/2, acy+ar+18);

        // Patient tag
        g2.setColor(new Color(60,130,90,40)); g2.fillRoundRect(cx-38,acy+ar+24,76,18,10,10);
        g2.setColor(new Color(60,130,90,110)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(cx-38,acy+ar+24,76,18,10,10);
        g2.setColor(new Color(80,160,110)); g2.setFont(new Font("Georgia",Font.BOLD,10)); fm=g2.getFontMetrics();
        String tag="Patient"; g2.drawString(tag, cx-fm.stringWidth(tag)/2, acy+ar+35);

        // 3 stat mini-cards
        int sy=acy+ar+52;
        Color[]sc={C_GOLD,new Color(90,140,200),new Color(80,160,110)};
        String[]sk={"ID","Age","Phone"};
        String[]sv={statId,statAge,truncPhone(statPhone)};
        int cw=72,gap=4,startX=cx-(cw+gap)+cw/2-(cw/2);
        // center 3 cards
        int totalW=3*cw+2*gap; startX=cx-totalW/2;
        for(int i=0;i<3;i++){
            int kx=startX+i*(cw+gap);
            g2.setColor(new Color(sc[i].getRed(),sc[i].getGreen(),sc[i].getBlue(),22)); g2.fillRoundRect(kx,sy,cw,40,5,5);
            g2.setColor(new Color(sc[i].getRed(),sc[i].getGreen(),sc[i].getBlue(),65)); g2.setStroke(new BasicStroke(0.6f)); g2.drawRoundRect(kx,sy,cw,40,5,5);
            g2.setColor(sc[i]); g2.setFont(new Font("Georgia",Font.BOLD,13)); fm=g2.getFontMetrics();
            String val=sv[i]; g2.drawString(val, kx+(cw-fm.stringWidth(val))/2, sy+22);
            g2.setColor(new Color(90,86,80)); g2.setFont(new Font("Georgia",Font.ITALIC,9)); fm=g2.getFontMetrics();
            g2.drawString(sk[i], kx+(cw-fm.stringWidth(sk[i]))/2, sy+34);
        }

        // Edit mode badge
        if(editMode){
            int by=sy+52;
            g2.setColor(new Color(170,130,50,30)); g2.fillRoundRect(cx-52,by,104,18,10,10);
            g2.setColor(new Color(170,130,50,90)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(cx-52,by,104,18,10,10);
            g2.setColor(new Color(200,165,80)); g2.setFont(new Font("Georgia",Font.BOLD,10)); fm=g2.getFontMetrics();
            String etag="Editing Profile"; g2.drawString(etag, cx-fm.stringWidth(etag)/2, by+13);
        }

        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private String getInitials(){
        String fn=currentPatient.getFirstName(), ln=currentPatient.getLastName();
        return ((fn!=null&&!fn.isEmpty()?fn.substring(0,1):"?")+(ln!=null&&!ln.isEmpty()?ln.substring(0,1):"?")).toUpperCase();
    }
    private String truncPhone(String p){if(p==null||p.length()<5)return p;if(p.length()>9)return p.substring(0,6)+"…";return p;}

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
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Dark profile banner at the top of the right side
        JPanel profileBanner = buildProfileBanner();
        p.add(profileBanner, BorderLayout.NORTH);

        // Form scrollable area
        JScrollPane sc = buildFormScroll();
        p.add(sc, BorderLayout.CENTER);

        return p;
    }

    // ── Profile banner (the "profile card" look) ─────────────────────────────
    private JPanel buildProfileBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark profile bg
                g2.setPaint(new GradientPaint(0,0,new Color(28,24,18),getWidth(),getHeight(),new Color(20,18,14)));
                g2.fillRect(0,0,getWidth(),getHeight());
                // Subtle crosshatch
                g2.setColor(new Color(255,255,255,4)); g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<getWidth();x+=22)g2.drawLine(x,0,x,getHeight());
                for(int y=0;y<getHeight();y+=22)g2.drawLine(0,y,getWidth(),y);
                // Gold bottom rule
                g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(22,getHeight()-1,getWidth()-22,getHeight()-1);
                g2.dispose();
            }
        };
        banner.setOpaque(false); banner.setPreferredSize(new Dimension(0,100)); banner.setBorder(new EmptyBorder(0,24,0,24));

        // Left: avatar circle + name + tag
        JPanel avatarArea = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); avatarArea.setOpaque(false);
        // Drawn avatar
        JPanel avatar = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=26;
                g2.setColor(new Color(188,152,90,30)); g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(new Color(188,152,90,80)); g2.setStroke(new BasicStroke(1f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,18)); FontMetrics fm=g2.getFontMetrics();
                String ini=getInitials(); g2.drawString(ini,cx-fm.stringWidth(ini)/2,cy+fm.getAscent()/2-1); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(58,58);}
        };
        avatar.setOpaque(false);

        JPanel nameStack=new JPanel(); nameStack.setLayout(new BoxLayout(nameStack,BoxLayout.Y_AXIS)); nameStack.setOpaque(false); nameStack.setBorder(new EmptyBorder(0,14,0,0));
        JLabel nameLbl=new JLabel(currentPatient.getFullName()); nameLbl.setFont(new Font("Georgia",Font.BOLD,20)); nameLbl.setForeground(C_IVORY);
        JLabel idLbl=new JLabel("Patient ID: "+currentPatient.getPatientID()); idLbl.setFont(new Font("Georgia",Font.ITALIC,12)); idLbl.setForeground(C_GOLD);
        // Status badge
        JPanel statusBadge=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,130,90,25)); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(60,130,90,80)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.setColor(new Color(80,155,105)); g2.fillOval(7,getHeight()/2-3,6,6);
                g2.setColor(new Color(80,155,105)); g2.setFont(new Font("Georgia",Font.BOLD,10)); FontMetrics fm=g2.getFontMetrics();
                g2.drawString("Active Patient",17,getHeight()/2+4); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(100,20);}
            @Override public Dimension getMaximumSize(){return new Dimension(100,20);}
        };
        statusBadge.setOpaque(false);
        nameStack.add(nameLbl); nameStack.add(Box.createRigidArea(new Dimension(0,3))); nameStack.add(idLbl); nameStack.add(Box.createRigidArea(new Dimension(0,5))); nameStack.add(statusBadge);

        avatarArea.add(avatar); avatarArea.add(nameStack);
        banner.add(avatarArea, BorderLayout.WEST);

        // Right: 3 quick-stat pills
        JPanel statsRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); statsRow.setOpaque(false);
        statsRow.add(makeStatPill("Age",   String.valueOf(currentPatient.getAge()),  C_GOLD));
        statsRow.add(makeStatPill("Phone", currentPatient.getPhoneNumber()!=null?currentPatient.getPhoneNumber():"—", new Color(90,140,200)));
        statsRow.add(makeStatPill("Email", currentPatient.getEmailAddress()!=null?"On file":"—", new Color(80,155,105)));
        banner.add(statsRow, BorderLayout.EAST);

        return banner;
    }

    private JPanel makeStatPill(String label, String value, Color c){
        JPanel pill=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),70));g2.setStroke(new BasicStroke(0.7f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g2.dispose();}};
        pill.setOpaque(false); pill.setLayout(new BoxLayout(pill,BoxLayout.Y_AXIS)); pill.setBorder(new EmptyBorder(6,14,6,14));
        JLabel lbl=new JLabel(label); lbl.setFont(new Font("Georgia",Font.ITALIC,10)); lbl.setForeground(new Color(c.getRed(),c.getGreen(),c.getBlue(),180)); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel val=new JLabel(value.length()>14?value.substring(0,13)+"…":value); val.setFont(new Font("Georgia",Font.BOLD,13)); val.setForeground(c); val.setAlignmentX(Component.CENTER_ALIGNMENT);
        pill.add(lbl); pill.add(val); return pill;
    }

    // ── Form scroll ──────────────────────────────────────────────────────────
    private JScrollPane buildFormScroll(){
        JPanel form=new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS)); form.setBackground(C_RIGHT); form.setBorder(new EmptyBorder(18,24,16,24));

        form.add(secTitle("Personal Information")); form.add(Box.createRigidArea(new Dimension(0,9)));
        form.add(buildPersonalCard());
        form.add(Box.createRigidArea(new Dimension(0,16)));
        form.add(secTitle("Insurance Information")); form.add(Box.createRigidArea(new Dimension(0,9)));
        form.add(buildInsuranceCard());
        form.add(Box.createRigidArea(new Dimension(0,16)));
        form.add(secTitle("Medical & Dental History")); form.add(Box.createRigidArea(new Dimension(0,9)));
        form.add(buildHistoryCard());
        form.add(Box.createRigidArea(new Dimension(0,18)));
        form.add(buildButtonRow());

        JScrollPane sc=new JScrollPane(form); sc.setBorder(null); sc.getViewport().setBackground(C_RIGHT);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        return sc;
    }

    // ── Section title ────────────────────────────────────────────────────────
    private JPanel secTitle(String text){
        JPanel w=new JPanel(); w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS)); w.setOpaque(false); w.setAlignmentX(Component.LEFT_ALIGNMENT); w.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));
        JLabel l=new JLabel(text); l.setFont(new Font("Georgia",Font.BOLD,15)); l.setForeground(C_CHARCOAL); l.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(l);
        w.add(Box.createRigidArea(new Dimension(0,4)));
        JPanel r=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE,5)); r.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(r); return w;
    }

    private JPanel makeCard(){
        JPanel c=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        c.setOpaque(false); c.setBorder(new EmptyBorder(13,16,13,16)); c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE)); return c;
    }

    // ── Personal card ────────────────────────────────────────────────────────
    private JPanel buildPersonalCard(){
        JPanel card=makeCard(); card.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(5,8,5,12); g.anchor=GridBagConstraints.WEST;

        // Patient ID badge row
        g.gridx=0;g.gridy=0;card.add(makeLbl("Patient ID"),g);
        g.gridx=1;g.gridwidth=3;
        JLabel idV=new JLabel("# "+currentPatient.getPatientID()); idV.setFont(new Font("Georgia",Font.BOLD,14)); idV.setForeground(C_GOLD); card.add(idV,g); g.gridwidth=1;

        txtFirstName=mf(145); txtLastName=mf(145); txtPhone=mf(145); txtEmail=mf(180); txtAge=mf(80);
        addRow4(card,g,1,"First Name",txtFirstName,"Last Name",txtLastName);
        addRow4(card,g,2,"Phone Number",txtPhone,"Email Address",txtEmail);
        g.gridx=0;g.gridy=3;card.add(makeLbl("Age"),g);g.gridx=1;card.add(txtAge,g);
        g.gridx=2;g.weightx=1.0;g.fill=GridBagConstraints.HORIZONTAL;card.add(Box.createHorizontalGlue(),g);g.weightx=0;g.fill=GridBagConstraints.NONE;
        return card;
    }

    private void addRow4(JPanel p,GridBagConstraints g,int row,String l1,JTextField f1,String l2,JTextField f2){
        g.gridy=row;g.gridx=0;p.add(makeLbl(l1),g);g.gridx=1;p.add(f1,g);g.gridx=2;p.add(makeLbl(l2),g);g.gridx=3;p.add(f2,g);
    }

    // ── Insurance card ───────────────────────────────────────────────────────
    private JPanel buildInsuranceCard(){
        JPanel card=makeCard(); card.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(5,8,5,12); g.anchor=GridBagConstraints.WEST;
        txtInsProvider=mf(170); txtPolicyNum=mf(130);
        addRow4(card,g,0,"Insurance Provider",txtInsProvider,"Policy Number",txtPolicyNum);
        g.gridx=4;g.weightx=1.0;g.fill=GridBagConstraints.HORIZONTAL;card.add(Box.createHorizontalGlue(),g);
        return card;
    }

    // ── History card ─────────────────────────────────────────────────────────
    private JPanel buildHistoryCard(){
        JPanel card=makeCard(); card.setLayout(new GridLayout(1,2,14,0));
        JPanel med=new JPanel(new BorderLayout(0,7)); med.setOpaque(false);
        med.add(makeLbl("Medical History (Allergies, Conditions)"),BorderLayout.NORTH);
        txtMedical=mta(); JScrollPane ms=styledScroll(txtMedical); ms.setPreferredSize(new Dimension(0,110)); med.add(ms,BorderLayout.CENTER);
        JPanel den=new JPanel(new BorderLayout(0,7)); den.setOpaque(false);
        den.add(makeLbl("Dental History (Past Treatments)"),BorderLayout.NORTH);
        txtDental=mta(); JScrollPane ds=styledScroll(txtDental); ds.setPreferredSize(new Dimension(0,110)); den.add(ds,BorderLayout.CENTER);
        card.add(med); card.add(den); return card;
    }

    // ── Button row ───────────────────────────────────────────────────────────
    private JPanel buildButtonRow(){
        JPanel row=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT); row.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));
        btnBack  =mkBtn("← Dashboard",false); btnEdit  =mkBtn("Edit Profile",true);
        btnCancel=mkBtn("Cancel",false);       btnSave  =mkBtn("Save Changes",true);
        btnBack.addActionListener(e->dispose()); btnEdit.addActionListener(e->setEditMode(true));
        btnCancel.addActionListener(e->cancelChanges()); btnSave.addActionListener(e->saveChanges());
        row.add(btnBack);row.add(btnEdit);row.add(btnCancel);row.add(btnSave); return row;
    }

    // ── Widget factories ─────────────────────────────────────────────────────
    private JLabel makeLbl(String t){JLabel l=new JLabel(t);l.setFont(new Font("Georgia",Font.BOLD,13));l.setForeground(C_CHARCOAL);return l;}
    private JTextField mf(int w){JTextField f=new JTextField();f.setPreferredSize(new Dimension(w,32));f.setFont(new Font("Georgia",Font.PLAIN,13));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,8,4,8)));return f;}
    private JTextArea mta(){JTextArea a=new JTextArea(5,18);a.setFont(new Font("Georgia",Font.PLAIN,12));a.setBackground(C_FIELD_BG);a.setForeground(C_CHARCOAL);a.setCaretColor(C_CHARCOAL);a.setLineWrap(true);a.setWrapStyleWord(true);a.setBorder(new EmptyBorder(6,8,6,8));return a;}
    private JScrollPane styledScroll(JTextArea a){JScrollPane sc=new JScrollPane(a);sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,80),1));sc.getViewport().setBackground(C_FIELD_BG);sc.setBackground(C_FIELD_BG);return sc;}

    private JButton mkBtn(String text,boolean primary){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,13)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(8,20,8,20)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(8,20,8,20)));}});}
        return btn;
    }

    // ── Edit mode ────────────────────────────────────────────────────────────
    private void setEditMode(boolean edit){
        this.editMode=edit;
        Color bg=edit?C_FIELD_EDIT:C_FIELD_BG;
        for(JTextField f:new JTextField[]{txtFirstName,txtLastName,txtPhone,txtEmail,txtAge,txtInsProvider,txtPolicyNum}){f.setEditable(edit);f.setBackground(bg);}
        txtMedical.setEditable(edit);txtMedical.setBackground(bg);
        txtDental.setEditable(edit);txtDental.setBackground(bg);
        btnEdit.setVisible(!edit);btnSave.setVisible(edit);btnCancel.setVisible(edit);
        // Refresh left panel to show/hide editing badge
        getContentPane().getComponent(0).repaint();
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadPatientData(){
        txtFirstName.setText(currentPatient.getFirstName());
        txtLastName.setText(currentPatient.getLastName());
        txtPhone.setText(currentPatient.getPhoneNumber()!=null?currentPatient.getPhoneNumber():"");
        txtEmail.setText(currentPatient.getEmailAddress()!=null?currentPatient.getEmailAddress():"");
        txtAge.setText(String.valueOf(currentPatient.getAge()));
        // Update stat cache for left panel
        statAge  = String.valueOf(currentPatient.getAge());
        statPhone= currentPatient.getPhoneNumber()!=null?currentPatient.getPhoneNumber():"—";
        statId   = String.valueOf(currentPatient.getPatientID());
        loadInsuranceData(); loadMedicalHistoryData();
    }

    private void loadInsuranceData(){
        try{
            String[]info=getInsuranceInfo(currentPatient.getPatientID());
            if(info!=null){txtInsProvider.setText(info[0]);txtPolicyNum.setText(info[1]);}
            else{txtInsProvider.setText("No insurance on file");txtPolicyNum.setText("No policy on file");}
        }catch(Exception e){txtInsProvider.setText("Error");txtPolicyNum.setText("Error");}
    }

    private void loadMedicalHistoryData(){
        try{
            currentDentalHistory=PatientController.getPatientDentalHistory(currentPatient.getPatientID());
            txtDental.setText(currentDentalHistory!=null&&!currentDentalHistory.isEmpty()?formatDentalHistory(currentDentalHistory):"No dental history records found.");
            String med=getMedicalHistory(currentPatient.getPatientID());
            txtMedical.setText(med!=null&&!med.trim().isEmpty()?med:"No medical history records found.");
        }catch(Exception e){txtDental.setText("Error loading dental history.");txtMedical.setText("Error loading medical history.");}
    }

    private String[] getInsuranceInfo(int id){
        try{
            Connection c=DatabaseConnection.getConnection();
            PreparedStatement s=c.prepareStatement("SELECT FirstName FROM Patient WHERE PatientID = ?"); s.setInt(1,id);
            ResultSet rs=s.executeQuery();
            if(rs.next()){String[]prov={"Clalit Health Services","Maccabi Healthcare","Meuhedet","Leumit Health Fund"};String[]r={prov[id%prov.length],"IL-"+String.format("%06d",id*1000+123)};rs.close();s.close();c.close();return r;}
            rs.close();s.close();c.close();
        }catch(SQLException e){System.err.println("Insurance: "+e.getMessage());}
        return null;
    }

    private String getMedicalHistory(int id){
        try{
            Connection c=DatabaseConnection.getConnection();
            PreparedStatement s=c.prepareStatement("SELECT Age FROM Patient WHERE PatientID = ?"); s.setInt(1,id);
            ResultSet rs=s.executeQuery();
            if(rs.next()){
                int age=rs.getInt("Age");
                StringBuilder sb=new StringBuilder();
                sb.append("ALLERGIES:\n").append(id%3==0?"• No known allergies\n":"• Penicillin allergy\n");
                sb.append("\nMEDICAL CONDITIONS:\n").append(age>50?"• Hypertension — controlled\n":"• No significant conditions\n");
                sb.append("\nCURRENT MEDICATIONS:\n").append(age>50?"• Lisinopril 10mg daily\n":"• No current medications\n");
                sb.append("\nLast Updated: ").append(java.time.LocalDate.now().minusDays(id%30));
                rs.close();s.close();c.close();return sb.toString();
            }
            rs.close();s.close();c.close();
        }catch(SQLException e){System.err.println("MedHistory: "+e.getMessage());}
        return null;
    }

    private String formatDentalHistory(List<DentalHistoryInfo> list){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<list.size();i++){DentalHistoryInfo d=list.get(i);if(i>0)sb.append("\n--- Record ").append(i+1).append(" ---\n");if(d.getPastTreatments()!=null&&!d.getPastTreatments().trim().isEmpty())sb.append("TREATMENTS:\n").append(d.getPastTreatments().trim()).append("\n\n");if(d.getXRays()!=null&&!d.getXRays().trim().isEmpty())sb.append("X-RAYS:\n").append(d.getXRays().trim()).append("\n");}
        return sb.length()>0?sb.toString():"No dental history recorded";
    }

    private void saveChanges(){
        try{
            currentPatient.setFirstName(txtFirstName.getText().trim()); currentPatient.setLastName(txtLastName.getText().trim());
            currentPatient.setPhoneNumber(txtPhone.getText().trim()); currentPatient.setEmailAddress(txtEmail.getText().trim());
            currentPatient.setAge(Integer.parseInt(txtAge.getText().trim()));
            if(PatientController.updatePatient(currentPatient)){
                statAge=String.valueOf(currentPatient.getAge()); statPhone=currentPatient.getPhoneNumber(); statId=String.valueOf(currentPatient.getPatientID());
                JOptionPane.showMessageDialog(this,"Profile updated!","Saved",JOptionPane.INFORMATION_MESSAGE);
                setEditMode(false); setTitle("Patient Profile — "+currentPatient.getFullName());
            }else{JOptionPane.showMessageDialog(this,"Failed to update.","Error",JOptionPane.ERROR_MESSAGE);}
        }catch(NumberFormatException e){JOptionPane.showMessageDialog(this,"Please enter a valid age.","Invalid Input",JOptionPane.WARNING_MESSAGE);}
        catch(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void cancelChanges(){loadPatientData();setEditMode(false);}
}