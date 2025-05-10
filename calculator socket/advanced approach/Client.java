import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 2001);
        System.out.println("Got connected...");

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String msg1 = br.readLine();
        System.out.println("server says: " + msg1);

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println("Hello Server");
        pw.flush();

        BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String msg = br1.readLine();

            if (msg.equals("quit")) {
                pw.println("quit");
                pw.flush();
                System.out.println(br.readLine());
                break;
            }

            if (msg.equals("calculator")) {
                // enter calculator mode
                pw.println("calculator");
                pw.flush();
                String response = br.readLine();
                System.out.println(response);

                // handle calculator interactions with the server
                while (true) {
                    System.out.print("calc> ");
                    String calculatorInput = br1.readLine();
                    if (calculatorInput.equals("exit") 
                        || calculatorInput.equals("quit")) 
                    {
                        pw.println("exit");
                        pw.flush();
                        System.out.println(br.readLine()); // exit calculator mode
                        break;

                    }
                    pw.println(calculatorInput);
                    pw.flush();
                    String result = br.readLine();
                    System.out.println(result);
                }
            } else {
                pw.println(msg);
                pw.flush();
            }
        }

        socket.close();
    }
}
