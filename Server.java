package Simple.group.chatting.application;

import java.net.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class Server implements Runnable {
    
    private Socket socket;

    // List of clients
    public static Vector<BufferedWriter> client = new Vector<>();
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chatdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Your password here

    public Server(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // Set up input/output streams
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Add the writer to the list of clients
            client.add(writer);

            // Load chat history and send to the newly connected client
            sendChatHistory(writer);

            while (true) {
                String data = reader.readLine().trim();
                System.out.println("Received: " + data);

                // Save message to database
                saveMessageToDatabase(data);

                // Broadcast the message to all clients
                for (BufferedWriter bw : client) {
                    try {
                        bw.write(data);
                        bw.write("\r\n");
                        bw.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(2024)) {
            System.out.println("Server is running on port 2024...");
            while (true) {
                Socket socket = serverSocket.accept();
                Server server = new Server(socket);
                Thread thread = new Thread(server);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a message to the database.
     * @param message The message to save
     */
    private void saveMessageToDatabase(String message) {
        String sql = "INSERT INTO messages (message) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends chat history from the database to the client who just connected.
     * @param writer BufferedWriter of the client
     */
    private void sendChatHistory(BufferedWriter writer) {
        String sql = "SELECT message FROM messages ORDER BY id";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String message = rs.getString("message");
                writer.write(message);
                writer.write("\r\n");
                writer.flush();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
