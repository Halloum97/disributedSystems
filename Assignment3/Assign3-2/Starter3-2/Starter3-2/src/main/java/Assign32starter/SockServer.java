package Assign32starter;

import java.net.*;
import java.util.Base64;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import org.json.*;

public class SockServer {
    static Stack<String> imageSource = new Stack<>();
    private static Map<String, Integer> leaderboard = new HashMap<>();
    private static final int MAX_HINTS = 4; // number of hints per Wonder
    private static int roundsLeft = 0;
    private static int currentRound = 1;
    private static final String[] images = {
        "img/Colosseum1.png", "img/GrandCanyon1.png", "img/Stonehenge1.png",
        "img/Colosseum2.png", "img/GrandCanyon2.png", "img/Stonehenge2.png"
    };

    public static void main(String[] args) {
        try (ServerSocket serv = new ServerSocket(8888)) {
            System.out.println("Server ready for connection");

            while (true) {
                try (Socket sock = serv.accept()) {
                    ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                    PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

                    String input;
                    JSONObject response;
                    String name = "";
                    int points = 0;

                    while ((input = (String) in.readObject()) != null) {
                        JSONObject request = new JSONObject(input);
                        response = new JSONObject();

                        String type = request.optString("type", "unknown");

                        switch (type) {
                            case "hello":
                                response.put("type", "greeting");
                                response.put("message", "Hello! Please enter your name and age.");
                                sendResponse(out, response);
                                break;

                            case "name":
                                name = request.getString("name");
                                int age = request.optInt("age", -1);
                                if (age != -1) {
                                    response.put("type", "welcome");
                                    response.put("message", "Welcome, " + name + "! You are now ready to play.");
                                } else {
                                    response.put("type", "error");
                                    response.put("message", "Invalid age format.");
                                }
                                sendResponse(out, response);
                                break;

                            case "rounds":
                                roundsLeft = request.getInt("rounds");
                                response.put("type", "start_game");
                                response.put("message", "Game started with " + roundsLeft + " rounds.");
                                sendImageForRound(out, currentRound);
                                sendResponse(out, response);
                                break;

                            case "guess":
                                String guess = request.getString("guess");
                                boolean isCorrect = guess.equalsIgnoreCase("correctAnswer"); // Placeholder answer
                                if (isCorrect) {
                                    points += 10;
                                    currentRound++;
                                    roundsLeft--;
                                    response.put("type", "correct_guess");
                                    response.put("message", "Correct! Starting next round.");
                                    if (roundsLeft > 0) {
                                        sendImageForRound(out, currentRound);
                                    } else {
                                        response.put("type", "game_over");
                                        response.put("message", "Game over! Your score: " + points);
                                    }
                                } else {
                                    response.put("type", "incorrect_guess");
                                    response.put("message", "Incorrect guess. Try again.");
                                }
                                sendResponse(out, response);
                                break;

                            case "skip":
                                response.put("type", "skip_round");
                                response.put("message", "Skipping to the next Wonder.");
                                currentRound++;
                                roundsLeft--;
                                if (roundsLeft > 0) {
                                    sendImageForRound(out, currentRound);
                                } else {
                                    response.put("type", "game_over");
                                    response.put("message", "Game over! Your score: " + points);
                                }
                                sendResponse(out, response);
                                break;

                            case "next":
                                response.put("type", "next_hint");
                                response.put("message", "Here's another hint.");
                                sendImageForRound(out, currentRound); // Send next hint for current image
                                sendResponse(out, response);
                                break;

                            case "remaining":
                                response.put("type", "remaining_hints");
                                response.put("message", "You have " + MAX_HINTS + " hints remaining.");
                                sendResponse(out, response);
                                break;

                            case "leaderboard":
                                response.put("type", "leaderboard");
                                response.put("message", "Leaderboard: " + leaderboard);
                                sendResponse(out, response);
                                break;

                            case "quit":
                                response.put("type", "goodbye");
                                response.put("message", "Thanks for playing, " + name + "! Your score: " + points);
                                updateLeaderboard(name, points);
                                sendResponse(out, response);
                                return;

                            default:
                                response.put("type", "error");
                                response.put("message", "Unknown command: " + type);
                                sendResponse(out, response);
                                break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendImageForRound(PrintWriter out, int round) throws Exception {
        JSONObject response = new JSONObject();
        response.put("type", "image");
        
        if (round - 1 < images.length) {
            sendImg(images[round - 1], response);
            response.put("message", "Here's an image for round " + round);
        } else {
            response.put("message", "No more images available.");
        }

        sendResponse(out, response);
    }

    private static void sendResponse(PrintWriter out, JSONObject response) {
        out.println(response.toString());
        out.flush();
    }

    private static void updateLeaderboard(String name, int points) {
        leaderboard.put(name, Math.max(leaderboard.getOrDefault(name, 0), points));
    }

    public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
        File file = new File(filename);
        if (file.exists()) {
            BufferedImage img = ImageIO.read(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            String encodedImage = Base64.getEncoder().encodeToString(baos.toByteArray());
            obj.put("image", encodedImage);
        } else {
            obj.put("image", "Image not found");
        }
        return obj;
    }
}


