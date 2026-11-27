package ui;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Colors, fonts and button styles shared by every screen. */
public class Theme {
    public static final Color CREAM = new Color(0xF7F3EA);
    public static final Color SIDEBAR = new Color(0xF2ECDD);
    public static final Color CARD = new Color(0xF5EFE3);
    public static final Color GREEN = new Color(0x1F4D3A);
    public static final Color GOLD = new Color(0xA68B4B);
    public static final Color BORDER = new Color(0xC9BFA6);
    public static final Color RED = new Color(0x8B2E2E);

    public static final Font LOGO = new Font("Serif", Font.PLAIN, 28);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 20);
    public static final Font LABEL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BUTTON = new Font("SansSerif", Font.BOLD, 14);

    // Gold button used for main actions (LOGIN, CREATE ACCOUNT, BOOK NOW...)
    public static JButton goldButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(GOLD);
        b.setForeground(Color.WHITE);
        b.setFont(BUTTON);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setPreferredSize(new Dimension(100, 38));
        return b;
    }

    // White button with a thin border (BACK TO ROOMS, CANCEL...)
    public static JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(Color.BLACK);
        b.setFont(BUTTON);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(BORDER));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setPreferredSize(new Dimension(100, 38));
        return b;
    }

    // Looks like a blue underlined link
    public static JButton linkButton(String text) {
        JButton b = new JButton("<html><u>" + text + "</u></html>");
        b.setForeground(new Color(0x1A3E7A));
        b.setFont(LABEL);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ------------------------------------------------------------ new helpers

    /** Centered page heading, e.g. "ROOM CATALOG" */
    public static JLabel title(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(HEADING);
        return l;
    }

    /** Slightly smaller heading for the right-hand panels */
    public static JLabel sideTitle(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(HEADING.deriveFont(18f));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return l;
    }

    /** "₱1,234.50" (Philippine peso) */
    public static String money(double v) {
        return String.format(Locale.US, "\u20B1%,.2f", v);
    }

    /** "₱150" when whole, otherwise "₱150.50" */
    public static String price(double v) {
        return v == Math.rint(v) ? String.format(Locale.US, "\u20B1%,d", (long) v) : money(v);
    }

    /** Escapes text so it can be placed inside an HTML label */
    public static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Rounded "Available" style pill. Change the color later with setBackground(...). */
    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setBackground(bg);
        l.setForeground(Color.WHITE);
        l.setFont(LABEL.deriveFont(Font.BOLD, 12f));
        l.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        return l;
    }

    /** One "left text ........ right text" line */
    public static JPanel row(JLabel left, JLabel right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /** Dropdown styled like the mockups */
    public static void styleCombo(JComboBox<String> box) {
        box.setFont(LABEL.deriveFont(14f));
        box.setBackground(Color.WHITE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        box.setPreferredSize(new Dimension(100, 32));
    }

    /**
     * Loads a photo and crops/scales it to w x h. If the file is missing, a plain
     * placeholder is drawn instead, so the app still works without any images.
     */
    public static ImageIcon loadImage(String path, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean drawn = false;
        if (path != null && !path.isEmpty()) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    BufferedImage src = ImageIO.read(f);
                    if (src != null) {
                        double scale = Math.max((double) w / src.getWidth(), (double) h / src.getHeight());
                        int sw = (int) Math.round(src.getWidth() * scale);
                        int sh = (int) Math.round(src.getHeight() * scale);
                        g.drawImage(src, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
                        drawn = true;
                    }
                } catch (IOException e) {
                    System.out.println("Could not read image " + path + ": " + e.getMessage());
                }
            }
        }

        if (!drawn) {
            // no photo file yet: draw a simple room illustration so the card still looks good
            g.setPaint(new GradientPaint(0, 0, new Color(0xEFE8D6), 0, h, new Color(0xD9CDB0)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(0xC9D9DD));                       // window
            g.fillRect((int) (w * 0.62), (int) (h * 0.10), (int) (w * 0.26), (int) (h * 0.30));
            g.setColor(new Color(0xB9A97E));                       // headboard
            g.fillRoundRect((int) (w * 0.14), (int) (h * 0.42), (int) (w * 0.72), (int) (h * 0.22), 10, 10);
            g.setColor(new Color(0xFBF8F0));                       // mattress
            g.fillRoundRect((int) (w * 0.10), (int) (h * 0.56), (int) (w * 0.80), (int) (h * 0.22), 12, 12);
            g.setColor(Color.WHITE);                               // pillows
            g.fillRoundRect((int) (w * 0.18), (int) (h * 0.50), (int) (w * 0.26), (int) (h * 0.12), 10, 10);
            g.fillRoundRect((int) (w * 0.54), (int) (h * 0.50), (int) (w * 0.26), (int) (h * 0.12), 10, 10);
            g.setColor(GOLD);                                      // blanket
            g.fillRoundRect((int) (w * 0.10), (int) (h * 0.66), (int) (w * 0.80), (int) (h * 0.12), 12, 12);
            g.setColor(new Color(0x8A7B55));                       // bed legs
            g.fillRect((int) (w * 0.14), (int) (h * 0.78), (int) (w * 0.05), (int) (h * 0.08));
            g.fillRect((int) (w * 0.81), (int) (h * 0.78), (int) (w * 0.05), (int) (h * 0.08));
        }
        g.dispose();
        return new ImageIcon(out);
    }

    // ------------------------------------------------------------ more helpers

    /** Sidebar navigation button (flat, highlighted green when active) */
    public static JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BUTTON.deriveFont(16f));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBackground(SIDEBAR);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return b;
    }

    /** Red button for risky actions (BLOCK GUEST...) */
    public static JButton dangerButton(String text) {
        JButton b = goldButton(text);
        b.setBackground(RED);
        return b;
    }

    /** "Available", "Only 2 left" or "Sold Out" */
    public static String availabilityText(int roomsLeft) {
        if (roomsLeft <= 0) {
            return "Sold Out";
        }
        return roomsLeft <= 3 ? "Only " + roomsLeft + " left" : "Available";
    }

    public static Color availabilityColor(int roomsLeft) {
        if (roomsLeft <= 0) {
            return RED;
        }
        return roomsLeft <= 3 ? GOLD : GREEN;
    }

    /** Runs a task every time the text in the field changes */
    public static void onTextChange(javax.swing.JTextField field, Runnable task) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { task.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { task.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { task.run(); }
        });
    }

    /** HTML text with a fixed width so long values wrap inside detail panels */
    public static String wrapHtml(int widthPx, String... lines) {
        StringBuilder sb = new StringBuilder("<html><body style='width:" + widthPx + "px'>");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("<br>");
            }
            sb.append(html(lines[i]));
        }
        return sb.append("</body></html>").toString();
    }

    /** One "CAPTION:  value" line inside a GridBagLayout detail box */
    public static JLabel addDetailRow(JPanel box, int y, String caption, java.awt.Component value) {
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridy = y;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(4, 0, 4, 8);
        JLabel cap = new JLabel(caption);
        cap.setFont(LABEL.deriveFont(14f));
        cap.setPreferredSize(new Dimension(78, 18));
        box.add(cap, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(4, 0, 4, 0);
        value.setFont(LABEL.deriveFont(14f));
        box.add(value, c);
        return cap; // returned so a caller can hide the caption together with its value
    }

    /** "SEARCH: [ field ]" row used on the admin pages */
    public static JPanel searchRow(javax.swing.JTextField field) {
        JPanel row = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        JLabel l = new JLabel("SEARCH:");
        l.setFont(LABEL.deriveFont(Font.BOLD, 13f));
        field.setFont(LABEL.deriveFont(14f));
        field.setPreferredSize(new Dimension(260, 30));
        row.add(l);
        row.add(field);
        return row;
    }
}
