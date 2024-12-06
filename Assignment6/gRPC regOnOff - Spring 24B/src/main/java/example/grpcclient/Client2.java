package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import service.*;
import com.google.protobuf.Empty;
import java.util.Scanner;

public class Client2 {
    private final RegistryGrpc.RegistryBlockingStub registryStub;

    public Client2(Channel regChannel) {
        registryStub = RegistryGrpc.newBlockingStub(regChannel);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.out.println("Expected arguments: <host(String)> <port(int)> <regHost(String)> <regPort(int)> <message(String)> <regOn(bool)>");
            System.exit(1);
        }

        String regHost = args[2];
        int regPort = Integer.parseInt(args[3]);

        String regTarget = regHost + ":" + regPort;
        ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget).usePlaintext().build();

        try {
            Client2 client = new Client2(regChannel);
            client.runClient();
        } finally {
            regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void runClient() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Fetching available services...");
            ServicesListRes serviceListRes = fetchAvailableServices();

            if (serviceListRes == null || serviceListRes.getServicesCount() == 0) {
                System.out.println("No services available.");
                return;
            }

            System.out.println("Available services:");
            for (int i = 0; i < serviceListRes.getServicesCount(); i++) {
                System.out.printf("%d. %s%n", i + 1, serviceListRes.getServices(i));
            }
            System.out.println("Select a service by number (or type 0 to exit):");
            int serviceChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (serviceChoice == 0) {
                System.out.println("Exiting...");
                break;
            }

            if (serviceChoice < 1 || serviceChoice > serviceListRes.getServicesCount()) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            String chosenService = serviceListRes.getServices(serviceChoice - 1);
            System.out.printf("You chose: %s%n", chosenService);

            SingleServerRes serverRes = findServiceServer(chosenService);
            if (serverRes == null || !serverRes.hasConnection()) {
                System.out.println("Could not locate a server for the chosen service.");
                continue;
            }
            
            // Extract IP and port from the connection object
            String target = serverRes.getConnection().getUri() + ":" + serverRes.getConnection().getPort();
            System.out.printf("Connecting to server %s at %s:%d%n", 
                              serverRes.getConnection().getName(),
                              serverRes.getConnection().getUri(), 
                              serverRes.getConnection().getPort());
            
            ManagedChannel serviceChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            try {
                interactWithService(chosenService, serviceChannel);
            } catch (Exception e) {
                System.out.println("Failed to connect or interact with the service: " + e.getMessage());
            } finally {
                serviceChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        
        }
    }

    private ServicesListRes fetchAvailableServices() {
        try {
            GetServicesReq request = GetServicesReq.newBuilder().build(); // Create the expected request object
            return registryStub.getServices(request); // Pass the correct request type
        } catch (Exception e) {
            System.err.println("Failed to fetch available services: " + e.getMessage());
            return null;
        }
    }
    

    private SingleServerRes findServiceServer(String serviceName) {
        try {
            FindServerReq request = FindServerReq.newBuilder().setServiceName(serviceName).build();
            return registryStub.findServer(request);
        } catch (Exception e) {
            System.err.println("Failed to find a server for the service: " + e.getMessage());
            return null;
        }
    }

    private void interactWithService(String serviceName, ManagedChannel channel) {
        Scanner scanner = new Scanner(System.in);

        if (serviceName.equalsIgnoreCase("echo")) {
            EchoGrpc.EchoBlockingStub echoStub = EchoGrpc.newBlockingStub(channel);
            System.out.println("Enter a message to echo:");
            String message = scanner.nextLine();
            ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
            try {
                ServerResponse response = echoStub.parrot(request);
                System.out.println("Response: " + response.getMessage());
            } catch (Exception e) {
                System.err.println("Failed to call Echo service: " + e.getMessage());
            }
        } else if (serviceName.equalsIgnoreCase("joke")) {
            JokeGrpc.JokeBlockingStub jokeStub = JokeGrpc.newBlockingStub(channel);
            System.out.println("How many jokes would you like?");
            int num = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            JokeReq request = JokeReq.newBuilder().setNumber(num).build();
            try {
                JokeRes response = jokeStub.getJoke(request);
                System.out.println("Jokes:");
                response.getJokeList().forEach(System.out::println);
            } catch (Exception e) {
                System.err.println("Failed to call Joke service: " + e.getMessage());
            }
        } else {
            System.out.println("Service interaction for " + serviceName + " is not implemented.");
        }
    }
}
