
# Wonder of the World Guessing Game

## Project Description
This project is a client-server application for a single-player game in which the client guesses famous Wonders of the World based on images provided by the server. The server hosts a collection of images of famous landmarks and sends these images to the client one at a time for each round of the game. The client can guess the name of the Wonder, request hints, skip to the next Wonder, and check how many hints remain. The game supports multiple rounds, and at the end of the game, the player’s score is displayed, along with a leaderboard showing high scores.

## Checklist of Requirements
- [x] **Display the picture grid and output messages to the client.**
- [x] **Handle user commands: name, rounds, guess, skip, next, remaining, leaderboard, quit.**
- [x] **Use JSON protocol for structured client-server communication.**
- [x] **Server sends images based on rounds and manages hints.**
- [x] **Leaderboard tracking and persistence of high scores.**
- [ ] **Implement error handling for invalid commands and unexpected inputs.**

## Protocol Description
The client and server communicate using a JSON-based protocol. Each request from the client includes a `"type"` field that specifies the type of command. Here are the details of each request and possible responses.

### Requests and Responses
1. **hello**
   - **Request**: `{ "type": "hello" }`
   - **Response**: `{ "type": "greeting", "message": "Hello! Please enter your name and age." }`

2. **name**
   - **Request**: `{ "type": "name", "name": "Joe", "age": 15 }`
   - **Response**: `{ "type": "welcome", "message": "Welcome, Joe! You are now ready to play." }`

3. **rounds**
   - **Request**: `{ "type": "rounds", "rounds": 3 }`
   - **Response**: `{ "type": "start_game", "message": "Game started with 3 rounds." }`
   - **Additional Response**: Sends an image for the first round.

4. **guess**
   - **Request**: `{ "type": "guess", "guess": "Colosseum" }`
   - **Response (correct)**: `{ "type": "correct_guess", "message": "Correct! Starting next round." }`
   - **Response (incorrect)**: `{ "type": "incorrect_guess", "message": "Incorrect guess. Try again." }`
   - **Additional Response**: Sends a new image if the guess was correct.

5. **skip**
   - **Request**: `{ "type": "skip" }`
   - **Response**: `{ "type": "skip_round", "message": "Skipping to the next Wonder." }`
   - **Additional Response**: Sends an image for the next round.

6. **next**
   - **Request**: `{ "type": "next" }`
   - **Response**: `{ "type": "next_hint", "message": "Here's another hint." }`
   - **Additional Response**: Sends a new hint image for the current round.

7. **remaining**
   - **Request**: `{ "type": "remaining" }`
   - **Response**: `{ "type": "remaining_hints", "message": "You have X hints remaining." }`

8. **leaderboard**
   - **Request**: `{ "type": "leaderboard" }`
   - **Response**: `{ "type": "leaderboard", "message": "Leaderboard: {...}" }`

9. **quit**
   - **Request**: `{ "type": "quit" }`
   - **Response**: `{ "type": "goodbye", "message": "Thanks for playing, Joe! Your score: 30" }`

10. **Errors**
    - **Response**: `{ "type": "error", "message": "Unknown command: [type]" }`
    - Sent if an unrecognized or invalid command is received.



## Program Design for Robustness
The program is designed to handle unexpected inputs and errors gracefully:
- **Error Handling**: The server checks for missing or incorrect fields in requests and responds with appropriate error messages without crashing.
- **Structured Protocol**: By using a JSON-based protocol with clearly defined request and response types, the server ensures that all communication is standardized, reducing the chance of errors.
- **Command Validation**: The server checks each command type and validates parameters (e.g., name, age, guess) before proceeding with further processing.

## Changes Needed for UDP Protocol
If we were to change from TCP to UDP, we would need to address the following:
- **Reliability**: Unlike TCP, UDP does not guarantee that packets are delivered in order or even delivered at all. We would need to implement custom mechanisms for acknowledging received packets and handling retries for lost packets.
- **Connectionless Protocol**: UDP is connectionless, so we would need to identify and track clients based on their IP and port, which would add complexity to the server.
- **Error Handling and Packet Loss**: Additional logic would be required to detect and handle packet loss, since UDP does not ensure data integrity.