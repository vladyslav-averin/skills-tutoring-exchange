package server;

import dao.DatabaseInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private static final int PORT = 8080;
    // Keep track of connected clients so direct notifications can find the right receiver.
    private static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Starting Server on port " + PORT + "...");

        // Make sure the database tables and new columns exist before clients connect
        DatabaseInitializer.initializeDatabase();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for clients.");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create a new thread (ClientHandler) for each connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                activeClients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized void sendToUser(String username, Object payload) {
        // Send private data only to the client that is logged in as this user.
        // If the user is offline, nothing is sent. The message itself is still saved in the database.
        if (username == null) {
            return;
        }

        for (ClientHandler client : activeClients) {
            String currentUsername = client.getCurrentUsername();
            if (username.equals(currentUsername)) {
                client.sendDataToClient(payload);
                return;
            }
        }
    }
    
    public static synchronized void removeClient(ClientHandler clientHandler) {
        activeClients.remove(clientHandler);
    }
}
