package Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import Control.InventoryManagement;
import Control.SupplierManagement;
import javax.swing.Timer;

public class XMLImportPage extends JFrame {

    private static final Color C_BG      = new Color(15,  17,  20);
    private static final Color C_BANNER  = new Color(20,  22,  27);
    private static final Color C_BODY    = new Color(245, 242, 236);
    private static final Color C_GOLD    = new Color(188, 152, 90);
    private static final Color C_IVORY   = new Color(250, 247, 241);
    private static final Color C_CHARCOAL= new Color(35,  36,  40);
    private static final Color C_MUTED   = new Color(110, 106, 98);
    private static final Color C_CARD_BG = new Color(237, 233, 224);
    private static final Color C_SUCCESS = new Color(60,  130, 90);
    private static final Color C_DANGER  = new Color(150, 65,  55);

    private JTextArea resultArea;
    private float bannerAlpha = 0f;

    public XMLImportPage() {
        super("Import XML Data — DentalCare");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);
        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);

        Timer fade=new Timer(16,e->{bannerAlpha=Math.min(1f,bannerAlpha+0.03f);root.getComponent(0).repaint();if(bannerAlpha>=1f)((Timer)e.getSource()).stop();});
        fade.start();
    }

    private JPanel buildBanner() {
        JPanel banner=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,bannerAlpha));int w=getWidth(),h=getHeight();g2.setPaint(new GradientPaint(0,0,C_BANNER,w,h,C_BG));g2.fillRect(0,0,w,h);g2.setColor(new Color(255,255,255,4));g2.setStroke(new BasicStroke(0.4f));for(int x=0;x<w;x+=22)g2.drawLine(x,0,x,h);for(int y=0;y<h;y+=22)g2.drawLine(0,y,w,y);g2.setColor(new Color(188,152,90,60));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(30,h-1,w-30,h-1);g2.dispose();}};
        banner.setOpaque(false); banner.setPreferredSize(new Dimension(0,80)); banner.setBorder(new EmptyBorder(0,28,0,28));

        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); left.setOpaque(false);
        left.add(makeEmblem());
        JPanel bs=new JPanel(); bs.setLayout(new BoxLayout(bs,BoxLayout.Y_AXIS)); bs.setOpaque(false); bs.setBorder(new EmptyBorder(0,12,0,0));
        JLabel bl=new JLabel("Import XML Data"); bl.setFont(new Font("Georgia",Font.BOLD,17)); bl.setForeground(C_IVORY);
        JLabel sl=new JLabel("Import inventory and supplier records from XML files"); sl.setFont(new Font("Georgia",Font.ITALIC,11)); sl.setForeground(C_GOLD);
        bs.add(bl); bs.add(sl); left.add(bs);
        banner.add(left,BorderLayout.WEST); return banner;
    }

    private JPanel buildBody() {
        JPanel body=new JPanel(new BorderLayout()); body.setBackground(C_BODY); body.setBorder(new EmptyBorder(18,30,14,30));

        // Button row
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); btnRow.setOpaque(false); btnRow.setBorder(new EmptyBorder(0,0,12,0));
        JButton bInv=mkBtn("Import Inventory XML",true,null);
        JButton bSup=mkBtn("Import Suppliers XML",true,null);
        bInv.addActionListener(e->importInventoryXml());
        bSup.addActionListener(e->importSupplierXml());
        btnRow.add(bInv); btnRow.add(bSup);
        body.add(btnRow,BorderLayout.NORTH);

        // Result area
        JPanel cardWrap=new JPanel(new BorderLayout()){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(C_CARD_BG);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.setColor(new Color(188,152,90,55));g2.setStroke(new BasicStroke(0.8f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);g2.dispose();}};
        cardWrap.setOpaque(false); cardWrap.setBorder(new EmptyBorder(14,16,14,16));
        JLabel rl=new JLabel("Import Log"); rl.setFont(new Font("Georgia",Font.BOLD,13)); rl.setForeground(C_CHARCOAL); rl.setBorder(new EmptyBorder(0,0,8,0));
        resultArea=new JTextArea("No imports yet.\n\nSelect an XML file to begin.");
        resultArea.setEditable(false); resultArea.setFont(new Font("Georgia",Font.PLAIN,13)); resultArea.setBackground(C_CARD_BG); resultArea.setForeground(C_CHARCOAL); resultArea.setLineWrap(true); resultArea.setWrapStyleWord(true); resultArea.setBorder(null);
        JScrollPane sc=new JScrollPane(resultArea); sc.setBorder(null); sc.getViewport().setBackground(C_CARD_BG);
        cardWrap.add(rl,BorderLayout.NORTH); cardWrap.add(sc,BorderLayout.CENTER);
        body.add(cardWrap,BorderLayout.CENTER);

        // Back button bar
        JPanel bar=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(C_BODY);g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(new Color(188,152,90,45));g2.setStroke(new BasicStroke(0.8f));g2.drawLine(0,0,getWidth(),0);g2.dispose();}};
        bar.setLayout(new FlowLayout(FlowLayout.RIGHT,8,0)); bar.setOpaque(false); bar.setBorder(new EmptyBorder(8,0,0,0));
        JButton back=mkBtn("← Back",false,null); back.addActionListener(e->dispose());
        bar.add(back); body.add(bar,BorderLayout.SOUTH);
        return body;
    }

    private void importInventoryXml(){
        JFileChooser chooser=new JFileChooser(); chooser.setDialogTitle("Select Inventory XML File");
        if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            File f=chooser.getSelectedFile();
            boolean ok=InventoryManagement.importInventoryFromXML(f.getAbsolutePath());
            resultArea.setText(ok?"✅ Inventory imported successfully from:\n"+f.getAbsolutePath():"❌ Failed to import inventory XML.\nCheck file format and try again.");
        }
    }
    private void importSupplierXml(){
        JFileChooser chooser=new JFileChooser(); chooser.setDialogTitle("Select Supplier XML File");
        if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            File f=chooser.getSelectedFile();
            boolean ok=SupplierManagement.importSuppliersFromXML(f.getAbsolutePath());
            resultArea.setText(ok?"✅ Suppliers imported successfully from:\n"+f.getAbsolutePath():"❌ Failed to import suppliers XML.\nCheck file format and try again.");
        }
    }

    private JPanel makeEmblem(){JPanel e=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int cx=getWidth()/2,cy=getHeight()/2,r=15;g2.setColor(new Color(188,152,90,60));g2.setStroke(new BasicStroke(0.8f));g2.drawOval(cx-r-3,cy-r-3,r*2+6,r*2+6);g2.setColor(new Color(188,152,90,100));g2.setStroke(new BasicStroke(1.1f));g2.drawOval(cx-r,cy-r,r*2,r*2);g2.setPaint(new RadialGradientPaint(cx,cy,r,new float[]{0f,1f},new Color[]{new Color(24,26,30),new Color(15,17,20)}));g2.fillOval(cx-r,cy-r,r*2,r*2);int arm=6,th=3;g2.setColor(new Color(188,152,90));g2.fillRoundRect(cx-th/2,cy-arm,th,arm*2,2,2);g2.fillRoundRect(cx-arm,cy-th/2,arm*2,th,2,2);g2.dispose();}@Override public Dimension getPreferredSize(){return new Dimension(38,38);}};e.setOpaque(false);return e;}
    private JButton mkBtn(String text,boolean primary,Color accent){JButton btn=new JButton(text);btn.setFont(new Font("Georgia",primary?Font.BOLD:Font.PLAIN,12));btn.setFocusPainted(false);btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));btn.setOpaque(true);Color border=accent!=null?accent:(primary?C_GOLD:new Color(188,152,90,100));if(primary){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,14,6,14)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}@Override public void mouseExited(MouseEvent e){btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}});}else{btn.setBackground(C_CARD_BG);btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));btn.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent!=null?accent:C_GOLD,1),new EmptyBorder(6,12,6,12)));}@Override public void mouseExited(MouseEvent e){btn.setForeground(accent!=null?accent:C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,12,6,12)));}});}return btn;}

    public static void main(String[]args){SwingUtilities.invokeLater(()->new XMLImportPage().setVisible(true));}
}