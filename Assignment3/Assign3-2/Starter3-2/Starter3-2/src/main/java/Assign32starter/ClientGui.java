package Assign32starter;

import java.awt.Dimension;
import org.json.JSONObject;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

public class ClientGui implements Assign32starter.OutputPanel.EventHandlers {
    JDialog frame;
    PicturePanel picPanel;
    OutputPanel outputPanel;
    String currentMessage;

    Socket sock;
    OutputStream out;
    ObjectOutputStream os;
    BufferedReader bufferedReader;

    String host = "localhost";
    int port = 8888;

    public ClientGui(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        frame = new JDialog();
        frame.setLayout(new GridBagLayout());
        frame.setMinimumSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        picPanel = new PicturePanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.25;
        frame.add(picPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.75;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        outputPanel = new OutputPanel();
        outputPanel.addEventHandlers(this);
        frame.add(outputPanel, c);

        picPanel.newGame(1);
        insertImage("img/Colosseum1.png", 0, 0);

        open();
        currentMessage = "{\"type\": \"hello\"}"; // Initial "hello" message to the server
        sendRequest(currentMessage);

        receiveResponse();
        close();
    }

    public void show(boolean makeModal) {
        frame.pack();
        frame.setModal(makeModal);
        frame.setVisible(true);
    }

    public void newGame(int dimension) {
        picPanel.newGame(dimension);
        outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
    }

    public boolean insertImage(String filename, int row, int col) throws IOException {
        String error = "";
        try {
            if (picPanel.insertImage(filename, row, col)) {
                outputPanel.appendOutput("Inserted " + filename + " at (" + row + ", " + col + ")");
                return true;
            }
            error = "File(\"" + filename + "\") not found.";
        } catch (PicturePanel.InvalidCoordinateException e) {
            error = e.toString();
        }
        outputPanel.appendOutput(error);
        return false;
    }

	@Override
	public void submitClicked() {
		try {
			open();
			String input = outputPanel.getInputText().trim();
			JSONObject request = new JSONObject();
	
			// Check for specific commands
			if (input.matches("\\d+")) { // If input is a number, assume it's for rounds
				request.put("type", "rounds");
				request.put("rounds", Integer.parseInt(input));
			} else if (input.equalsIgnoreCase("skip")) {
				request.put("type", "skip");
			} else if (input.equalsIgnoreCase("next")) {
				request.put("type", "next");
			} else if (input.equalsIgnoreCase("remaining")) {
				request.put("type", "remaining");
			} else if (input.equalsIgnoreCase("leaderboard")) {
				request.put("type", "leaderboard");
			} else if (input.equalsIgnoreCase("quit")) {
				request.put("type", "quit");
			} else if (input.toLowerCase().startsWith("name:")) { // For setting name and age
				String[] parts = input.split(" ");
				if (parts.length >= 4) {
					String name = parts[1];
					String age = parts[3];
					request.put("type", "name");
					request.put("name", name);
					request.put("age", Integer.parseInt(age));
				} else {
					outputPanel.appendOutput("Invalid format. Use 'name: [name] age: [age]'.");
					return;
				}
			} else { // Default case assumes it's a guess
				request.put("type", "guess");
				request.put("guess", input);
			}
	
			sendRequest(request.toString());
			receiveResponse();
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	

    @Override
    public void inputUpdated(String input) {
        if (input.equals("surprise")) {
            outputPanel.appendOutput("You found me!");
        }
    }

    private void sendRequest(String message) throws IOException {
        os.writeObject(message);
        os.flush();
    }

    private void receiveResponse() throws IOException {
    String responseStr = bufferedReader.readLine();
    JSONObject response = new JSONObject(responseStr);

    String type = response.optString("type", "unknown");
    switch (type) {
        case "greeting":
        case "welcome":
        case "menu":
        case "start_game":
        case "correct_guess":
        case "incorrect_guess":
        case "skip_round":
        case "next_hint":
        case "remaining_hints":
        case "leaderboard":
        case "goodbye":
            outputPanel.appendOutput(response.optString("message", "No message"));
            break;

        case "error":
            outputPanel.appendOutput("Error: " + response.optString("message", "Unknown error"));
            break;

        case "image":
            String imageEncoded = response.optString("image", "");
            if (!imageEncoded.isEmpty()) {
                displayDecodedImage(imageEncoded);
            } else {
                outputPanel.appendOutput("No image data received.");
            }
            break;

        default:
            outputPanel.appendOutput("Unknown response type received.");
   		}
	}

private void displayDecodedImage(String imageEncoded) {
    try {
        byte[] imageBytes = Base64.getDecoder().decode(imageEncoded);
        ByteArrayInputStream is = new ByteArrayInputStream(imageBytes);
        
        // Display the decoded image in the top-left cell (0, 0)
        picPanel.insertImage(is, 0, 0);
        outputPanel.appendOutput("Image received and displayed.");
    } catch (PicturePanel.InvalidCoordinateException e) {
        outputPanel.appendOutput("Error: Invalid coordinates for image display.");
    } catch (Exception e) {
        outputPanel.appendOutput("Error displaying image: " + e.getMessage());
    }
}


    public void open() throws UnknownHostException, IOException {
        this.sock = new Socket(host, port);
        this.out = sock.getOutputStream();
        this.os = new ObjectOutputStream(out);
        this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (bufferedReader != null) bufferedReader.close();
            if (sock != null) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            String host = "localhost";
            int port = 8888;

            ClientGui main = new ClientGui(host, port);
            main.show(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
