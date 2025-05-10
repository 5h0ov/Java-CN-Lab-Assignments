import java.io.*;
import java.net.*;
import java.util.*;

class ThreadHandler extends Thread {
    Socket socket;
    int clientId;

    // constructor to initialize socket and client ID for this client thread
    ThreadHandler(Socket s, int id) {
        socket = s;
        clientId = id;
    }

    public void run() {
        try {
            // set up input and output streams
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            
            pw.println("Connected! Type 'calculate x + y' for calculations or 'quit' to exit");
            pw.flush();

            String input;
            while ((input = br.readLine()) != null) {
                if (input.equals("quit")) {
                    pw.println("Goodbye!");
                    pw.flush();
                    break;
                }

                // check if it's a calculation request
                // an efficient method to differentiate between normal messages and calculation requests
                if (input.startsWith("calculate ")) {
                    // extract the calculation part (remove "calculate ")
                    String calculation = input.substring(10).trim();
                    
                    // parse according to format and calculate
                    try {
                        String[] parts = calculation.split(" "); // split by space
                        if (parts.length != 3) { // must have 3 parts: number, operator, number
                            pw.println("Format: calculate number operator number");
                        } else { // calculate the result

                            // parse the numbers and operator
                            double num1 = Double.parseDouble(parts[0]);
                            String op = parts[1];
                            double num2 = Double.parseDouble(parts[2]);
                            double result = 0;
                            
                            // switch case for different customizable operators
                            switch (op) {
                                case "+": result = num1 + num2; break;
                                case "-": result = num1 - num2; break;
                                case "*": result = num1 * num2; break;
                                case "/": 
                                    if (num2 == 0) {
                                        pw.println("Error: Division by zero");
                                        pw.flush();
                                        continue;
                                    }
                                    result = num1 / num2; 
                                    break;
                                case "%": 
                                    if (num2 == 0) {
                                        pw.println("Error: Division by zero");
                                        pw.flush();
                                        continue;
                                    }
                                    result = num1 % num2; 
                                    break;
                                default:
                                    pw.println("Invalid operator. Use +, -, *, /, %");
                                    pw.flush();
                                    continue;
                            }
                            pw.println("Result: " + result);
                        }
                    } catch (Exception e) {
                        pw.println("Error: " + e.getMessage());
                    }
                } else {
                    // Normal message
                    System.out.println("Client " + clientId + ": " + input);
                    pw.println("Message received");
                }
                
                pw.flush();
            }
        } catch (Exception e) {
            System.out.println("Error for client " + clientId + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            ConcurrentServer.releaseClientId(clientId);
        }
    }
}

public class ConcurrentServer {
    private static int nextId = 1;
    private static List<Integer> freeIds = new ArrayList<>(); // store free IDs for better overall tracking

    // get a new client ID
    public static synchronized int getClientId() {
        if (freeIds.isEmpty()) {
            return nextId++;
        }
        return freeIds.remove(0);
    }

    public static synchronized void releaseClientId(int id) {
        freeIds.add(id);
        Collections.sort(freeIds);
        System.out.println("Client " + id + " disconnected. Free IDs: " + freeIds);
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(2001);
            System.out.println("Server started on port 2001");

            while (true) {
                Socket socket = serverSocket.accept();
                int clientId = getClientId();
                System.out.println("New client connected: " + clientId);
                new ThreadHandler(socket, clientId).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
}