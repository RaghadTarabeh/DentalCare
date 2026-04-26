package Boundary;

import Entity.Patient;
import Entity.Appointment;
import Control.AppointmentController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.Timer;

public class MyAppointmentsPage extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color C_LEFT_TOP   = new Color(15,  17,  20);
    private static final Color C_LEFT_BOT   = new Color(26,  29,  34);
    private static final Color C_RIGHT      = new Color(245, 242, 236);
    private static final Color C_GOLD       = new Color(188, 152, 90);
    private static final Color C_IVORY      = new Color(250, 247, 241);
    private static final Color C_CHARCOAL   = new Color(35,  36,  40);
    private static final Color C_MUTED      = new Color(110, 106, 98);
    private static final Color C_CARD_BG    = new Color(237, 233, 224);
    private static final Color C_TABLE_HDR  = new Color(48,  42,  32);
    private static final Color C_TABLE_ROW  = new Color(240, 236, 227);
    private static final Color C_TABLE_ALT  = new Color(233, 228, 218);
    private static final Color C_SEL_BG     = new Color(188, 152, 90, 80);
    // Status accent colours (muted to fit ivory palette)
    private static final Color C_SCHEDULED  = new Color(70,  130, 90);
    private static final Color C_COMPLETED  = new Color(70,  110, 150);
    private static final Color C_CANCELLED  = new Color(150, 90,  80);
    private static final Color C_SUSPENDED  = new Color(170, 130, 50);
    private static final Color C_URGENT     = new Color(170, 70,  70);

    private final Patient currentPatient;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Appointment> appointments;

    private JButton btnDetails, btnCancel, btnReschedule, btnSuspend, btnApprove, btnBookNew, btnBack;

    private float alpha    = 0f;
    private float pulseVal = 0f;
    private int   pulseDir = 1;

    private static final String[] COLS = {"Date", "Time", "Dentist", "Reason", "Status", "Notes"};

    public MyAppointmentsPage(Patient patient) {
        this.currentPatient = patient;
        setTitle("My Appointments — " + patient.getFullName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1100, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LEFT_TOP);
        setContentPane(root);

        // Left painted panel
        JPanel leftPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                paintLeft(g2, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(290, 0); }
        };
        leftPanel.setBackground(C_LEFT_TOP);
        root.add(leftPanel, BorderLayout.WEST);

        // Divider
        JPanel div = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                GradientPaint gp = new GradientPaint(0,30,new Color(188,152,90,0),0,getHeight()*.5f,new Color(188,152,90,150),false);
                g2.setPaint(gp); g2.fillRect(0,0,1,getHeight()); g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(1,0); }
        };
        div.setOpaque(false);
        root.add(div, BorderLayout.CENTER);

        // Right panel
        JPanel right = buildRight();
        right.setPreferredSize(new Dimension(808, 660));
        root.add(right, BorderLayout.EAST);

        // Timers
        Timer fade = new Timer(16, e -> {
            alpha = Math.min(1f, alpha + 0.025f);
            leftPanel.repaint();
            if (alpha >= 1f) ((Timer)e.getSource()).stop();
        });
        fade.start();
        Timer pulse = new Timer(30, e -> {
            pulseVal += 0.04f * pulseDir;
            if (pulseVal >  1f) { pulseVal =  1f; pulseDir=-1; }
            if (pulseVal < -1f) { pulseVal = -1f; pulseDir= 1; }
            leftPanel.repaint();
        });
        pulse.start();

        loadAppointments();
        updateButtonStates();
    }

    // ── Left panel painting ──────────────────────────────────────────────────
    private void paintLeft(Graphics2D g2, int w, int h) {
        int cx = w/2, cy = h/2 - 60;
        GradientPaint bg = new GradientPaint(0,0,C_LEFT_TOP,w,h,C_LEFT_BOT);
        g2.setPaint(bg); g2.fillRect(0,0,w,h);
        RadialGradientPaint glow = new RadialGradientPaint(cx,cy,130,
                new float[]{0f,1f},new Color[]{new Color(188,152,90,20),new Color(0,0,0,0)});
        g2.setPaint(glow); g2.fillRect(0,0,w,h);
        // crosshatch
        g2.setColor(new Color(255,255,255,5)); g2.setStroke(new BasicStroke(0.4f));
        for(int x=0;x<w;x+=22) g2.drawLine(x,0,x,h);
        for(int y=0;y<h;y+=22) g2.drawLine(0,y,w,y);

        float r = 50f + pulseVal*2f;
        paintEmblem(g2, cx, cy, r);

        // Brand
        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,19));
        FontMetrics fm = g2.getFontMetrics();
        String brand = "DentalCare";
        g2.drawString(brand, cx-fm.stringWidth(brand)/2, cy+(int)r+30);

        g2.setColor(C_GOLD); g2.setStroke(new BasicStroke(0.8f));
        int ry = cy+(int)r+42;
        g2.drawLine(cx-60,ry,cx+60,ry);

        g2.setFont(new Font("Georgia",Font.ITALIC,12));
        fm = g2.getFontMetrics();
        String sub = "My Appointments";
        g2.drawString(sub, cx-fm.stringWidth(sub)/2, ry+17);

        // Patient box
        int boxY = ry+36;
        g2.setColor(new Color(188,152,90,25));
        g2.fillRoundRect(cx-85, boxY, 170, 52, 6,6);
        g2.setColor(new Color(188,152,90,65)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawRoundRect(cx-85, boxY, 170, 52, 6,6);

        g2.setColor(new Color(160,153,140)); g2.setFont(new Font("Georgia",Font.ITALIC,10));
        fm = g2.getFontMetrics();
        String fl = "Viewing for";
        g2.drawString(fl, cx-fm.stringWidth(fl)/2, boxY+14);

        g2.setColor(C_IVORY); g2.setFont(new Font("Georgia",Font.BOLD,13));
        fm = g2.getFontMetrics();
        String name = currentPatient.getFullName();
        while(fm.stringWidth(name)>150 && name.length()>4) name=name.substring(0,name.length()-4)+"…";
        g2.drawString(name, cx-fm.stringWidth(name)/2, boxY+31);

        g2.setColor(new Color(90,86,80)); g2.setFont(new Font("Georgia",Font.PLAIN,11));
        fm = g2.getFontMetrics();
        String id = "ID: "+currentPatient.getPatientID();
        g2.drawString(id, cx-fm.stringWidth(id)/2, boxY+47);

        // Stats placeholder — row count
        int count = appointments == null ? 0 : appointments.size();
        int statsY = boxY + 76;
        g2.setColor(new Color(188,152,90,30));
        g2.fillRoundRect(cx-85, statsY, 170, 38, 6,6);
        g2.setColor(new Color(188,152,90,60)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawRoundRect(cx-85, statsY, 170, 38, 6,6);
        g2.setColor(C_GOLD); g2.setFont(new Font("Georgia",Font.BOLD,20));
        fm = g2.getFontMetrics();
        String countStr = String.valueOf(count);
        g2.drawString(countStr, cx-fm.stringWidth(countStr)/2, statsY+23);
        g2.setColor(new Color(130,124,112)); g2.setFont(new Font("Georgia",Font.ITALIC,11));
        fm = g2.getFontMetrics();
        String cl = count==1?"appointment":"appointments";
        g2.drawString(cl, cx-fm.stringWidth(cl)/2, statsY+36);

        // Copyright
        g2.setColor(new Color(55,52,48)); g2.setFont(new Font("Serif",Font.PLAIN,10));
        fm = g2.getFontMetrics();
        String copy = "© DentalCare System";
        g2.drawString(copy, cx-fm.stringWidth(copy)/2, h-16);
    }

    private void paintEmblem(Graphics2D g2, int cx, int cy, float r) {
        g2.setColor(new Color(188,152,90,40)); g2.setStroke(new BasicStroke(0.7f));
        g2.drawOval((int)(cx-r-12),(int)(cy-r-12),(int)(r*2+24),(int)(r*2+24));
        g2.setColor(new Color(188,152,90,85)); g2.setStroke(new BasicStroke(1.1f));
        g2.drawOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        RadialGradientPaint disk = new RadialGradientPaint(cx,cy,r,new float[]{0f,.65f,1f},
                new Color[]{new Color(24,26,30),new Color(20,22,26),new Color(15,17,20)});
        g2.setPaint(disk); g2.fillOval((int)(cx-r),(int)(cy-r),(int)(r*2),(int)(r*2));
        float arm=r*.42f,th=r*.18f;
        g2.setColor(C_GOLD);
        g2.fill(new RoundRectangle2D.Float(cx-th/2,cy-arm,th,arm*2,3,3));
        g2.fill(new RoundRectangle2D.Float(cx-arm,cy-th/2,arm*2,th,3,3));
        g2.setColor(new Color(188,152,90,95)); g2.setStroke(new BasicStroke(1f));
        for(int i=0;i<12;i++){
            double a=i*Math.PI/6-Math.PI/2;
            float in=r+3,out=r+(i%3==0?10:7);
            g2.drawLine((int)(cx+Math.cos(a)*in),(int)(cy+Math.sin(a)*in),
                        (int)(cx+Math.cos(a)*out),(int)(cy+Math.sin(a)*out));
        }
        g2.setColor(new Color(188,152,90,120));
        float dd=r*.63f,dr=r*.07f;
        for(int i=0;i<4;i++){
            double a=Math.PI/4+i*Math.PI/2;
            g2.fill(new Ellipse2D.Float((float)(cx+Math.cos(a)*dd)-dr,(float)(cy+Math.sin(a)*dd)-dr,dr*2,dr*2));
        }
    }

    // ── Right panel ──────────────────────────────────────────────────────────
    private JPanel buildRight() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_RIGHT);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,55)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(30,getHeight()-1,getWidth()-30,getHeight()-1); g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(18, 28, 14, 28));

        JLabel titleLbl = new JLabel("My Appointments");
        titleLbl.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLbl.setForeground(C_CHARCOAL);

        JLabel patLbl = new JLabel(currentPatient.getFullName() + "  ·  ID " + currentPatient.getPatientID());
        patLbl.setFont(new Font("Georgia", Font.ITALIC, 13));
        patLbl.setForeground(C_GOLD);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(titleLbl);
        titleStack.add(Box.createRigidArea(new Dimension(0,3)));
        titleStack.add(patLbl);
        topBar.add(titleStack, BorderLayout.WEST);

        // War-time notice pill
        JLabel noticeLbl = new JLabel("War-time policy active");
        noticeLbl.setFont(new Font("Georgia", Font.ITALIC, 11));
        noticeLbl.setForeground(C_SUSPENDED);
        noticeLbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170,130,50,100),1),
                new EmptyBorder(3,10,3,10)));
        topBar.add(noticeLbl, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Table
        panel.add(buildTablePanel(), BorderLayout.CENTER);

        // Button bar
        panel.add(buildButtonBar(), BorderLayout.SOUTH);

        return panel;
    }

    // ── Table panel ──────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(C_RIGHT);
        wrap.setBorder(new EmptyBorder(10, 28, 6, 28));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Georgia", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(C_TABLE_ROW);
        table.setForeground(C_CHARCOAL);
        table.setFillsViewportHeight(true);

        // Header
        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(new Font("Georgia", Font.BOLD, 13));
        hdr.setBackground(C_TABLE_HDR);
        hdr.setForeground(new Color(215,185,120));
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0,C_GOLD));
        hdr.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)hdr.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,val,sel,foc,row,col);
                setBorder(new EmptyBorder(0,10,0,6));

                if (sel) {
                    setBackground(C_SEL_BG);
                    setForeground(C_CHARCOAL);
                } else {
                    setBackground(row%2==0 ? C_TABLE_ROW : C_TABLE_ALT);
                    // Color status column
                    if (col==4 && val!=null) {
                        String s = val.toString();
                        if      (s.contains("Complet"))  setForeground(C_COMPLETED);
                        else if (s.contains("Cancel"))   setForeground(C_CANCELLED);
                        else if (s.contains("Suspend"))  setForeground(C_SUSPENDED);
                        else if (s.contains("Urgent"))   setForeground(C_URGENT);
                        else                              setForeground(C_SCHEDULED);
                    } else {
                        setForeground(C_CHARCOAL);
                    }
                }
                setHorizontalAlignment(col<2 ? CENTER : LEFT);
                return this;
            }
        });

        // Column widths
        int[] widths = {88, 62, 145, 160, 100, 180};
        for(int i=0;i<widths.length;i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateButtonStates();
        });

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,55),1));
        sc.getViewport().setBackground(C_TABLE_ROW);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        wrap.add(sc, BorderLayout.CENTER);
        return wrap;
    }

    // ── Button bar ───────────────────────────────────────────────────────────
    private JPanel buildButtonBar() {
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(C_RIGHT); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(188,152,90,50)); g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(28,0,getWidth()-28,0); g2.dispose();
            }
        };
        bar.setLayout(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 28, 14, 28));

        // Left group: action buttons on selected appointment
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        btnDetails    = makeBtn("View Details",    false, null);
        btnCancel     = makeBtn("Cancel",          false, C_CANCELLED);
        btnReschedule = makeBtn("Reschedule",      false, null);
        btnSuspend    = makeBtn("Suspend",         false, C_SUSPENDED);
        btnApprove    = makeBtn("Approve",         false, C_SCHEDULED);

        btnDetails.addActionListener(this::viewDetails);
        btnCancel.addActionListener(this::cancelAppointment);
        btnReschedule.addActionListener(this::rescheduleAppointment);
        btnSuspend.addActionListener(this::suspendAppointment);
        btnApprove.addActionListener(this::approveAppointment);

        leftBtns.add(btnDetails);
        leftBtns.add(btnCancel);
        leftBtns.add(btnReschedule);
        leftBtns.add(btnSuspend);
        leftBtns.add(btnApprove);

        // Right group: navigation
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        btnBookNew = makeBtn("+ Book New",   true,  null);
        btnBack    = makeBtn("← Dashboard", false, null);

        btnBookNew.addActionListener(e -> dispose());
        btnBack.addActionListener(e -> goBack());

        rightBtns.add(btnBookNew);
        rightBtns.add(btnBack);

        bar.add(leftBtns,  BorderLayout.WEST);
        bar.add(rightBtns, BorderLayout.EAST);
        return bar;
    }

    // ── Button factory ───────────────────────────────────────────────────────
    private JButton makeBtn(String text, boolean primary, Color accentOverride) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", primary ? Font.BOLD : Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        Color border = accentOverride != null ? accentOverride : (primary ? C_GOLD : new Color(188,152,90,100));

        if (primary) {
            btn.setBackground(new Color(35,27,12)); btn.setForeground(new Color(230,200,145));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_GOLD,1),new EmptyBorder(6,16,6,16)));
            btn.addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){btn.setBackground(new Color(50,38,14));btn.setForeground(C_IVORY);}
                @Override public void mouseExited(MouseEvent e) {btn.setBackground(new Color(35,27,12));btn.setForeground(new Color(230,200,145));}
            });
        } else {
            btn.setBackground(C_CARD_BG); btn.setForeground(C_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));
            btn.addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){btn.setForeground(C_CHARCOAL);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border.equals(new Color(188,152,90,100))?C_GOLD:border,1),new EmptyBorder(6,14,6,14)));}
                @Override public void mouseExited(MouseEvent e) {btn.setForeground(C_MUTED);btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(border,1),new EmptyBorder(6,14,6,14)));}
            });
        }
        return btn;
    }

    // ── Data ─────────────────────────────────────────────────────────────────
    private void loadAppointments() {
        tableModel.setRowCount(0);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
        try {
            appointments = AppointmentController.getPatientAppointments(currentPatient.getPatientID());
            if (appointments.isEmpty()) {
                tableModel.addRow(new Object[]{"No appointments", "", "", "", "", ""});
            } else {
                for (Appointment a : appointments) {
                    tableModel.addRow(new Object[]{
                        df.format(a.getAppointmentDate()),
                        tf.format(a.getAppointmentTime()),
                        AppointmentController.getStaffNameById(a.getStaffID()),
                        AppointmentController.getVisitReasonById(a.getVisitReasonID()),
                        AppointmentController.getStatusNameById(a.getAppointmentStatusID()),
                        notesFor(a)
                    });
                }
            }
        } catch (Exception e) {
            tableModel.addRow(new Object[]{"Error loading", "", "", "", "", e.getMessage()});
        }
        updateButtonStates();
    }

    private String notesFor(Appointment a) {
        switch(a.getAppointmentStatusID()){
            case 1: return "Regular checkup";
            case 2: return "All good";
            case 3: return "Patient cancellation";
            case 4: return "War-time suspension";
            case 5: return "Urgent treatment";
            default: return "Standard appointment";
        }
    }

    private void updateButtonStates() {
        int row = table.getSelectedRow();
        boolean hasSel = row >= 0 && appointments != null && !appointments.isEmpty() && row < appointments.size();
        btnDetails.setEnabled(hasSel);
        if (hasSel) {
            int sid = appointments.get(row).getAppointmentStatusID();
            btnCancel.setEnabled(sid==1||sid==5);
            btnReschedule.setEnabled(sid==1||sid==4||sid==5);
            btnSuspend.setEnabled(sid==1||sid==5);
            btnApprove.setEnabled(sid==4||sid==5);
        } else {
            btnCancel.setEnabled(false); btnReschedule.setEnabled(false);
            btnSuspend.setEnabled(false); btnApprove.setEnabled(false);
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void viewDetails(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row<0||appointments==null||row>=appointments.size()) return;
        Appointment a = appointments.get(row);
        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy"),tf=new SimpleDateFormat("HH:mm");
        JOptionPane.showMessageDialog(this,
            String.format("Appointment ID: %d\nDate: %s  ·  Time: %s\nDentist: %s\nReason: %s\nStatus: %s\nNotes: %s\n\nPatient: %s  (ID: %d)",
                a.getAppointmentID(), df.format(a.getAppointmentDate()), tf.format(a.getAppointmentTime()),
                AppointmentController.getStaffNameById(a.getStaffID()),
                AppointmentController.getVisitReasonById(a.getVisitReasonID()),
                AppointmentController.getStatusNameById(a.getAppointmentStatusID()),
                notesFor(a), currentPatient.getFullName(), currentPatient.getPatientID()),
            "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelAppointment(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row<0||appointments==null||row>=appointments.size()) return;
        Appointment a = appointments.get(row);
        SimpleDateFormat df=new SimpleDateFormat("dd/MM/yyyy"),tf=new SimpleDateFormat("HH:mm");
        int ok = JOptionPane.showConfirmDialog(this,
            "Cancel appointment on "+df.format(a.getAppointmentDate())+" at "+tf.format(a.getAppointmentTime())+"?\n\nThis action cannot be undone.",
            "Cancel Appointment", JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) {
            if (AppointmentController.cancelAppointment(a.getAppointmentID()))
                { JOptionPane.showMessageDialog(this,"Appointment cancelled. Refund will be processed within 3-5 days.","Done",JOptionPane.INFORMATION_MESSAGE); loadAppointments(); }
            else JOptionPane.showMessageDialog(this,"Failed to cancel appointment.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rescheduleAppointment(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row<0||appointments==null||row>=appointments.size()) return;
        Appointment a = appointments.get(row);

        JDialog dlg = new JDialog(this, "Reschedule Appointment", true);
        dlg.setSize(380, 230);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(C_RIGHT);
        panel.setBorder(new EmptyBorder(20,24,20,24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(7,6,7,12); g.anchor=GridBagConstraints.WEST;

        g.gridx=0;g.gridy=0; JLabel l1=new JLabel("New Date"); l1.setFont(new Font("Georgia",Font.BOLD,13)); l1.setForeground(C_CHARCOAL); panel.add(l1,g);
        g.gridx=1; JSpinner ds=new JSpinner(new SpinnerDateModel()); JSpinner.DateEditor de=new JSpinner.DateEditor(ds,"dd/MM/yyyy"); ds.setEditor(de); ds.setPreferredSize(new Dimension(140,30));
        styleSpinner(ds); panel.add(ds,g);
        g.gridx=0;g.gridy=1; JLabel l2=new JLabel("New Time"); l2.setFont(new Font("Georgia",Font.BOLD,13)); l2.setForeground(C_CHARCOAL); panel.add(l2,g);
        g.gridx=1;
        String[] slots={"09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00"};
        JComboBox<String> tc=new JComboBox<>(slots); tc.setPreferredSize(new Dimension(110,30));
        tc.setBackground(new Color(228,223,213)); tc.setFont(new Font("Georgia",Font.PLAIN,12)); panel.add(tc,g);

        g.gridx=0;g.gridy=2;g.gridwidth=2;
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btnRow.setOpaque(false);
        JButton conf=makeBtn("Confirm",true,null), cancel=makeBtn("Cancel",false,null);
        conf.addActionListener(ev->{
            try{
                java.util.Date nd=(java.util.Date)ds.getValue();
                boolean ok=AppointmentController.rescheduleAppointment(a.getAppointmentID(),
                        new java.sql.Date(nd.getTime()), java.sql.Time.valueOf((String)tc.getSelectedItem()+":00"));
                if(ok){JOptionPane.showMessageDialog(dlg,"Rescheduled successfully.","Done",JOptionPane.INFORMATION_MESSAGE);dlg.dispose();loadAppointments();}
                else JOptionPane.showMessageDialog(dlg,"Failed to reschedule.","Error",JOptionPane.ERROR_MESSAGE);
            }catch(Exception ex){JOptionPane.showMessageDialog(dlg,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
        });
        cancel.addActionListener(ev->dlg.dispose());
        btnRow.add(cancel); btnRow.add(conf);
        panel.add(btnRow,g);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private void styleSpinner(JSpinner sp) {
        JComponent ed=sp.getEditor();
        if(ed instanceof JSpinner.DefaultEditor){
            JTextField tf=((JSpinner.DefaultEditor)ed).getTextField();
            tf.setBackground(new Color(228,223,213)); tf.setForeground(C_CHARCOAL); tf.setBorder(new EmptyBorder(4,8,4,8));
        }
        sp.setBorder(BorderFactory.createLineBorder(new Color(188,152,90,90),1));
    }

    private void suspendAppointment(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row<0||appointments==null||row>=appointments.size()) return;
        Appointment a = appointments.get(row);
        int ok = JOptionPane.showConfirmDialog(this,
            "Suspend this appointment (war-time policy)?\n\n• Resume within 24 hours: appointment restored\n• After 24 hours: auto-cancelled with full refund",
            "War-time Suspension", JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) {
            if (AppointmentController.suspendAppointment(a.getAppointmentID()))
                { JOptionPane.showMessageDialog(this,"Appointment suspended. You have 24 hours to resume.","Suspended",JOptionPane.INFORMATION_MESSAGE); loadAppointments(); }
            else JOptionPane.showMessageDialog(this,"Failed to suspend.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveAppointment(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row<0||appointments==null||row>=appointments.size()) return;
        Appointment a = appointments.get(row);
        int ok = JOptionPane.showConfirmDialog(this,
            "Approve and reactivate this appointment?","Approve",JOptionPane.YES_NO_OPTION);
        if (ok==JOptionPane.YES_OPTION) {
            if (AppointmentController.approveAppointment(a.getAppointmentID()))
                { JOptionPane.showMessageDialog(this,"Appointment approved and scheduled.","Approved",JOptionPane.INFORMATION_MESSAGE); loadAppointments(); }
            else JOptionPane.showMessageDialog(this,"Failed to approve.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goBack() { dispose(); }
}