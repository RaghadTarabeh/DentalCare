package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import Control.DatabaseConnection;
import javax.swing.Timer;

public class StaffManagement extends JFrame {

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
    private static final Color C_TBL_HDR   = new Color(48,  42,  32);
    private static final Color C_TBL_ROW   = new Color(240, 236, 227);
    private static final Color C_TBL_ALT   = new Color(233, 228, 218);
    private static final Color C_SEL       = new Color(188, 152, 90, 80);
    private static final Color C_DENTIST   = new Color(60,  100, 160);
    private static final Color C_SECRETARY = new Color(60,  130, 90);
    private static final Color C_HYGIENIST = new Color(130, 80,  160);
    private static final Color C_MANAGER   = new Color(188, 152, 90);
    private static final Color C_DANGER    = new Color(150, 60,  50);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> roleFilter;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] COLS = {
        "ID","First Name","Last Name","Phone","Email","Role","Specialization","Qualification","Schedule","Manager"
    };

    public StaffManagement() {
        super("Staff Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        // Left painted panel
        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(252, 0); }
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
        right.setPreferredSize(new Dimension(946, 680));
        root.add(right, BorderLayout.EAST);

        Timer fade = new Timer(16, e -> { alpha = Math.min(1f, alpha+0.025f); left.repaint(); if (alpha>=1f) ((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadStaffData();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx = w/2, cy = h/2 - 60;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,125,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r = 50f + pulse * 2f;
        paintEmblem(g2, cx, cy, r);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,18));
        FontMetrics fm = g2.getFontMetrics(); String br = "DentalCare";
        g2.drawString(br, cx-fm.stringWidth(br)/2, cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry = cy+(int)r+40; g2.drawLine(cx-55,ry,cx+55,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Staff Management"; g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // Staff count badge
        int count = tableModel==null?0:tableModel.getRowCount();
        int bx=cx-65,by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,130,44,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,130,44,6,6);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,22)); fm=g2.getFontMetrics();
        String cnt=String.valueOf(count); g2.drawString(cnt, cx-fm.stringWidth(cnt)/2, by+28);
        g2.setColor(new Color(130,124,112)); g2.setFont(new Font("Georgia",Font.ITALIC,11)); fm=g2.getFontMetrics();
        String cl=count==1?"staff member":"staff members"; g2.drawString(cl, cx-fm.stringWidth(cl)/2, by+42);

        // Role legend
        int ly = by+60;
        Color[]lc={C_DENTIST,C_SECRETARY,C_HYGIENIST,C_MANAGER};
        String[]ll={"Dentist","Secretary","Hygienist","Manager"};
        for(int i=0;i<4;i++){
            int lx=cx-62, lyi=ly+i*18;
            g2.setColor(new Color(lc[i].getRed(),lc[i].getGreen(),lc[i].getBlue(),55));
            g2.fillRoundRect(lx,lyi,124,14,4,4);
            g2.setColor(lc[i]); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
            g2.drawString(ll[i], cx-fm.stringWidth(ll[i])/2, lyi+10);
        }

        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188,152,90,40)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int)(cx-r-11),(int)(cy-r-11),(int)(r*2+22),(int)(r*2+22));
        g2.setColor(new Color(188,152,90,85)); g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},
                new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)}));
        g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        float arm=r*.42f,th=r*.18f; g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx-th/2,cy-arm,th,arm*2,3,3));
        g2.fill(new RoundRectangle2D.Float(cx-arm,cy-th/2,arm*2,th,3,3));
        g2.setColor(new Color(188,152,90,95)); g2.setStroke(new BasicStroke(1f));
        for(int i=0;i<12;i++){double a=i*Math.PI/6-Math.PI/2;float in=r+3,out=r+(i%3==0?10:7);
            g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),(int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));}
        g2.setColor(new Color(188,152,90,120)); float dd=r*.63f,dr=r*.07f;
        for(int i=0;i<4;i++){double a=Math.PI/4+i*Math.PI/2;
            g2.fill(new Ellipse2D.Float((float)(cx+Math.cos(a)*dd)-dr,(float)(cy+Math.sin(a)*dd)-dr,dr*2,dr*2));}
    }

    // ── Right panel ──────────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar with search + filter
        JPanel top = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) { super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(24,getHeight()-1,getWidth()-24,getHeight()-1); g2.dispose(); }
        };
        top.setOpaque(false); top.setBorder(new EmptyBorder(13,26,10,26));

        JLabel tl=new JLabel("Staff Management"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl);
        top.add(ts, BorderLayout.WEST);

        // Search + filter row
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); ctrl.setOpaque(false);
        JLabel rl=new JLabel("Role:"); rl.setFont(new Font("Georgia",Font.BOLD,12)); rl.setForeground(C_CHARCOAL);
        roleFilter=new JComboBox<>(new String[]{"All Roles","Dentist","Secretary","Hygienist","Clinic Manager"});
        styleCombo(roleFilter); roleFilter.addActionListener(e->filterByRole((String)roleFilter.getSelectedItem()));
        JLabel sl=new JLabel("Search:"); sl.setFont(new Font("Georgia",Font.BOLD,12)); sl.setForeground(C_CHARCOAL);
        searchField=new JTextField(13); styleField(searchField);
        JButton sbtn=mkBtn("Search",false,null); sbtn.addActionListener(e->searchStaff()); searchField.addActionListener(e->searchStaff());
        ctrl.add(rl); ctrl.add(roleFilter); ctrl.add(Box.createHorizontalStrut(10)); ctrl.add(sl); ctrl.add(searchField); ctrl.add(sbtn);
        top.add(ctrl, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // Table
        p.add(buildTablePanel(), BorderLayout.CENTER);

        // Button bar
        p.add(buildButtonBar(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.setBorder(new EmptyBorder(8,26,6,26));
        tableModel = new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table = new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,12)); table.setRowHeight(30); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD));
        hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,8,0,5));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{
                    setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==0){setForeground(C_GOLD);setHorizontalAlignment(CENTER);}
                    else if(col==5&&v!=null){ // Role column: color by role
                        String role=v.toString();
                        if(role.contains("Dentist"))setForeground(C_DENTIST);
                        else if(role.contains("Secretary"))setForeground(C_SECRETARY);
                        else if(role.contains("Hygienist"))setForeground(C_HYGIENIST);
                        else if(role.contains("Manager"))setForeground(C_MANAGER);
                        else setForeground(C_CHARCOAL);
                        setHorizontalAlignment(LEFT);
                    }
                    else if(col==9&&v!=null){ // Manager flag
                        setForeground("Yes".equals(v.toString())?C_GOLD:C_MUTED);
                        setHorizontalAlignment(CENTER);
                    }
                    else{setForeground(C_CHARCOAL);setHorizontalAlignment(LEFT);}
                }
                return this;
            }
        });

        int[]widths={52,90,90,105,155,80,110,95,140,68};
        for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1));
        sc.getViewport().setBackground(C_TBL_ROW);
        wrap.add(sc, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create(); g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
            g2.setColor(new Color(188,152,90,45)); g2.setStroke(new BasicStroke(0.8f)); g2.drawLine(26,0,getWidth()-26,0); g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,26,12,26));

        JPanel lb = new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        JButton bAdd=mkBtn("+ Add",true,null), bEdit=mkBtn("Edit",false,null), bRemove=mkBtn("Remove",false,C_DANGER);
        JButton bSched=mkBtn("Schedule",false,null), bRole=mkBtn("Assign Role",false,null), bReport=mkBtn("Performance",false,null);
        bAdd.addActionListener(e->addStaff()); bEdit.addActionListener(e->editStaff()); bRemove.addActionListener(e->removeStaff());
        bSched.addActionListener(e->manageSchedule()); bRole.addActionListener(e->assignRole()); bReport.addActionListener(e->generateReport());
        lb.add(bAdd);lb.add(bEdit);lb.add(bRemove);lb.add(Box.createHorizontalStrut(8));lb.add(bSched);lb.add(bRole);lb.add(bReport);

        JPanel rb = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->{dispose();try{new ClinicManagerPage().setVisible(true);}catch(Exception ignored){}});
        rb.add(back);

        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST);
        return bar;
    }

    // ── Widget factories ─────────────────────────────────────────────────────
    private JButton mkBtn(String text, boolean primary, Color accent) {
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));
            btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}
        return btn;
    }

    private void styleField(JTextField f){
        f.setFont(new Font("Georgia",Font.PLAIN,12)); f.setBackground(C_FIELD_BG); f.setForeground(C_CHARCOAL);
        f.setCaretColor(C_CHARCOAL); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));
    }

    private void styleCombo(JComboBox<String> cb){
        cb.setFont(new Font("Georgia",Font.PLAIN,12)); cb.setBackground(C_FIELD_BG); cb.setForeground(C_CHARCOAL);
        cb.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadStaffData() {
        tableModel.setRowCount(0);
        try {
            Connection conn=DatabaseConnection.getConnection();
            String q="SELECT s.StaffID,s.FirstName,s.LastName,s.PhoneNumber,s.EmailAddress,r.RoleName,s.SpecializationID,s.Qualification,s.ScheduleDetails,s.IsClinicManager FROM Staff s LEFT JOIN Role r ON s.RoleID=r.RoleID ORDER BY s.StaffID";
            ResultSet rs=conn.prepareStatement(q).executeQuery();
            while(rs.next()) tableModel.addRow(new Object[]{rs.getInt("StaffID"),rs.getString("FirstName"),rs.getString("LastName"),rs.getString("PhoneNumber"),rs.getString("EmailAddress"),rs.getString("RoleName"),specName(rs.getInt("SpecializationID")),rs.getString("Qualification"),rs.getString("ScheduleDetails"),rs.getString("IsClinicManager")});
            rs.close(); conn.close();
        }catch(SQLException e){JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
        repaint();
    }

    private void filterByRole(String role){
        tableModel.setRowCount(0);
        try{
            Connection conn=DatabaseConnection.getConnection();
            String q="All Roles".equals(role)?
                "SELECT s.StaffID,s.FirstName,s.LastName,s.PhoneNumber,s.EmailAddress,r.RoleName,s.SpecializationID,s.Qualification,s.ScheduleDetails,s.IsClinicManager FROM Staff s LEFT JOIN Role r ON s.RoleID=r.RoleID ORDER BY s.StaffID":
                "SELECT s.StaffID,s.FirstName,s.LastName,s.PhoneNumber,s.EmailAddress,r.RoleName,s.SpecializationID,s.Qualification,s.ScheduleDetails,s.IsClinicManager FROM Staff s LEFT JOIN Role r ON s.RoleID=r.RoleID WHERE r.RoleName=? ORDER BY s.StaffID";
            PreparedStatement ps=conn.prepareStatement(q); if(!"All Roles".equals(role))ps.setString(1,role);
            ResultSet rs=ps.executeQuery();
            while(rs.next()) tableModel.addRow(new Object[]{rs.getInt("StaffID"),rs.getString("FirstName"),rs.getString("LastName"),rs.getString("PhoneNumber"),rs.getString("EmailAddress"),rs.getString("RoleName"),specName(rs.getInt("SpecializationID")),rs.getString("Qualification"),rs.getString("ScheduleDetails"),rs.getString("IsClinicManager")});
            rs.close(); ps.close(); conn.close();
        }catch(SQLException e){JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
        repaint();
    }

    private String specName(int id){switch(id){case 1:return "General";case 2:return "Orthodontics";case 3:return "Periodontics";case 4:return "Endodontics";default:return "Unknown";}}
    private int roleId(String n){switch(n){case "Dentist":return 1;case "Secretary":return 2;case "Hygienist":return 3;case "Clinic Manager":return 4;default:return 1;}}
    private int specId(String n){switch(n){case "General":return 1;case "Orthodontics":return 2;case "Periodontics":return 3;case "Endodontics":return 4;default:return 1;}}

    private void searchStaff(){
        String term=searchField.getText().toLowerCase().trim(); if(term.isEmpty())return;
        for(int i=0;i<tableModel.getRowCount();i++){
            String fn=tableModel.getValueAt(i,1).toString().toLowerCase(),ln=tableModel.getValueAt(i,2).toString().toLowerCase();
            if(fn.contains(term)||ln.contains(term)){table.setRowSelectionInterval(i,i);table.scrollRectToVisible(table.getCellRect(i,0,true));return;}
        }
        JOptionPane.showMessageDialog(this,"No staff found for: "+term);
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void addStaff(){
        showStaffDialog(-1,"","","","","Dentist","General","","","No");
    }
    private void editStaff(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a staff member.");return;}
        showStaffDialog((int)tableModel.getValueAt(row,0),tableModel.getValueAt(row,1).toString(),tableModel.getValueAt(row,2).toString(),tableModel.getValueAt(row,3).toString(),tableModel.getValueAt(row,4).toString(),tableModel.getValueAt(row,5).toString(),tableModel.getValueAt(row,6).toString(),tableModel.getValueAt(row,7).toString(),tableModel.getValueAt(row,8).toString(),tableModel.getValueAt(row,9).toString());
    }

    private void showStaffDialog(int sid,String fn,String ln,String ph,String em,String role,String spec,String qual,String sched,String mgr){
        boolean isNew=sid<0;
        JDialog dlg=new JDialog(this,isNew?"Add Staff Member":"Edit Staff Member",true);
        dlg.setSize(440,500); dlg.setLocationRelativeTo(this);
        JPanel panel=new JPanel(new GridBagLayout()); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(22,26,18,26));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,6,6,12); g.anchor=GridBagConstraints.WEST;
        JTextField fFN=mkDialogField(fn),fLN=mkDialogField(ln),fPH=mkDialogField(ph),fEM=mkDialogField(em),fQL=mkDialogField(qual),fSC=mkDialogField(sched);
        JComboBox<String> fRole=new JComboBox<>(new String[]{"Dentist","Secretary","Hygienist","Clinic Manager"});
        JComboBox<String> fSpec=new JComboBox<>(new String[]{"General","Orthodontics","Periodontics","Endodontics"});
        JComboBox<String> fMgr=new JComboBox<>(new String[]{"No","Yes"});
        styleCombo(fRole); styleCombo(fSpec); styleCombo(fMgr);
        fRole.setSelectedItem(role); fSpec.setSelectedItem(spec); fMgr.setSelectedItem(mgr);
        String[]labels={"First Name","Last Name","Phone","Email","Role","Specialization","Qualification","Schedule","Manager"};
        JComponent[]fields={fFN,fLN,fPH,fEM,fRole,fSpec,fQL,fSC,fMgr};
        for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;JLabel l=new JLabel(labels[i]);l.setFont(new Font("Georgia",Font.BOLD,12));l.setForeground(C_CHARCOAL);panel.add(l,g);g.gridx=1;panel.add(fields[i],g);}
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow.setOpaque(false);
        JButton save=mkBtn(isNew?"Add Staff":"Update",true,null),cancel=mkBtn("Cancel",false,null);
        save.addActionListener(ev->{
            try{
                if(fFN.getText().trim().isEmpty()||fLN.getText().trim().isEmpty()){JOptionPane.showMessageDialog(dlg,"Name is required.");return;}
                Connection conn=DatabaseConnection.getConnection();
                if(isNew){
                    ResultSet mr=conn.prepareStatement("SELECT MAX(StaffID) FROM Staff").executeQuery();int ns=1;if(mr.next())ns=mr.getInt(1)+1;mr.close();
                    PreparedStatement s=conn.prepareStatement("INSERT INTO Staff (StaffID,FirstName,LastName,PhoneNumber,EmailAddress,RoleID,SpecializationID,Qualification,ScheduleDetails,IsClinicManager) VALUES(?,?,?,?,?,?,?,?,?,?)");
                    s.setInt(1,ns);s.setString(2,fFN.getText().trim());s.setString(3,fLN.getText().trim());s.setString(4,fPH.getText().trim());s.setString(5,fEM.getText().trim());s.setInt(6,roleId((String)fRole.getSelectedItem()));s.setInt(7,specId((String)fSpec.getSelectedItem()));s.setString(8,fQL.getText().trim());s.setString(9,fSC.getText().trim());s.setString(10,(String)fMgr.getSelectedItem());s.executeUpdate();s.close();
                }else{
                    PreparedStatement s=conn.prepareStatement("UPDATE Staff SET FirstName=?,LastName=?,PhoneNumber=?,EmailAddress=?,RoleID=?,SpecializationID=?,Qualification=?,ScheduleDetails=?,IsClinicManager=? WHERE StaffID=?");
                    s.setString(1,fFN.getText().trim());s.setString(2,fLN.getText().trim());s.setString(3,fPH.getText().trim());s.setString(4,fEM.getText().trim());s.setInt(5,roleId((String)fRole.getSelectedItem()));s.setInt(6,specId((String)fSpec.getSelectedItem()));s.setString(7,fQL.getText().trim());s.setString(8,fSC.getText().trim());s.setString(9,(String)fMgr.getSelectedItem());s.setInt(10,sid);s.executeUpdate();s.close();
                }
                conn.close(); loadStaffData(); dlg.dispose(); JOptionPane.showMessageDialog(this,isNew?"Staff added.":"Staff updated.");
            }catch(SQLException ex){JOptionPane.showMessageDialog(dlg,"DB error: "+ex.getMessage());}
        });
        cancel.addActionListener(ev->dlg.dispose());
        btnRow.add(cancel); btnRow.add(save);
        g.gridx=0;g.gridy=labels.length;g.gridwidth=2;panel.add(btnRow,g);
        dlg.setContentPane(panel); dlg.setVisible(true);
    }

    private JTextField mkDialogField(String val){JTextField f=new JTextField(val,16);styleField(f);return f;}

    private void removeStaff(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a staff member.");return;}
        int ok=JOptionPane.showConfirmDialog(this,"Remove this staff member?","Confirm",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){
            try{int sid=(int)tableModel.getValueAt(row,0);Connection conn=DatabaseConnection.getConnection();PreparedStatement s=conn.prepareStatement("DELETE FROM Staff WHERE StaffID=?");s.setInt(1,sid);s.executeUpdate();s.close();conn.close();loadStaffData();JOptionPane.showMessageDialog(this,"Staff removed.");}
            catch(SQLException e){JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage());}
        }
    }

    private void manageSchedule(){int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a staff member.");return;}JOptionPane.showMessageDialog(this,"Schedule management for: "+tableModel.getValueAt(r,1)+" "+tableModel.getValueAt(r,2));}
    private void assignRole(){
        int r=table.getSelectedRow(); if(r<0){JOptionPane.showMessageDialog(this,"Select a staff member.");return;}
        String newRole=(String)JOptionPane.showInputDialog(this,"Select new role:","Assign Role",JOptionPane.QUESTION_MESSAGE,null,new String[]{"Dentist","Secretary","Hygienist","Clinic Manager"},"Dentist");
        if(newRole!=null){tableModel.setValueAt(newRole,r,5);JOptionPane.showMessageDialog(this,"Role assigned.");}
    }
    private void generateReport(){JOptionPane.showMessageDialog(this,"Performance report would include:\n• Appointments handled\n• Revenue per dentist\n• Patient satisfaction\n• Schedule adherence");}

    public static void main(String[]args){SwingUtilities.invokeLater(()->new StaffManagement().setVisible(true));}
}