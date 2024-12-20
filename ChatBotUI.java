import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class ChatBotUI extends JFrame {
    private JTextPane chatArea;
    private JTextField userInputField;
    private JButton sendButton, settingsButton;
    private JPanel settingsPanel;

    private Color backgroundColor = Color.DARK_GRAY;
    private Color userMessageColor = Color.GREEN;  // User message color
    private Color botMessageColor = Color.CYAN;    // Bot message color
    private int fontSize = 14;

    private ChatBotService chatBotService;
    private User currentUser;

    public ChatBotUI(User user) {
        this.currentUser = user;

        setTitle("Customer Support Chatbot");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Using JTextPane for rich text
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(backgroundColor);
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        chatBotService = new ChatBotService(chatArea, currentUser);

        userInputField = new JTextField();
        sendButton = new JButton("Send");
        settingsButton = new JButton("Settings");

        sendButton.addActionListener(e -> handleSendButtonClick());
        settingsButton.addActionListener(e -> toggleSettingsPanel());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        setupSettingsPanel();

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(settingsButton, BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.EAST);

        settingsPanel.setVisible(false);
    }

    private void setupSettingsPanel() {
        settingsPanel = new JPanel(new GridLayout(3, 2));

        settingsPanel.add(new JLabel("Background Color:"));
        JComboBox<String> bgColorComboBox = new JComboBox<>(new String[]{"Dark Mode", "Light Gray", "White"});
        bgColorComboBox.addActionListener(e -> changeBackgroundColor((String) bgColorComboBox.getSelectedItem()));
        settingsPanel.add(bgColorComboBox);

        settingsPanel.add(new JLabel("User Message Color:"));
        JComboBox<String> userColorComboBox = new JComboBox<>(new String[]{"Green", "Blue", "Yellow"});
        userColorComboBox.addActionListener(e -> changeUserMessageColor((String) userColorComboBox.getSelectedItem()));
        settingsPanel.add(userColorComboBox);

        settingsPanel.add(new JLabel("Bot Message Color:"));
        JComboBox<String> botColorComboBox = new JComboBox<>(new String[]{"Cyan", "White", "Orange"});
        botColorComboBox.addActionListener(e -> changeBotMessageColor((String) botColorComboBox.getSelectedItem()));
        settingsPanel.add(botColorComboBox);

        settingsPanel.add(new JLabel("Font Size:"));
        JComboBox<String> fontSizeComboBox = new JComboBox<>(new String[]{"12", "14", "16", "18"});
        fontSizeComboBox.addActionListener(e -> changeFontSize((String) fontSizeComboBox.getSelectedItem()));
        settingsPanel.add(fontSizeComboBox);
    }

    private void toggleSettingsPanel() {
        settingsPanel.setVisible(!settingsPanel.isVisible());
    }

    private void changeBackgroundColor(String color) {
        switch (color) {
            case "White" -> backgroundColor = Color.WHITE;
            case "Light Gray" -> backgroundColor = Color.LIGHT_GRAY;
            case "Dark Mode" -> backgroundColor = Color.DARK_GRAY;
        }
        chatArea.setBackground(backgroundColor);
    }

    private void changeUserMessageColor(String color) {
        switch (color) {
            case "Green" -> userMessageColor = Color.GREEN;
            case "Blue" -> userMessageColor = Color.BLUE;
            case "Yellow" -> userMessageColor = Color.YELLOW;
        }
    }

    private void changeBotMessageColor(String color) {
        switch (color) {
            case "Cyan" -> botMessageColor = Color.CYAN;
            case "White" -> botMessageColor = Color.WHITE;
            case "Orange" -> botMessageColor = Color.ORANGE;
        }
    }

    private void changeFontSize(String size) {
        fontSize = Integer.parseInt(size);
        chatArea.setFont(new Font("Arial", Font.PLAIN, fontSize));
    }

    private void handleSendButtonClick() {
        String userMessage = userInputField.getText().trim();
        if (!userMessage.isEmpty()) {
            appendMessage("You : "+userMessage, userMessageColor);  // Display user message in user color
            userInputField.setText("");
            
            processUserMessage(userMessage);
            SoundNotifier.playSound("message_received.wav");
        }
    }

    private void processUserMessage(String message) {
        try {
            String response = chatBotService.processQuery(message);
            appendMessage("Bot : "+response, botMessageColor);  // Display bot response in bot color
            chatBotService.logChatHistory(message, response);
            SoundNotifier.playSound("message_received.wav");
        } catch (Exception e) {
            appendMessage("Oops! Something went wrong. Please try again.", botMessageColor);
            e.printStackTrace();
        }
    }

    // Helper method to append message to chat area with a specific color
    private void appendMessage(String message, Color color) {
        StyledDocument doc = chatArea.getStyledDocument();
        Style style = chatArea.addStyle("style", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontSize(style, fontSize);

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
