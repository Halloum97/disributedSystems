package example.grpcclient;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerMethodDefinition;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class Node {
  static private Server server;
  int port;

  Node(int port) {
    this.port = port;
  }

  private void start() throws IOException {
    ArrayList<String> services = new ArrayList<>();
    server = ServerBuilder.forPort(port)
        .addService(new EchoImpl())
        .addService(new JokeImpl())
        .addService(new RegistryAnswerImpl(services))
        .addService(new FlowersImpl())
        .addService(new FollowImpl())
        .addService(new LibraryServiceImpl())
        .build()
        .start();

    for (var service : server.getServices()) {
      for (ServerMethodDefinition<?, ?> method : service.getMethods()) {
        services.add(method.getMethodDescriptor().getFullMethodName());
        System.out.println(method.getMethodDescriptor().getFullMethodName());
      }
    }

    System.out.println("Server running ...");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          Node.this.stop();
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 6) {
      System.out.println("Expected arguments: <regAddr(string)> <regPort(int)> <nodeAddr(string)> <nodePort(int)> <name(String)> <registerOn(bool)>");
      System.exit(1);
    }
    int regPort = 9003;
    int nodePort = 9099;
    try {
      regPort = Integer.parseInt(args[1]);
      nodePort = Integer.parseInt(args[3]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port] must be an integer");
      System.exit(2);
    }
    final Node server = new Node(nodePort);
    System.out.println(args[0]);
    System.out.println(args[1]);
    System.out.println(args[2]);
    System.out.println(args[3]);
    System.out.println(args[4]);

    if (args[5].equals("true")) {
      Register regThread = new Register(args[0], regPort, args[2], nodePort, args[4]);
      regThread.start();
    }

    server.start();
    server.blockUntilShutdown();
  }
}