package Boundary;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import Control.InventoryManagement;
import Control.SupplierManagement;
import Entity.InventoryItem;
import Entity.Supplier;
import javax.swing.Timer;

public class InventoryPage extends JFrame {

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
    private static final Color C_LOW       = new Color(160, 65,  55);   // low stock warning
    private static final Color C_OK        = new Color(60,  130, 90);   // good stock

    private JTextField tfItemID, tfName, tfDescription, tfCategoryID,
                       tfQuantity, tfSerialNumber, tfSearchID, tfExpirationDate;
    private JComboBox<String> cbSupplier;
    private JTable table;
    private DefaultTableModel model;
    private InventoryManagement inventoryManager;
    private SupplierManagement supplierManager;
    private JFrame parentFrame;

    private float alpha = 0f, pulse = 0f;
    private int pDir = 1;

    private static final String[] COLS = {"ID","Name","Description","Category","Qty","Supplier","Serial #","Expiration"};

    public InventoryPage()           { this(null); }
    public InventoryPage(JFrame parent) {
        super("Inventory Management");
        this.parentFrame = parent;
        inventoryManager = new InventoryManagement();
        supplierManager  = new SupplierManagement();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1160, 660);
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
            @Override public Dimension getPreferredSize() { return new Dimension(240, 0); }
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
        right.setPreferredSize(new Dimension(918, 700));
        root.add(right, BorderLayout.EAST);

        Timer fade = new Timer(16, e -> { alpha=Math.min(1f,alpha+0.025f); left.repaint(); if(alpha>=1f)((Timer)e.getSource()).stop(); });
        fade.start();
        Timer pt = new Timer(30, e -> { pulse+=0.04f*pDir; if(pulse>1f){pulse=1f;pDir=-1;} if(pulse<-1f){pulse=-1f;pDir=1;} left.repaint(); });
        pt.start();

        loadSuppliers();
        loadTableData();
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx=w/2, cy=h/2-60;
        g2.setPaint(new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT)); g2.fillRect(0,0,w,h);
        g2.setPaint(new RadialGradientPaint(cx,cy,120,new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)})); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h); for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);

        float r = 48f + pulse*2f;
        paintEmblem(g2,cx,cy,r);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,18));
        FontMetrics fm=g2.getFontMetrics(); String br="DentalCare";
        g2.drawString(br,cx-fm.stringWidth(br)/2,cy+(int)r+28);
        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry=cy+(int)r+40; g2.drawLine(cx-55,ry,cx+55,ry);
        g2.setFont(new Font("Georgia",Font.ITALIC,12)); fm=g2.getFontMetrics();
        String sub="Inventory Management"; g2.drawString(sub,cx-fm.stringWidth(sub)/2,ry+17);

        // Item count badge
        int count=model==null?0:model.getRowCount();
        int bx=cx-58,by=ry+34;
        g2.setColor(new Color(188,152,90,25)); g2.fillRoundRect(bx,by,116,42,6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f)); g2.drawRoundRect(bx,by,116,42,6,6);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,20)); fm=g2.getFontMetrics();
        String cnt=String.valueOf(count); g2.drawString(cnt,cx-fm.stringWidth(cnt)/2,by+26);
        g2.setColor(new Color(130,124,112)); g2.setFont(new Font("Georgia",Font.ITALIC,10)); fm=g2.getFontMetrics();
        String cl="inventory items"; g2.drawString(cl,cx-fm.stringWidth(cl)/2,by+39);

        // Quick tips
        int ty=by+58;
        String[]tips={"Select a row to fill the form","Edit fields then click Update","Serial # must be unique"};
        for(int i=0;i<tips.length;i++){
            g2.setColor(new Color(80,76,70)); g2.setFont(new Font("Georgia",Font.ITALIC,9)); fm=g2.getFontMetrics();
            g2.drawString("· "+tips[i],cx-58,ty+i*14);
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
    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(C_RIGHT);

        // Top bar
        JPanel top = new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(22,getHeight()-1,getWidth()-22,getHeight()-1);g2.dispose();}};
        top.setOpaque(false); top.setBorder(new EmptyBorder(13,24,10,24));
        JLabel tl=new JLabel("Inventory Management"); tl.setFont(new Font("Georgia",Font.BOLD,20)); tl.setForeground(C_CHARCOAL);
        JLabel sl=new JLabel("Add, update and track dental supplies and equipment"); sl.setFont(new Font("Georgia",Font.ITALIC,12)); sl.setForeground(C_GOLD);
        JPanel ts=new JPanel(); ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS)); ts.setOpaque(false); ts.add(tl); ts.add(Box.createRigidArea(new Dimension(0,3))); ts.add(sl);
        top.add(ts,BorderLayout.WEST);
        // Search on right
        JPanel sr=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0)); sr.setOpaque(false);
        JLabel sl2=new JLabel("Search ID:"); sl2.setFont(new Font("Georgia",Font.BOLD,12)); sl2.setForeground(C_CHARCOAL);
        tfSearchID=new JTextField(8); styleField(tfSearchID);
        JButton sb=mkBtn("Search",false,null); sb.addActionListener(e->searchItemByID()); tfSearchID.addActionListener(e->searchItemByID());
        sr.add(sl2); sr.add(tfSearchID); sr.add(sb);
        top.add(sr,BorderLayout.EAST);
        p.add(top,BorderLayout.NORTH);

        // Center: form card (left) + table (right) split
        JPanel center=new JPanel(new GridLayout(1,2,14,0)); center.setBackground(C_RIGHT); center.setBorder(new EmptyBorder(10,24,6,24));
        center.add(buildFormCard());
        center.add(buildTableCard());
        p.add(center,BorderLayout.CENTER);

        // Button bar
        p.add(buildButtonBar(),BorderLayout.SOUTH);
        return p;
    }

    // ── Form card ────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card=makeCard(); card.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints(); g.insets=new Insets(5,10,5,10); g.anchor=GridBagConstraints.WEST;

        tfItemID=makeField();tfName=makeField();tfDescription=makeField();tfCategoryID=makeField();
        tfQuantity=makeField();tfSerialNumber=makeField();tfExpirationDate=makeField();
        cbSupplier=new JComboBox<>(); styleCombo(cbSupplier);

        String[]labels={"Item ID","Name","Description","Category ID","Quantity","Supplier","Serial #","Expiry (dd-MM-yyyy)"};
        JComponent[]fields={tfItemID,tfName,tfDescription,tfCategoryID,tfQuantity,cbSupplier,tfSerialNumber,tfExpirationDate};
        for(int i=0;i<labels.length;i++){
            g.gridx=0;g.gridy=i; JLabel l=new JLabel(labels[i]); l.setFont(new Font("Georgia",Font.BOLD,12)); l.setForeground(C_CHARCOAL); card.add(l,g);
            g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1.0; card.add(fields[i],g);
            g.fill=GridBagConstraints.NONE;g.weightx=0;
        }
        return card;
    }

    // ── Table card ───────────────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card=makeCard(); card.setLayout(new BorderLayout(0,6));
        model=new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(model); table.setFont(new Font("Georgia",Font.PLAIN,11)); table.setRowHeight(27); table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TBL_ROW); table.setForeground(C_CHARCOAL); table.setFillsViewportHeight(true);
        JTableHeader hdr=table.getTableHeader(); hdr.setFont(new Font("Georgia",Font.BOLD,11)); hdr.setBackground(C_TBL_HDR);
        hdr.setForeground(new Color(215,185,120)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD)); hdr.setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col); setBorder(new EmptyBorder(0,6,0,4));
                if(sel){setBackground(C_SEL);setForeground(C_CHARCOAL);}
                else{setBackground(row%2==0?C_TBL_ROW:C_TBL_ALT);
                    if(col==0){setForeground(C_GOLD);setHorizontalAlignment(CENTER);}
                    else if(col==4){// qty: red if low
                        try{int qty=Integer.parseInt(v.toString().trim());setForeground(qty<=5?C_LOW:qty>=20?C_OK:C_CHARCOAL);}catch(Exception ex){setForeground(C_CHARCOAL);}
                        setHorizontalAlignment(CENTER);}
                    else{setForeground(C_CHARCOAL);setHorizontalAlignment(LEFT);}
                } return this;}
        });
        int[]widths={44,90,100,55,44,60,90,88}; for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1)); sc.getViewport().setBackground(C_TBL_ROW);
        card.add(sc,BorderLayout.CENTER); return card;
    }

    private JPanel makeCard(){
        JPanel c=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        c.setOpaque(false); c.setBorder(new EmptyBorder(14,16,14,16)); return c;
    }

    // ── Button bar ───────────────────────────────────────────────────────────
    private JPanel buildButtonBar(){
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_RIGHT);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(22,0,getWidth()-22,0);g2.dispose();}};
        bar.setLayout(new BorderLayout()); bar.setOpaque(false); bar.setBorder(new EmptyBorder(7,24,12,24));
        JPanel lb=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0)); lb.setOpaque(false);
        JButton bAdd=mkBtn("+ Add",true,null),bUpd=mkBtn("Update",false,null),bDel=mkBtn("Delete",false,new Color(150,65,55)),bClr=mkBtn("Clear",false,null);
        bAdd.addActionListener(e->addItem()); bUpd.addActionListener(e->updateItem()); bDel.addActionListener(e->deleteItem()); bClr.addActionListener(e->clearForm());
        lb.add(bAdd);lb.add(bUpd);lb.add(bDel);lb.add(bClr);
        JPanel rb=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); rb.setOpaque(false);
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->goBack()); rb.add(back);
        bar.add(lb,BorderLayout.WEST); bar.add(rb,BorderLayout.EAST); return bar;
    }

    // ── Widget factories ─────────────────────────────────────────────────────
    private JButton mkBtn(String text,boolean primary,Color accent){
        JButton btn=new JButton(text); btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12)); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setOpaque(true);
        Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));
        if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}
        else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}
        return btn;
    }

    private JTextField makeField(){JTextField f=new JTextField();f.setPreferredSize(new Dimension(160,30));styleField(f);return f;}
    private void styleField(JTextField f){f.setFont(new Font("Georgia",Font.PLAIN,12));f.setBackground(C_FIELD_BG);f.setForeground(C_CHARCOAL);f.setCaretColor(C_CHARCOAL);f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1),new EmptyBorder(4,7,4,7)));}
    private void styleCombo(JComboBox<String> cb){cb.setPreferredSize(new Dimension(160,30));cb.setFont(new Font("Georgia",Font.PLAIN,12));cb.setBackground(C_FIELD_BG);cb.setForeground(C_CHARCOAL);cb.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));}

    // ── Data operations ───────────────────────────────────────────────────────
    private void loadSuppliers(){
        cbSupplier.removeAllItems();
        for(Supplier s:supplierManager.getAllSuppliers()) cbSupplier.addItem(s.getSupplierID()+" - "+s.getSupplierName());
    }

    private void loadTableData(){
        model.setRowCount(0);
        for(InventoryItem it:inventoryManager.getAllInventoryItems()){
            String exp=it.getExpirationDate()!=null?it.getExpirationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")):"";
            model.addRow(new Object[]{it.getInventoryItemID(),it.getItemName(),it.getDescription(),it.getCategoryID(),it.getQuantityInStock(),it.getSupplierID(),it.getSerialNumber(),exp});
        }
        repaint();
    }

    private void fillFormFromTable(){
        int r=table.getSelectedRow(); if(r<0)return;
        tfItemID.setText(model.getValueAt(r,0)+""); tfName.setText(model.getValueAt(r,1)+"");
        tfDescription.setText(model.getValueAt(r,2)+""); tfCategoryID.setText(model.getValueAt(r,3)+"");
        tfQuantity.setText(model.getValueAt(r,4)+""); tfSerialNumber.setText(model.getValueAt(r,6)+"");
        tfExpirationDate.setText(model.getValueAt(r,7)+""); tfSearchID.setText(model.getValueAt(r,0)+"");
        String sid=model.getValueAt(r,5)+"";
        for(int i=0;i<cbSupplier.getItemCount();i++){if(cbSupplier.getItemAt(i).startsWith(sid)){cbSupplier.setSelectedIndex(i);break;}}
    }

    private void addItem(){
        try{
            int id=Integer.parseInt(tfItemID.getText().trim());
            String name=tfName.getText().trim(),desc=tfDescription.getText().trim(),serial=tfSerialNumber.getText().trim();
            int cat=Integer.parseInt(tfCategoryID.getText().trim()),qty=Integer.parseInt(tfQuantity.getText().trim()),sup=Integer.parseInt(cbSupplier.getSelectedItem().toString().split(" - ")[0]);
            String dateStr=tfExpirationDate.getText().trim();
            if(dateStr.isEmpty()){JOptionPane.showMessageDialog(this,"Expiration required.");return;}
            java.util.Date d=new java.text.SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            java.sql.Date sqlD=new java.sql.Date(d.getTime());
            if(InventoryManagement.getInventoryItemById(id)!=null){JOptionPane.showMessageDialog(this,"ID already exists.","Warning",JOptionPane.WARNING_MESSAGE);return;}
            InventoryManagement.addInventoryItem(id,name,desc,cat,qty,sup,sqlD,serial);
            loadTableData(); clearForm(); JOptionPane.showMessageDialog(this,"Item added.");
        }catch(NumberFormatException nf){JOptionPane.showMessageDialog(this,"Check numeric fields.","Error",JOptionPane.ERROR_MESSAGE);}
        catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void updateItem(){
        try{
            int id=Integer.parseInt(tfItemID.getText());
            String name=tfName.getText().trim(),desc=tfDescription.getText().trim(),serial=tfSerialNumber.getText().trim();
            int cat=Integer.parseInt(tfCategoryID.getText().trim()),qty=Integer.parseInt(tfQuantity.getText().trim()),sup=Integer.parseInt(cbSupplier.getSelectedItem().toString().split(" - ")[0]);
            String dateStr=tfExpirationDate.getText().trim();
            if(dateStr.isEmpty()){JOptionPane.showMessageDialog(this,"Expiration required.");return;}
            java.util.Date d=new java.text.SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            InventoryManagement.updateInventoryItem(id,name,desc,cat,qty,sup,new java.sql.Date(d.getTime()),serial);
            loadTableData(); clearForm(); JOptionPane.showMessageDialog(this,"Item updated.");
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void deleteItem(){
        try{
            int id=Integer.parseInt(tfItemID.getText());
            int ok=JOptionPane.showConfirmDialog(this,"Delete item ID "+id+"?","Confirm",JOptionPane.YES_NO_OPTION);
            if(ok==JOptionPane.YES_OPTION){InventoryManagement.removeInventoryItem(id);loadTableData();clearForm();JOptionPane.showMessageDialog(this,"Item deleted.");}
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void searchItemByID(){
        try{
            int id=Integer.parseInt(tfSearchID.getText());
            InventoryItem it=InventoryManagement.getInventoryItemById(id);
            if(it!=null){JOptionPane.showMessageDialog(this,it.toString(),"Found",JOptionPane.INFORMATION_MESSAGE);}
            else JOptionPane.showMessageDialog(this,"Item not found.");
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Invalid ID.","Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void clearForm(){
        tfItemID.setText("");tfName.setText("");tfDescription.setText("");tfCategoryID.setText("");
        tfQuantity.setText("");tfSerialNumber.setText("");tfExpirationDate.setText("");tfSearchID.setText("");
        if(cbSupplier.getItemCount()>0)cbSupplier.setSelectedIndex(0);
    }

    private void goBack(){
        dispose();
        if(parentFrame!=null)parentFrame.setVisible(true);
        else MainMenu.showMainMenu();
    }

    public static void main(String[]args){SwingUtilities.invokeLater(()->new InventoryPage().setVisible(true));}
}