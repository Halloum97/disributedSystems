/**
  File: Server.java
  Author: Student in Fall 2020B
  Description: Server class in package taskone.
*/

package taskone;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.JSONObject;

/**
 * Class: Server
 * Description: Server tasks.
 */
class Server {
    static Socket conn;
    static Performer performer;


    public static void main(String[] args) throws Exception {
        int port;
        StringList strings = new StringList();
        performer = new Performer(strings);

        if (args.length != 1) {
            // gradle runServer -Pport=8000 -q --console=plain
            System.out.println("Usage: gradle runServer -Pport=8000 -q --console=plain");
            System.exit(1);
        }

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
            return; // to satisfy compiler
        }

        ServerSocket server = new ServerSocket(port);
        System.out.println("Server Started on port " + port);

        while (true) {
            System.out.println("Accepting a Request...");
            conn = server.accept();
            System.out.println("Connected to client");
            doPerform();
        }
    }

    public static void doPerform() {
        boolean quit = false;
        try (OutputStream out = conn.getOutputStream(); InputStream in = conn.getInputStream()) {
            while (!quit) {
                byte[] messageBytes = NetworkUtils.receive(in);
                JSONObject message = JsonUtils.fromByteArray(messageBytes);
                JSONObject returnMessage;

                int choice = message.getInt("selected");
                switch (choice) {
                    case 1: // Add
                        String inStr = message.getString("data");
                        returnMessage = performer.add(inStr);
                        break;
                    case 2: // Display
                        returnMessage = performer.display();
                        break;
                    case 3: // Count
                        returnMessage = performer.count();
                        break;
                    case 0: // Quit
                        returnMessage = performer.quit();
                        quit = true;
                        break;
                    default: // Invalid input
                        returnMessage = performer.error("Invalid selection: " + choice + " is not an option");
                        break;
                }

                // Send response back to the client
                byte[] output = JsonUtils.toByteArray(returnMessage);
                NetworkUtils.send(out, output);
            }

            System.out.println("Closing connection with client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
