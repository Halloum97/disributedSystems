package client;

import buffers.RequestProtos.*;
import buffers.ResponseProtos.*;

import java.io.*;
import java.net.Socket;
import java.util.List;

class SockBaseClient {
    public static void main(String[] args) throws Exception {
        Socket serverSock = null;
        OutputStream out = null;
        InputStream in = null;
    
        int port = 8000; // default port
    
        // Ensure correct arguments
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }
        String host = args[0];
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }
    
        try {
            // Connect to server
            serverSock = new Socket(host, port);
            out = serverSock.getOutputStream();
            in = serverSock.getInputStream();
    
            // Send name request
            Request op = nameRequest().build();
            op.writeDelimitedTo(out);
    
            while (true) {
                // Read server response
                Response response = Response.parseDelimitedFrom(in);
                System.out.println("Got a response: " + response.toString());
    
                Request.Builder req = Request.newBuilder();
    
                switch (response.getResponseType()) {
                    case GREETING:
                        System.out.println(response.getMessage());
                        req = chooseMenu(req, response);
                        break;
    
                    case LEADERBOARD:
                        displayLeaderboard(response);
                        req = chooseMenu(req, response);
                        break;
    
                    case PLAY:
                        System.out.println("Game board:\n" + response.getBoard());
                        System.out.println(response.getMessage());
                        req = playGameMenu();
                        break;
    
                    case BYE:
                        System.out.println(response.getMessage());
                        return;
    
                    case ERROR:
                        System.out.println("Error: " + response.getMessage() + " Type: " + response.getErrorType());
                        req = chooseMenu(req, response);
                        break;
    
                    default:
                        System.out.println("Unhandled response type: " + response.getResponseType());
                        req = chooseMenu(req, response);
                        break;
                }
    
                req.build().writeDelimitedTo(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exitAndClose(in, out, serverSock);
        }
    }
    

    /**
     * handles building a simple name requests, asks the user for their name and builds the request
     * @return Request.Builder which holds all teh information for the NAME request
     */
    static Request.Builder nameRequest() throws IOException {
        System.out.println("Please provide your name for the server.");
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String strToSend = stdin.readLine();

        return Request.newBuilder()
                .setOperationType(Request.OperationType.NAME)
                .setName(strToSend);
    }

    /**
     * Shows the main menu and lets the user choose a number, it builds the request for the next server call
     * @return Request.Builder which holds the information the server needs for a specific request
     */
    static Request.Builder chooseMenu(Request.Builder req, Response response) throws IOException {
        while (true) {
            System.out.println(response.getMenuoptions());
            System.out.print("Enter a number 1-3: ");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String menuSelect = stdin.readLine();
    
            switch (menuSelect) {
                case "1": // Request leaderboard
                    req.setOperationType(Request.OperationType.LEADERBOARD);
                    return req;
    
                case "2": // Start game
                    req.setOperationType(Request.OperationType.START);
                    System.out.print("Enter difficulty level (1-20): ");
                    String difficulty = stdin.readLine();
                    try {
                        req.setDifficulty(Integer.parseInt(difficulty));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid difficulty. Defaulting to 4.");
                        req.setDifficulty(4);
                    }
                    return req;
    
                case "3": // Quit game
                    req.setOperationType(Request.OperationType.QUIT);
                    return req;
    
                default:
                    System.out.println("\nNot a valid choice, please choose again.");
                    break;
            }
        }
    }
    

    /**
     * Exits the connection
     */
    static void exitAndClose(InputStream in, OutputStream out, Socket serverSock) throws IOException {
        if (in != null)   in.close();
        if (out != null)  out.close();
        if (serverSock != null) serverSock.close();
        System.exit(0);
    }

    /**
     * Handles the clear menu logic when the user chooses that in Game menu. It retuns the values exactly
     * as needed in the CLEAR request row int[0], column int[1], value int[3]
     */
    static int[] boardSelectionClear() throws Exception {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose what kind of clear by entering an integer (1 - 5)");
        System.out.print(" 1 - Clear value \n 2 - Clear row \n 3 - Clear column \n 4 - Clear Grid \n 5 - Clear Board \n");

        String selection = stdin.readLine();

        while (true) {
            if (selection.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                int temp = Integer.parseInt(selection);

                if (temp < 1 || temp > 5) {
                    throw new NumberFormatException();
                }

                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.println("Choose what kind of clear by entering an integer (1 - 5)");
                System.out.print("1 - Clear value \n 2 - Clear row \n 3 - Clear column \n 4 - Clear Grid \n 5 - Clear Board \n");
            }
            selection = stdin.readLine();
        }

        int[] coordinates = new int[3];

        switch (selection) {
            case "1":
                // clear value, so array will have {row, col, 1}
                coordinates = boardSelectionClearValue();
                break;
            case "2":
                // clear row, so array will have {row, -1, 2}
                coordinates = boardSelectionClearRow();
                break;
            case "3":
                // clear col, so array will have {-1, col, 3}
                coordinates = boardSelectionClearCol();
                break;
            case "4":
                // clear grid, so array will have {gridNum, -1, 4}
                coordinates = boardSelectionClearGrid();
                break;
            case "5":
                // clear entire board, so array will have {-1, -1, 5}
                coordinates[0] = -1;
                coordinates[1] = -1;
                coordinates[2] = 5;
                break;
            default:
                break;
        }

        return coordinates;
    }

    private static void displayLeaderboard(Response response) {
        System.out.println(response.getMessage());
        List<Entry> leaderboard = response.getLeaderList();
        leaderboard.stream()
                .sorted((e1, e2) -> e2.getPoints() - e1.getPoints()) // Sort by points descending
                .forEach(entry -> System.out.println(entry.getName() + ": " + entry.getPoints() + " points, " + entry.getLogins() + " logins"));
    }

    static Request.Builder playGameMenu() throws IOException {
        Request.Builder req = Request.newBuilder();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
        System.out.println("Game Menu:");
        System.out.println("1. Enter a move");
        System.out.println("2. Clear area");
        System.out.println("3. Quit game");
    
        String choice = stdin.readLine();
        switch (choice) {
            case "1": // Enter a move
                System.out.print("Enter row (1-9): ");
                int row = Integer.parseInt(stdin.readLine()) - 1;
    
                System.out.print("Enter column (1-9): ");
                int column = Integer.parseInt(stdin.readLine()) - 1;
    
                System.out.print("Enter value (1-9): ");
                int value = Integer.parseInt(stdin.readLine());
    
                req.setOperationType(Request.OperationType.UPDATE)
                   .setRow(row)
                   .setColumn(column)
                   .setValue(value);
                break;
    
            case "2": // Clear area
                try {
                    int[] coordinates = boardSelectionClear();
                    req.setOperationType(Request.OperationType.CLEAR)
                    .setRow(coordinates[0])
                    .setColumn(coordinates[1])
                    .setValue(coordinates[2]);
                } catch (Exception e) {
                    System.out.println("Error during board selection: " + e.getMessage());
                    return playGameMenu(); // Re-display the game menu
                }
                break;
    
            case "3": // Quit game
                req.setOperationType(Request.OperationType.QUIT);
                break;
    
            default:
                System.out.println("Invalid choice. Returning to game menu.");
                return playGameMenu();
        }
    
        return req;
    }
    

    static int[] boardSelectionClearValue() throws Exception {
        int[] coordinates = new int[3];

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose coordinates of the value you want to clear");
        System.out.print("Enter the row as an integer (1 - 9): ");
        String row = stdin.readLine();

        while (true) {
            if (row.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                Integer.parseInt(row);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.print("Enter the row as an integer (1 - 9): ");
            }
            row = stdin.readLine();
        }

        coordinates[0] = Integer.parseInt(row);

        System.out.print("Enter the column as an integer (1 - 9): ");
        String col = stdin.readLine();

        while (true) {
            if (col.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                Integer.parseInt(col);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.print("Enter the column as an integer (1 - 9): ");
            }
            col = stdin.readLine();
        }

        coordinates[1] = Integer.parseInt(col);
        coordinates[2] = 1;

        return coordinates;
    }

    static int[] boardSelectionClearRow() throws Exception {
        int[] coordinates = new int[3];

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose the row you want to clear");
        System.out.print("Enter the row as an integer (1 - 9): ");
        String row = stdin.readLine();

        while (true) {
            if (row.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                Integer.parseInt(row);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.print("Enter the row as an integer (1 - 9): ");
            }
            row = stdin.readLine();
        }

        coordinates[0] = Integer.parseInt(row);
        coordinates[1] = -1;
        coordinates[2] = 2;

        return coordinates;
    }

    static int[] boardSelectionClearCol() throws Exception {
        int[] coordinates = new int[3];

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose the column you want to clear");
        System.out.print("Enter the column as an integer (1 - 9): ");
        String col = stdin.readLine();

        while (true) {
            if (col.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                Integer.parseInt(col);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.print("Enter the column as an integer (1 - 9): ");
            }
            col = stdin.readLine();
        }

        coordinates[0] = -1;
        coordinates[1] = Integer.parseInt(col);
        coordinates[2] = 3;
        return coordinates;
    }

    static int[] boardSelectionClearGrid() throws Exception {
        int[] coordinates = new int[3];

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Choose area of the grid you want to clear");
        System.out.println(" 1 2 3 \n 4 5 6 \n 7 8 9 \n");
        System.out.print("Enter the grid as an integer (1 - 9): ");
        String grid = stdin.readLine();

        while (true) {
            if (grid.equalsIgnoreCase("exit")) {
                return new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
            try {
                Integer.parseInt(grid);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("That's not an integer!");
                System.out.print("Enter the grid as an integer (1 - 9): ");
            }
            grid = stdin.readLine();
        }

        coordinates[0] = Integer.parseInt(grid);
        coordinates[1] = -1;
        coordinates[2] = 4;

        return coordinates;
    }
}
