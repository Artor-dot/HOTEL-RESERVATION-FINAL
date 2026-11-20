package ui;

import model.User;
import service.FileHandler;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

/** Three cards side by side: User Login | Create Account | Forget Password */
public class LoginPanel extends JPanel {

    private MainFrame frame;

    // Login card
    private JTextField loginUser = new JTextField();
    private JPasswordField loginPass = new JPasswordField();
    private JCheckBox showPass = new JCheckBox("Show Password");

    // Create account card
    private JTextField regName = new JTextField();
    private JTextField regEmail = new JTextField();
    private JTextField regUser = new JTextField();
    private JPasswordField regPass = new JPasswordField();
    private JPasswordField regConfirm = new JPasswordField();

    // Forget password card
    private JTextField resetUser = new JTextField();
    private JTextField resetEmail = new JTextField();
    private JPasswordField resetNew = new JPasswordField();
    private JPasswordField resetConfirm = new JPasswordField();

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(1, 3, 20, 0));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(buildLoginCard());
        add(buildCreateCard());
        add(buildResetCard());
    }

    // ---------------------------------------------------------------- cards

    private JPanel buildLoginCard() {
        JPanel card = newCard("USER LOGIN");
        addField(card, "USERNAME:", loginUser);
        addField(card, "PASSWORD:", loginPass);

        showPass.setOpaque(false);
        showPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPass.addActionListener(e -> {
            // (char) 0 shows the text, '•' hides it
            loginPass.setEchoChar(showPass.isSelected() ? (char) 0 : '\u2022');
        });
        card.add(showPass);
        card.add(Box.createVerticalStrut(15));

        JButton loginBtn = Theme.goldButton("LOGIN");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));

        JPanel links = new JPanel(new GridLayout(1, 2));
        links.setOpaque(false);
        links.setAlignmentX(Component.LEFT_ALIGNMENT);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton forgot = Theme.linkButton("Forgot Password");
        JButton create = Theme.linkButton("Create Account");
        forgot.addActionListener(e -> resetUser.requestFocusInWindow());
        create.addActionListener(e -> regName.requestFocusInWindow());
        links.add(forgot);
        links.add(create);
        card.add(links);
        return card;
    }

    private JPanel buildCreateCard() {
        JPanel card = newCard("CREATE ACCOUNT");
        addField(card, "FULL NAME:", regName);
        addField(card, "EMAIL:", regEmail);
        addField(card, "USERNAME:", regUser);
        addField(card, "PASSWORD:", regPass);
        addField(card, "CONFIRM PASSWORD:", regConfirm);

        JButton btn = Theme.goldButton("CREATE ACCOUNT");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> doCreateAccount());
        card.add(btn);
        card.add(Box.createVerticalStrut(8));

        JButton back = Theme.linkButton("Back to Login");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> loginUser.requestFocusInWindow());
        card.add(back);
        return card;
    }

    private JPanel buildResetCard() {
        JPanel card = newCard("FORGET PASSWORD");
        addField(card, "USERNAME:", resetUser);
        addField(card, "REGISTERED EMAIL:", resetEmail);
        addField(card, "NEW PASSWORD:", resetNew);
        addField(card, "CONFIRM PASSWORD:", resetConfirm);

        JButton btn = Theme.goldButton("RESET PASSWORD");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> doResetPassword());
        card.add(btn);
        card.add(Box.createVerticalStrut(8));

        JButton back = Theme.linkButton("Back to Login");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> loginUser.requestFocusInWindow());
        card.add(back);
        return card;
    }

    // -------------------------------------------------------------- actions

    private void doLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = FileHandler.login(username, password); // reads data/users.txt
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Incorrect username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            loginPass.setText("");
            return;
        }

        JOptionPane.showMessageDialog(this, "Welcome, " + user.getFullName() + "!",
                "Login Successful", JOptionPane.INFORMATION_MESSAGE);
        clearAllFields();
        frame.showApp(user);
    }

    private void doCreateAccount() {
        String name = regName.getText().trim();
        String email = regEmail.getText().trim();
        String username = regUser.getText().trim();
        String pass = new String(regPass.getPassword());
        String confirm = new String(regConfirm.getPassword());

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showWarning("Please fill in all fields.");
            return;
        }
        if (hasPipe(name) || hasPipe(email) || hasPipe(username) || hasPipe(pass)) {
            showWarning("The \"|\" character is not allowed.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showWarning("Please enter a valid email address.");
            return;
        }
        if (pass.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            showWarning("Passwords do not match.");
            return;
        }
        if (FileHandler.usernameExists(username)) {
            JOptionPane.showMessageDialog(this, "That username is already taken.",
                    "Username Taken", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean saved = FileHandler.addUser(new User(name, email, username, pass));
        if (saved) {
            JOptionPane.showMessageDialog(this, "Account created! You can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            regName.setText("");
            regEmail.setText("");
            regUser.setText("");
            regPass.setText("");
            regConfirm.setText("");
            loginUser.setText(username);
            loginPass.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Could not save your account.",
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doResetPassword() {
        String username = resetUser.getText().trim();
        String email = resetEmail.getText().trim();
        String newPass = new String(resetNew.getPassword());
        String confirm = new String(resetConfirm.getPassword());

        if (username.isEmpty() || email.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showWarning("Please fill in all fields.");
            return;
        }
        if (hasPipe(newPass)) {
            showWarning("The \"|\" character is not allowed.");
            return;
        }
        if (newPass.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return;
        }
        if (!newPass.equals(confirm)) {
            showWarning("Passwords do not match.");
            return;
        }

        boolean ok = FileHandler.resetPassword(username, email, newPass);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Password reset successful. Please log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            resetUser.setText("");
            resetEmail.setText("");
            resetNew.setText("");
            resetConfirm.setText("");
            loginUser.setText(username);
            loginPass.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Username and email do not match any account.",
                    "Reset Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------- helpers

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    private boolean hasPipe(String text) {
        return text.contains("|");
    }

    private void clearAllFields() {
        loginUser.setText("");
        loginPass.setText("");
        showPass.setSelected(false);
        loginPass.setEchoChar('\u2022');
    }

    // A card with a logo + heading at the top
    private JPanel newCard(String heading) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)));

        JLabel logo = new JLabel("TRIPLE THREAT", SwingConstants.CENTER);
        logo.setFont(Theme.LOGO.deriveFont(24f));
        stretch(logo);
        card.add(logo);
        card.add(Box.createVerticalStrut(8));

        JLabel title = new JLabel(heading, SwingConstants.CENTER);
        title.setFont(Theme.HEADING);
        stretch(title);
        card.add(title);
        card.add(Box.createVerticalStrut(15));
        return card;
    }

    // Label on top, field below it
    private void addField(JPanel card, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.LABEL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setPreferredSize(new Dimension(100, 30));

        card.add(label);
        card.add(Box.createVerticalStrut(3));
        card.add(field);
        card.add(Box.createVerticalStrut(10));
    }

    private void stretch(JLabel label) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
    }
}
