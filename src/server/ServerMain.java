package server;

import dao.DatabaseInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import model.Chat;
import model.Notification;
import model.Observer;

public class ServerMain implements Observer {
    private static final int PORT = 8080;
    // Keep track of all connected clients to broadcast notifications
    private static List<ClientHandler> activeClients = new ArrayList<>();
    private static Chat globalChat = new Chat();

    public static void main(String[] args) {
        System.out.println("Starting Server on port " + PORT + "...");

        // Make sure the database tables and new columns exist before clients connect
        DatabaseInitializer.initializeDatabase();
        
        ServerMain serverInstance = new ServerMain();
        globalChat.addObserver(serverInstance);
        
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

    // A method that allows the Server to broadcast to all clients (useful for Chat/Notifications)
    public static synchronized void broadcast(Object payload) {
        for (ClientHandler client : activeClients) {
            client.sendDataToClient(payload);
        }
    }
    
    public static synchronized void removeClient(ClientHandler clientHandler) {
        activeClients.remove(clientHandler);
    }
    
    public static Chat getGlobalChat() {
        return globalChat;
    }

    @Override
    public void update(Notification notification) {
        // This method is called by the Chat object (Subject) when a new message is added.
        // We fulfill the Observer pattern by broadcasting the Notification to all connected clients!
        System.out.println("Server acting as Observer: Broadcasting Notification -> " + notification.getMessageInformation());
        broadcast(notification);
    }
}
