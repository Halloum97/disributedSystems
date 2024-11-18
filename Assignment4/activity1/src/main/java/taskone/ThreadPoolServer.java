package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: gradle runTask3 -Pport=8000 -PmaxThreads=5");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int maxThreads = Integer.parseInt(args[1]);
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
        StringList sharedState = new StringList();

        System.out.println("Thread Pool Server started on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from client");
            threadPool.execute(() -> {      
                Performer performer = new Performer(sharedState);
                NetworkUtils.processClient(clientSocket, performer);
            });
        }
    }
}
