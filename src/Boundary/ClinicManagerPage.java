package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class ClinicManagerPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_BG_DARK     = new Color(0x0f1114);
    private static final Color C_IVORY       = new Color(0xfaf7f1);
    private static final Color C_GOLD        = new Color(0xbc985a);
    private static final Color C_MUTED       = new Color(0x8a8278);
    private static final Color C_CARD_BG     = new Color(0x16191d);
    private static final Color C_CARD_HOVER  = new Color(0x1e2126);
    private static final Color C_CARD_BORDER = new Color(0x2a2d32);

    // Card accent colours
    private static final Color[] ACCENTS = {
        new Color(0x5b8fa8),  // Staff Management      — steel blue
        new Color(0x5a9e7a),  // Patient Management    — sage green
        new Color(0xbc985a),  // Appointment Scheduling— gold (brand)
        new Color(0x8a72b8),  // Financial Reports     — muted violet
        new Color(0x4e9ea8),  // Inventory Management  — teal
        new Color(0xb86a6a),  // Clinic Settings       — muted rose
    };

    public ClinicManagerPage() {
        setTitle("DentalCare — Clinic Manager Dashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);

        JPanel root = new RootPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildGrid(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Root panel — dark bg + fade-in ───────────────────────────────────────
    class RootPanel extends JPanel {
        float alpha = 0f;
        RootPanel() {
            setBackground(C_BG_DARK);
            Timer t = new Timer(16, e -> {
                alpha = Math.min(1f, alpha + 0.025f);
                repaint();
                if (alpha >= 1f) ((Timer)e.getSource()).stop();
            });
            t.start();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int w = getWidth(), h = getHeight();
            g2.setColor(C_BG_DARK); g2.fillRect(0,0,w,h);
            g2.setColor(new Color(255,255,255,4)); g2.setStroke(new BasicStroke(0.4f));
            for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h);
            for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
            g2.dispose();
        }
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(C_BG_DARK); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(40,getHeight()-1,getWidth()-40,getHeight()-1); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(new EmptyBorder(24,50,20,50));

        // Left: emblem + portal title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false);
        MainMenu.MiniEmblem emblem = new MainMenu.MiniEmblem();
        emblem.setPreferredSize(new Dimension(44,44));
        JPanel brandStack = new JPanel(); brandStack.setLayout(new BoxLayout(brandStack,BoxLayout.Y_AXIS)); brandStack.setOpaque(false); brandStack.setBorder(new EmptyBorder(0,14,0,0));
        JLabel portalLbl=new JLabel("Clinic Manager"); portalLbl.setFont(new Font("Georgia",Font.BOLD,22)); portalLbl.setForeground(C_IVORY);
        JLabel systemLbl=new JLabel("DentalCare Management System"); systemLbl.setFont(new Font("Georgia",Font.ITALIC,12)); systemLbl.setForeground(C_GOLD);
        brandStack.add(portalLbl); brandStack.add(systemLbl);
        left.add(emblem); left.add(brandStack);

        // Right: time + admin badge
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS)); right.setOpaque(false);
        JLabel welcomeLbl=new JLabel("Welcome back,"); welcomeLbl.setFont(new Font("Georgia",Font.ITALIC,13)); welcomeLbl.setForeground(C_MUTED); welcomeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel nameLbl=new JLabel("Clinic Manager"); nameLbl.setFont(new Font("Georgia",Font.BOLD,22)); nameLbl.setForeground(C_IVORY); nameLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        String time=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLbl=new JLabel("Today  ·  "+time); timeLbl.setFont(new Font("Georgia",Font.ITALIC,12)); timeLbl.setForeground(new Color(0x5a5650)); timeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Admin access badge
        JPanel adminBadge = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(188,152,90,22)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(188,152,90,75)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,11)); FontMetrics fm=g2.getFontMetrics();
                String t="Admin Access"; g2.drawString(t,(getWidth()-fm.stringWidth(t))/2,getHeight()/2+4); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(88,22);}
            @Override public Dimension getMaximumSize(){return new Dimension(88,22);}
        };
        adminBadge.setOpaque(false); adminBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel rule = new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(getWidth()-90,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(90,8)); rule.setMaximumSize(new Dimension(Integer.MAX_VALUE,8)); rule.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(welcomeLbl); right.add(Box.createRigidArea(new Dimension(0,3)));
        right.add(nameLbl);    right.add(Box.createRigidArea(new Dimension(0,3)));
        right.add(timeLbl);    right.add(Box.createRigidArea(new Dimension(0,5)));
        right.add(adminBadge); right.add(Box.createRigidArea(new Dimension(0,5)));
        right.add(rule);

        header.add(left,BorderLayout.WEST); header.add(right,BorderLayout.EAST);
        return header;
    }

    // ── Management grid ──────────────────────────────────────────────────────
    private JPanel buildGrid() {
        JPanel wrapper=new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.setBorder(new EmptyBorder(22,50,18,50));
        JPanel grid=new JPanel(new GridLayout(2,3,20,20)); grid.setOpaque(false);

        String[][] cards={
            {"Staff Management",       "Manage dentists, schedules, and staff roles."},
            {"Patient Management",     "View and manage patient records and data."},
            {"Appointment Scheduling", "Manage clinic-wide appointment system."},
            {"Financial Reports",      "View clinic revenue and financial analytics."},
            {"Inventory Management",   "Manage dental supplies and equipment."},
            {"Clinic Settings",        "Configure clinic preferences and system settings."},
        };
        // Icon types reusing ServiceIcon: 0=doc, 1=person, 2=clock, 3=diamond(finance), 4=ring+dot(inventory), 5=gear(settings)
        int[]iconTypes={5,0,2,3,4,1};
        Runnable[]actions={
            this::openStaffManagement, this::openPatientManagement,
            this::openAppointmentManagement, this::openFinancialReports,
            this::openInventoryManagement, this::openClinicSettings
        };

        for(int i=0;i<6;i++) grid.add(createCard(cards[i][0],iconTypes[i],cards[i][1],ACCENTS[i],actions[i]));
        wrapper.add(grid,BorderLayout.CENTER);
        return wrapper;
    }

    // Painted icon — same 6-type system as PatientPage/DentistPage
    static class ServiceIcon extends JPanel {
        private final int type; private final Color color;
        ServiceIcon(int type,Color color){this.type=type;this.color=color;setOpaque(false);setPreferredSize(new Dimension(22,22));}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int cx=getWidth()/2,cy=getHeight()/2,r=8; g2.setColor(color); g2.setStroke(new BasicStroke(1.4f));
            switch(type){
                case 0:// document
                    g2.drawRoundRect(cx-6,cy-r,12,r*2,2,2); g2.setStroke(new BasicStroke(1f));
                    g2.drawLine(cx-3,cy-3,cx+3,cy-3); g2.drawLine(cx-3,cy,cx+3,cy); g2.drawLine(cx-3,cy+3,cx+2,cy+3); break;
                case 1:// person
                    g2.drawOval(cx-4,cy-r,8,8); g2.drawArc(cx-r,cy+1,r*2,10,0,180); break;
                case 2:// clock
                    g2.drawOval(cx-r,cy-r,r*2,r*2); g2.drawLine(cx,cy-4,cx,cy); g2.drawLine(cx,cy,cx+3,cy+3); break;
                case 3:// diamond (finance)
                    int[]xs={cx,cx+r,cx,cx-r},ys={cy-r,cy,cy+r,cy}; g2.drawPolygon(xs,ys,4); break;
                case 4:// ring+dot (inventory)
                    g2.drawOval(cx-r,cy-r,r*2,r*2); g2.fillOval(cx-3,cy-3,6,6); break;
                case 5:// gear (settings) — simplified as ring with spikes
                    g2.drawOval(cx-5,cy-5,10,10);
                    g2.setStroke(new BasicStroke(1.3f));
                    for(int i=0;i<8;i++){double a=i*Math.PI/4;g2.drawLine((int)(cx+Math.cos(a)*5),(int)(cy+Math.sin(a)*5),(int)(cx+Math.cos(a)*8),(int)(cy+Math.sin(a)*8));} break;
            }
            g2.dispose();
        }
    }

    private JPanel createCard(String title,int iconType,String desc,Color accent,Runnable action){
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
                g2.setColor(hover?C_CARD_HOVER:C_CARD_BG); g2.fillRoundRect(0,0,w,h,8,8);
                g2.setColor(hover?new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),190):C_CARD_BORDER);
                g2.setStroke(new BasicStroke(hover?1.2f:0.8f)); g2.drawRoundRect(0,0,w-1,h-1,8,8);
                if(hover){g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),110));g2.fillRoundRect(0,h-3,w,3,2,2);}
                g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),hover?160:70));
                g2.fillRoundRect(0,0,3,h,2,2);
                g2.setColor(new Color(255,255,255,3)); g2.setStroke(new BasicStroke(0.3f));
                for(int x=0;x<w;x+=18)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=18)g2.drawLine(0,y,w,y);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false);

        JPanel inner=new JPanel(); inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS)); inner.setOpaque(false); inner.setBorder(new EmptyBorder(22,26,22,20));
        JPanel topRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); topRow.setOpaque(false);
        ServiceIcon icon=new ServiceIcon(iconType,accent); icon.setPreferredSize(new Dimension(22,26));
        JLabel titleLbl=new JLabel("  "+title); titleLbl.setFont(new Font("Georgia",Font.BOLD,16)); titleLbl.setForeground(C_IVORY);
        topRow.add(icon); topRow.add(titleLbl);

        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),80));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,50,getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(50,10)); rule.setMaximumSize(new Dimension(50,10)); rule.setAlignmentX(Component.LEFT_ALIGNMENT);

        String htmlDesc="<html><body style='width:165px;font-family:Georgia;font-size:12pt;color:#7a756b;line-height:1.5;'>"+desc+"</body></html>";
        JLabel descLbl=new JLabel(htmlDesc); descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(topRow); inner.add(Box.createRigidArea(new Dimension(0,9))); inner.add(rule); inner.add(Box.createRigidArea(new Dimension(0,10))); inner.add(descLbl);
        card.add(inner,BorderLayout.CENTER);
        return card;
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    private JPanel buildFooter(){
        JPanel footer=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BG_DARK);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,50));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(40,0,getWidth()-40,0);g2.dispose();}};
        footer.setOpaque(false); footer.setBorder(new EmptyBorder(14,50,16,50));
        JPanel leftInfo=new JPanel(); leftInfo.setLayout(new BoxLayout(leftInfo,BoxLayout.Y_AXIS)); leftInfo.setOpaque(false);
        JLabel roleLbl=new JLabel("Clinic Manager Portal  ·  Full Administrative Access"); roleLbl.setFont(new Font("Georgia",Font.ITALIC,12)); roleLbl.setForeground(new Color(0x4a4640));
        JLabel copyLbl=new JLabel("© 2025 DentalCare System  ·  Professional Dental Management"); copyLbl.setFont(new Font("Georgia",Font.ITALIC,11)); copyLbl.setForeground(new Color(0x3a3830));
        leftInfo.add(roleLbl); leftInfo.add(Box.createRigidArea(new Dimension(0,2))); leftInfo.add(copyLbl);
        JButton backBtn=buildBackButton(); backBtn.addActionListener(e->{dispose();try{MainMenu.showMainMenu();}catch(Exception ignored){}});
        footer.add(leftInfo,BorderLayout.WEST); footer.add(backBtn,BorderLayout.EAST);
        return footer;
    }

    private JButton buildBackButton(){
        JButton btn=new JButton("← Back to Main Menu"); btn.setFont(new Font("Georgia",Font.PLAIN,13)); btn.setForeground(C_MUTED); btn.setBackground(C_CARD_BG);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_CARD_BORDER,1),new EmptyBorder(8,20,8,20)));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){btn.setForeground(C_IVORY);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));}
            @Override public void mouseExited(MouseEvent e) {btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_CARD_BORDER,1),new EmptyBorder(8,20,8,20)));}
        });
        return btn;
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    private void navigateTo(java.util.function.Supplier<JFrame> factory, String name){
        this.setVisible(false);
        try{JFrame page=factory.get();page.setVisible(true);page.addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){ClinicManagerPage.this.setVisible(true);}});}
        catch(Exception e){JOptionPane.showMessageDialog(this,name+" — coming soon!","Info",JOptionPane.INFORMATION_MESSAGE);this.setVisible(true);}
    }

    private void openStaffManagement()      { navigateTo(StaffManagement::new,        "Staff Management"); }
    private void openPatientManagement()    { navigateTo(PatientManagement::new,       "Patient Management"); }
    private void openAppointmentManagement(){ navigateTo(AppointmentManagement::new,  "Appointment Management"); }
    private void openFinancialReports()     { navigateTo(FinancialReportsPage::new,   "Financial Reports"); }
    private void openInventoryManagement()  { navigateTo(()->new InventoryPage(this), "Inventory Management"); }
    private void openClinicSettings()       { navigateTo(ClinicSettingsPage::new,     "Clinic Settings"); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
            new ClinicManagerPage().setVisible(true);
        });
    }
}