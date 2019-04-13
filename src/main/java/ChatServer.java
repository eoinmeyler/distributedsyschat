// Reference: https://www.baeldung.com/a-guide-to-java-sockets

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {
    private static final Logger LOG = LoggerFactory.getLogger(ChatServer.class);

    private ServerSocket serverSocket;

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

                new ChatBack(userName, out).start();

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

                //System.out.println("Closing connection with " + clientUser);

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

        private static void chatClient(String userName, PrintWriter printWriter) throws IOException {
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
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        String name = args[0];
        int port = Integer.parseInt(args[1]);
        server.start(name, port);
    }

}
