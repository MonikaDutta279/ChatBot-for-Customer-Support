import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        emailField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        loginButton.addActionListener(e -> handleLogin());

        setLayout(new GridLayout(3, 2));
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);

        setVisible(true);
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        User user = ChatBotService.authenticateUser(email, password);
        if (user != null) {
            ChatBotUI chatBotUI = new ChatBotUI(user);
            chatBotUI.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginUI::new);
    }
}
