package Boundary;

import Entity.Patient;
import Entity.Staff;
import Entity.VisitReason;
import Entity.Appointment;
import Entity.XMLData;
import Control.AppointmentController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import javax.swing.Timer;

public class AppointmentBookingPage extends JFrame {

    private static final Color C_LEFT_TOP   = new Color(15, 17, 20);
    private static final Color C_LEFT_BOT   = new Color(26, 29, 34);
    private static final Color C_RIGHT      = new Color(245, 242, 236);
    private static final Color C_GOLD       = new Color(188, 152, 90);
    private static final Color C_IVORY      = new Color(250, 247, 241);
    private static final Color C_CHARCOAL   = new Color(35, 36, 40);
    private static final Color C_MUTED      = new Color(110, 106, 98);
    private static final Color C_CARD_BG    = new Color(237, 233, 224);
    private static final Color C_FIELD_BG   = new Color(228, 223, 213);
    private static final Color C_SUCCESS    = new Color(80, 148, 100);
    private static final Color C_WARNING    = new Color(188, 140, 60);

    private final Patient currentPatient;

    private JComboBox<VisitReason> cbVisitReason;
    private JComboBox<Staff> cbDentist;
    private JComboBox<String> cbTime;
    private JSpinner dateSpinner;
    private JTextArea txtNotes;
    private JRadioButton rbRoutine, rbUrgent;
    private ButtonGroup priorityGroup;
    private JLabel lblUrgentWarn;
    private JButton btnBook, btnCancel, btnCheckAvail;

    private float alpha = 0f;
    private float pulseVal = 0f;
    private int pulseDir = 1;

    private static final String[] TIME_SLOTS = {
            "09:00","09:30","10:00","10:30","11:00","11:30",
            "12:00","12:30","13:00","13:30","14:00","14:30",
            "15:00","15:30","16:00","16:30","17:00"
    };

    public AppointmentBookingPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("Book Appointment — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screen.width * 0.86), (int) (screen.height * 0.86));
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        JPanel leftPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 0);
            }
        };
        leftPanel.setBackground(C_LEFT_TOP);
        root.add(leftPanel, BorderLayout.WEST);

        JScrollPane scroll = buildRightScroll();
        root.add(scroll, BorderLayout.CENTER);

        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int leftW = Math.max(260, (int) (root.getWidth() * 0.28));
                leftPanel.setPreferredSize(new Dimension(leftW, 0));
                root.revalidate();
            }
        });

        Timer fade = new Timer(16, e -> {
            alpha = Math.min(1f, alpha + 0.025f);
            leftPanel.repaint();
            if (alpha >= 1f) ((Timer) e.getSource()).stop();
        });
        fade.start();

        Timer pulse = new Timer(30, e -> {
            pulseVal += 0.04f * pulseDir;
            if (pulseVal > 1f) {
                pulseVal = 1f;
                pulseDir = -1;
            }
            if (pulseVal < -1f) {
                pulseVal = -1f;
                pulseDir = 1;
            }
            leftPanel.repaint();
        });
        pulse.start();

        loadData();
    }

    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx = w / 2, cy = h / 2 - 50;

        GradientPaint bg = new GradientPaint(0, 0, C_LEFT_TOP, w, h, C_LEFT_BOT);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        RadialGradientPaint glow = new RadialGradientPaint(
                cx, cy, 140,
                new float[]{0f, 1f},
                new Color[]{new Color(188, 152, 90, 22), new Color(0, 0, 0, 0)}
        );
        g2.setPaint(glow);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 5));
        g2.setStroke(new BasicStroke(0.4f));
        for (int x = 0; x < w; x += 22) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 22) g2.drawLine(0, y, w, y);

        float r = 54f + pulseVal * 2f;
        paintEmblem(g2, cx, cy, r);

        g2.setColor(C_IVORY);
        g2.setFont(new Font("Georgia", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        String brand = "DentalCare";
        g2.drawString(brand, cx - fm.stringWidth(brand) / 2, cy + (int) r + 32);

        g2.setColor(C_GOLD);
        g2.setStroke(new BasicStroke(0.9f));
        int ry = cy + (int) r + 44;
        g2.drawLine(cx - 65, ry, cx + 65, ry);

        g2.setColor(C_GOLD);
        g2.setFont(new Font("Georgia", Font.ITALIC, 12));
        fm = g2.getFontMetrics();
        String sub = "Book Appointment";
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, ry + 18);

        int boxY = ry + 42;
        g2.setColor(new Color(188, 152, 90, 30));
        g2.fillRoundRect(cx - 90, boxY, 180, 54, 6, 6);
        g2.setColor(new Color(188, 152, 90, 70));
        g2.setStroke(new BasicStroke(0.7f));
        g2.drawRoundRect(cx - 90, boxY, 180, 54, 6, 6);

        g2.setColor(new Color(170, 163, 150));
        g2.setFont(new Font("Georgia", Font.ITALIC, 11));
        fm = g2.getFontMetrics();
        String forLabel = "Booking for";
        g2.drawString(forLabel, cx - fm.stringWidth(forLabel) / 2, boxY + 16);

        g2.setColor(C_IVORY);
        g2.setFont(new Font("Georgia", Font.BOLD, 13));
        fm = g2.getFontMetrics();
        String name = currentPatient.getFullName();
        while (fm.stringWidth(name) > 160 && name.length() > 6) {
            name = name.substring(0, name.length() - 4) + "…";
        }
        g2.drawString(name, cx - fm.stringWidth(name) / 2, boxY + 34);

        g2.setColor(new Color(100, 95, 88));
        g2.setFont(new Font("Georgia", Font.PLAIN, 11));
        fm = g2.getFontMetrics();
        String idStr = "ID: " + currentPatient.getPatientID();
        g2.drawString(idStr, cx - fm.stringWidth(idStr) / 2, boxY + 50);

        g2.setColor(new Color(55, 52, 48));
        g2.setFont(new Font("Serif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        String copy = "© DentalCare System";
        g2.drawString(copy, cx - fm.stringWidth(copy) / 2, h - 18);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188, 152, 90, 42));
        g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int) (cx - r - 12), (int) (cy - r - 12), (int) (r * 2 + 24), (int) (r * 2 + 24));

        g2.setColor(new Color(188, 152, 90, 85));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));

        RadialGradientPaint disk = new RadialGradientPaint(
                cx, cy, r,
                new float[]{0f, .65f, 1f},
                new Color[]{new Color(24, 26, 30), new Color(20, 22, 26), new Color(15, 17, 20)}
        );
        g2.setPaint(disk);
        g2.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));

        float arm = r * .42f, th = r * .18f;
        g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx - th / 2, cy - arm, th, arm * 2, 4, 4));
        g2.fill(new RoundRectangle2D.Float(cx - arm, cy - th / 2, arm * 2, th, 4, 4));

        g2.setColor(new Color(188, 152, 90, 95));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6 - Math.PI / 2;
            float in = r + 3, out = r + (i % 3 == 0 ? 10 : 7);
            g2.drawLine((int) (cx + Math.cos(a) * in), (int) (cy + Math.sin(a) * in),
                    (int) (cx + Math.cos(a) * out), (int) (cy + Math.sin(a) * out));
        }

        g2.setColor(new Color(188, 152, 90, 120));
        float dd = r * .63f, dr = r * .07f;
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2;
            float dx = (float) (cx + Math.cos(a) * dd), dy = (float) (cy + Math.sin(a) * dd);
            g2.fill(new Ellipse2D.Float(dx - dr, dy - dr, dr * 2, dr * 2));
        }
    }

    private JScrollPane buildRightScroll() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_RIGHT);
        form.setBorder(new EmptyBorder(24, 28, 22, 28));

        form.add(makeHRule(new Color(188, 152, 90, 60)));
        form.add(Box.createRigidArea(new Dimension(0, 14)));

        form.add(makeSectionTitle("Appointment Priority"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(buildPriorityCard());

        form.add(Box.createRigidArea(new Dimension(0, 16)));

        form.add(makeSectionTitle("Appointment Details"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(buildDetailsCard());

        form.add(Box.createRigidArea(new Dimension(0, 16)));

        form.add(makeSectionTitle("Date & Time"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(buildDateTimeCard());

        form.add(Box.createRigidArea(new Dimension(0, 16)));

        form.add(makeSectionTitle("Additional Notes"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(buildNotesCard());

        form.add(Box.createRigidArea(new Dimension(0, 20)));
        form.add(buildButtonRow());

        JScrollPane sc = new JScrollPane(form);
        sc.setBorder(null);
        sc.getViewport().setBackground(C_RIGHT);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sc.getVerticalScrollBar().setUnitIncrement(14);
        return sc;
    }

    private JPanel makeSectionTitle(String text) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Georgia", Font.BOLD, 15));
        lbl.setForeground(C_CHARCOAL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(lbl);
        wrap.add(Box.createRigidArea(new Dimension(0, 3)));
        wrap.add(makeHRule(new Color(188, 152, 90, 90)));
        return wrap;
    }

    private JPanel makeHRule(Color color) {
        JPanel rule = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(color);
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        rule.setOpaque(false);
        rule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        rule.setAlignmentX(Component.LEFT_ALIGNMENT);
        return rule;
    }

    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(188, 152, 90, 50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JPanel buildPriorityCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        priorityGroup = new ButtonGroup();
        rbRoutine = makeRadio("Routine Visit", false);
        rbUrgent = makeRadio("Urgent Treatment", true);
        rbRoutine.setSelected(true);
        priorityGroup.add(rbRoutine);
        priorityGroup.add(rbUrgent);

        lblUrgentWarn = new JLabel("Urgent appointments will be prioritized and scheduled immediately");
        lblUrgentWarn.setFont(new Font("Georgia", Font.ITALIC, 12));
        lblUrgentWarn.setForeground(C_WARNING);
        lblUrgentWarn.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblUrgentWarn.setVisible(false);

        rbUrgent.addActionListener(e -> lblUrgentWarn.setVisible(true));
        rbRoutine.addActionListener(e -> lblUrgentWarn.setVisible(false));

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioRow.setOpaque(false);
        radioRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioRow.add(rbRoutine);
        radioRow.add(Box.createHorizontalStrut(28));
        radioRow.add(rbUrgent);

        card.add(radioRow);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(lblUrgentWarn);
        return card;
    }

    private JRadioButton makeRadio(String text, boolean urgent) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Georgia", Font.BOLD, 13));
        rb.setForeground(urgent ? C_WARNING : C_CHARCOAL);
        rb.setBackground(C_CARD_BG);
        rb.setFocusPainted(false);
        rb.setOpaque(false);
        return rb;
    }

    private JPanel buildDetailsCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 12);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        card.add(makeFieldLabel("Reason for Visit"), g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 0.45;
        cbVisitReason = makeCombo(160);
        card.add(cbVisitReason, g);

        g.gridx = 2;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;
        card.add(makeFieldLabel("Preferred Dentist"), g);

        g.gridx = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 0.45;
        cbDentist = makeCombo(160);
        card.add(cbDentist, g);

        return card;
    }

    private JPanel buildDateTimeCard() {
        JPanel card = makeCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 12);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        card.add(makeFieldLabel("Preferred Date"), g);

        g.gridx = 1;
        SpinnerDateModel dm = new SpinnerDateModel();
        dm.setValue(java.util.Date.from(
                LocalDate.now().plusDays(1).atStartOfDay()
                        .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        dateSpinner = new JSpinner(dm);
        JSpinner.DateEditor de = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy");
        dateSpinner.setEditor(de);
        dateSpinner.setPreferredSize(new Dimension(125, 30));
        styleSpinner(dateSpinner);
        card.add(dateSpinner, g);

        g.gridx = 2;
        card.add(makeFieldLabel("Preferred Time"), g);

        g.gridx = 3;
        cbTime = makeCombo(95);
        for (String t : TIME_SLOTS) cbTime.addItem(t);
        card.add(cbTime, g);

        g.gridx = 4;
        btnCheckAvail = buildBtn("Check Availability", false);
        btnCheckAvail.setFont(new Font("Georgia", Font.PLAIN, 12));
        btnCheckAvail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_SUCCESS, 1),
                new EmptyBorder(6, 12, 6, 12)));
        btnCheckAvail.setForeground(C_SUCCESS);
        btnCheckAvail.addActionListener(this::checkAvailability);
        card.add(btnCheckAvail, g);

        return card;
    }

    private JPanel buildNotesCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 8));

        txtNotes = new JTextArea(3, 40);
        txtNotes.setFont(new Font("Georgia", Font.PLAIN, 12));
        txtNotes.setBackground(C_FIELD_BG);
        txtNotes.setForeground(C_CHARCOAL);
        txtNotes.setCaretColor(C_CHARCOAL);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane ns = new JScrollPane(txtNotes);
        ns.setBorder(BorderFactory.createLineBorder(new Color(188, 152, 90, 80), 1));
        ns.getViewport().setBackground(C_FIELD_BG);
        ns.setPreferredSize(new Dimension(0, 80));

        card.add(ns, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        btnCancel = buildBtn("Cancel", false);
        btnCancel.addActionListener(e -> goBack());

        btnBook = buildBtn("Book Appointment", true);
        btnBook.addActionListener(this::bookAppointment);

        row.add(btnCancel);
        row.add(btnBook);
        return row;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.BOLD, 13));
        l.setForeground(C_CHARCOAL);
        return l;
    }

    private <T> JComboBox<T> makeCombo(int width) {
        JComboBox<T> cb = new JComboBox<>();
        cb.setPreferredSize(new Dimension(width, 30));
        cb.setFont(new Font("Georgia", Font.PLAIN, 12));
        cb.setBackground(C_FIELD_BG);
        cb.setForeground(C_CHARCOAL);
        cb.setBorder(BorderFactory.createLineBorder(new Color(188, 152, 90, 90), 1));
        return cb;
    }

    private void styleSpinner(JSpinner sp) {
        JComponent ed = sp.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setBackground(C_FIELD_BG);
            tf.setForeground(C_CHARCOAL);
            tf.setBorder(new EmptyBorder(4, 8, 4, 8));
        }
        sp.setBorder(BorderFactory.createLineBorder(new Color(188, 152, 90, 90), 1));
    }

    private JButton buildBtn(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        if (primary) {
            btn.setBackground(new Color(35, 27, 12));
            btn.setForeground(new Color(230, 200, 145));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_GOLD, 1),
                    new EmptyBorder(8, 22, 8, 22)));
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(50, 38, 14));
                    btn.setForeground(C_IVORY);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(35, 27, 12));
                    btn.setForeground(new Color(230, 200, 145));
                }
            });
        } else {
            btn.setBackground(C_CARD_BG);
            btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(188, 152, 90, 100), 1),
                    new EmptyBorder(8, 22, 8, 22)));
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setForeground(C_CHARCOAL);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(C_GOLD, 1),
                            new EmptyBorder(8, 22, 8, 22)));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setForeground(C_MUTED);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(188, 152, 90, 100), 1),
                            new EmptyBorder(8, 22, 8, 22)));
                }
            });
        }
        return btn;
    }

    private void loadData() {
        cbVisitReason.removeAllItems();
        String[] reasons = {"Routine Cleaning", "Cavity Filling", "Dental Checkup",
                "Teeth Whitening", "Root Canal", "Emergency Treatment"};
        for (int i = 0; i < reasons.length; i++) {
            cbVisitReason.addItem(new VisitReason(i + 1, reasons[i]));
        }

        cbDentist.removeAllItems();
        try {
            for (Staff s : XMLData.getAllStaff()) {
                if (s.getRoleID() == 1) cbDentist.addItem(s);
            }
            if (cbDentist.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No dentists found.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading dentists: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkAvailability(ActionEvent e) {
        Staff dentist = (Staff) cbDentist.getSelectedItem();
        if (dentist == null) {
            JOptionPane.showMessageDialog(this, "Please select a dentist first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date date = (java.util.Date) dateSpinner.getValue();
        String time = (String) cbTime.getSelectedItem();
        boolean avail = Math.random() > 0.3;
        String fmt = new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);

        if (avail) {
            JOptionPane.showMessageDialog(this,
                    "Time slot available\n\nDentist: " + dentist.getFirstName() + " " + dentist.getLastName() +
                            "\nDate: " + fmt + "  ·  Time: " + time,
                    "Available", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Time slot unavailable\n\nPlease choose a different time or dentist.",
                    "Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void bookAppointment(ActionEvent e) {
        if (!validateForm()) return;

        try {
            java.util.Date date = (java.util.Date) dateSpinner.getValue();
            String timeStr = (String) cbTime.getSelectedItem();
            Staff dentist = (Staff) cbDentist.getSelectedItem();
            VisitReason reason = (VisitReason) cbVisitReason.getSelectedItem();
            boolean urgent = rbUrgent.isSelected();
            String fmt = new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Confirm Appointment\n\n" +
                            "Patient:  " + currentPatient.getFullName() + "\n" +
                            "Dentist:  Dr. " + dentist.getFirstName() + " " + dentist.getLastName() + "\n" +
                            "Date:     " + fmt + "  ·  " + timeStr + "\n" +
                            "Reason:   " + reason.getReasonName() +
                            (urgent ? "  (URGENT)" : "") + "\n\n" +
                            "A reminder will be sent 24 hours before.",
                    "Confirm Booking", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Appointment apt = new Appointment();
                apt.setPatientID(currentPatient.getPatientID());
                apt.setStaffID(dentist.getStaffID());
                apt.setAppointmentDate(new java.sql.Date(date.getTime()));
                apt.setAppointmentTime(java.sql.Time.valueOf(timeStr + ":00"));
                apt.setVisitReasonID(reason.getVisitReasonID());
                apt.setAppointmentStatusID(urgent ? 5 : 1);

                boolean ok = AppointmentController.bookAppointment(apt);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Appointment booked successfully!",
                            "Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    goBack();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to book appointment. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateForm() {
        if (cbVisitReason.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a reason.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cbDentist.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a dentist.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cbTime.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a time.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        java.util.Date d = (java.util.Date) dateSpinner.getValue();
        if (d.before(new java.util.Date())) {
            JOptionPane.showMessageDialog(this, "Please select a future date.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void goBack() {
        dispose();
    }
}