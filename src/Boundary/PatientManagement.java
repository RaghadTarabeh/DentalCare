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

public class PatientManagement extends JFrame {

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
    private static final Color C_DANGER    = new Color(150, 70,  60);
    private static final Color C_SUCCESS   = new Color(60,  130, 90);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private float alpha=0f, pulse=0f;
    private int pDir=1;

    private static final String[] COLS={"ID","First Name","Last Name","Phone","Email","Age","Insurance","Policy"};

    public PatientManagement() {
        super("Patient Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 680);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root=new JPanel(new BorderLayout()); root.setBackground(C_LEFT_TOP); setContentPane(root);

        JPanel left=new JPanel(null){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
                paintLeft(g2,getWidth(),getHeight()); g2.dispose();
            }
            @Override public Dimension getPreferredSize(){return new Dimension(260,0);}
        };
        left.setBackground(C_LEFT_TOP); root.add(left,BorderLayout.WEST);

        JPanel div=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setPaint(new GradientPaint(0,30,new Color(188,152,90,0),0,getHeight()*.5f,new Color(188,152,90,150),false));g2.fillRect(0,0,1,getHeight());g2.dispose();}@Override public Dimension getPreferredSize(){return new Dimension(1,0);}};
        div.setOpaque(false); root.add(div,BorderLayout.CENTER);

        JPanel right=buildRight(); right.setPreferredSize(new Dimension(938,680)); root.add(right,BorderLayout.EAST);

        Timer fade=new Timer(16,e->{alpha=Math.min(1f,alpha+0.025f);left.repaint();if(alpha>=1f)((Timer)e.getSource()).stop();}); fade.start();
        Timer pt=new Timer(30,e->{pulse+=0.04f*pDir;if(pulse>1f){pulse=1f;pDir=-1;}if(pulse<-1f){pulse=-1f;pDir=1;}left.repaint();}); pt.start();

        loadData();
    }

    private void paintLeft(Graphics2D g2,int w,int h){
        int cx=w/2,cy=h/2-40;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,120,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);
        float r=48f+pulse*2f; paintEmblem(g2,cx,cy,r);
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,18)); FontMetrics fm=g2.getFontMetrics();
        String br="DentalCare"; g2.drawString(br,cx-fm.stringWidth(br)/2,cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f)); int ry=cy+(int)r+40;
        g2.drawLine(cx-55,ry,cx+55,ry); g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Patient Management"; g2.drawString(sub,cx-fm.stringWidth(sub)/2,ry+17);
        // count badge
        int count=tableModel==null?0:tableModel.getRowCount();
        int bx=cx-65,by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,130,44,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,130,44,6,6);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,22)); fm=g2.getFontMetrics();
        String cnt=String.valueOf(count); g2.drawString(cnt,cx-fm.stringWidth(cnt)/2,by+28);
        g2.setColor(new Color(130,124,112)); g2.setFont(new Font("Georgia",Font.ITALIC,11)); fm=g2.getFontMetrics();
        String cl=count==1?"patient":"patients"; g2.drawString(cl,cx-fm.stringWidth(cl)/2,by+42);
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

    private JPanel buildRight(){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);
        // Top bar with search
        JPanel top=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(24,getHeight()-1,getWidth()-24,getHeight()-1);g2.dispose();}};
        top.setOpaque(false); top.setBorder(new EmptyBorder(14,26,11,26));
        JLabel tl=new JLabel("Patient Management"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); top.add(ts,BorderLayout.WEST);
        // Search row
        JPanel sr=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); sr.setOpaque(false);
        searchField=new JTextField(14); searchField.setFont(new Font("Georgia",Font.PLAIN,12)); searchField.setBackground(C_FIELD_BG); searchField.setForeground(C_CHARCOAL); searchField.setCaretColor(C_CHARCOAL); searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,8,4,8)));
        JButton searchBtn=mkBtn("Search",false,null); searchBtn.addActionListener(e->searchPatients());
        searchField.addActionListener(e->searchPatients());
        sr.add(new JLabel("Search: "){{setFont(new Font("Georgia",Font.BOLD,12));setForeground(C_CHARCOAL);}});
        sr.add(searchField); sr.add(searchBtn); top.add(sr,BorderLayout.EAST);
        p.add(top,BorderLayout.NORTH);
        // Table
        p.add(buildTablePanel(),BorderLayout.CENTER);
        // Button bar
        p.add(buildButtonBar(),BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildTablePanel(){
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(C_RIGHT); wrap.setBorder(new EmptyBorder(8,26,6,26));
        tableModel=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); table.setFont(new Font("Georgia",Font.PLAIN,12)); table.setRowHeight(30); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,12)); hdr.setBackground(C_TBL_HDR); hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,8,0,5));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);setForeground(col==0?C_GOLD:C_CHARCOAL);}
                setHorizontalAlignment(col==0||col==5?CENTER:LEFT); return this;}
        });
        int[]widths={50,90,90,110,170,45,130,110}; for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        wrap.add(sc,BorderLayout.CENTER); return wrap;
    }

    private JPanel buildButtonBar(){
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(26,0,getWidth()-26,0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,26,12,26));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        JButton btnAdd=mkBtn("+ Add",true,null); JButton btnEdit=mkBtn("Edit",false,null); JButton btnRemove=mkBtn("Remove",false,C_DANGER);
        JButton btnMed=mkBtn("Medical Hist.",false,null); JButton btnDental=mkBtn("Dental Hist.",false,null); JButton btnApt=mkBtn("Appointments",false,null); JButton btnTx=mkBtn("Treatments",false,null);
        btnAdd.addActionListener(e->addPatient()); btnEdit.addActionListener(e->editPatient()); btnRemove.addActionListener(e->removePatient());
        btnMed.addActionListener(e->viewMedicalHistory()); btnDental.addActionListener(e->viewDentalHistory()); btnApt.addActionListener(e->viewAppointments()); btnTx.addActionListener(e->viewTreatmentPlans());
        lb.add(btnAdd);lb.add(btnEdit);lb.add(btnRemove);lb.add(Box.createHorizontalStrut(8));lb.add(btnMed);lb.add(btnDental);lb.add(btnApt);lb.add(btnTx);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton btnBack=mkBtn("← Back",false,null); btnBack.addActionListener(e->{dispose();try{new ClinicManagerPage().setVisible(true);}catch(Exception ex){}});
        rb.add(btnBack); bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}
        return btn;
    }

    private void loadData(){
        tableModel.setRowCount(0);
        try{
            Connection conn=DatabaseConnection.getConnection();
            ResultSet rs=conn.prepareStatement("SELECT PatientID,FirstName,LastName,PhoneNumber,EmailAddress,Age FROM Patient ORDER BY PatientID").executeQuery();
            while(rs.next()){
                int pid=rs.getInt("PatientID"); String ins="",pol="";
                try{PreparedStatement is=conn.prepareStatement("SELECT TOP 1 ProviderName,PolicyNumber FROM Insurance WHERE PatientID=?");is.setInt(1,pid);ResultSet ir=is.executeQuery();if(ir.next()){ins=ir.getString("ProviderName");pol=ir.getString("PolicyNumber");}ir.close();is.close();}catch(Exception ignored){}
                tableModel.addRow(new Object[]{pid,rs.getString("FirstName"),rs.getString("LastName"),rs.getString("PhoneNumber"),rs.getString("EmailAddress"),rs.getInt("Age"),ins,pol});
            }
            rs.close(); conn.close();
        }catch(SQLException e){JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
        repaint();
    }

    private void searchPatients(){
        String term=searchField.getText().toLowerCase().trim(); if(term.isEmpty())return;
        for(int i=0;i<tableModel.getRowCount();i++){
            String fn=tableModel.getValueAt(i,1).toString().toLowerCase(), ln=tableModel.getValueAt(i,2).toString().toLowerCase(), ph=tableModel.getValueAt(i,3).toString().toLowerCase();
            if(fn.contains(term)||ln.contains(term)||ph.contains(term)){table.setRowSelectionInterval(i,i);table.scrollRectToVisible(table.getCellRect(i,0,true));return;}
        }
        JOptionPane.showMessageDialog(this,"No patients found for: "+term);
    }

    private void addPatient(){ showPatientDialog(-1,null,null,null,null,0,null,null); }
    private void editPatient(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}
        showPatientDialog((int)tableModel.getValueAt(row,0),(String)tableModel.getValueAt(row,1),(String)tableModel.getValueAt(row,2),(String)tableModel.getValueAt(row,3),(String)tableModel.getValueAt(row,4),(int)tableModel.getValueAt(row,5),(String)tableModel.getValueAt(row,6),(String)tableModel.getValueAt(row,7));
    }

    private void showPatientDialog(int pid,String fn,String ln,String phone,String email,int age,String ins,String pol){
        boolean isNew=pid<0;
        JDialog dlg=new JDialog(this,isNew?"Add Patient":"Edit Patient",true); dlg.setSize(460,460); dlg.setLocationRelativeTo(this);
        JPanel panel=new JPanel(new GridBagLayout()); panel.setBackground(C_RIGHT); panel.setBorder(new EmptyBorder(22,26,18,26));
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(6,6,6,12); g.anchor=GridBagConstraints.WEST;
        JTextField fFN=new JTextField(isNew?"":fn,16),fLN=new JTextField(isNew?"":ln,16),fPH=new JTextField(isNew?"":phone,16),fEM=new JTextField(isNew?"":email,16),fIN=new JTextField(isNew?"":ins!=null?ins:"",16),fPO=new JTextField(isNew?"":pol!=null?pol:"",16);
        JSpinner fAG=new JSpinner(new SpinnerNumberModel(isNew?25:age,1,120,1));
        styleField(fFN);styleField(fLN);styleField(fPH);styleField(fEM);styleField(fIN);styleField(fPO);
        String[]labels={"First Name","Last Name","Phone","Email","Age","Insurance Provider","Policy Number"};
        JComponent[]fields={fFN,fLN,fPH,fEM,fAG,fIN,fPO};
        for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;JLabel l=new JLabel(labels[i]);l.setFont(new Font("Georgia",Font.BOLD,12));l.setForeground(C_CHARCOAL);panel.add(l,g);g.gridx=1;panel.add(fields[i],g);}
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow.setOpaque(false);
        JButton save=mkBtn(isNew?"Add Patient":"Update",true,null),cancel=mkBtn("Cancel",false,null);
        save.addActionListener(ev->{ try{
            if(fFN.getText().trim().isEmpty()||fLN.getText().trim().isEmpty()){JOptionPane.showMessageDialog(dlg,"Name is required.");return;}
            Connection conn=DatabaseConnection.getConnection();
            if(isNew){
                ResultSet mr=conn.prepareStatement("SELECT MAX(PatientID) FROM Patient").executeQuery(); int np=1; if(mr.next())np=mr.getInt(1)+1; mr.close();
                PreparedStatement s=conn.prepareStatement("INSERT INTO Patient VALUES(?,?,?,?,?,?)"); s.setInt(1,np);s.setString(2,fFN.getText().trim());s.setString(3,fLN.getText().trim());s.setString(4,fPH.getText().trim());s.setString(5,fEM.getText().trim());s.setInt(6,(int)fAG.getValue());s.executeUpdate();s.close();
            }else{
                PreparedStatement s=conn.prepareStatement("UPDATE Patient SET FirstName=?,LastName=?,PhoneNumber=?,EmailAddress=?,Age=? WHERE PatientID=?");
                s.setString(1,fFN.getText().trim());s.setString(2,fLN.getText().trim());s.setString(3,fPH.getText().trim());s.setString(4,fEM.getText().trim());s.setInt(5,(int)fAG.getValue());s.setInt(6,pid);s.executeUpdate();s.close();
            }
            conn.close(); loadData(); dlg.dispose(); JOptionPane.showMessageDialog(this,isNew?"Patient added.":"Patient updated.");
        }catch(SQLException ex){JOptionPane.showMessageDialog(dlg,"DB error: "+ex.getMessage());}});
        cancel.addActionListener(ev->dlg.dispose());
        btnRow.add(cancel); btnRow.add(save);
        g.gridx=0;g.gridy=labels.length;g.gridwidth=2;panel.add(btnRow,g);
        dlg.setContentPane(panel); dlg.setVisible(true);
    }

    private void styleField(JTextField f){f.setFont(new Font("Georgia",Font.PLAIN,12));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));}

    private void removePatient(){
        int row=table.getSelectedRow(); if(row<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}
        int ok=JOptionPane.showConfirmDialog(this,"Remove this patient and all related records?","Confirm",JOptionPane.YES_NO_OPTION);
        if(ok==JOptionPane.YES_OPTION){try{int pid=(int)tableModel.getValueAt(row,0);Connection conn=DatabaseConnection.getConnection();PreparedStatement s=conn.prepareStatement("DELETE FROM Patient WHERE PatientID=?");s.setInt(1,pid);s.executeUpdate();s.close();conn.close();loadData();JOptionPane.showMessageDialog(this,"Patient removed.");}catch(SQLException e){JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage());}}
    }

    private void viewMedicalHistory(){int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}JOptionPane.showMessageDialog(this,"Medical history for: "+tableModel.getValueAt(r,1)+" "+tableModel.getValueAt(r,2));}
    private void viewDentalHistory(){int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}int pid=(int)tableModel.getValueAt(r,0);String name=tableModel.getValueAt(r,1)+" "+tableModel.getValueAt(r,2);try{new DentalHistoryPage(pid,name).setVisible(true);}catch(Exception e){JOptionPane.showMessageDialog(this,"DentalHistoryPage not available.");}}
    private void viewAppointments(){int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}JOptionPane.showMessageDialog(this,"Appointments for: "+tableModel.getValueAt(r,1)+" "+tableModel.getValueAt(r,2));}
    private void viewTreatmentPlans(){int r=table.getSelectedRow();if(r<0){JOptionPane.showMessageDialog(this,"Select a patient.");return;}JOptionPane.showMessageDialog(this,"Treatment plans for: "+tableModel.getValueAt(r,1)+" "+tableModel.getValueAt(r,2));}

    public static void main(String[]args){SwingUtilities.invokeLater(()->new PatientManagement().setVisible(true));}
}