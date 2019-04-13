// Reference: https://www.baeldung.com/a-guide-to-java-sockets
package com.c12340426.distsys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(ChatClient.class);

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void showOnlineUsers (){
        //Connect to LoginTracker
        //Send details
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        String loginIP = "127.0.0.1";
        int loginPort = 5000;


        try {
            clientSocket = new Socket(loginIP, loginPort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println("?");
            String user;
            while((user = in.readLine()) != null){
                System.out.println(user);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            LOG.debug("Error when initializing connection", e);
        }
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            LOG.debug("Error when initializing connection", e);
        }

    }

    public void sendMessage(String msg) {
        try {
            out.println(msg);
            //return in.readLine();
        } catch (Exception e) {
            return;
        }
    }

    public String readMessage(){
        String input = "";
        try {
            input = in.readLine();
        } catch (IOException e) {
            System.out.println("Error reading socket: " + e.getMessage());
        }
        return input;
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            LOG.debug("error when closing", e);
        }

    }



    private static void keyHandler(String userName, ChatClient chatClient) throws IOException {
        BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));
        String kbInput = "";
        while (!kbInput.equals("bye")){
            try {
                kbInput = keyboardIn.readLine();
                chatClient.sendMessage(userName + ": " + kbInput);
            } catch (IOException e) {
                System.out.println("Error reading from keyboard: " + e.getMessage());
                throw new IOException (e);
            }
        }
    }

    private static class ResponseHandler extends Thread {
        private ChatClient client;

        public ResponseHandler(ChatClient client) {
            this.client = client;
        }

        public void run() {
            String inputLine = "";
            while (inputLine != null) {
                inputLine = client.readMessage();
                System.out.println(inputLine);
            }

        }
    }

    public static void main(String[] args) throws IOException {
        ChatClient chatClient = new ChatClient();
        if(args.length == 0) {
            chatClient.showOnlineUsers();
        } else {
            String userName = args[0];
            String remoteIP = args[1];
            int remotePort = Integer.parseInt(args[2]);
            System.out.println(userName);
            chatClient.startConnection(remoteIP, remotePort);
            System.out.println("Connected...");
            new ResponseHandler(chatClient).start();
            keyHandler(userName, chatClient);
            chatClient.stopConnection();
        }

    }
}


