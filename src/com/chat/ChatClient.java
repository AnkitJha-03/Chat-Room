package com.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient implements Runnable {
    private final int PORT;
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean clientRunning;

    ChatClient() {
        PORT = 8080;
        clientRunning = true;
    }

    @Override
    public void run() {
        try {
            client = new Socket("localhost", PORT); // connect to server
            in = new BufferedReader(new InputStreamReader(client.getInputStream())); // msg receiver
            out = new PrintWriter(client.getOutputStream(), true); // msg sender

            // separate handling for user input
            Thread inThread = new Thread(new InputHandler());
            inThread.start();

            // handling server msg
            String inMsg;
            while (clientRunning && (inMsg = in.readLine()) != null) {
                System.out.println(inMsg);
            }
        } catch (Exception e) {
            System.out.println("Server connection failed!");
        } finally {
            shutdown();
        }
    }


    public void shutdown() {
        try {
            clientRunning = false;
            if(out != null) {
                out.println("/quit"); // sending quit msg to server
                out.close();
            }
            in.close();
            if (!client.isClosed()) client.close();
        } catch (Exception e) {
            // ignore
        }
    }

    // user input handler
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (clientRunning) {
                    String msg = inReader.readLine(); // taking input
                    if (msg.equals("/quit")) {
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("Input stream failed!");
            } finally {
                shutdown();
            }
        }
    }


    public static void main(String[] args) {
        ChatClient client = new ChatClient();

        // handling forced shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));

        client.run();
    }
}
