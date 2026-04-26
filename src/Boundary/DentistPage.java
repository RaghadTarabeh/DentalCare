package Boundary;

import Entity.Staff;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class DentistPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_SIDEBAR   = new Color(15,  17,  20);
    private static final Color C_SIDEBAR2  = new Color(22,  25,  29);
    private static final Color C_BODY      = new Color(245, 242, 236);
    private static final Color C_GOLD      = new Color(188, 152, 90);
    private static final Color C_IVORY     = new Color(250, 247, 241);
    private static final Color C_CHARCOAL  = new Color(35,  36,  40);
    private static final Color C_MUTED     = new Color(110, 106, 98);
    private static final Color C_CARD_BG   = new Color(237, 233, 224);

    private static final Color[] ACCENTS = {
        new Color(91,  143, 168),  // Patient Reports
        new Color(90,  158, 122),  // Appointments
        new Color(138, 114, 184),  // Update Profile
        new Color(184, 106, 106),  // Logout
    };

    private final Staff dentist;
    private float sideAlpha = 0f;

    public DentistPage() {
        JOptionPane.showMessageDialog(this,"No dentist information provided.","Error",JOptionPane.ERROR_MESSAGE);
        dispose(); dentist=null;
    }

    public DentistPage(Staff dentist) {
        this.dentist = dentist;
        setTitle("DentalCare — Dentist Workstation");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_SIDEBAR);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(),    BorderLayout.CENTER);

        Timer fade = new Timer(16, e -> {
            sideAlpha = Math.min(1f, sideAlpha+0.03f);
            root.getComponent(0).repaint();
            if(sideAlpha>=1f)((Timer)e.getSource()).stop();
        });
        fade.start();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,sideAlpha));
                int w=getWidth(),h=getHeight();
                g2.setPaint(new GradientPaint(0,0,new Color(22,25,29),0,h,C_SIDEBAR));
                g2.fillRect(0,0,w,h);
                // crosshatch
                g2.setColor(new Color(255,255,255,13)); g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h);
                for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
                // right border
                g2.setColor(new Color(188,152,90,28)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(w-1,0,w-1,h);
                g2.dispose();
            }
        };
        sb.setOpaque(false);
        sb.setPreferredSize(new Dimension(270,0));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        inner.add(buildBrandRow());
        inner.add(buildDoctorCard());
        inner.add(Box.createRigidArea(new Dimension(0,12)));
        inner.add(buildClockBlock());
        inner.add(Box.createRigidArea(new Dimension(0,10)));
        inner.add(buildInfoRows());
        inner.add(Box.createVerticalGlue());
        inner.add(buildSidebarFooter());

        sb.add(inner,BorderLayout.CENTER);
        return sb;
    }

    private JPanel buildBrandRow() {
        JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        row.setOpaque(false); row.setBorder(new EmptyBorder(16,14,13,14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,56));

        // Mini emblem
        JPanel logo=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=13;
                g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.8f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));
                g2.fillOval(cx-r,cy-r,r*2,r*2);
                int arm=6,th=3; g2.setColor(C_GOLD);
                g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2); g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(30,30);}
        };
        logo.setOpaque(false);

        JPanel bs=new JPanel(); bs.setLayout(new BoxLayout(bs,BoxLayout.Y_AXIS)); bs.setOpaque(false);
        JLabel bn=new JLabel("DentalCare"); bn.setFont(new Font("Georgia",Font.BOLD,14)); bn.setForeground(C_IVORY);
        JLabel bs2=new JLabel("Dentist Workstation"); bs2.setFont(new Font("Georgia",Font.ITALIC,10)); bs2.setForeground(new Color(188,152,90,160));
        bs.add(bn); bs.add(bs2);

        row.add(logo); row.add(bs);
        // bottom rule
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE,60));
        wrap.add(row,BorderLayout.CENTER);
        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,22));g2.fillRect(0,0,getWidth(),1);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(0,1));
        wrap.add(rule,BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildDoctorCard() {
        JPanel card=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(188,152,90,14)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose();
            }
        };
        card.setOpaque(false); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14,14,14,14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

        JPanel outerCard = new JPanel(new BorderLayout()); outerCard.setOpaque(false);
        outerCard.setBorder(new EmptyBorder(0,14,0,14));
        outerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

        // Avatar
        JPanel avatar=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=26;
                g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(188,152,90,28),new Color(188,152,90,10)}));
                g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(new Color(188,152,90,80)); g2.setStroke(new BasicStroke(1f)); g2.drawOval(cx-r,cy-r,r*2,r*2);
                String ini=getInitials(); g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,19));
                FontMetrics fm=g2.getFontMetrics(); g2.drawString(ini,cx-fm.stringWidth(ini)/2,cy+fm.getAscent()/2-1);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(58,58);}
        };
        avatar.setOpaque(false); avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(avatar); card.add(Box.createRigidArea(new Dimension(0,8)));

        JLabel nameLbl=new JLabel("Dr. "+dentist.getFirstName()+" "+dentist.getLastName());
        nameLbl.setFont(new Font("Georgia",Font.BOLD,15)); nameLbl.setForeground(C_IVORY); nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameLbl); card.add(Box.createRigidArea(new Dimension(0,2)));

        JLabel specLbl=new JLabel("General Dentistry  ·  ID: "+dentist.getStaffID());
        specLbl.setFont(new Font("Georgia",Font.ITALIC,10)); specLbl.setForeground(new Color(188,152,90,170)); specLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(specLbl); card.add(Box.createRigidArea(new Dimension(0,9)));

        // Rule
        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,50));g2.setStroke(new BasicStroke(0.7f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setMaximumSize(new Dimension(Integer.MAX_VALUE,6)); rule.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(rule); card.add(Box.createRigidArea(new Dimension(0,9)));

        // 2×2 stat grid
        JPanel grid=new JPanel(new GridLayout(2,2,6,6)); grid.setOpaque(false); grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        Color[]sc={C_GOLD,new Color(60,130,90),new Color(60,100,165),new Color(138,114,184)};
        String[]sv={"3","12","47","5"};
        String[]sl={"Today","This Week","Patients","Plans"};
        for(int i=0;i<4;i++) grid.add(makeStatCell(sv[i],sl[i],sc[i]));
        card.add(grid); card.add(Box.createRigidArea(new Dimension(0,9)));

        // Active badge
        JPanel badge=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,130,90,20)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(60,130,90,75)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.setColor(new Color(80,155,105)); g2.fillOval(8,getHeight()/2-3,6,6);
                g2.setColor(new Color(80,155,105)); g2.setFont(new Font("Georgia",Font.BOLD,10)); FontMetrics fm=g2.getFontMetrics();
                String t="On Duty"; g2.drawString(t,18,getHeight()/2+4); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(74,20);}
            @Override public Dimension getMaximumSize(){return new Dimension(74,20);}
        };
        badge.setOpaque(false); badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(badge);

        outerCard.add(card,BorderLayout.CENTER);
        return outerCard;
    }

    private JPanel makeStatCell(String val, String label, Color c){
        JPanel cell=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),15));g2.fillRoundRect(0,0,getWidth(),getHeight(),5,5);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),45));g2.setStroke(new BasicStroke(0.6f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,5,5);g2.dispose();}};
        cell.setOpaque(false); cell.setLayout(new BoxLayout(cell,BoxLayout.Y_AXIS)); cell.setBorder(new EmptyBorder(5,6,5,6));
        JLabel vl=new JLabel(val); vl.setFont(new Font("Georgia",Font.BOLD,15)); vl.setForeground(c); vl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ll=new JLabel(label); ll.setFont(new Font("Georgia",Font.ITALIC,9)); ll.setForeground(new Color(90,86,80)); ll.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(vl); cell.add(ll); return cell;
    }

    private JPanel buildClockBlock(){
        JPanel block=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(22,25,29));g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);g2.setColor(new Color(42,45,50));g2.setStroke(new BasicStroke(0.7f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);g2.dispose();}};
        block.setOpaque(false); block.setLayout(new BoxLayout(block,BoxLayout.Y_AXIS)); block.setBorder(new EmptyBorder(10,14,10,14));
        block.setAlignmentX(Component.LEFT_ALIGNMENT); block.setMaximumSize(new Dimension(Integer.MAX_VALUE,65));

        JPanel bwrap=new JPanel(new BorderLayout()); bwrap.setOpaque(false); bwrap.setBorder(new EmptyBorder(0,14,0,14)); bwrap.setMaximumSize(new Dimension(Integer.MAX_VALUE,65));
        bwrap.add(block,BorderLayout.CENTER);

        String time=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String date=LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy",java.util.Locale.ENGLISH));
        JLabel tl=new JLabel(time); tl.setFont(new Font("Georgia",Font.BOLD,26)); tl.setForeground(C_IVORY); tl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel dl=new JLabel(date); dl.setFont(new Font("Georgia",Font.ITALIC,10)); dl.setForeground(new Color(90,86,80)); dl.setAlignmentX(Component.CENTER_ALIGNMENT);
        block.add(tl); block.add(Box.createRigidArea(new Dimension(0,2))); block.add(dl);
        return bwrap;
    }

    private JPanel buildInfoRows(){
        JPanel wrap=new JPanel(); wrap.setLayout(new BoxLayout(wrap,BoxLayout.Y_AXIS)); wrap.setOpaque(false); wrap.setBorder(new EmptyBorder(0,14,0,14));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));

        String[]icons={"📋","🗓","📧"};
        String[]labels={"Qualification","Schedule","Contact"};
        String[]vals={
            dentist.getQualification()!=null&&!dentist.getQualification().isEmpty()?dentist.getQualification():"DDS",
            dentist.getScheduleDetails()!=null&&!dentist.getScheduleDetails().isEmpty()?dentist.getScheduleDetails():"Sun–Thu",
            dentist.getEmailAddress()!=null&&dentist.getEmailAddress().length()>16?dentist.getEmailAddress().substring(0,14)+"…":dentist.getEmailAddress()!=null?dentist.getEmailAddress():"—"
        };

        for(int i=0;i<3;i++){
            JPanel row=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(22,25,29));g2.fillRoundRect(0,0,getWidth(),getHeight(),5,5);g2.setColor(new Color(42,45,50));g2.setStroke(new BasicStroke(0.6f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,5,5);g2.dispose();}};
            row.setLayout(new BorderLayout()); row.setOpaque(false); row.setBorder(new EmptyBorder(6,10,6,10));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,30)); row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel iLbl=new JLabel(icons[i]+"  "+labels[i]); iLbl.setFont(new Font("Georgia",Font.ITALIC,10)); iLbl.setForeground(new Color(100,96,90));
            JLabel vLbl=new JLabel(vals[i]); vLbl.setFont(new Font("Georgia",Font.BOLD,10)); vLbl.setForeground(new Color(180,172,158));
            row.add(iLbl,BorderLayout.WEST); row.add(vLbl,BorderLayout.EAST);
            wrap.add(row); if(i<2)wrap.add(Box.createRigidArea(new Dimension(0,5)));
        }
        return wrap;
    }

    private JPanel buildSidebarFooter(){
        JPanel foot=new JPanel(new BorderLayout()); foot.setOpaque(false); foot.setBorder(new EmptyBorder(10,14,12,14));
        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,20));g2.fillRect(0,0,getWidth(),1);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(0,1));

        JButton btn=new JButton("← Back to Main Menu"); btn.setFont(new Font("Georgia",Font.PLAIN,11)); btn.setForeground(new Color(90,86,80)); btn.setBackground(new Color(22,25,29));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(42,45,50),1),new EmptyBorder(7,14,7,14)));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_IVORY);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(7,14,7,14)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(new Color(90,86,80));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(42,45,50),1),new EmptyBorder(7,14,7,14)));}});
        btn.addActionListener(e->{dispose();try{MainMenu.showMainMenu();}catch(Exception ignored){}});
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));

        foot.add(rule,BorderLayout.NORTH); foot.add(btn,BorderLayout.CENTER);
        return foot;
    }

    // ── Main area ─────────────────────────────────────────────────────────────
    private JPanel buildMain(){
        JPanel main=new JPanel(new BorderLayout()); main.setBackground(C_BODY);

        // Top bar
        JPanel topBar=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(18,getHeight()-1,getWidth()-18,getHeight()-1);g2.dispose();}};
        topBar.setOpaque(false); topBar.setBorder(new EmptyBorder(14,22,12,22));
        JLabel tl=new JLabel("Dentist Dashboard"); tl.setFont(new Font("Georgia",Font.BOLD,19)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel("Select a module to get started"); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); ts.add(sl);

        // Date chip
        String dateChip=LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM",java.util.Locale.ENGLISH));
        JPanel chip=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(188,152,90,18));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setColor(new Color(188,152,90,65));g2.setStroke(new BasicStroke(0.7f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g2.dispose();}};
        chip.setOpaque(false); chip.setBorder(new EmptyBorder(5,14,5,14));
        JLabel chipLbl=new JLabel(dateChip); chipLbl.setFont(new Font("Georgia",Font.BOLD,12)); chipLbl.setForeground(C_GOLD);
        chip.add(chipLbl);

        topBar.add(ts,BorderLayout.WEST); topBar.add(chip,BorderLayout.EAST);
        main.add(topBar,BorderLayout.NORTH);

        // 2×2 card grid
        JPanel grid=new JPanel(new GridLayout(2,2,14,14)); grid.setBackground(C_BODY); grid.setBorder(new EmptyBorder(14,20,12,20));
        String[][]cards={
            {"Patient Reports","View comprehensive treatment progress reports for your patients.","View Reports"},
            {"Upcoming Appointments","Manage your schedule. View today's queue, confirm and complete appointments.","View Schedule"},
            {"Update Profile","Edit your professional information, specialization and contact details.","Edit Profile"},
            {"Logout","Sign out of the DentalCare system securely.","Sign Out"},
        };
        int[]iconTypes={0,1,2,3};
        String[]arrowLabels={"View Reports","View Schedule","Edit Profile","Sign Out"};
        Runnable[]actions={this::openTPReport,this::openUpcomingAppointments,this::openUpdateMyInfo,this::performLogout};

        for(int i=0;i<4;i++) grid.add(buildCard(cards[i][0],iconTypes[i],cards[i][1],arrowLabels[i],ACCENTS[i],actions[i]));
        main.add(grid,BorderLayout.CENTER);

        // Footer
        JPanel footer=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,35));g2.setStroke(new BasicStroke(0.7f));g2.drawLine(18,0,getWidth()-18,0);g2.dispose();}};
        footer.setOpaque(false); footer.setBorder(new EmptyBorder(6,22,9,22));
        JLabel copy=new JLabel("Staff ID: "+dentist.getStaffID()+"  ·  © 2025 DentalCare System  ·  Professional Dental Management");
        copy.setFont(new Font("Georgia",Font.ITALIC,10)); copy.setForeground(C_MUTED);
        footer.add(copy,BorderLayout.WEST);
        main.add(footer,BorderLayout.SOUTH);

        return main;
    }

    private JPanel buildCard(String title,int iconType,String desc,String actionLabel,Color accent,Runnable action){
        JPanel card=new JPanel(){
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
                g2.setColor(hover?new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80):new Color(188,152,90,35));
                g2.setStroke(new BasicStroke(hover?0.9f:0.7f)); g2.drawRoundRect(0,0,w-1,h-1,9,9);
                // Top accent bar
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),hover?200:120));
                g2.fillRoundRect(0,0,w,3,3,3);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false);

        JPanel inner=new JPanel(new BorderLayout()); inner.setOpaque(false); inner.setBorder(new EmptyBorder(16,18,14,16));

        // Top row: icon + title
        JPanel topRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); topRow.setOpaque(false);
        // Icon bubble
        JPanel iconBubble=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=16;
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),55)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                // icon
                g2.setColor(accent); g2.setStroke(new BasicStroke(1.4f)); int ir=9;
                switch(iconType){
                    case 0:// doc
                        g2.drawRoundRect(cx-6,cy-ir,12,ir*2,2,2); g2.setStroke(new BasicStroke(1.1f));
                        g2.drawLine(cx-4,cy-4,cx+4,cy-4); g2.drawLine(cx-4,cy,cx+4,cy); g2.drawLine(cx-4,cy+4,cx+2,cy+4); break;
                    case 1:// clock
                        g2.drawOval(cx-ir,cy-ir,ir*2,ir*2); g2.drawLine(cx,cy-5,cx,cy); g2.drawLine(cx,cy,cx+4,cy+4); break;
                    case 2:// person
                        g2.drawOval(cx-4,cy-ir,9,9); g2.drawArc(cx-ir,cy+1,ir*2,10,0,180); break;
                    case 3:// logout
                        g2.drawRoundRect(cx-7,cy-ir,12,ir*2,2,2); g2.setStroke(new BasicStroke(1.4f));
                        g2.drawLine(cx,cy,cx+6,cy); g2.drawLine(cx+4,cy-3,cx+6,cy); g2.drawLine(cx+4,cy+3,cx+6,cy); break;
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(36,36);}
        };
        iconBubble.setOpaque(false);
        JLabel titleLbl=new JLabel(title); titleLbl.setFont(new Font("Georgia",Font.BOLD,15)); titleLbl.setForeground(C_CHARCOAL);
        topRow.add(iconBubble); topRow.add(titleLbl);
        inner.add(topRow,BorderLayout.NORTH);

        // Description
        String html="<html><body style='width:190px;font-family:Georgia;font-size:11pt;color:#7a746a;line-height:1.5;'>"+desc+"</body></html>";
        JLabel descLbl=new JLabel(html); descLbl.setBorder(new EmptyBorder(8,2,0,0));
        inner.add(descLbl,BorderLayout.CENTER);

        // Action link at bottom
        JPanel bottom=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,30));g2.setStroke(new BasicStroke(0.6f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bottom.setOpaque(false); bottom.setBorder(new EmptyBorder(8,2,0,0));
        JLabel actionLbl=new JLabel(actionLabel+" ›"); actionLbl.setFont(new Font("Georgia",Font.BOLD,11)); actionLbl.setForeground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),200));
        bottom.add(actionLbl,BorderLayout.WEST);
        inner.add(bottom,BorderLayout.SOUTH);

        card.add(inner,BorderLayout.CENTER);
        return card;
    }

    private String getInitials(){
        String fn=dentist.getFirstName(),ln=dentist.getLastName();
        return ((fn!=null&&!fn.isEmpty()?fn.substring(0,1):"?")+(ln!=null&&!ln.isEmpty()?ln.substring(0,1):"?")).toUpperCase();
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    private void navigateTo(java.util.function.Supplier<JFrame> factory,String name){
        this.setVisible(false);
        try{JFrame p=factory.get();p.setVisible(true);p.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){DentistPage.this.setVisible(true);}});}
        catch(Exception e){JOptionPane.showMessageDialog(this,name+" — coming soon!","Info",JOptionPane.INFORMATION_MESSAGE);this.setVisible(true);}
    }
    private void openTPReport()             {navigateTo(()->new TPReportPage(dentist),"Patient Reports");}
    private void openUpcomingAppointments() {navigateTo(()->new DentistUpcomingAppointments(dentist),"Upcoming Appointments");}
    private void openUpdateMyInfo()         {navigateTo(()->new DentistUpdateInfoPage(dentist),"Update Profile");}
    private void performLogout(){
        int ok=JOptionPane.showConfirmDialog(this,"Are you sure you want to logout?","Confirm Logout",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){dispose();try{MainMenu.showMainMenu();}catch(Exception ignored){}}
    }
}