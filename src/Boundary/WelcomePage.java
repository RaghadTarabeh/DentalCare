package Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;

public class WelcomePage extends JFrame {

    public WelcomePage() {
        setTitle("DentalCare");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(new WelcomePanel());
    }

    private void openMainMenu() {
        dispose();
        try {
            new MainMenu().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "MainMenu לא נפתח כרגע.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_BG       = new Color(7,   9,  12);
    private static final Color C_BG2      = new Color(12,  14, 18);
    private static final Color C_GOLD     = new Color(188, 152, 90);
    private static final Color C_GOLD_LT  = new Color(212, 178, 116);
    private static final Color C_IVORY    = new Color(240, 235, 224);
    private static final Color C_WHITE    = new Color(255, 255, 255);

    // ── Inner panel ───────────────────────────────────────────────────────────
    class WelcomePanel extends JPanel {

        private final Rectangle btnEnter = new Rectangle();
        private boolean enterHover = false;

        // Emblem pulse
        private float pulse = 0f;
        private int   pDir  = 1;

        public WelcomePanel() {
            setBackground(C_BG);

            // Emblem pulse — no fade, instant display
            Timer pulseTimer = new Timer(30, e -> {
                pulse += 0.04f * pDir;
                if (pulse >  1f) { pulse =  1f; pDir = -1; }
                if (pulse < -1f) { pulse = -1f; pDir =  1; }
                repaint();
            });
            pulseTimer.start();

            MouseAdapter m = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean nh = btnEnter.contains(e.getPoint());
                    if (nh != enterHover) {
                        enterHover = nh;
                        setCursor(Cursor.getPredefinedCursor(nh ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (btnEnter.contains(e.getPoint())) openMainMenu();
                }
                @Override public void mouseExited(MouseEvent e) {
                    enterHover = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            };
            addMouseMotionListener(m);
            addMouseListener(m);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();
            int cx = w / 2,    cy = h / 2;

            // ── Background ───────────────────────────────────────────────────
            g2.setPaint(new GradientPaint(0,0,C_BG, w,h,C_BG2));
            g2.fillRect(0,0,w,h);

            // Crosshatch texture
            g2.setColor(new Color(188,152,90,25));
            g2.setStroke(new BasicStroke(0.4f));
            for(int x=0;x<w;x+=35) g2.drawLine(x,0,x,h);
            for(int y=0;y<h;y+=35) g2.drawLine(0,y,w,y);

            // Radial spotlight behind emblem
            g2.setPaint(new RadialGradientPaint(cx, cy-60, 260,
                new float[]{0f,1f},
                new Color[]{new Color(188,152,90,55), new Color(0,0,0,0)}));
            g2.fillRect(0,0,w,h);

            // ── Corner ornaments ─────────────────────────────────────────────
            paintCorners(g2, w, h);

            // ── Top & bottom gold lines ───────────────────────────────────────
            paintHRule(g2, 0, w/2, 22, w);
            paintHRule(g2, 0, w/2, h-22, w);

            // ── Emblem ────────────────────────────────────────────────────────
            float embR = 44f + pulse * 1.8f;
            paintEmblem(g2, cx, cy - 130, embR);

            // ── Brand label ───────────────────────────────────────────────────
            g2.setFont(new Font("Georgia", Font.PLAIN, 11));
            g2.setColor(new Color(188,152,90,115));
            FontMetrics fm = g2.getFontMetrics();
            String brand = "D  E  N  T  A  L  C  A  R  E";
            g2.drawString(brand, cx - fm.stringWidth(brand)/2, cy - 58);

            // ── Main headline ─────────────────────────────────────────────────
            // Line 1 — bold
            g2.setFont(new Font("Georgia", Font.BOLD, 58));
            g2.setColor(C_WHITE);
            fm = g2.getFontMetrics();
            String line1 = "Where Your Smile";
            g2.drawString(line1, cx - fm.stringWidth(line1)/2, cy + 14);

            // Line 2 — light italic gold
            g2.setFont(new Font("Georgia", Font.ITALIC, 58));
            g2.setColor(new Color(188,152,90,220));
            fm = g2.getFontMetrics();
            String line2 = "Meets Your Story.";
            g2.drawString(line2, cx - fm.stringWidth(line2)/2, cy + 78);

            // ── Diamond ornament ──────────────────────────────────────────────
            paintDiamondOrnament(g2, cx, cy + 108);

            // ── Tagline ───────────────────────────────────────────────────────
            g2.setFont(new Font("Georgia", Font.ITALIC, 14));
            g2.setColor(new Color(188,152,90,100));
            fm = g2.getFontMetrics();
            String tag = "Excellence in Care  ·  Since 2024";
            g2.drawString(tag, cx - fm.stringWidth(tag)/2, cy + 148);

            // ── Enter button ──────────────────────────────────────────────────
            int btnW = 220, btnH = 50;
            int btnX = cx - btnW/2;
            int btnY = cy + 178;
            btnEnter.setBounds(btnX, btnY, btnW, btnH);
            paintButton(g2, btnEnter, "E N T E R   S Y S T E M", enterHover);

            // ── Footer ────────────────────────────────────────────────────────
            g2.setFont(new Font("Georgia", Font.PLAIN, 10));
            g2.setColor(new Color(188,152,90,50));
            fm = g2.getFontMetrics();
            String copy = "© DentalCare Management System";
            g2.drawString(copy, cx - fm.stringWidth(copy)/2, h - 16);

            g2.dispose();
        }

        private void paintCorners(Graphics2D g2, int w, int h) {
            g2.setColor(new Color(188,152,90,70));
            g2.setStroke(new BasicStroke(0.8f));
            int s = 30, m = 18;
            // TL
            g2.drawLine(m,m,m+s,m); g2.drawLine(m,m,m,m+s);
            // TR
            g2.drawLine(w-m,m,w-m-s,m); g2.drawLine(w-m,m,w-m,m+s);
            // BL
            g2.drawLine(m,h-m,m+s,h-m); g2.drawLine(m,h-m,m,h-m-s);
            // BR
            g2.drawLine(w-m,h-m,w-m-s,h-m); g2.drawLine(w-m,h-m,w-m,h-m-s);
        }

        private void paintHRule(Graphics2D g2, int x0, int cx, int y, int w) {
            int m = 60;
            g2.setPaint(new GradientPaint(m,y,new Color(188,152,90,0), cx,y,new Color(188,152,90,90)));
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawLine(m,y,cx,y);
            g2.setPaint(new GradientPaint(cx,y,new Color(188,152,90,90), w-m,y,new Color(188,152,90,0)));
            g2.drawLine(cx,y,w-m,y);
        }

        private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
            // Outer ring
            g2.setColor(new Color(188,152,90,30));
            g2.setStroke(new BasicStroke(0.6f));
            g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));

            // Main ring
            g2.setColor(new Color(188,152,90,80));
            g2.setStroke(new BasicStroke(0.9f));
            g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));

            // Filled disk
            g2.setPaint(new RadialGradientPaint(cx,cy,r,
                new float[]{0f,0.6f,1f},
                new Color[]{new Color(26,28,34),new Color(18,20,24),new Color(7,9,12)}));
            g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));

            // Cross
            float arm=r*0.44f, th=r*0.19f;
            g2.setColor(C_GOLD);
            g2.fill(new RoundRectangle2D.Float(cx-th/2, cy-arm, th, arm*2, 4,4));
            g2.fill(new RoundRectangle2D.Float(cx-arm,  cy-th/2, arm*2, th, 4,4));

            // Tick marks
            g2.setColor(new Color(188,152,90,90));
            g2.setStroke(new BasicStroke(0.9f));
            for(int i=0;i<12;i++){
                double a=i*Math.PI/6-Math.PI/2;
                float in=r+3, out=r+(i%3==0?11:7);
                g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),
                            (int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));
            }

            // 4 corner dots
            g2.setColor(new Color(188,152,90,120));
            float dd=r*0.65f, dr=r*0.08f;
            for(int i=0;i<4;i++){
                double a=Math.PI/4+i*Math.PI/2;
                float dx=(float)(cx+Math.cos(a)*dd), dy=(float)(cy+Math.sin(a)*dd);
                g2.fill(new Ellipse2D.Float(dx-dr,dy-dr,dr*2,dr*2));
            }
        }

        private void paintDiamondOrnament(Graphics2D g2, int cx, int y) {
            int lineLen = 70, gap = 10, dSize = 5;
            g2.setStroke(new BasicStroke(0.6f));
            // Left line
            g2.setPaint(new GradientPaint(cx-lineLen-gap,y,new Color(188,152,90,0),cx-gap,y,new Color(188,152,90,120)));
            g2.drawLine(cx-lineLen-gap, y, cx-gap, y);
            // Right line
            g2.setPaint(new GradientPaint(cx+gap,y,new Color(188,152,90,120),cx+lineLen+gap,y,new Color(188,152,90,0)));
            g2.drawLine(cx+gap, y, cx+lineLen+gap, y);
            // Diamond
            g2.setColor(new Color(188,152,90,160));
            int[] xs = {cx, cx+dSize, cx, cx-dSize};
            int[] ys = {y-dSize, y, y+dSize, y};
            g2.fillPolygon(xs, ys, 4);
        }

        private void paintButton(Graphics2D g2, Rectangle r, String text, boolean hover) {
            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Hover glow
            if(hover){
                g.setColor(new Color(188,152,90,30));
                g.fillRoundRect(r.x-3,r.y+4,r.width+6,r.height+2,4,4);
            }

            // Fill
            g.setColor(hover ? new Color(20,15,6) : new Color(10,8,3));
            g.fillRect(r.x, r.y, r.width, r.height);

            // Border — double line luxury style
            g.setColor(hover ? C_GOLD_LT : new Color(188,152,90,140));
            g.setStroke(new BasicStroke(hover ? 1f : 0.8f));
            g.drawRect(r.x, r.y, r.width, r.height);
            g.setColor(new Color(188,152,90,30));
            g.setStroke(new BasicStroke(0.5f));
            g.drawRect(r.x+3, r.y+3, r.width-6, r.height-6);

            // Text
            g.setFont(new Font("Georgia", Font.PLAIN, 13));
            g.setColor(hover ? C_IVORY : new Color(188,152,90,200));
            FontMetrics fm = g.getFontMetrics();
            int tx = r.x + (r.width  - fm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(text, tx, ty);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new WelcomePage().setVisible(true);
        });
    }
}