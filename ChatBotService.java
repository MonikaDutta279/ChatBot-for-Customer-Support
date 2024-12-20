import javax.swing.*;
import javax.swing.text.*;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatBotService {
    private ExecutorService executor;
    private JTextPane chatArea;  // JTextPane to display chat messages
    private static Map<String, String> keywordResponses;

    // Stores previous interactions for context awareness
    private String lastUserQuery = "";
    private String lastResponse = "";
    private User currentUser;  // The currently authenticated user

    private Color userMessageColor = Color.GREEN;  // Default color for user messages
    private Color botMessageColor = Color.CYAN;    // Default color for bot responses

    public ChatBotService(JTextPane chatArea, User user) {
        this.executor = Executors.newFixedThreadPool(10);  // Allow handling 10 users simultaneously
        this.chatArea = chatArea;
        this.currentUser = user;  // Set the authenticated user
        loadKeywordResponses();  // Load keyword-response pairs from the database
    }

    /**
     * Loads keyword-response pairs from the database into the chatbot's response map.
     */
    private void loadKeywordResponses() {
        keywordResponses = DatabaseHelper.getKeywordResponses();
        if (keywordResponses == null || keywordResponses.isEmpty()) {
            keywordResponses = Map.of("default", "Sorry, I couldn't find an answer to your query.");
        }
    }

    /**
     * Logs the chat history into the database, linking it to the currently authenticated user.
     *
     * @param message  The user's message
     * @param response The bot's response
     */
    public void logChatHistory(String message, String response) throws SQLException {
        if (currentUser == null) {
            System.err.println("Cannot log chat history: No authenticated user.");
            return;
        }

        DatabaseHelper.logChatHistory(currentUser.getUserId(), message, response);
    }

    /**
     * Handles user queries by processing them and updating the UI with bot responses.
     *
     * @param userMessage The user's input message
     */
    public void handleUserQuery(String userMessage) {
        executor.submit(() -> {
            // Process query here (NLP and database check)
            String response = processQuery(userMessage);

            // Update the UI with the response (SwingUtilities.invokeLater for thread safety)
            SwingUtilities.invokeLater(() -> {
                appendMessage(userMessage, userMessageColor);  // Display user message in user color
                appendMessage(response, botMessageColor);     // Display bot response in bot color
            });

            // Log the interaction in the database, linked to the user ID
            if (currentUser != null) {
                try {
                    logChatHistory(userMessage, response);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Update the context variables
            lastUserQuery = userMessage;
            lastResponse = response;
        });
    }

    /**
     * Processes a user query to determine the bot's response.
     *
     * @param query The user's input query
     * @return The bot's response
     */
    public String processQuery(String query) {
        // Ensure that the query is processed to handle case insensitivity and extra spaces
        String processedText = query.trim().toLowerCase();  // Case-insensitive matching

        // Log the processed query for debugging
        System.out.println("Processed Query: " + processedText);

        // Step 1: Context-aware response
        if (!lastUserQuery.isEmpty()) {
            if (processedText.contains("yes") || processedText.contains("confirm")) {
                if (lastUserQuery.contains("order")) {
                    return "Okay! I’ll proceed with your order. Do you need help with anything else?";
                } else if (lastUserQuery.contains("refund")) {
                    return "Got it! I'll initiate the refund process. Let me know if there’s anything else.";
                }
            } else if (processedText.contains("no")) {
                return "Alright, let me know if you need assistance with anything else.";
            }
        }

        // Step 2: Check FAQ database for a direct match
        String dbResponse = DatabaseHelper.getFAQAnswer(processedText);
        if (dbResponse != null) {
            return dbResponse; // Return response directly from the database if found
        }

        // Step 3: Check for keyword responses in the loaded map
        for (Map.Entry<String, String> entry : keywordResponses.entrySet()) {
            if (processedText.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Step 4: Fallback response if no match is found
        return "Sorry, I didn't understand that. Could you please rephrase your query?";
    }

    /**
     * Authenticates a user based on their email and password.
     *
     * @param email    The user's email
     * @param password The user's password
     * @return A User object if authentication is successful, otherwise null
     */
    public static User authenticateUser(String email, String password) {
        User user = null;

        try (Connection conn = DatabaseHelper.getConnection()) {
            String query = "SELECT user_id, name, email FROM users WHERE email = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found, create and return User object
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                user = new User(userId, name, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    
    private void appendMessage(String message, Color color) {
    StyledDocument doc = chatArea.getStyledDocument();

    // Create a new mutable attribute set
    MutableAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setForeground(attrs, color);  // Set color
    StyleConstants.setFontSize(attrs, 14);  // Optional: set font size

    try {
        // Append the message with the style applied
        doc.insertString(doc.getLength(), message + "\n", attrs);
    } catch (BadLocationException e) {
        e.printStackTrace();
    }
}
}
