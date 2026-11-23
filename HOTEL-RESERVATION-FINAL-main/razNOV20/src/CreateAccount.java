import javax.swing.*;

public class CreateAccount {
    private JPanel mainPanel;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton CREATEACCOUNTButton;
    private JButton backToLoginButton;

    public CreateAccount() {
        CREATEACCOUNTButton.addActionListener(e -> {
            String fullName = textField1.getText().trim();
            String email = textField2.getText().trim();
            String username = textField3.getText().trim();
            String pass1 = new String(passwordField1.getPassword());
            String pass2 = new String(passwordField2.getPassword());

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                JOptionPane.showMessageDialog(login.frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(login.frame, "Please enter a valid email address containing '@'.", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pass1.equals(pass2)) {
                JOptionPane.showMessageDialog(login.frame, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (login.users.containsKey(username)) {
                JOptionPane.showMessageDialog(login.frame, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            login.users.put(username, pass1);
            login.emails.put(username, email);
            JOptionPane.showMessageDialog(login.frame, "Account created! You can now log in.");
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