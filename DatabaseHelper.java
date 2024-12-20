
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/chatbot_customer";
    private static final String USER = "root";
    private static final String PASSWORD = "Monika@09";

    /**
     * Establishes a connection to the database.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Retrieves an FAQ answer based on the query.
     * This method performs a case-insensitive search.
     *
     * @param query The user's query
     * @return The answer if found, otherwise a fallback message
     */
    public static String getFAQAnswer(String query) {
        String response = "Sorry, I didn't understand that. Could you please rephrase your query?";

        try (Connection conn = getConnection()) {
            // Normalize and prepare the query string for case-insensitive matching
            String sql = "SELECT answer FROM faqs WHERE LOWER(question) LIKE?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + query.toLowerCase() + "%");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    response = rs.getString("answer");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Logs the chat history into the database.
     *
     * @param userId   The ID of the user
     * @param message  The user's message
     * @param response The bot's response
     */
    public static void logChatHistory(int userId, String message, String response) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO chat_history (user_id, message, response) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, message);
                stmt.setString(3, response);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all keyword-response pairs from the database.
     *
     * @return A map containing keywords as keys and their corresponding responses as values
     */
    public static Map<String, String> getKeywordResponses() {
        Map<String, String> responses = new HashMap<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT question, answer FROM faqs";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String question = rs.getString("question").toLowerCase(); // Convert to lowercase for consistency
                    String answer = rs.getString("answer");
                    responses.put(question, answer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responses;
    }

    /**
     * Authenticates a user based on their email and password.
     * If the user doesn't exist, creates a new user in the database.
     *
     * @param email    The user's email
     * @param password The user's password
     * @return A User object if authentication is successful, otherwise null
     */
    public static User authenticateUser(String email, String password) {
        User user = null;

        try (Connection conn = getConnection()) {
            // Check if the user already exists
            String query = "SELECT user_id, name, email, password FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Validate password here (assuming you use hashed passwords in production)
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        int userId = rs.getInt("user_id");
                        String name = rs.getString("name");
                        user = new User(userId, name, email);
                    } else {
                        System.out.println("Incorrect password.");
                    }
                } else {
                    // User doesn't exist, create a new one
                    String insertQuery = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        String name = email.split("@")[0]; // Default name is part of email before '@'
                        insertStmt.setString(1, name);
                        insertStmt.setString(2, email);
                        insertStmt.setString(3, password); // In real systems, hash passwords before storing
                        insertStmt.executeUpdate();

                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int userId = generatedKeys.getInt(1);
                            user = new User(userId, name, email);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
}
