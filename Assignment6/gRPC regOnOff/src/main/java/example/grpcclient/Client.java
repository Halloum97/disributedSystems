package example.grpcclient;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.protobuf.Empty; // needed to use Empty

public class Client {
  private final EchoGrpc.EchoBlockingStub blockingStub;
  private final JokeGrpc.JokeBlockingStub blockingStub2;
  private final RegistryGrpc.RegistryBlockingStub blockingStub3;
  private final RegistryGrpc.RegistryBlockingStub blockingStub4;
  private final FlowersGrpc.FlowersBlockingStub flowersStub;
  private final FollowGrpc.FollowBlockingStub followStub;
  private final LibraryServiceGrpc.LibraryServiceBlockingStub libraryStub;

  public Client(Channel channel, Channel regChannel) {
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
    blockingStub4 = RegistryGrpc.newBlockingStub(channel);
    flowersStub = FlowersGrpc.newBlockingStub(channel);
    followStub = FollowGrpc.newBlockingStub(channel);
    libraryStub = LibraryServiceGrpc.newBlockingStub(channel);
  }

  public Client(Channel channel) {
    blockingStub = EchoGrpc.newBlockingStub(channel);
    blockingStub2 = JokeGrpc.newBlockingStub(channel);
    blockingStub3 = null;
    blockingStub4 = null;
    flowersStub = FlowersGrpc.newBlockingStub(channel);
    followStub = FollowGrpc.newBlockingStub(channel);
    libraryStub = LibraryServiceGrpc.newBlockingStub(channel);
  }

  public void askServerToParrot(String message) {
    ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
    ServerResponse response;
    try {
      response = blockingStub.parrot(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
    System.out.println("Received from server: " + response.getMessage());
  }

  public void askForJokes(int num) {
    JokeReq request = JokeReq.newBuilder().setNumber(num).build();
    JokeRes response;
    Empty empt = Empty.newBuilder().build();
    try {
      response = blockingStub2.getJoke(request);
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
    System.out.println("Your jokes: ");
    for (String joke : response.getJokeList()) {
      System.out.println("--- " + joke);
    }
  }

  public void setJoke(String joke) {
    JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
    JokeSetRes response;
    try {
      response = blockingStub2.setJoke(request);
      System.out.println(response.getOk());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void getNodeServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub4.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void getServices() {
    GetServicesReq request = GetServicesReq.newBuilder().build();
    ServicesListRes response;
    try {
      response = blockingStub3.getServices(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServer(String name) {
    FindServerReq request = FindServerReq.newBuilder().setServiceName(name).build();
    SingleServerRes response;
    try {
      response = blockingStub3.findServer(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void findServers(String name) {
    FindServersReq request = FindServersReq.newBuilder().setServiceName(name).build();
    ServerListRes response;
    try {
      response = blockingStub3.findServers(request);
      System.out.println(response.toString());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void plantFlower(String name, int waterTimes, int bloomTime) {
    FlowerReq request = FlowerReq.newBuilder().setName(name).setWaterTimes(waterTimes).setBloomTime(bloomTime).build();
    FlowerRes response;
    try {
      response = flowersStub.plantFlower(request);
      System.out.println(response.getMessage());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void viewFlowers() {
    FlowerViewRes response;
    try {
      response = flowersStub.viewFlowers(Empty.newBuilder().build());
      if (response.getIsSuccess()) {
        for (Flower flower : response.getFlowersList()) {
          System.out.println(flower.getName() + " - " + flower.getFlowerState());
        }
      } else {
        System.out.println(response.getError());
      }
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void waterFlower(String name) {
    FlowerCare request = FlowerCare.newBuilder().setName(name).build();
    WaterRes response;
    try {
      response = flowersStub.waterFlower(request);
      System.out.println(response.getMessage());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void careForFlower(String name) {
    FlowerCare request = FlowerCare.newBuilder().setName(name).build();
    CareRes response;
    try {
      response = flowersStub.careForFlower(request);
      System.out.println(response.getMessage());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void addUser(String name) {
    UserReq request = UserReq.newBuilder().setName(name).build();
    UserRes response;
    try {
      response = followStub.addUser(request);
      System.out.println(response.getError());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void followUser(String name, String followName) {
    UserReq request = UserReq.newBuilder().setName(name).setFollowName(followName).build();
    UserRes response;
    try {
      response = followStub.follow(request);
      System.out.println(response.getError());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void viewFollowing(String name) {
    UserReq request = UserReq.newBuilder().setName(name).build();
    UserRes response;
    try {
      response = followStub.viewFollowing(request);
      if (response.getIsSuccess()) {
        for (String followedUser : response.getFollowedUserList()) {
          System.out.println(followedUser);
        }
      } else {
        System.out.println(response.getError());
      }
    } catch (Exception e) {
      System.err.println("RPC failed: " + e);
      return;
    }
  }

  public void addBook(String title, String author, int year) {
    AddBookRequest request = AddBookRequest.newBuilder()
        .setTitle(title)
        .setAuthor(author)
        .setYear(year)
        .build();
    AddBookResponse response;
    try {
      response = libraryStub.addBook(request);
      System.out.println(response.getMessage());
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
  }

  public void listBooks() {
    ListBooksRequest request = ListBooksRequest.newBuilder().build();
    ListBooksResponse response;
    try {
      response = libraryStub.listBooks(request);
      for (Book book : response.getBooksList()) {
        System.out.println(book.getTitle() + " by " + book.getAuthor() + " (" + book.getYear() + ")");
      }
    } catch (Exception e) {
      System.err.println("RPC failed: " + e.getMessage());
      return;
    }
  }

  private static void runAutomatedTests(Client client) {
    System.out.println("\n=== Testing Flowers Service ===");
    
    // Test planting flowers
    System.out.println("\nTesting plantFlower:");
    client.plantFlower("Rose", 3, 24);
    client.plantFlower("Lily", 2, 12);
    // Error case - duplicate name
    client.plantFlower("Rose", 1, 12);
    
    // Test viewing flowers
    System.out.println("\nTesting viewFlowers:");
    client.viewFlowers();
    
    // Test watering flowers
    System.out.println("\nTesting waterFlower:");
    client.waterFlower("Rose");
    client.waterFlower("Rose");
    client.waterFlower("Rose"); // Should start blooming
    // Error case - watering blooming flower
    client.waterFlower("Rose");
    
    // Test caring for flowers
    System.out.println("\nTesting careForFlower:");
    client.careForFlower("Rose");
    // Error case - nonexistent flower
    client.careForFlower("Sunflower");
    
    System.out.println("\n=== Testing Follow Service ===");
    
    // Test adding users
    System.out.println("\nTesting addUser:");
    client.addUser("Alice");
    client.addUser("Bob");
    client.addUser("Charlie");
    // Error case - duplicate user
    client.addUser("Alice");
    
    // Test following users
    System.out.println("\nTesting follow:");
    client.followUser("Alice", "Bob");
    client.followUser("Alice", "Charlie");
    // Error case - nonexistent user
    client.followUser("Alice", "David");
    
    // Test viewing following
    System.out.println("\nTesting viewFollowing:");
    client.viewFollowing("Alice");
    // Error case - nonexistent user
    client.viewFollowing("David");

    System.out.println("\n=== Testing Library Service ===");

    // Test adding books
    System.out.println("\nTesting addBook:");
    client.addBook("The Great Gatsby", "F. Scott Fitzgerald", 1925);
    client.addBook("1984", "George Orwell", 1949);
    // Error case - duplicate book
    client.addBook("The Great Gatsby", "F. Scott Fitzgerald", 1925);

    // Test listing books
    System.out.println("\nTesting listBooks:");
    client.listBooks();
  }

  public static void main(String[] args) throws Exception {

    boolean autoMode = false;
    if (args.length > 5 && args[5].equals("true")) {
        autoMode = true;
    }
    if (args.length != 6) {
      System.out
          .println("Expected arguments: <host(String)> <port(int)> <regHost(string)> <regPort(int)> <message(String)> <regOn(bool)>");
      System.exit(1);
    }
    int port = 9099;
    int regPort = 9003;
    String host = args[0];
    String regHost = args[2];
    String message = args[4];
    try {
      port = Integer.parseInt(args[1]);
      regPort = Integer.parseInt(args[3]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }

    String target = host + ":" + port;
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        .usePlaintext().build();

    String regTarget = regHost + ":" + regPort;
    ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget).usePlaintext().build();
    try {
      Client client = new Client(channel, regChannel);

      if (autoMode) {
        runAutomatedTests(client);
        return;
      }
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        System.out.println("Choose a service:");
        System.out.println("1. Parrot");
        System.out.println("2. Jokes");
        System.out.println("3. Flowers");
        System.out.println("4. Follow");
        System.out.println("5. Library");
        System.out.println("6. Exit");
        String choice = reader.readLine();

        switch (choice) {
          case "1":
            System.out.println("Enter a message:");
            String messageInput = reader.readLine();
            client.askServerToParrot(messageInput);
            break;
          case "2":
            System.out.println("How many jokes would you like?");
            String num = reader.readLine();
            client.askForJokes(Integer.valueOf(num));
            break;
          case "3":
            System.out.println("Choose an action:");
            System.out.println("1. Plant Flower");
            System.out.println("2. View Flowers");
            System.out.println("3. Water Flower");
            System.out.println("4. Care for Flower");
            String flowerChoice = reader.readLine();
            switch (flowerChoice) {
              case "1":
                System.out.println("Enter flower name:");
                String flowerName = reader.readLine();
                System.out.println("Enter water times:");
                int waterTimes = Integer.parseInt(reader.readLine());
                System.out.println("Enter bloom time:");
                int bloomTime = Integer.parseInt(reader.readLine());
                client.plantFlower(flowerName, waterTimes, bloomTime);
                break;
              case "2":
                client.viewFlowers();
                break;
              case "3":
                System.out.println("Enter flower name:");
                String waterFlowerName = reader.readLine();
                client.waterFlower(waterFlowerName);
                break;
              case "4":
                System.out.println("Enter flower name:");
                String careFlowerName = reader.readLine();
                client.careForFlower(careFlowerName);
                break;
              default:
                System.out.println("Invalid choice.");
            }
            break;
          case "4":
            System.out.println("Choose an action:");
            System.out.println("1. Add User");
            System.out.println("2. Follow User");
            System.out.println("3. View Following");
            String followChoice = reader.readLine();
            switch (followChoice) {
              case "1":
                System.out.println("Enter user name:");
                String userName = reader.readLine();
                client.addUser(userName);
                break;
              case "2":
                System.out.println("Enter your name:");
                String followerName = reader.readLine();
                System.out.println("Enter name to follow:");
                String followName = reader.readLine();
                client.followUser(followerName, followName);
                break;
              case "3":
                System.out.println("Enter your name:");
                String viewName = reader.readLine();
                client.viewFollowing(viewName);
                break;
              default:
                System.out.println("Invalid choice.");
            }
            break;
          case "5":
            System.out.println("Choose an action:");
            System.out.println("1. Add Book");
            System.out.println("2. List Books");
            String libraryChoice = reader.readLine();
            switch (libraryChoice) {
              case "1":
                System.out.println("Enter book title:");
                String title = reader.readLine();
                System.out.println("Enter book author:");
                String author = reader.readLine();
                System.out.println("Enter publication year:");
                int year = Integer.parseInt(reader.readLine());
                client.addBook(title, author, year);
                break;
              case "2":
                client.listBooks();
                break;
              default:
                System.out.println("Invalid choice.");
            }
            break;
          case "6":
            return;
          default:
            System.out.println("Invalid choice.");
        }
      }
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
      regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}