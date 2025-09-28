package com.chat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Runnable {
    private final int PORT;
    private boolean serverRunning;
    private static Map<String, ClientHandler> clients;
    private final ExecutorService pool;
    ServerSocket serverSocket;

    ChatServer(int port, int userLimit) {
        PORT = port;
        serverRunning = true;
        clients = new ConcurrentHashMap<>();
        pool = Executors.newFixedThreadPool(userLimit);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT); // start server
            System.out.println("Chat-Server is running on port " + PORT + "...");
            while (serverRunning) {
                Socket clientSocket = serverSocket.accept(); // wait for new client
                pool.execute(new ClientHandler(clientSocket)); // assign client to server
            }
        } catch (Exception e) {
            System.out.println("Server error!");
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            serverRunning = false;
            pool.shutdown();
            if (!serverSocket.isClosed()) serverSocket.close();
            clients.forEach((username, ch) -> {
                ch.shutdown();
            });
        } catch (Exception e) {
            // ignore
        }
    }

    public static void broadcast(String sender, String msg) {
        clients.forEach((username, ch) -> {
            if(!username.equals(sender)) {
                ch.sendMessage(sender + ": " + msg);
            }
        });
    }

    public static void notifyAllClients(String msg) {
        clients.forEach((username, ch) -> ch.sendMessage(msg));
    }

    public static boolean addClient(String username, ClientHandler ch) {
        if(!username.isEmpty() && (clients.putIfAbsent(username, ch) == null)) {
            notifyAllClients(username + " entered the chat!");
            return true;
        };
        return false;
    }

    public static void removeClient(String username) {
        clients.remove(username);
        notifyAllClients(username + " left the chat!");
    }


    public static void main(String[] args) {
        ChatServer server = new ChatServer(8080, 3);
        server.run();
    }
}
