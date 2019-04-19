// Reference: https://www.baeldung.com/a-guide-to-java-sockets

package com.c12340426.distsys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Port;

public class ChatServer {
    private static final Logger LOG = LoggerFactory.getLogger(ChatServer.class);

    private ServerSocket serverSocket;

    public void login(String name, String ip, String loginTrackIP, int port){
        //Connect to LoginTracker
        //Send details
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        String loginIP = loginTrackIP;
        int loginPort = 5000;


        try {
            clientSocket = new Socket(loginIP, loginPort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println(name + " " + ip + " " + port);
            in.close();
            out.close();
        } catch (IOException e) {
            LOG.debug("Error when initializing connection", e);
        }
    }

    public void start(String name, int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Starting with Port " + port);
            while (true) {
                System.out.println(name + " is waiting for connection...");
                new ClientHandler(name, serverSocket.accept()).start();


            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }

    }

    public void stop() {
        try {

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;


        public ClientHandler(String userName, Socket socket) {
            this.userName = userName;
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;

                ChatBack chatBack = new ChatBack(userName, out);
                chatBack.start();

                while (!chatBack.isDone()) {
                    inputLine = in.readLine();
                    System.out.println(inputLine);
                }

                System.out.println("Closing connection with " + userName);

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
    }

    private static class ChatBack extends Thread {
        private PrintWriter backChannel;
        private BufferedReader in;
        private String userName;
        private boolean done = false;

        public ChatBack(String userName, PrintWriter backChannel) {
            this.backChannel = backChannel;
            this.userName = userName;
        }

        public void run() {
            try {

                //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //String inputLine;
                //inputLine = in.readLine();
                chatClient(userName, backChannel);

                //in.close();


            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }

        private void chatClient(String userName, PrintWriter printWriter) throws IOException {
            BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));
            String kbInput = "";
            while (!kbInput.equals("bye")){
                try {
                    kbInput = keyboardIn.readLine();
                    printWriter.println(userName + ": " + kbInput);
                } catch (IOException e) {
                    System.out.println("Error reading from keyboard: " + e.getMessage());
                    throw new IOException (e);
                }
            }
            done = true;
        }

        public boolean isDone() {
            return done;
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        String loginTrackIP = args[0];
        String name = args[1];
        String ip = args[2];
        int port = Integer.parseInt(args[3]);
        server.login(name, ip, loginTrackIP, port);
        server.start(name, port);
    }

}
