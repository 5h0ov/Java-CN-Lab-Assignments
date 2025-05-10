import java.io.*;
import java.net.*;
import java.util.*;

class ThreadHandler extends Thread {
    private Socket socket;
    private int clientId;
    private PrintWriter writer;
    private static List<ThreadHandler> clients = new ArrayList<ThreadHandler>(); // Added explicit type for Java 7
                                                                                 // compatibility

    public ThreadHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    public void run() {
        try {
            // the reader and writer for this client thread
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());

            writer.println("Connected as client #" + clientId + ". Type 'quit' to exit.");
            writer.flush();

            // add this client to the list of connected clients
            synchronized (clients) {
                clients.add(this);
            }

            // custom function broadcast() to announce new client to all users
            broadcast("Client #" + clientId + " has joined the chatroom.", null);

            String message;
            // read messages from client until they quit or disconnect
            while ((message = reader.readLine()) != null) {
                if (message.equals("quit")) {
                    break;
                }

                broadcast("Client #" + clientId + ": " + message, this);

                // logging on server side
                System.out.println("Client #" + clientId + " says: " + message);
            }
        } catch (IOException e) {
            System.out.println("Error in client #" + clientId + ": " + e.getMessage());
        } finally {
            disconnect(); // custom function to handle client disconnection/service interruption
        }
    }

    // send a message to all clients (except the sender if specified for custom
    // messages)
    // currently sender = null
    private void broadcast(String message, ThreadHandler sender) {
        synchronized (clients) {
            for (ThreadHandler client : clients) {
                if (client != sender) {
                    client.writer.println(message);
                    client.writer.flush();
                }
            }
        }
    }

    // custom function to handle client disconnection
    private void disconnect() {
        try {
            // remove from list of connected clients
            synchronized (clients) {
                clients.remove(this);
            }

            // custom function to release client ID from the list of used IDs
            ConcurrentServer.releaseClientId(clientId);

            // notify other clients
            broadcast("Client #" + clientId + " has left the chatroom.", null);

            // close this client's socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            // logging in server side
            System.out.println("Client #" + clientId + " disconnected");
        } catch (IOException e) {
            System.out.println("Error during disconnection: " + e.getMessage());
        }
    }
}

public class ConcurrentServer {
    private static int nextClientId = 1;
    private static List<Integer> freeClientIds = new ArrayList<Integer>();

    // get a client ID (reuse freed IDs if available)
    // this is done to have an optimized server
    public static synchronized int getClientId() {
        if (freeClientIds.isEmpty()) {
            return nextClientId++; // increment the next client ID -> this is gonna be client 1
        } else {
            Collections.sort(freeClientIds);
            return freeClientIds.remove(0); // get the smallest freed client ID if not empty
        }
    }

    // custom function release a client ID when a client disconnects
    public static synchronized void releaseClientId(int id) {
        freeClientIds.add(id); // add this id to the maintained list of free client IDs
        System.out.println("Released client ID: " + id + ". Available IDs: " + freeClientIds);
    }

    // main server function
    public static void main(String args[]) {
        ServerSocket serverSocket = null;

        // try-catch block for efficient error handling of server socket
        try {
            serverSocket = new ServerSocket(2001);
            System.out.println("Chatroom server started on port 2001");

            while (true) {

                Socket clientSocket = serverSocket.accept();

                // get a client ID (reusing freed IDs if available)
                int clientId = getClientId();

                // show client connection on server side with client ID and IP address
                System.out.println(
                        "Client #" + clientId + " connected from " + clientSocket.getInetAddress().getHostAddress());

                // start a new thread for this client
                ThreadHandler clientThread = new ThreadHandler(clientSocket, clientId);
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing server socket: " + e.getMessage());
                }
            }
        }
    }
}