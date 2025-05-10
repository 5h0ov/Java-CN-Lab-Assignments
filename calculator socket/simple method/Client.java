import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 2001);
            System.out.println("Connected to server");

            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOutput = new PrintWriter(socket.getOutputStream());
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Server: " + serverInput.readLine());

            String input;
            while (true) {
                System.out.print("> ");
                input = userInput.readLine();
                
                if (input == null || input.equalsIgnoreCase("quit")) {
                    serverOutput.println("quit");
                    serverOutput.flush();
                    System.out.println("Server: " + serverInput.readLine());
                    break;
                }

                // send the user input to the server
                serverOutput.println(input);
                serverOutput.flush();
                
                // receive the server's response
                String response = serverInput.readLine();
                System.out.println("Server: " + response);
            }

            socket.close();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}