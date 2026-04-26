package Boundary;

import Entity.Patient;
import Control.PatientController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class SecretaryMenu extends JFrame {

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
        new Color(0x5b8fa8),  // Manage Appointments — steel blue
        new Color(0x5a9e7a),  // Book Appointment    — sage green
        new Color(0x8a72b8),  // Inventory           — muted violet
        new Color(0x4e9ea8),  // XML Import          — teal
        new Color(0xbc985a),  // Suppliers           — gold
        new Color(0x7a7268),  // Back to Main Menu   — neutral
    };

    public SecretaryMenu() {
        setTitle("DentalCare — Secretary Dashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 700);
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
        JLabel portalLbl=new JLabel("Secretary Dashboard"); portalLbl.setFont(new Font("Georgia",Font.BOLD,22)); portalLbl.setForeground(C_IVORY);
        JLabel systemLbl=new JLabel("DentalCare Management System"); systemLbl.setFont(new Font("Georgia",Font.ITALIC,12)); systemLbl.setForeground(C_GOLD);
        brandStack.add(portalLbl); brandStack.add(systemLbl);
        left.add(emblem); left.add(brandStack);

        // Right: time + active badge
        JPanel right = new JPanel(); right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS)); right.setOpaque(false);
        JLabel adminLbl=new JLabel("Administrative Portal"); adminLbl.setFont(new Font("Georgia",Font.ITALIC,13)); adminLbl.setForeground(C_MUTED); adminLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        String time=LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        JLabel timeLbl=new JLabel("Today  ·  "+time); timeLbl.setFont(new Font("Georgia",Font.ITALIC,12)); timeLbl.setForeground(new Color(0x5a5650)); timeLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Active status badge
        JPanel activeBadge = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60,130,90,22)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(60,130,90,75)); g2.setStroke(new BasicStroke(0.8f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.setColor(new Color(80,160,110)); g2.fillOval(8,getHeight()/2-3,6,6);
                g2.setFont(new Font("Georgia",Font.BOLD,11)); FontMetrics fm=g2.getFontMetrics();
                String t="Active"; g2.drawString(t,18,getHeight()/2+4); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(62,22);}
            @Override public Dimension getMaximumSize(){return new Dimension(62,22);}
        };
        activeBadge.setOpaque(false); activeBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel rule = new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(getWidth()-90,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setPreferredSize(new Dimension(90,8)); rule.setMaximumSize(new Dimension(Integer.MAX_VALUE,8)); rule.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(adminLbl); right.add(Box.createRigidArea(new Dimension(0,4)));
        right.add(timeLbl);  right.add(Box.createRigidArea(new Dimension(0,5)));
        right.add(activeBadge); right.add(Box.createRigidArea(new Dimension(0,5)));
        right.add(rule);

        header.add(left,BorderLayout.WEST); header.add(right,BorderLayout.EAST);
        return header;
    }

    // ── Service grid ─────────────────────────────────────────────────────────
    private JPanel buildGrid() {
        JPanel wrapper=new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.setBorder(new EmptyBorder(22,50,18,50));
        JPanel grid=new JPanel(new GridLayout(2,3,20,20)); grid.setOpaque(false);

        String[][] services={
            {"Manage Appointments",  "View and manage all patient appointments."},
            {"Book Appointment",     "Schedule new appointments for patients."},
            {"Inventory Management", "Manage clinic inventory and supplies."},
            {"XML Import",           "Import patient and system data."},
            {"Manage Suppliers",     "Add and update supply vendors."},
            {"Back to Main Menu",    "Return to the main system menu."},
        };
        int[]iconTypes={2,1,4,0,3,5};
        Runnable[]actions={
            this::openAppointmentManagement,
            this::openAppointmentBooking,
            this::openInventoryPage,
            this::openXMLImportPage,
            this::openSupplierPage,
            this::goBack
        };

        for(int i=0;i<6;i++) grid.add(createCard(services[i][0],iconTypes[i],services[i][1],ACCENTS[i],actions[i]));
        wrapper.add(grid,BorderLayout.CENTER);
        return wrapper;
    }

    // Painted icon — same 6-type system used throughout
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
                case 3:// diamond
                    int[]xs={cx,cx+r,cx,cx-r},ys={cy-r,cy,cy+r,cy}; g2.drawPolygon(xs,ys,4); break;
                case 4:// ring+dot
                    g2.drawOval(cx-r,cy-r,r*2,r*2); g2.fillOval(cx-3,cy-3,6,6); break;
                case 5:// arrow-left (back)
                    g2.drawLine(cx+r,cy,cx-r+2,cy);
                    g2.drawLine(cx-r+2,cy,cx-r+2+4,cy-4);
                    g2.drawLine(cx-r+2,cy,cx-r+2+4,cy+4); break;
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
        JLabel roleLbl=new JLabel("Secretary Portal  ·  Administrative Access"); roleLbl.setFont(new Font("Georgia",Font.ITALIC,12)); roleLbl.setForeground(new Color(0x4a4640));
        JLabel copyLbl=new JLabel("© 2025 DentalCare System  ·  Professional Dental Management"); copyLbl.setFont(new Font("Georgia",Font.ITALIC,11)); copyLbl.setForeground(new Color(0x3a3830));
        leftInfo.add(roleLbl); leftInfo.add(Box.createRigidArea(new Dimension(0,2))); leftInfo.add(copyLbl);
        footer.add(leftInfo,BorderLayout.WEST);
        return footer;
    }

    // ── Navigation ───────────────────────────────────────────────────────────
    private void navigateTo(java.util.function.Supplier<JFrame> factory, String name){
        this.setVisible(false);
        try{
            JFrame page=factory.get();
            page.setVisible(true);
            page.addWindowListener(new WindowAdapter(){
                @Override public void windowClosed(WindowEvent e){
                    SecretaryMenu.this.setVisible(true);
                }
            });
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this,name+" — coming soon!","Info",JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(true);
        }
    }

    private void openAppointmentManagement(){ navigateTo(AppointmentManagement::new, "Appointment Management"); }
    private void openInventoryPage(){
        this.setVisible(false);
        try{InventoryPage p=new InventoryPage(this);p.setVisible(true);}
        catch(Exception e){JOptionPane.showMessageDialog(this,"Inventory Management — coming soon!","Info",JOptionPane.INFORMATION_MESSAGE);this.setVisible(true);}
    }
    private void openXMLImportPage()  { navigateTo(XMLImportPage::new,  "XML Import"); }
    private void openSupplierPage()   { navigateTo(SupplierPage::new,   "Supplier Management"); }

    private void openAppointmentBooking() {
        // Dialog styled to match the system
        JDialog dlg=new JDialog(this,"Book Appointment",true);
        dlg.setSize(420,250); dlg.setLocationRelativeTo(this);

        JPanel panel=new JPanel(new BorderLayout()); panel.setBackground(C_BG_DARK);
        panel.setBorder(new EmptyBorder(0,0,0,0));

        // Painted top accent
        JPanel inner=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BG_DARK);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,80));g2.setStroke(new BasicStroke(1f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        inner.setOpaque(false); inner.setBorder(new EmptyBorder(24,28,20,28));

        JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setOpaque(false);

        JLabel tl=new JLabel("Book Appointment"); tl.setFont(new Font("Georgia",Font.BOLD,18)); tl.setForeground(C_IVORY);
        JLabel sl=new JLabel("Enter patient ID to schedule appointment"); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_MUTED);

        JPanel ruleP=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,80));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,60,getHeight()/2);g2.dispose();}};
        ruleP.setOpaque(false);ruleP.setPreferredSize(new Dimension(60,8));ruleP.setMaximumSize(new Dimension(60,8));ruleP.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inputRow=new JPanel(new BorderLayout(10,0)); inputRow.setOpaque(false); inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        JLabel idLbl=new JLabel("Patient ID:"); idLbl.setFont(new Font("Georgia",Font.BOLD,13)); idLbl.setForeground(C_IVORY); idLbl.setPreferredSize(new Dimension(80,36));
        JTextField idField=new JTextField(); idField.setFont(new Font("Georgia",Font.PLAIN,13)); idField.setBackground(new Color(0x16191d)); idField.setForeground(C_IVORY); idField.setCaretColor(C_GOLD);
        idField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(6,10,6,10)));
        inputRow.add(idLbl,BorderLayout.WEST); inputRow.add(idField,BorderLayout.CENTER);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); btnRow.setOpaque(false);
        JButton cancel=buildDialogBtn("Cancel",false); JButton book=buildDialogBtn("Book Appointment",true);
        book.addActionListener(e->{
            String inp=idField.getText().trim();
            if(inp.isEmpty()){JOptionPane.showMessageDialog(dlg,"Please enter a Patient ID.");return;}
            try{int pid=Integer.parseInt(inp);Patient p=PatientController.getPatientById(pid);
                if(p!=null){dlg.dispose();new AppointmentBookingPage(p).setVisible(true);}
                else JOptionPane.showMessageDialog(dlg,"Patient not found with ID: "+pid,"Not Found",JOptionPane.ERROR_MESSAGE);
            }catch(NumberFormatException ex){JOptionPane.showMessageDialog(dlg,"Please enter a valid numeric ID.","Invalid",JOptionPane.WARNING_MESSAGE);}
        });
        cancel.addActionListener(e->dlg.dispose());
        idField.addActionListener(e->book.doClick());
        btnRow.add(cancel); btnRow.add(book);

        content.add(tl); content.add(Box.createRigidArea(new Dimension(0,4))); content.add(sl);
        content.add(Box.createRigidArea(new Dimension(0,5))); content.add(ruleP);
        content.add(Box.createRigidArea(new Dimension(0,16))); content.add(inputRow);
        content.add(Box.createRigidArea(new Dimension(0,18))); content.add(btnRow);

        inner.add(content,BorderLayout.CENTER); panel.add(inner,BorderLayout.CENTER); dlg.setContentPane(panel);
        dlg.addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){idField.requestFocusInWindow();}});
        dlg.setVisible(true);
    }

    private JButton buildDialogBtn(String text, boolean primary){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,13));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(8,20,8,20)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_IVORY);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(8,20,8,20)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,100),1),new EmptyBorder(8,20,8,20)));}});}
        return btn;
    }

    private void goBack(){
        dispose();
        try{MainMenu.showMainMenu();}catch(Exception ignored){}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
            new SecretaryMenu().setVisible(true);
        });
    }
}