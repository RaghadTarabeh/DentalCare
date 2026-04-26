package Boundary;

import Entity.Staff;
import Entity.Patient;
import Entity.XMLData;
import Control.PatientController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.Timer;

public class MainMenu extends JFrame {

    private static MainMenu instance;

    // ── Palette (mirrors WelcomePage) ────────────────────────────────────────
    private static final Color C_BG_DARK      = new Color(0x0f1114);
    private static final Color C_BG_DARK2     = new Color(0x1a1c20);
    private static final Color C_IVORY        = new Color(0xfaf7f1);
    private static final Color C_PARCHMENT    = new Color(0xf5f2ec);
    private static final Color C_GOLD         = new Color(0xbc985a);
    private static final Color C_GOLD_LIGHT   = new Color(0xd7b978);
    private static final Color C_CHARCOAL     = new Color(0x201e1a);
    private static final Color C_MUTED        = new Color(0x8a8278);
    private static final Color C_CARD_BG      = new Color(0x16191d);
    private static final Color C_CARD_HOVER   = new Color(0x1e2126);
    private static final Color C_CARD_BORDER  = new Color(0x2a2d32);

    MainMenu() {
        setTitle("DentalCare — Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(new MainPanel());
    }

    public static MainMenu getInstance() {
        if (instance == null) instance = new MainMenu();
        return instance;
    }

    public static void showMainMenu() {
        MainMenu m = getInstance();
        m.setVisible(true);
        m.toFront();
    }

    // ── Main Panel ───────────────────────────────────────────────────────────
    class MainPanel extends JPanel {

        private float alpha = 0f;
        private final Timer fadeTimer;

        MainPanel() {
            setLayout(new BorderLayout());
            setBackground(C_BG_DARK);

            fadeTimer = new Timer(16, e -> {
                alpha = Math.min(1f, alpha + 0.025f);
                repaint();
                if (alpha >= 1f) ((Timer) e.getSource()).stop();
            });
            fadeTimer.start();

            add(buildHeader(), BorderLayout.NORTH);
            add(buildCardGrid(), BorderLayout.CENTER);
            add(buildFooter(), BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int w = getWidth(), h = getHeight();

            // Background
            g2.setColor(C_BG_DARK);
            g2.fillRect(0, 0, w, h);

            // Subtle crosshatch
            g2.setColor(new Color(255, 255, 255, 4));
            g2.setStroke(new BasicStroke(0.4f));
            for (int x = 0; x < w; x += 22) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 22) g2.drawLine(0, y, w, y);

            // Top gold rule
            g2.setColor(new Color(188, 152, 90, 70));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawLine(40, 0, w - 40, 0);

            g2.dispose();
        }
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(C_BG_DARK);
                g2.fillRect(0, 0, w, h);

                // Bottom border rule with gradient feel
                g2.setColor(new Color(188, 152, 90, 55));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(40, h - 1, w - 40, h - 1);
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 50, 26, 50));

        // Left: mini emblem + brand
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        MiniEmblem emblem = new MiniEmblem();
        emblem.setPreferredSize(new Dimension(46, 46));

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(0, 14, 0, 0));

        JLabel brandLabel = new JLabel("DentalCare");
        brandLabel.setFont(new Font("Georgia", Font.BOLD, 22));
        brandLabel.setForeground(C_IVORY);

        JLabel subLabel = new JLabel("Clinic Management System");
        subLabel.setFont(new Font("Georgia", Font.ITALIC, 12));
        subLabel.setForeground(C_GOLD);

        brandPanel.add(brandLabel);
        brandPanel.add(subLabel);

        left.add(emblem);
        left.add(brandPanel);

        // Right: heading
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        JLabel heading = new JLabel("Select Your Role");
        heading.setFont(new Font("Georgia", Font.BOLD, 26));
        heading.setForeground(C_IVORY);
        heading.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel sub = new JLabel("Choose a portal to continue");
        sub.setFont(new Font("Georgia", Font.ITALIC, 13));
        sub.setForeground(C_MUTED);
        sub.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Gold rule under heading
        JPanel rule = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_GOLD);
                g2.setStroke(new BasicStroke(0.9f));
                g2.drawLine(getWidth() - 100, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        rule.setOpaque(false);
        rule.setPreferredSize(new Dimension(100, 8));
        rule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));

        right.add(heading);
        right.add(Box.createRigidArea(new Dimension(0, 4)));
        right.add(sub);
        right.add(Box.createRigidArea(new Dimension(0, 6)));
        right.add(rule);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── Card Grid ────────────────────────────────────────────────────────────
    private JPanel buildCardGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(24, 50, 20, 50));

        JPanel grid = new JPanel(new GridLayout(2, 2, 22, 22));
        grid.setOpaque(false);

        String[][] roles = {
            {"Patient",        "Access your health records,\nappointments & history."},
            {"Dentist",        "Patient management,\ntreatment plans & notes."},
            {"Secretary",      "Schedule appointments &\nadministrative tasks."},
            {"Clinic Manager", "System administration,\nreports & analytics."},
        };
        // icon types: 0=circle, 1=diamond-cross, 2=circle-dot, 3=diamond
        int[] iconTypes = {0, 1, 2, 3};
        Runnable[] actions = {
            this::openPatientMenu, this::openDentistMenu,
            this::openSecretaryMenu, this::openClinicManagerMenu
        };

        for (int i = 0; i < 4; i++) {
            grid.add(createRoleCard(roles[i][0], iconTypes[i], roles[i][1], actions[i]));
        }

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    // Painted icon panel so we never depend on Unicode glyph support
    static class RoleIcon extends JPanel {
        private final int type; // 0=ring, 1=cross-diamond, 2=ring+dot, 3=open-diamond
        RoleIcon(int type) {
            this.type = type;
            setOpaque(false);
            setPreferredSize(new Dimension(22, 22));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setColor(new Color(188, 152, 90));
            g2.setStroke(new BasicStroke(1.4f));
            int r = 8;
            switch (type) {
                case 0: // open ring
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    break;
                case 1: // cross inside square
                    g2.drawRoundRect(cx - r, cy - r, r * 2, r * 2, 3, 3);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawLine(cx - 4, cy, cx + 4, cy);
                    g2.drawLine(cx, cy - 4, cx, cy + 4);
                    break;
                case 2: // ring with center dot
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                    break;
                case 3: // rotated square (diamond)
                    int[] xs = {cx, cx + r, cx, cx - r};
                    int[] ys = {cy - r, cy, cy + r, cy};
                    g2.drawPolygon(xs, ys, 4);
                    break;
            }
            g2.dispose();
        }
    }

    private JPanel createRoleCard(String title, int iconType, String desc, Runnable onClick) {
        JPanel card = new JPanel() {
            boolean hover = false;
            {
                setLayout(new BorderLayout());
                setBackground(C_CARD_BG);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(hover ? C_CARD_HOVER : C_CARD_BG);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(hover ? new Color(188, 152, 90, 190) : C_CARD_BORDER);
                g2.setStroke(new BasicStroke(hover ? 1.2f : 0.8f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                if (hover) {
                    g2.setColor(new Color(188, 152, 90, 120));
                    g2.fillRoundRect(0, h - 3, w, 3, 2, 2);
                }
                g2.setColor(new Color(255, 255, 255, 3));
                g2.setStroke(new BasicStroke(0.3f));
                for (int x = 0; x < w; x += 18) g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 18) g2.drawLine(0, y, w, y);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(26, 28, 26, 28));

        // Icon + title row
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);

        RoleIcon icon = new RoleIcon(iconType);
        icon.setPreferredSize(new Dimension(22, 26));

        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLabel.setForeground(C_IVORY);

        topRow.add(icon);
        topRow.add(titleLabel);

        // Thin gold rule
        JPanel rule = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(188, 152, 90, 90));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(0, getHeight() / 2, 56, getHeight() / 2);
                g2.dispose();
            }
        };
        rule.setOpaque(false);
        rule.setPreferredSize(new Dimension(56, 12));
        rule.setMaximumSize(new Dimension(56, 12));
        rule.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description — wider so text doesn't clip
        String htmlDesc = "<html><body style='width:200px; font-family:Georgia; font-size:13pt; color:#7a756b; line-height:1.5;'>"
                + desc.replace("\n", "<br>") + "</body></html>";
        JLabel descLabel = new JLabel(htmlDesc);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(topRow);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        inner.add(rule);
        inner.add(Box.createRigidArea(new Dimension(0, 12)));
        inner.add(descLabel);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(188, 152, 90, 50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(40, 0, getWidth() - 40, 0);
                g2.dispose();
            }
        };
        footer.setLayout(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 50, 18, 50));

        JLabel copy = new JLabel("© 2025 DentalCare System  ·  University Project  ·  Professional Dental Management");
        copy.setFont(new Font("Georgia", Font.ITALIC, 11));
        copy.setForeground(new Color(70, 66, 60));

        JButton exitBtn = buildExitButton();

        footer.add(copy, BorderLayout.WEST);
        footer.add(exitBtn, BorderLayout.EAST);
        return footer;
    }

    private JButton buildExitButton() {
        JButton btn = new JButton("Exit System") {
            private boolean hover = false;
            {
                setFont(new Font("Georgia", Font.PLAIN, 13));
                setForeground(C_MUTED);
                setBackground(C_CARD_BG);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(C_CARD_BORDER, 1),
                        new EmptyBorder(8, 20, 8, 20)));
                setFocusPainted(false);
                setOpaque(true);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        hover = true;
                        setForeground(C_IVORY);
                        setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(C_GOLD, 1),
                                new EmptyBorder(8, 20, 8, 20)));
                        repaint();
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hover = false;
                        setForeground(C_MUTED);
                        setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(C_CARD_BORDER, 1),
                                new EmptyBorder(8, 20, 8, 20)));
                        repaint();
                    }
                });
                addActionListener(e -> exitApp());
            }
        };
        return btn;
    }

    // ── Mini Emblem Component ────────────────────────────────────────────────
    static class MiniEmblem extends JPanel {
        MiniEmblem() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            int r = Math.min(cx, cy) - 2;

            g2.setColor(C_BG_DARK);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            g2.setColor(new Color(188, 152, 90, 110));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            float arm = r * 0.44f, th = r * 0.18f;
            g2.setColor(new Color(188, 152, 90));
            g2.fill(new RoundRectangle2D.Float(cx - th/2, cy - arm, th, arm*2, 3, 3));
            g2.fill(new RoundRectangle2D.Float(cx - arm, cy - th/2, arm*2, th, 3, 3));
            g2.dispose();
        }
        private static final Color C_BG_DARK = new Color(0x0f1114);
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    private void openPatientMenu() {
        showAccessDialog("Patient Portal", "Patient ID:", input -> {
            try {
                int id = Integer.parseInt(input);
                Patient p = PatientController.getPatientById(id);
                if (p != null) { setVisible(false); new PatientPage(p).setVisible(true); return true; }
                showErrorDialog("Not Found", "No patient found with ID: " + id); return false;
            } catch (NumberFormatException e) { showErrorDialog("Invalid Input", "Please enter a valid numeric ID."); return false; }
        });
    }

    private void openDentistMenu() {
        showAccessDialog("Dentist Portal", "Dentist ID:", input -> {
            Staff d = findDentistByID(input);
            if (d != null) { new DentistPage(d).setVisible(true); return true; }
            showErrorDialog("Not Found", "No dentist found with ID: " + input); return false;
        });
    }

    private void openSecretaryMenu() {
        showAccessDialog("Secretary Portal", "Secretary ID:", input -> {
            if (input.equals("99")) {
                SecretaryMenu page = new SecretaryMenu();
                page.setVisible(true); page.toFront(); page.requestFocus();
                setVisible(false);
                page.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosed(WindowEvent e) { MainMenu.showMainMenu(); }
                });
                return true;
            }
            showErrorDialog("Invalid ID", "Secretary ID must be 99."); return false;
        });
    }

    private void openClinicManagerMenu() {
        showAccessDialog("Clinic Manager Portal", "Manager ID:", input -> {
            if (input.equals("100")) {
                ClinicManagerPage page = new ClinicManagerPage();
                page.setVisible(true); page.toFront(); page.requestFocus();
                setVisible(false);
                page.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosed(WindowEvent e) { MainMenu.showMainMenu(); }
                });
                return true;
            }
            showErrorDialog("Invalid ID", "Clinic Manager ID must be 100."); return false;
        });
    }

    private void showAccessDialog(String portalTitle, String fieldLabel,
                                   java.util.function.Function<String, Boolean> validator) {
        JDialog dialog = new JDialog(this, portalTitle, true);
        dialog.setSize(440, 270);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(false);

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(C_BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle crosshatch
                g2.setColor(new Color(255,255,255,4));
                g2.setStroke(new BasicStroke(0.4f));
                for(int x=0;x<getWidth();x+=22) g2.drawLine(x,0,x,getHeight());
                for(int y=0;y<getHeight();y+=22) g2.drawLine(0,y,getWidth(),y);
                // top gold accent
                g2.setColor(new Color(188,152,90,80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(0,0,getWidth(),0);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(28, 32, 24, 32));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Header
        JLabel titleLbl = new JLabel(portalTitle);
        titleLbl.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLbl.setForeground(C_IVORY);

        JLabel subLbl = new JLabel("Enter your credentials to continue");
        subLbl.setFont(new Font("Georgia", Font.ITALIC, 12));
        subLbl.setForeground(C_MUTED);

        // Gold rule
        JPanel rule = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(188, 152, 90, 100));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(0, getHeight()/2, 70, getHeight()/2);
                g2.dispose();
            }
        };
        rule.setOpaque(false);
        rule.setPreferredSize(new Dimension(70, 10));
        rule.setMaximumSize(new Dimension(70, 10));

        // Input row
        JPanel inputRow = new JPanel(new BorderLayout(12, 0));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lbl = new JLabel(fieldLabel);
        lbl.setFont(new Font("Georgia", Font.BOLD, 14));
        lbl.setForeground(C_IVORY);
        lbl.setPreferredSize(new Dimension(90, 42));

        JTextField field = new JTextField();
        field.setFont(new Font("Georgia", Font.PLAIN, 14));
        field.setBackground(C_CARD_BG);
        field.setForeground(C_IVORY);
        field.setCaretColor(C_GOLD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_CARD_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)));

        inputRow.add(lbl, BorderLayout.WEST);
        inputRow.add(field, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton cancel = buildDialogButton("Cancel", false);
        JButton access = buildDialogButton("Access Portal", true);

        access.addActionListener(e -> {
            String inp = field.getText().trim();
            if (!inp.isEmpty()) { if (validator.apply(inp)) dialog.dispose(); }
            else showErrorDialog("Required", "Please enter your " + fieldLabel.toLowerCase().replace(":", "") + ".");
        });
        cancel.addActionListener(e -> dialog.dispose());
        field.addActionListener(e -> access.doClick());

        btnRow.add(cancel);
        btnRow.add(access);

        content.add(titleLbl);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(subLbl);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(rule);
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(inputRow);
        content.add(Box.createRigidArea(new Dimension(0, 22)));
        content.add(btnRow);

        panel.add(content, BorderLayout.CENTER);
        dialog.setContentPane(panel);

        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { field.requestFocusInWindow(); }
        });

        dialog.setVisible(true);
    }

    private JButton buildDialogButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        if (primary) {
            btn.setBackground(new Color(28, 20, 8));
            btn.setForeground(new Color(230, 200, 145));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_GOLD, 1),
                    new EmptyBorder(9, 22, 9, 22)));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(40, 28, 10)); btn.setForeground(C_IVORY);
                }
                @Override public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(28, 20, 8)); btn.setForeground(new Color(230,200,145));
                }
            });
        } else {
            btn.setBackground(C_CARD_BG);
            btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_CARD_BORDER, 1),
                    new EmptyBorder(9, 22, 9, 22)));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    btn.setForeground(C_IVORY); btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(188,152,90,100), 1),
                            new EmptyBorder(9,22,9,22)));
                }
                @Override public void mouseExited(MouseEvent e) {
                    btn.setForeground(C_MUTED); btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(C_CARD_BORDER, 1),
                            new EmptyBorder(9,22,9,22)));
                }
            });
        }
        return btn;
    }

    // ── Utilities ────────────────────────────────────────────────────────────
    private void exitApp() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit DentalCare?",
                "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) System.exit(0);
    }

    private void showErrorDialog(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private Staff findDentistByID(String idStr) {
        try {
            int id = Integer.parseInt(idStr);
            for (Staff s : XMLData.getAllStaff())
                if (s.getStaffID() == id && s.getRoleID() == 1) return s;
        } catch (NumberFormatException ignored) {}
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            MainMenu.showMainMenu();
        });
    }
}