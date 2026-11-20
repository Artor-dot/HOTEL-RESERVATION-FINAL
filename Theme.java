package ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/** Colors, fonts and button styles shared by every screen. */
public class Theme {
    public static final Color CREAM = new Color(0xF7F3EA);
    public static final Color SIDEBAR = new Color(0xF2ECDD);
    public static final Color CARD = new Color(0xF5EFE3);
    public static final Color GREEN = new Color(0x1F4D3A);
    public static final Color GOLD = new Color(0xA68B4B);
    public static final Color BORDER = new Color(0xC9BFA6);

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
}
