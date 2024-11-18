package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: gradle runTask2 -Pport=8000");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        StringList sharedState = new StringList();
        System.out.println("Threaded Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from client");
            new Thread(() -> handleClient(clientSocket, sharedState)).start();
        }
    }

    private static void handleClient(Socket clientSocket, StringList sharedState) {
        try (clientSocket) {
            Performer performer = new Performer(sharedState);
            NetworkUtils.processClient(clientSocket, performer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
