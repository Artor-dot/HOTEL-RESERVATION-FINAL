package ui;

import model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * The one and only window. It uses a CardLayout to switch between:
 *   "login" -> LoginPanel (login / create account / forgot password)
 *   "app"   -> AppPanel   (guests: sidebar + Browse / Reservations / Payments / Profile)
 *             AdminPanel (admins: Guests / Reservations / Payments)
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel root = new JPanel(cardLayout);
    private JPanel appPanel; // AppPanel for guests, AdminPanel for admins

    public MainFrame() {
        super("Triple Threat Client Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 640);
        setMinimumSize(new java.awt.Dimension(960, 600));
        setLocationRelativeTo(null); // center on screen

        root.add(new LoginPanel(this), "login");
        setContentPane(root);
        showLogin();
    }

    public void showLogin() {
        setTitle("Triple Threat Client Login");
        cardLayout.show(root, "login");
    }

    // Called by LoginPanel after a successful login
    public void showApp(User user) {
        if (appPanel != null) {
            root.remove(appPanel); // remove the previous user's session
        }
        if (user.isAdmin()) {
            appPanel = new AdminPanel(this, user);
            setTitle("Triple Threat — Admin");
        } else {
            appPanel = new AppPanel(this, user);
            setTitle("Triple Threat — Hotel Reservation System");
        }
        root.add(appPanel, "app");
        cardLayout.show(root, "app");
    }
}
