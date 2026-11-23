import javax.swing.*;

public class ForgotPassword {
    private JPanel mainPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton RESETPASSWORDButton;
    private JButton backToLoginButton;

    public ForgotPassword() {
        RESETPASSWORDButton.addActionListener(e -> {
            String username = textField1.getText().trim();
            String email = textField2.getText().trim();
            String newPass1 = new String(passwordField1.getPassword());
            String newPass2 = new String(passwordField2.getPassword());

            if (username.isEmpty() || email.isEmpty() || newPass1.isEmpty() || newPass2.isEmpty()) {
                JOptionPane.showMessageDialog(login.frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(login.frame, "Please enter a valid email address containing '@'.", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!login.users.containsKey(username)) {
                JOptionPane.showMessageDialog(login.frame, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!login.emails.get(username).equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(login.frame, "Registered email does not match this account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass1.equals(newPass2)) {
                JOptionPane.showMessageDialog(login.frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            login.users.put(username, newPass1);
            JOptionPane.showMessageDialog(login.frame, "Password updated! Please log in.");
            goToLogin();
        });

        backToLoginButton.addActionListener(e -> goToLogin());
    }

    private void goToLogin() {
        login.frame.setTitle("Login");
        login.frame.setContentPane(new login().getMainPanel());
        login.frame.pack();
        login.frame.setLocationRelativeTo(null);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}