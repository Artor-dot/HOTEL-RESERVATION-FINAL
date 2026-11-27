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
 * Admin screen after logging in with an ADMIN account: same sidebar style as the guest app,
 * with Guests / Reservations / Payments pages.
 */
public class AdminPanel extends JPanel {

    private final MainFrame frame;
    private final User admin;

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pages = new JPanel(pageLayout);

    private final String[] navLabels = {"Guests", "Reservations", "Payments"};
    private final String[] pageNames = {"guests", "reservations", "payments"};
    private final JButton[] navButtons = new JButton[3];

    private final AdminGuestsPanel guestsPanel = new AdminGuestsPanel();
    private final AdminReservationsPanel reservationsPanel = new AdminReservationsPanel();
    private final AdminPaymentsPanel paymentsPanel = new AdminPaymentsPanel();

    public AdminPanel(MainFrame frame, User admin) {
        this.frame = frame;
        this.admin = admin;

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        pages.setBackground(Theme.CREAM);
        pages.add(guestsPanel, "guests");
        pages.add(reservationsPanel, "reservations");
        pages.add(paymentsPanel, "payments");
        add(pages, BorderLayout.CENTER);

        showPage("guests");
    }

    public void showPage(String name) {
        switch (name) {
            case "guests":       guestsPanel.refresh(); break;
            case "reservations": reservationsPanel.refresh(); break;
            case "payments":     paymentsPanel.refresh(); break;
            default: break;
        }
        pageLayout.show(pages, name);

        for (int i = 0; i < navButtons.length; i++) {
            boolean active = pageNames[i].equals(name);
            navButtons[i].setBackground(active ? Theme.GREEN : Theme.SIDEBAR);
            navButtons[i].setForeground(active ? Color.WHITE : Color.BLACK);
        }
    }

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
        side.add(Box.createVerticalStrut(8));

        JLabel role = new JLabel("ADMIN PANEL", SwingConstants.CENTER);
        role.setFont(Theme.BUTTON.deriveFont(13f));
        role.setForeground(Theme.GOLD);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(role);
        side.add(Box.createVerticalStrut(30));

        for (int i = 0; i < navLabels.length; i++) {
            final String page = pageNames[i];
            navButtons[i] = Theme.navButton(navLabels[i]);
            navButtons[i].addActionListener(e -> showPage(page));
            side.add(navButtons[i]);
            side.add(Box.createVerticalStrut(8));
        }

        JButton logout = Theme.navButton("Logout");
        logout.addActionListener(e -> doLogout());
        side.add(logout);

        side.add(Box.createVerticalGlue());
        JLabel who = new JLabel("Signed in as " + admin.getUsername(), SwingConstants.CENTER);
        who.setFont(Theme.LABEL);
        who.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(who);
        return side;
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            frame.showLogin();
        }
    }
}
