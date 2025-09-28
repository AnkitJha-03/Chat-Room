package com.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(client.getLocalAddress() + " is connected!");
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); // msg receiver
            out = new PrintWriter(client.getOutputStream(), true); // msg sender

            // taking username
            while(true) {
                out.println("Enter a username : ");
                username = in.readLine();

                if(username == null || username.isBlank()) {
                    out.println("Username can't be empty!");
                    continue;
                }

                if(username.equals("/quit")) {
                    username = "";
                    break;
                }

                if(!ChatServer.addClient(username, this)) {
                    System.out.println("This username already exists in chat!");
                } else {
                    break;
                }
            }

            // handling client msg
            String msg;
            while((msg = in.readLine()) != null) {
                if(msg.equals("/quit") || username.isEmpty()) {
                    ChatServer.removeClient(username);
                    break;
                } else {
                    ChatServer.broadcast(username, msg);
                }
            }

        } catch (Exception ignore) {
            System.out.println(username + " client handling error!");
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        System.out.println(username + " is disconnected!");
        try {
            in.close();
            out.close();
            if(!client.isClosed()) client.close();
        } catch (Exception e) {
            // ignore
        }
    }

    // from server to client
    public void sendMessage(String msg) {
        out.println(msg);
    }
}
