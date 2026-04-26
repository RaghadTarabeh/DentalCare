package Boundary;

import Entity.Patient;
import Control.MedicalHistoryController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;

public class MedicalHistoryPage extends JFrame {

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
    private static final Color C_ALERT      = new Color(160, 60,  60);
    private static final Color C_SAFE       = new Color(60,  130, 90);

    private final Patient currentPatient;
    private boolean editMode = false;

    private JTextArea txtAllergies, txtConditions, txtMedications;
    private JTextArea txtPastTreatments, txtXRays, txtNotes;
    private JLabel lblAlertBadge, lblLastUpdate;
    private JButton btnEdit, btnSave, btnCancel, btnXRay, btnPrint, btnBack;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    public MedicalHistoryPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("Medical History — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Same size as BillingPage
        setSize(1100, 660);

        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        JPanel left = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
                paintLeft(g2,getWidth(),getHeight());
                g2.dispose();
            }

            @Override public Dimension getPreferredSize(){
                return new Dimension(270,0);
            }
        };

        left.setBackground(C_LEFT_TOP);
        root.add(left,BorderLayout.WEST);

        JPanel div=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(
                        0,30,new Color(188,152,90,0),
                        0,getHeight()*.5f,new Color(188,152,90,150),
                        false
                ));
                g2.fillRect(0,0,1,getHeight());
                g2.dispose();
            }

            @Override public Dimension getPreferredSize(){
                return new Dimension(1,0);
            }
        };

        div.setOpaque(false);
        root.add(div,BorderLayout.CENTER);

        JPanel right = buildRight();

        // Same right-panel size as BillingPage
        right.setPreferredSize(new Dimension(828,660));

        root.add(right,BorderLayout.EAST);

        Timer fade=new Timer(16,e->{
            alpha=Math.min(1f,alpha+0.025f);
            left.repaint();
            if(alpha>=1f)((Timer)e.getSource()).stop();
        });
        fade.start();

        Timer pt=new Timer(30,e->{
            pulse+=0.04f*pDir;
            if(pulse>1f){pulse=1f;pDir=-1;}
            if(pulse<-1f){pulse=-1f;pDir=1;}
            left.repaint();
        });
        pt.start();

        loadHistory();
        setEditMode(false);
    }

    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-55;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,130,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
        float r=50f+pulse*2f; paintEmblem(g2,cx,cy,r);
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,19));
        FontMetrics fm=g2.getFontMetrics(); String br="DentalCare";
        g2.drawString(br,cx-fm.stringWidth(br)/2,cy+(int)r+30);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f)); int ry=cy+(int)r+42;
        g2.drawLine(cx-58,ry,cx+58,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Medical History"; g2.drawString(sub,cx-fm.stringWidth(sub)/2,ry+17);
        int bx=cx-80,by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,160,50,6,6);
        g2.setColor(new Color(160,153,140)); g2.setFont(new Font("Georgia",Font.ITALIC,10)); fm=g2.getFontMetrics();
        String fl="Records for"; g2.drawString(fl,cx-fm.stringWidth(fl)/2,by+14);
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,13)); fm=g2.getFontMetrics();
        String name=currentPatient.getFullName(); while(fm.stringWidth(name)>148&&name.length()>4)name=name.substring(0,name.length()-4)+"…";
        g2.drawString(name,cx-fm.stringWidth(name)/2,by+31);
        g2.setColor(new Color(90,86,80)); g2.setFont(new Font("Georgia",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String id="ID: "+currentPatient.getPatientID(); g2.drawString(id,cx-fm.stringWidth(id)/2,by+46);
        // Allergy alert box
        int ay=by+62;
        g2.setColor(new Color(160,60,60,30)); g2.fillRoundRect(cx-80,ay,160,36,6,6);
        g2.setColor(new Color(160,60,60,70)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(cx-80,ay,160,36,6,6);
        g2.setColor(new Color(190,100,90)); g2.setFont(new Font("Georgia",Font.BOLD,10)); fm=g2.getFontMetrics();
        String alert="Allergy Alert"; g2.drawString(alert,cx-fm.stringWidth(alert)/2,ay+14);
        g2.setColor(new Color(130,80,80)); g2.setFont(new Font("Georgia",Font.ITALIC,9)); fm=g2.getFontMetrics();
        String asub="Review before treatment"; g2.drawString(asub,cx-fm.stringWidth(asub)/2,ay+29);
        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10)); fm=g2.getFontMetrics();
        String copy="© DentalCare System"; g2.drawString(copy,cx-fm.stringWidth(copy)/2,h-16);
    }

    private void paintEmblem(Graphics2D g2,int cx,int cy,float r){
        g2.setColor(new Color(188,152,90,40));g2.setStroke(new BasicStroke(0.7f));g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));
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

    private JPanel buildRight() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);
        // Top bar
        JPanel top=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(26,getHeight()-1,getWidth()-26,getHeight()-1);g2.dispose();}};
        top.setOpaque(false); top.setBorder(new EmptyBorder(14,26,11,26));
        JLabel tl=new JLabel("Medical & Dental History"); tl.setFont(new Font("Georgia",Font.BOLD,19)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel(currentPatient.getFullName()+" · ID "+currentPatient.getPatientID()); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        lblLastUpdate=new JLabel("Last updated: Never"); lblLastUpdate.setFont(new Font("Georgia",Font.ITALIC,10)); lblLastUpdate.setForeground(C_MUTED);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false);
        ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,2))); ts.add(sl); ts.add(Box.createRigidArea(new Dimension(0,2))); ts.add(lblLastUpdate);
        top.add(ts,BorderLayout.WEST);
        lblAlertBadge=new JLabel("⚠ Allergy Alert"); lblAlertBadge.setFont(new Font("Georgia",Font.BOLD,11)); lblAlertBadge.setForeground(C_ALERT);
        lblAlertBadge.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(160,60,60,100),1),new EmptyBorder(3,10,3,10)));
        lblAlertBadge.setVisible(false); top.add(lblAlertBadge,BorderLayout.EAST);
        p.add(top,BorderLayout.NORTH);
        // Scroll content
        p.add(buildFormScroll(),BorderLayout.CENTER);
        // Button bar
        p.add(buildButtonBar(),BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane buildFormScroll() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_RIGHT);

        // smaller padding
        form.setBorder(new EmptyBorder(10, 26, 8, 26));

        form.add(makeSecTitle("Medical History"));
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel medCard = makeCard();
        medCard.setLayout(new GridLayout(1, 3, 14, 0));

        // shorter text areas
        medCard.add(makeTextPane("Allergies", txtAllergies = makeArea(), 65));
        medCard.add(makeTextPane("Medical Conditions", txtConditions = makeArea(), 65));
        medCard.add(makeTextPane("Current Medications", txtMedications = makeArea(), 65));

        form.add(medCard);

        form.add(Box.createRigidArea(new Dimension(0, 10)));

        form.add(makeSecTitle("Dental History"));
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel denCard = makeCard();
        denCard.setLayout(new GridLayout(1, 3, 14, 0));

        // shorter text areas
        denCard.add(makeTextPane("Past Treatments", txtPastTreatments = makeArea(), 65));
        denCard.add(makeTextPane("X-Ray History", txtXRays = makeArea(), 65));
        denCard.add(makeTextPane("Dental Notes", txtNotes = makeArea(), 65));

        form.add(denCard);

        JScrollPane sc = new JScrollPane(form);
        sc.setBorder(null);
        sc.getViewport().setBackground(C_RIGHT);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sc.getVerticalScrollBar().setUnitIncrement(14);

        return sc;
    }

    private JPanel makeSecTitle(String text) {
        JPanel w=new JPanel(); w.setLayout(new BoxLayout(w,BoxLayout.Y_AXIS)); w.setOpaque(false); w.setAlignmentX(Component.LEFT_ALIGNMENT); w.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        JLabel l=new JLabel(text); l.setFont(new Font("Georgia",Font.BOLD,15)); l.setForeground(C_CHARCOAL); l.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(l);
        w.add(Box.createRigidArea(new Dimension(0,3)));
        JPanel rule=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(188,152,90,90));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,getHeight()/2,getWidth(),getHeight()/2);g2.dispose();}};
        rule.setOpaque(false); rule.setMaximumSize(new Dimension(Integer.MAX_VALUE,5)); rule.setAlignmentX(Component.LEFT_ALIGNMENT); w.add(rule); return w;
    }

    private JPanel makeCard() {
        JPanel c=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,50));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        c.setOpaque(false); c.setBorder(new EmptyBorder(14,16,14,16)); c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE)); return c;
    }

    private JPanel makeTextPane(String label, JTextArea area, int h) {
        JPanel p=new JPanel(new BorderLayout(0,5)); p.setOpaque(false);
        JLabel l=new JLabel(label); l.setFont(new Font("Georgia",Font.BOLD,12)); l.setForeground(C_CHARCOAL); p.add(l,BorderLayout.NORTH);
        JScrollPane sc=new JScrollPane(area); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,80),1)); sc.getViewport().setBackground(C_FIELD_BG); sc.setPreferredSize(new Dimension(0,h)); p.add(sc,BorderLayout.CENTER); return p;
    }

    private JTextArea makeArea() {
        JTextArea a=new JTextArea(); a.setFont(new Font("Georgia",Font.PLAIN,12)); a.setBackground(C_FIELD_BG); a.setForeground(C_CHARCOAL); a.setCaretColor(C_CHARCOAL); a.setLineWrap(true); a.setWrapStyleWord(true); a.setBorder(new EmptyBorder(5,7,5,7)); return a;
    }

    private JPanel buildButtonBar() {
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(26,0,getWidth()-26,0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,26,12,26));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); lb.setOpaque(false);
        btnEdit=mkBtn("Edit History",true,null); btnSave=mkBtn("Save Changes",true,C_SAFE); btnCancel=mkBtn("Cancel",false,null);
        btnXRay=mkBtn("View X-Rays",false,new Color(60,110,160)); btnPrint=mkBtn("Print",false,null);
        btnEdit.addActionListener(e->setEditMode(true)); btnSave.addActionListener(this::saveChanges); btnCancel.addActionListener(this::cancelEdit);
        btnXRay.addActionListener(this::viewXRays); btnPrint.addActionListener(this::printHistory);
        lb.add(btnEdit); lb.add(btnSave); lb.add(btnCancel); lb.add(Box.createHorizontalStrut(10)); lb.add(btnXRay); lb.add(btnPrint);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        btnBack=mkBtn("← Dashboard",false,null); btnBack.addActionListener(e->dispose()); rb.add(btnBack);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        if(primary&&accent==null){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,16,6,16)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else if(primary){btn.setBackground(new Color(accent.getRed()/4,accent.getGreen()/4,accent.getBlue()/4));btn.setForeground(new Color(accent.getRed()+60<255?accent.getRed()+60:255,accent.getGreen()+60<255?accent.getGreen()+60:255,accent.getBlue()+60<255?accent.getBlue()+60:255));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent,1),new EmptyBorder(6,16,6,16)));}
        else{Color border=accent!=null?accent:new Color(188,152,90,100);btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,14,6,14)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));}});}
        return btn;
    }

    private void loadHistory() {
        txtAllergies.setText("• Penicillin (severe — hives, swelling)\n• Latex (contact dermatitis)\n• Shellfish (mild digestive issues)");
        txtConditions.setText("• Hypertension — controlled\n• Type 2 Diabetes — well-managed\n• No heart conditions\n• No bleeding disorders");
        txtMedications.setText("• Lisinopril 10mg daily (blood pressure)\n• Metformin 500mg twice daily (diabetes)\n• Multivitamin daily");
        txtPastTreatments.setText("2023: Root canal — tooth #14\n2022: Crown — tooth #19\n2021: Filling — tooth #7\n2020: Wisdom teeth extraction (×4)\n2019: Orthodontic treatment completed");
        txtXRays.setText("Jan 2025: Full mouth — no new concerns\nJul 2024: Bitewing — small cavity tooth #7\nJan 2024: Panoramic — wisdom teeth eval\nJun 2023: Periapical — root canal follow-up");
        txtNotes.setText("Occasional teeth grinding at night.\nSensitive to cold temperatures.\nExcellent oral hygiene compliance.\nRegular 6-month schedule maintained.");
        lblLastUpdate.setText("Last updated: January 15, 2025 by Dr. Sarah Cohen");
        lblAlertBadge.setVisible(true);
    }

    private void setEditMode(boolean edit) {
        this.editMode=edit;
        JTextArea[]areas={txtAllergies,txtConditions,txtMedications,txtPastTreatments,txtXRays,txtNotes};
        Color bg=edit?C_FIELD_EDIT:C_FIELD_BG;
        for(JTextArea a:areas){a.setEditable(edit);a.setBackground(bg);}
        btnEdit.setVisible(!edit); btnSave.setVisible(edit); btnCancel.setVisible(edit);
    }

    private void saveChanges(ActionEvent e) {
        JOptionPane.showMessageDialog(this,"Medical history updated successfully!\nLast updated: "+new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()),"Saved",JOptionPane.INFORMATION_MESSAGE);
        lblLastUpdate.setText("Last updated: "+new java.text.SimpleDateFormat("MMMM dd, yyyy").format(new java.util.Date())+" by Patient Portal");
        lblAlertBadge.setVisible(!txtAllergies.getText().trim().isEmpty());
        setEditMode(false);
    }

    private void cancelEdit(ActionEvent e) {
        int ok=JOptionPane.showConfirmDialog(this,"Discard unsaved changes?","Cancel Edit",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){loadHistory();setEditMode(false);}
    }

    private void viewXRays(ActionEvent e) { JOptionPane.showMessageDialog(this,"X-Ray records for "+currentPatient.getFullName()+":\n\n• Jan 2025 — Full mouth (18 images)\n• Jul 2024 — Bitewing (4 images)\n• Jan 2024 — Panoramic (1 image)","X-Ray Records",JOptionPane.INFORMATION_MESSAGE); }
    private void printHistory(ActionEvent e) { JOptionPane.showMessageDialog(this,"Preparing medical history report…\nA copy will be sent to: "+currentPatient.getEmailAddress(),"Print History",JOptionPane.INFORMATION_MESSAGE); }
}