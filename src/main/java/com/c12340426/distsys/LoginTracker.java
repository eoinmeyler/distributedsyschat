// Reference: https://www.baeldung.com/a-guide-to-java-sockets

package com.c12340426.distsys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class LoginTracker {
    private static final Logger LOG = LoggerFactory.getLogger(LoginTracker.class);

    private ServerSocket serverSocket;
    Set<ChatUser> onlineUsers = new HashSet<ChatUser>();

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Starting with Port " + port);
            while (true) {
                new ClientHandler(serverSocket.accept(), onlineUsers).start();


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
        private Set<ChatUser> onlineUsers;


        public ClientHandler(Socket socket, Set<ChatUser> onlineUsers) {
            this.userName = userName;
            this.clientSocket = socket;
            this.onlineUsers = onlineUsers;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;

                //ChatBack chatBack = new ChatBack(userName, out);
                //chatBack.start();


                inputLine = in.readLine();
                System.out.println(inputLine);
                final String[] userInfo = inputLine.split(" ");
                if (userInfo[0].equals("?")){
                    for (ChatUser user:onlineUsers){
                        out.println(user.getName() + " " + user.getIp() + " " + user.getPort());
                    }
                } else {
                    ChatUser chatUser = new ChatUser(userInfo[0], userInfo[1], Integer.parseInt(userInfo[2]));
                    onlineUsers.add(chatUser);

                    System.out.println(chatUser.getName() + " is online, IP: " + chatUser.getIp() + ", Port: " + chatUser.getPort());
                }

                in.close();
                out.close();
                clientSocket.close();

            } catch (IOException e) {
                LOG.debug(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        LoginTracker server = new LoginTracker();
        int port = Integer.parseInt(args[0]);
        server.start(port);
    }

}
