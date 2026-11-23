package ui;

import model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Main screen after login: sidebar on the left, pages on the right.
 * Add your real panels in buildPages() (BrowsePanel, DetailsPanel, CheckoutPanel, ReservationsPanel).
 */
public class AppPanel extends JPanel {

    private MainFrame frame;
    private User user;

    private CardLayout pageLayout = new CardLayout();
    private JPanel pages = new JPanel(pageLayout);

    private String[] navLabels = {"Browse Rooms", "My Reservations", "Profile"};
    private String[] pageNames = {"browse", "reservations", "profile"};
    private JButton[] navButtons = new JButton[3];

    public AppPanel(MainFrame frame, User user) {
        this.frame = frame;
        this.user = user;

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        pages.setBackground(Theme.CREAM);
        buildPages();
        add(pages, BorderLayout.CENTER);

        showPage("browse");
    }

    // Other panels can call this to navigate, e.g. appPanel.showPage("details")
    public void showPage(String name) {
        pageLayout.show(pages, name);

        // highlight the active sidebar button
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = pageNames[i].equals(name);
            navButtons[i].setBackground(active ? Theme.GREEN : Theme.SIDEBAR);
            navButtons[i].setForeground(active ? Color.WHITE : Color.BLACK);
        }
    }

    public User getUser() {
        return user;
    }

    // --------------------------------------------------------------- pages

    private void buildPages() {
        // TODO: replace these placeholders with your real panels
        pages.add(placeholder("ROOM CATALOG", "Next: load rooms from data/rooms.txt"), "browse");
        pages.add(placeholder("MY RESERVATIONS", "Next: load data/reservations.txt in a JTable"), "reservations");
        pages.add(buildProfilePage(), "profile");
    }

    private JPanel buildProfilePage() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.CREAM);
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("PROFILE");
        title.setFont(Theme.HEADING);
        p.add(title);
        p.add(Box.createVerticalStrut(20));
        p.add(new JLabel("Full name: " + user.getFullName()));
        p.add(Box.createVerticalStrut(8));
        p.add(new JLabel("Email: " + user.getEmail()));
        p.add(Box.createVerticalStrut(8));
        p.add(new JLabel("Username: " + user.getUsername()));
        return p;
    }

    private JPanel placeholder(String title, String note) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.CREAM);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(Theme.HEADING);
        t.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        JLabel n = new JLabel(note, SwingConstants.CENTER);
        p.add(t, BorderLayout.NORTH);
        p.add(n, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------- sidebar

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(BorderFactory.createEmptyBorder(30, 25, 20, 25));

        JLabel logo = new JLabel("<html><div style='text-align:center'>TRIPLE<br>THREAT</div></html>",
                SwingConstants.CENTER);
        logo.setFont(Theme.LOGO.deriveFont(34f));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(logo);
        side.add(Box.createVerticalStrut(30));

        for (int i = 0; i < navLabels.length; i++) {
            final String page = pageNames[i];
            navButtons[i] = makeNavButton(navLabels[i]);
            navButtons[i].addActionListener(e -> showPage(page));
            side.add(navButtons[i]);
            side.add(Box.createVerticalStrut(8));
        }

        JButton logout = makeNavButton("Logout");
        logout.setBackground(Theme.SIDEBAR);
        logout.addActionListener(e -> doLogout());
        side.add(logout);

        return side;
    }

    private JButton makeNavButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.BUTTON.deriveFont(16f));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBackground(Theme.SIDEBAR);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return b;
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            frame.showLogin();
        }
    }
}
