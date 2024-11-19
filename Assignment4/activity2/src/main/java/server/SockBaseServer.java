package server;

import buffers.RequestProtos.*;
import buffers.ResponseProtos.*;




import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class SockBaseServer {
    static String logFilename = "logs.txt";

    // Please use these as given so it works with our test cases
    static String menuOptions = "\nWhat would you like to do? \n 1 - to see the leader board \n 2 - to enter a game \n 3 - quit the game";
    static String gameOptions = "\nChoose an action: \n (1-9) - Enter an int to specify the row you want to update \n c - Clear number \n r - New Board";


    ServerSocket serv = null;
    InputStream in = null;
    OutputStream out = null;
    Socket clientSocket = null;
    private final int id; // client id

    Game game; // current game

    private boolean inGame = false; // a game was started (you can decide if you want this
    private String name; // player name

    private int currentState =1; // I used something like this to keep track of where I am in the game, you can decide if you want that as well

    private static boolean grading = true; // if the grading board should be used
    private static ConcurrentHashMap<String, LeaderboardEntry> leaderboard = new ConcurrentHashMap<>();


    public SockBaseServer(Socket sock, Game game, int id) {
        this.clientSocket = sock;
        this.game = game;
        this.id = id;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (Exception e){
            System.out.println("Error in constructor: " + e);
        }
    }

    /**
     * Received a request, starts to evaluate what it is and handles it, not complete
     */
    public void startGame() throws IOException {
        try {
            while (true) {
                Request op = Request.parseDelimitedFrom(in);
                System.out.println("Got request: " + op.toString());
                Response response;
                boolean quit = false;
    
                switch (op.getOperationType()) {
                    case NAME:
                        response = nameRequest(op);
                        break;
                    case LEADERBOARD:
                        response = leaderboardRequest();
                        break;
                    case START:
                        response = startGameRequest(op);
                        break;
                    case UPDATE:
                        response = updateRequest(op);
                        break;
                    case CLEAR:
                        response = clearRequest(op);
                        break;
                    case QUIT:
                        response = quit();
                        quit = true;
                        break;
                    default:
                        response = error(2, op.getOperationType().name());
                        break;
                }
    
                response.writeDelimitedTo(out);
    
                if (quit) {
                    return;
                }
            }
        } catch (SocketException se) {
            System.out.println("Client disconnected");
        } catch (Exception ex) {
            Response error = error(0, "Unexpected server error: " + ex.getMessage());
            error.writeDelimitedTo(out);
        } finally {
            System.out.println("Client ID " + id + " disconnected");
            this.inGame = false;
            exitAndClose(in, out, clientSocket);
        }
    }
    

    void exitAndClose(InputStream in, OutputStream out, Socket serverSock) throws IOException {
        if (in != null)   in.close();
        if (out != null)  out.close();
        if (serverSock != null) serverSock.close();
    }

    /**
     * Handles the name request and returns the appropriate response
     * @return Request.Builder holding the reponse back to Client as specified in Protocol
     */
    private Response nameRequest(Request op) throws IOException {
        name = op.getName();

        writeToLog(name, Message.CONNECT);
        currentState = 2;

        System.out.println("Got a connection and a name: " + name);
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.GREETING)
                .setMessage("Hello " + name + " and welcome to a simple game of Sudoku.")
                .setMenuoptions(menuOptions)
                .setNext(currentState)
                .build();
    }

    /**
     * Starts to handle start of a game after START request, is not complete of course, just shows how to get to the board
     */
    private Response startGame(Request op) throws IOException {

        System.out.println("start game");

        game.newGame(grading, 4); // difficulty should be read from request!

        System.out.println(game.getDisplayBoard());

        return Response.newBuilder()
                .build();
    }

    /**
     * Handles the quit request, might need adaptation
     * @return Request.Builder holding the reponse back to Client as specified in Protocol
     */
    private  Response quit() throws IOException {
        this.inGame = false;
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.BYE)
                .setMessage("Thank you for playing! goodbye.")
                .build();
    }

    /**
     * Start of handling errors, not fully done
     * @return Request.Builder holding the reponse back to Client as specified in Protocol
     */
    private Response error(int err, String field) throws IOException {
        String message = "";
        int type = err;
        Response.Builder response = Response.newBuilder();

        switch (err) {
            case 1:
                message = "\nError: required field missing or empty";
                break;
            case 2:
                message = "\nError: request not supported";
                break;
            default:
                message = "\nError: cannot process your request";
                type = 0;
                break;
        }

        response
                .setResponseType(Response.ResponseType.ERROR)
                .setErrorType(type)
                .setMessage(message)
                .setNext(currentState)
                .build();

        return response.build();
    }
    
    /**
     * Writing a new entry to our log
     * @param name - Name of the person logging in
     * @param message - type Message from Protobuf which is the message to be written in the log (e.g. Connect) 
     * @return String of the new hidden image
     */
    public void writeToLog(String name, Message message) {
        try {
            // read old log file
            Logs.Builder logs = readLogFile();

            Date date = java.util.Calendar.getInstance().getTime();

            // we are writing a new log entry to our log
            // add a new log entry to the log list of the Protobuf object
            logs.addLog(date + ": " +  name + " - " + message);

            // open log file
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            // write to log file
            logsObj.writeTo(output);
        } catch(Exception e) {
            System.out.println("Issue while trying to save");
        }
    }

    /**
     * Reading the current log file
     * @return Logs.Builder a builder of a logs entry from protobuf
     */
    public Logs.Builder readLogFile() throws Exception {
        Logs.Builder logs = Logs.newBuilder();

        try {
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    private Response leaderboardRequest() {
        System.out.println("Leaderboard request received");
    
        List<Entry> leaderboardEntries = new ArrayList<>();
        for (LeaderboardEntry entry : leaderboard.values()) {
            leaderboardEntries.add(
                Entry.newBuilder()
                    .setName(entry.getName())
                    .setPoints(entry.getPoints())
                    .setLogins(entry.getLogins())
                    .build()
            );
        }
    
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.LEADERBOARD)
                .addAllLeader(leaderboardEntries)
                .setMessage("Current Leaderboard:")
                .build();
    }
    
    

    private Response startGameRequest(Request op) throws IOException {
        System.out.println("Start game request received");
    
        int difficulty = op.hasDifficulty() ? op.getDifficulty() : 4; // Default difficulty level
        game.newGame(grading, difficulty);
    
        System.out.println(game.getDisplayBoard());
    
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.START)
                .setBoard(game.getDisplayBoard()) // Send the generated board to the client
                .setMessage("New game started with difficulty " + difficulty)
                .setNext(3) // Move to game menu
                .build();
    }

    private Response updateRequest(Request op) throws IOException {
        System.out.println("Update request received");
    
        if (!op.hasRow() || !op.hasColumn() || !op.hasValue()) {
            return error(1, "Update fields (row, column, value) are missing");
        }
    
        int row = op.getRow();
        int column = op.getColumn();
        int value = op.getValue();
    
        // Update the cell and get the evaluation type
        Response.EvalType evalType = game.updateCell(row, column, value);
        String updatedBoard = game.getDisplayBoard();
    
        // If the move was valid, update the leaderboard
        if (evalType == Response.EvalType.UPDATE) {
            updateLeaderboard(name, game.getPoints()); // Update the leaderboard with the player's name and points
        }
    
        return Response.newBuilder()
                .setResponseType(Response.ResponseType.PLAY)
                .setType(evalType)
                .setBoard(updatedBoard)
                .setMessage(evalType == Response.EvalType.UPDATE ? "Move processed" : "Invalid move")
                .build();
    }

    private void updateLeaderboard(String playerName, int points) {
        leaderboard.compute(playerName, (key, entry) -> {
            if (entry == null) {
                return new LeaderboardEntry(playerName, points, 1);
            } else {
                entry.addPoints(points);
                entry.incrementLogins();
                return entry;
            }
        });
    
        // Save the leaderboard to file after every update
        saveLeaderboardToFile();
    }
    
    public static void saveLeaderboardToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("leaderboard.dat"))) {
            oos.writeObject(leaderboard);
            System.out.println("Leaderboard saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving leaderboard: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void loadLeaderboardFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("leaderboard.dat"))) {
            leaderboard = (ConcurrentHashMap<String, LeaderboardEntry>) ois.readObject();
            System.out.println("Leaderboard loaded from file.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing leaderboard file found. Starting fresh.");
            leaderboard = new ConcurrentHashMap<>();
        }
    }
    
    

    private Response clearRequest(Request op) throws IOException {
        System.out.println("Clear request received");
    
        // Validate that the request includes required fields
        if (!op.hasRow() || !op.hasColumn() || !op.hasValue()) {
            return error(1, "Clear fields (row, column, value) are missing");
        }
    
        int row = op.getRow();
        int column = op.getColumn();
        int value = op.getValue();
    
        // Call clearCell to process the clear operation
        Response.ResponseType resultType = game.clearCell(row, column, value);
    
        if (resultType == Response.ResponseType.ERROR) { // Handle invalid operations
            return Response.newBuilder()
                    .setResponseType(Response.ResponseType.ERROR)
                    .setMessage("Invalid clear operation. Please check the inputs (row, column, value).")
                    .build();
        }
    
        // If successful, return the updated board and success message
        String updatedBoard = game.getDisplayBoard();
    
        return Response.newBuilder()
                .setResponseType(resultType) // This will be ResponseType.PLAY for successful clears
                .setBoard(updatedBoard)
                .setMessage("Clear operation processed successfully.")
                .build();
    }    
    
    
    
    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <grading(boolean)>");
            System.exit(1);
        }
    
        int port = 8000;
        grading = Boolean.parseBoolean(args[1]);
        final ServerSocket[] serverSocket = {null};
    
        // Load the leaderboard from file during server startup
        loadLeaderboardFromFile();
    
        try {
            port = Integer.parseInt(args[0]);
            serverSocket[0] = new ServerSocket(port);
            System.out.println("Server started on port " + port + "...");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    
        ExecutorService executor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            try {
                if (serverSocket[0] != null && !serverSocket[0].isClosed()) {
                    serverSocket[0].close();
                }
                saveLeaderboardToFile(); // Save leaderboard before shutdown
            } catch (IOException e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
            executor.shutdown();
        }));
    
        int id = 1;
        while (true) {
            try {
                Socket clientSocket = serverSocket[0].accept();
                System.out.println("Attempting to connect to client-" + id);
                Game game = new Game();
                SockBaseServer server = new SockBaseServer(clientSocket, game, id++);
                executor.execute(() -> {
                    try {
                        server.startGame();
                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }    
    
    
}
