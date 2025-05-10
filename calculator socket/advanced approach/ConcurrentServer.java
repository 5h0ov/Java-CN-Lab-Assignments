// The following code has been heavily commented for my future reference and understanding. The comments explain the purpose of each block of code and the changes made to the original code provided in the assignment. This might help you understand the code better as well.

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

// threadHandler handles each client connection on a separate thread
class ThreadHandler extends Thread {
    Socket newsocket;
    int n; // client id
    FileWriter fileWriter = null; // for client-specific calculator log file
    BufferedWriter bufferedWriter = null; // for client-specific calculator log file
    boolean clientCalcLogCreated = false; // flag to check if the client's calculator log file is created

    // constructor to initialize the client socket and id (here, n)
    ThreadHandler(Socket s, int v) {
        newsocket = s;
        n = v;
    }

    public void run() {
        try {
            // create input and output streams for client communication
            // bufferedReader to read messages from the client
            BufferedReader br = new BufferedReader(new InputStreamReader(newsocket.getInputStream()));
            // printWriter to send messages to the client
            PrintWriter pw = new PrintWriter(newsocket.getOutputStream());
            pw.println("Hello client " + n + ". Enter 'quit' to exit or 'calculator' to enter calculator mode.");
            pw.flush();

            // Note: The client-specific calculations file will be created on demand when
            // the client enters calculator mode

            String clientInput;
            boolean inCalculatorMode = false;

            // main loop to handle client messages
            while (true) { // or do for(;;)
                clientInput = br.readLine(); // read message from client

                if (clientInput.equals("quit")) {
                    if (inCalculatorMode) {
                        pw.println("Exiting calculator mode.");
                        pw.flush();
                    }
                    pw.println("Goodbye client");
                    pw.flush();
                    break;
                }

                // enter calculator mode on demand
                if (clientInput.equals("calculator") && !inCalculatorMode) {
                    // upon entering calculator mode, create the client-specific calculator log file
                    // only once
                    if (!clientCalcLogCreated) {
                        fileWriter = new FileWriter("client" + n + "_calculations.txt", true);
                        bufferedWriter = new BufferedWriter(fileWriter);
                        clientCalcLogCreated = true;
                    }
                    pw.println("Calculator Mode On. Type 'exit' to exit calculator.");
                    pw.flush();
                    inCalculatorMode = true;
                    continue;
                }

                if (inCalculatorMode) {
                    // process arithmetic expression in calculator mode
                    if (clientInput.equals("exit")) {
                        pw.println("Exiting calculator mode.");
                        pw.flush();
                        inCalculatorMode = false;
                    } else {
                        // calculate the result of the expression
                        String result = calculateExpression(clientInput);
                        pw.println("Result: " + result);
                        pw.flush();

                        // log calculations to client-specific log file if created
                        if (clientCalcLogCreated) {
                            bufferedWriter.write(clientInput + " = " + result + "\n");
                            bufferedWriter.flush();
                        }

                        // append calculations to the global log file maintained by the server
                        ConcurrentServer.appendToGlobalLog("Client " + n + ": " + clientInput + " = " + result);
                    }
                } else {
                    // regular message from the client (non-calculator mode)
                    System.out.println("client no " + n + " says: " + clientInput);
                }
            }
        } catch (Exception e) { // catches any exception that may arise including CTRL + C
            System.out.println(e);
        } finally {
            // Performing Clean Up: close client-specific log files if they were opened
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing file writers: " + e.getMessage());
            }
            // release the client id so it can be reused for future connections
            ConcurrentServer.releaseClientId(n);
        }
    }

    // method to evaluate expressions with multiple operations
    private String calculateExpression(String expression) {
        try {
            // first try to match unary functions pattern
            Pattern unaryPattern = Pattern.compile("([a-zA-Z]+)\\s*\\(\\s*(-?\\d+\\.?\\d*)\\s*\\)");
            Matcher unaryMatcher = unaryPattern.matcher(expression);
            // the above regex pattern matches functions like sqrt(25), log(10), sin(30),
            // etc.

            if (unaryMatcher.matches()) {
                // process unary function
                String functionName = unaryMatcher.group(1).toLowerCase();
                double num = Double.parseDouble(unaryMatcher.group(2));
                double result = 0;

                // switch case for custom function
                switch (functionName) {
                    case "sqrt":
                        result = Math.sqrt(num);
                        break;
                    case "cbrt":
                        result = Math.cbrt(num);
                        break;
                    case "log":
                        result = Math.log(num);
                        break;
                    case "exp":
                        result = Math.exp(num);
                        break;
                    case "sin":
                        result = Math.sin(num);
                        break;
                    case "cos":
                        result = Math.cos(num);
                        break;
                    case "tan":
                        result = Math.tan(num);
                        break;
                    case "asin":
                        result = Math.asin(num);
                        break;
                    case "acos":
                        result = Math.acos(num);
                        break;
                    case "atan":
                        result = Math.atan(num);
                        break;
                    case "abs":
                        result = Math.abs(num);
                        break;
                    case "ceil":
                        result = Math.ceil(num);
                        break;
                    case "floor":
                        result = Math.floor(num);
                        break;
                    case "round":
                        // Note: Math.round returns a long, which is fine for display purposes.
                        result = Math.round(num);
                        break;
                    default:
                        return "Invalid function";
                }
                return String.valueOf(result);
            } else {
                // handle multi-operation expressions like 2+3*4-5/2, etc
                return evaluateArithmeticExpression(expression);
            }
        } catch (Exception e) {
            return "Error processing expression: " + e.getMessage();
        }
    }

    // method to evaluate arithmetic expressions with multiple operations
    private String evaluateArithmeticExpression(String expression) {
        try {
            // remove all spaces for better user experience
            expression = expression.replaceAll("\\s+", "");

            // check if expression is valid via regex pattern matching
            if (!expression.matches("^-?\\d+(\\.\\d+)?([-+*/%^]-?\\d+(\\.\\d+)?)*$")) {
                return "Invalid expression format";
            }

            // tokenize the expression - separate numbers and operators
            List<String> tokens = new ArrayList<>();
            StringBuilder currentNumber = new StringBuilder();
            // StringBuilder is used to efficiently build strings without creating new
            // string objects each time

            for (int i = 0; i < expression.length(); i++) {
                char c = expression.charAt(i);

                // if it's a digit, decimal point, or negative sign at the start of a number
                // then add to currentNumber
                if (Character.isDigit(c) || c == '.' ||
                        (c == '-' && (i == 0 || "+-*/%^".indexOf(expression.charAt(i - 1)) >= 0))) {
                    currentNumber.append(c);
                } else if ("+-*/%^".indexOf(c) >= 0) {
                    // add the completed number to tokens
                    if (currentNumber.length() > 0) {
                        tokens.add(currentNumber.toString());
                        currentNumber = new StringBuilder();

                        // finally it should look like this: 2 + 3 * 4 - 5 / 2
                    }
                    // add the operator
                    tokens.add(String.valueOf(c));
                }
            }

            // add the last number if it exists
            if (currentNumber.length() > 0) {
                tokens.add(currentNumber.toString());
            }

            // for loop to evaluate the expression based on operator precedence
            // first handle high precedence operators (* / % ^)
            for (int i = 1; i < tokens.size() - 1; i += 2) {
                String operator = tokens.get(i);

                // handle high precedence operators (* / % ^)
                if (operator.equals("*") || operator.equals("/") ||
                        operator.equals("%") || operator.equals("^")) {

                    // get the left and right operands
                    double left = Double.parseDouble(tokens.get(i - 1));
                    double right = Double.parseDouble(tokens.get(i + 1));
                    double result = 0;

                    // perform the operation based on the operator
                    switch (operator) {
                        case "*":
                            result = left * right;
                            break;
                        case "/":
                            if (right == 0)
                                return "Error: Division by zero";
                            result = left / right;
                            break;
                        case "%":
                            result = left % right;
                            break;
                        case "^":
                            result = Math.pow(left, right);
                            break;
                    }

                    // replace the three tokens with the result
                    tokens.set(i - 1, String.valueOf(result));
                    tokens.remove(i);
                    tokens.remove(i);
                    i -= 2; // adjust to check from the current position again
                }
            }

            // then handle low precedence operators (+ -)
            double result = Double.parseDouble(tokens.get(0));
            for (int i = 1; i < tokens.size(); i += 2) {
                String operator = tokens.get(i);
                double right = Double.parseDouble(tokens.get(i + 1));

                switch (operator) {
                    case "+":
                        result += right;
                        break;
                    case "-":
                        result -= right;
                        break;
                }
            }

            return String.valueOf(result);
        } catch (Exception e) {
            return "Error evaluating expression: " + e.getMessage();
        }
    }
}

// main server class that listens for incoming connections and assigns client
// ids
public class ConcurrentServer {
    // next new client id to assign if no freed ids are available
    private static int nextClientId = 1;
    // list to store freed client ids that can be reused when clients disconnect
    private static List<Integer> freeClientIds = new ArrayList<>();

    // for the following methods, synchronized is used to ensure thread safety as
    // multiple threads may access these methods concurrently if synchronized is not
    // used. This prevents race conditions and ensures that the shared resources are
    // accessed in a controlled manner.

    // synchronized method to get a client id, reusing freed ids if available
    public synchronized static int getClientId() {
        if (freeClientIds.isEmpty()) {
            return nextClientId++;
        } else {
            // remove and return the lowest available client id
            return freeClientIds.remove(0);
        }
    }

    // synchronized method to release a client id when a client disconnects
    public synchronized static void releaseClientId(int id) {
        freeClientIds.add(id);
        Collections.sort(freeClientIds); // ensure the lowest numbers are reused first by sorting the list
        System.out.println("Released client id: " + id + ". Available ids: " + freeClientIds);
    }

    // synchronized method to append calculation entries to the global log file
    public synchronized static void appendToGlobalLog(String logEntry) {
        // global log file "all_calculations.txt" stores all client calculations
        try (FileWriter fw = new FileWriter("all_calculations.txt", true);
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry + "\n");
            bw.flush();
        } catch (IOException e) {
            System.out.println("Error writing to global log file: " + e.getMessage());
        }
    }

    public static void main(String args[]) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(2001);
            System.out.println("Server started on port 2001.");

            // listen indefinitely for client connections
            while (true) {
                Socket newsocket = serverSocket.accept();
                // get a client id, reusing freed ids if possible
                int clientId = getClientId();
                System.out.println("Creating thread for client " + clientId);
                ThreadHandler t = new ThreadHandler(newsocket, clientId);
                t.start();
                System.out.println("Running thread for client " + clientId);
            }
        } catch (IOException e) {
            System.err.println("Exception caught while listening for connections: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close(); // to properly close the server socket without intereferring with the client
                                          // connections (the while loop)
                    System.out.println("ServerSocket closed.");
                } catch (IOException e) {
                    System.err.println("Error closing ServerSocket: " + e.getMessage());
                }
            }
        }
    }
}
