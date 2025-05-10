import java.net.*;
import java.io.*;

public class Client {
    // flag to track if we need to print a prompt
    private static volatile boolean needPrompt = true;
    
    public static void main(String[] args) throws Exception {
        Socket socket = null;
        
        try {
            // connect to the server socket
            socket = new Socket("localhost", 2001);
            System.out.println("Connected to chatroom server");
            final Socket finalSocket = socket;

            // set up input from the server and console
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // start a separate thread to read messages from the server for the chatroom logic
            // this creates a full-duplex communication system where the client can both send and receive messages without blocking
            // a full-duplex communication system is necessary for a chatroom where multiple clients can send messages at the same time
            // without this separate thread the client would have to alternate between checking for user input and checking for server messages, creating a poor user experience
            Thread serverListener = new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = serverReader.readLine()) != null) {
                            // clear the current line if we're at a prompt
                            if (needPrompt) {
                                System.out.print("\r"); //  \r is used to move cursor to the start of the line, i.e clear the line
                            }
                            
                            // server message
                            System.out.println(message);
                            
                            // prompt after server message
                            if (needPrompt) {
                                System.out.print("> ");
                                System.out.flush();
                            }
                        }
                    } catch (IOException e) {
                        if (!finalSocket.isClosed()) {
                            System.out.println("\rLost connection to server: " + e.getMessage());
                        }
                    }
                }
            });

            // start the server listener thread
            serverListener.setDaemon(true);
            serverListener.start();

            // main loop to read user input and send messages
            String userInput;
            System.out.println("Start typing messages (type 'quit' to exit):");
            
            while (true) {
                // print prompt before each input line
                System.out.print("> ");
                needPrompt = true; // to keep track of prompt status
                
                // reading user input
                userInput = consoleReader.readLine();
                needPrompt = false; // we dont need rpompt while processing input
                
                if (userInput == null) {
                    break; // end of stream
                }
                
                // else, send to server
                serverWriter.println(userInput);
                serverWriter.flush();

                if (userInput.equals("quit")) {
                    break; // exit out of the chatroom loop
                }
                
                // after sending wait for a bit to let server messages come through
                try {
                    Thread.sleep(100); // customizable delay
                } catch (InterruptedException e) {
                    // ignore this exception
                } // error handling
                
                // ready to prompt again
                needPrompt = true;
            }
        } catch (UnknownHostException e) {
            System.out.println("Cannot find server: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            // close the socket when done - best practice
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}