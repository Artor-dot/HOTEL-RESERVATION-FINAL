package ui;

import model.BookingDraft;
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
 * Pages: browse -> details -> checkout, plus reservations and profile.
 */
public class AppPanel extends JPanel {

    private MainFrame frame;
    private User user;

    // the booking being built; shared by Browse / Details / Checkout
    private BookingDraft draft = new BookingDraft();
    private BrowsePanel browsePanel;
    private DetailsPanel detailsPanel;
    private CheckoutPanel checkoutPanel;
    private ReservationsPanel reservationsPanel;
    private PaymentsPanel paymentsPanel;

    private CardLayout pageLayout = new CardLayout();
    private JPanel pages = new JPanel(pageLayout);

    private String[] navLabels = {"Browse Rooms", "My Reservations", "Payments", "Profile"};
    private String[] pageNames = {"browse", "reservations", "payments", "profile"};
    private JButton[] navButtons = new JButton[4];

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

    // Other panels can call this to navigate, e.g. app.showPage("details")
    public void showPage(String name) {
        // refresh the page's data before it appears
        switch (name) {
            case "browse":       browsePanel.refresh(); break;
            case "details":      detailsPanel.refresh(); break;
            case "checkout":     checkoutPanel.refresh(); break;
            case "reservations": reservationsPanel.refresh(); break;
            case "payments":     paymentsPanel.refresh(); break;
            default: break;
        }
        pageLayout.show(pages, name);

        // details + checkout belong to the "Browse Rooms" flow, so keep that button lit
        String highlight = (name.equals("details") || name.equals("checkout")) ? "browse" : name;
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = pageNames[i].equals(highlight);
            navButtons[i].setBackground(active ? Theme.GREEN : Theme.SIDEBAR);
            navButtons[i].setForeground(active ? Color.WHITE : Color.BLACK);
        }
    }

    public User getUser() {
        return user;
    }

    // --------------------------------------------------------------- pages

    private void buildPages() {
        browsePanel = new BrowsePanel(this, draft);
        detailsPanel = new DetailsPanel(this, draft);
        checkoutPanel = new CheckoutPanel(this, draft, user);
        reservationsPanel = new ReservationsPanel(this, user);
        paymentsPanel = new PaymentsPanel(user);

        pages.add(browsePanel, "browse");
        pages.add(detailsPanel, "details");
        pages.add(checkoutPanel, "checkout");
        pages.add(reservationsPanel, "reservations");
        pages.add(paymentsPanel, "payments");
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
        return Theme.navButton(text);
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            frame.showLogin();
        }
    }
}
