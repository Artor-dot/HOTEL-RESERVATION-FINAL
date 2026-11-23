import javax.swing.*;
import java.util.HashMap;

public class login {
    private JPanel mainPanel;
    private JCheckBox showPasswordCheckBox;
    private JTextField textField1;
    private JButton LOGINButton;
    private JButton forgotPasswordButton;
    private JButton createAccountButton;
    private JPasswordField passwordField1;

    public static JFrame frame;
    public static HashMap<String, String> users = new HashMap<>();
    public static HashMap<String, String> emails = new HashMap<>();

    public login() {
        showPasswordCheckBox.addActionListener(e -> {
            passwordField1.setEchoChar(showPasswordCheckBox.isSelected() ? (char) 0 : '•');
        });

        LOGINButton.addActionListener(e -> {
            String user = textField1.getText().trim();
            String pass = new String(passwordField1.getPassword());

            if (users.containsKey(user) && users.get(user).equals(pass)) {
                JOptionPane.showMessageDialog(frame, "Login successful! Welcome, " + user + ".");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        createAccountButton.addActionListener(e -> {
            frame.setTitle("Create Account");
            frame.setContentPane(new CreateAccount().getMainPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
        });

        forgotPasswordButton.addActionListener(e -> {
            frame.setTitle("Forgot Password");
            frame.setContentPane(new ForgotPassword().getMainPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Login");
            frame.setContentPane(new login().getMainPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}