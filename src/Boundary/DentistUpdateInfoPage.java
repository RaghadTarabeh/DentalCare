package Boundary;

import Entity.Staff;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;

public class DentistUpdateInfoPage extends JFrame {

    private static final Color C_BG      = new Color(15,  17,  20);
    private static final Color C_BANNER  = new Color(20,  22,  27);
    private static final Color C_BODY    = new Color(245, 242, 236);
    private static final Color C_GOLD    = new Color(188, 152, 90);
    private static final Color C_IVORY   = new Color(250, 247, 241);
    private static final Color C_CHARCOAL= new Color(35,  36,  40);
    private static final Color C_MUTED   = new Color(110, 106, 98);
    private static final Color C_CARD_BG = new Color(237, 233, 224);
    private static final Color C_FIELD_BG= new Color(228, 223, 213);
    private static final Color C_SUCCESS = new Color(60,  130, 90);

    private final Staff currentDentist;
    private boolean editMode = false;

    private JTextField txtFirstName, txtLastName, txtPhone, txtEmail, txtQual, txtSchedule;
    private JComboBox<String> cbSpec;
    private JTextArea txtNotes;
    private JButton btnEdit, btnSave, btnCancel, btnBack;

    private float bannerAlpha = 0f;

    public DentistUpdateInfoPage(Staff dentist) {
        this.currentDentist = dentist;
        setTitle("Update Profile — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);
        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        Timer fade=new Timer(16,e->{bannerAlpha=Math.min(1f,bannerAlpha+0.03f);root.getComponent(0).repaint();if(bannerAlpha>=1f)((Timer)e.getSource()).stop();});
        fade.start();

        loadData();
        setEditMode(false);
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
        JPanel emb=makeEmblem(); left.add(emb);
        JPanel bs=new JPanel(); bs.setLayout(new BoxLayout(bs,BoxLayout.Y_AXIS)); bs.setOpaque(false); bs.setBorder(new EmptyBorder(0,12,0,0));
        JLabel bl=new JLabel("Update My Profile"); bl.setFont(new Font("Georgia",Font.BOLD,18)); bl.setForeground(C_IVORY);
        JLabel sl=new JLabel("Dr. "+currentDentist.getFirstName()+" "+currentDentist.getLastName()); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        bs.add(bl); bs.add(sl); left.add(bs);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); right.setOpaque(false);
        JPanel idPill=makePill("Staff ID: "+currentDentist.getStaffID(),C_GOLD);
        JPanel rolePill=makePill("Dentist",C_SUCCESS);
        right.add(idPill); right.add(rolePill);

        banner.add(left,BorderLayout.WEST); banner.add(right,BorderLayout.EAST);
        return banner;
    }

    private JPanel buildBody() {
        JPanel body=new JPanel(new BorderLayout()); body.setBackground(C_BODY); body.setBorder(new EmptyBorder(18,32,10,32));

        JScrollPane sc=new JScrollPane(buildForm()); sc.setBorder(null); sc.getViewport().setBackground(C_BODY); sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        body.add(sc,BorderLayout.CENTER);
        body.add(buildButtonBar(),BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildForm() {
        JPanel form=new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS)); form.setBackground(C_BODY);

        // Personal info card
        form.add(secTitle("Personal Information"));
        form.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel pCard=makeCard(); pCard.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(7,10,7,14); g.anchor=GridBagConstraints.WEST;
        txtFirstName=mf(); txtLastName=mf(); txtPhone=mf(); txtEmail=mf(); txtQual=mf(); txtSchedule=mf();
        addRow4(pCard,g,0,"First Name",txtFirstName,"Last Name",txtLastName);
        addRow4(pCard,g,1,"Phone",txtPhone,"Email",txtEmail);
        addRow4(pCard,g,2,"Qualification",txtQual,"Schedule",txtSchedule);
        form.add(pCard); form.add(Box.createRigidArea(new Dimension(0,14)));

        // Professional info card
        form.add(secTitle("Professional Information"));
        form.add(Box.createRigidArea(new Dimension(0,6)));
        JPanel proCard=makeCard(); proCard.setLayout(new GridBagLayout());
        cbSpec=new JComboBox<>(new String[]{"General Dentistry","Orthodontics","Endodontics","Periodontics","Oral Surgery","Pediatric Dentistry","Prosthodontics","Oral Pathology"});
        cbSpec.setFont(new Font("Georgia",Font.PLAIN,12)); cbSpec.setBackground(C_FIELD_BG); cbSpec.setForeground(C_CHARCOAL); cbSpec.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
        g.gridx=0;g.gridy=0;proCard.add(makeLbl("Specialization"),g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.5;proCard.add(cbSpec,g);g.fill=GridBagConstraints.NONE;g.weightx=0;
        g.gridx=0;g.gridy=1;proCard.add(makeLbl("Professional Notes"),g);
        txtNotes=new JTextArea(4,28);txtNotes.setFont(new Font("Georgia",Font.PLAIN,12));txtNotes.setBackground(C_FIELD_BG);txtNotes.setForeground(C_CHARCOAL);txtNotes.setLineWrap(true);txtNotes.setWrapStyleWord(true);txtNotes.setBorder(new EmptyBorder(5,7,5,7));
        g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.5;g.gridy=1;proCard.add(new JScrollPane(txtNotes){{setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));}},g);
        g.fill=GridBagConstraints.NONE;g.weightx=0;
        form.add(proCard);
        return form;
    }

    private void addRow4(JPanel p,GridBagConstraints g,int row,String l1,JComponent f1,String l2,JComponent f2){
        g.gridx=0;g.gridy=row;p.add(makeLbl(l1),g);g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.4;p.add(f1,g);
        g.fill=GridBagConstraints.NONE;g.weightx=0;g.gridx=2;p.add(makeLbl(l2),g);g.gridx=3;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=0.4;p.add(f2,g);
        g.fill=GridBagConstraints.NONE;g.weightx=0;
    }

    private JPanel buildButtonBar(){
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,0,4,0));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); lb.setOpaque(false);
        btnEdit=mkBtn("Edit Profile",true,null); btnSave=mkBtn("Save Changes",true,null); btnCancel=mkBtn("Cancel",false,null);
        btnEdit.addActionListener(e->setEditMode(true)); btnSave.addActionListener(e->saveChanges()); btnCancel.addActionListener(e->cancelChanges());
        lb.add(btnEdit); lb.add(btnSave); lb.add(btnCancel);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        btnBack=mkBtn("← Back",false,null); btnBack.addActionListener(e->dispose()); rb.add(btnBack);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    private void setEditMode(boolean em){
        editMode=em;
        for(JTextField f:new JTextField[]{txtFirstName,txtLastName,txtPhone,txtEmail,txtQual,txtSchedule}){f.setEditable(em);f.setBackground(em?new Color(235,228,215):C_FIELD_BG);}
        cbSpec.setEnabled(em); txtNotes.setEditable(em);
        btnEdit.setVisible(!em); btnSave.setVisible(em); btnCancel.setVisible(em);
    }

    private void loadData(){
        txtFirstName.setText(currentDentist.getFirstName()); txtLastName.setText(currentDentist.getLastName());
        txtPhone.setText(currentDentist.getPhoneNumber()); txtEmail.setText(currentDentist.getEmailAddress());
        txtQual.setText(currentDentist.getQualification()); txtSchedule.setText(currentDentist.getScheduleDetails());
    }

    private void saveChanges(){
        try{
            currentDentist.setFirstName(txtFirstName.getText().trim()); currentDentist.setLastName(txtLastName.getText().trim());
            currentDentist.setPhoneNumber(txtPhone.getText().trim()); currentDentist.setEmailAddress(txtEmail.getText().trim());
            currentDentist.setQualification(txtQual.getText().trim()); currentDentist.setScheduleDetails(txtSchedule.getText().trim());
            setTitle("Update Profile — Dr. "+currentDentist.getFirstName()+" "+currentDentist.getLastName());
            setEditMode(false); JOptionPane.showMessageDialog(this,"Profile updated successfully.","Saved",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception e){JOptionPane.showMessageDialog(this,"Error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void cancelChanges(){loadData();setEditMode(false);}

    // ── Helpers ──────────────────────────────────────────────────────────────
    private JPanel makeEmblem(){JPanel e=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int cx=getWidth()/2,cy=getHeight()/2,r=16;g2.setColor(new Color(188,152,90,60));g2.setStroke(new BasicStroke(0.8f));g2.drawOval(cx-r-3,cy-r-3,r*2+6,r*2+6);g2.setColor(new Color(188,152,90,100));g2.setStroke(new BasicStroke(1.1f));g2.drawOval(cx-r,cy-r,r*2,r*2);g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));g2.fillOval(cx-r,cy-r,r*2,r*2);int arm=7,th=3;g2.setColor(C_GOLD);g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2);g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);g2.dispose();}@Override public Dimension getPreferredSize(){return new Dimension(40,40);}};e.setOpaque(false);return e;}
    private JPanel makePill(String text,Color c){JPanel p=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),22));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),75));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);g2.dispose();}};p.setOpaque(false);p.setBorder(new EmptyBorder(4,11,4,11));JLabel l=new JLabel(text);l.setFont(new Font("Georgia",Font.BOLD,11));l.setForeground(c);p.add(l);return p;}
    private JPanel secTitle(String t){JPanel w=new JPanel();w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS));w.setOpaque(false);w.setAlignmentX(Component.LEFT_ALIGNMENT);w.setMaximumSize(new Dimension(Integer.MAX_VALUE,28));JLabel l=new JLabel(t);l.setFont(new Font("Georgia",Font.BOLD,14));l.setForeground(C_CHARCOAL);l.setAlignmentX(Component.LEFT_ALIGNMENT);w.add(l);w.add(Box.createRigidArea(new Dimension(0,3)));JPanel r=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};r.setOpaque(false);r.setMaximumSize(new Dimension(Integer.MAX_VALUE,5));r.setAlignmentX(Component.LEFT_ALIGNMENT);w.add(r);return w;}
    private JPanel makeCard(){JPanel c=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};c.setOpaque(false);c.setBorder(new EmptyBorder(12,16,12,16));c.setAlignmentX(Component.LEFT_ALIGNMENT);c.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));return c;}
    private JLabel makeLbl(String t){JLabel l=new JLabel(t);l.setFont(new Font("Georgia",Font.BOLD,13));l.setForeground(C_CHARCOAL);return l;}
    private JTextField mf(){JTextField f=new JTextField(16);f.setFont(new Font("Georgia",Font.PLAIN,12));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));return f;}
    private JButton mkBtn(String text,boolean primary,Color accent){JButton btn=new JButton(text);btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));btn.setFocusPainted(false);btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));btn.setOpaque(true);Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}return btn;}
}