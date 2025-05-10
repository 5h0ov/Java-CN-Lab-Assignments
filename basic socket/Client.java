  

  /* import java.net.Socket;
   import java.io.IOException;
   import java.io.BufferedReader;	
   import java.io.PrintWriter;
   import java.io.InputStreamReader;*/

   import java.net.*;
   import java.io.*;
 	


   public class Client{
	public static void main(String [] args) throws Exception {
                Socket socket = new Socket("localhost",2001);
        //localhost= ip of server (for same machine it is localhost)
        //2001=port number of server assigned by programmer as id of process
                System.out.println("Got connected...");
                // if server already running then client socket would be connected to server socket

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //br gets input from client socket which is connected to server and gets string from server
                                
                PrintWriter pw = new PrintWriter(socket.getOutputStream());	 
                //pw will provide output to client socket and the output would be sent to server
                
                String msg1=br.readLine();// msg1 extracts string from br
                System.out.println("server says: " + msg1);// server's msg printed
              
                
	        pw.println("Hello Server");// pw print the string to server
		pw.flush(); // refreshing PrintWriter's buffer
                           
		
                BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
                // br1 gets input from console
                String msg=br1.readLine();
                // string is stored in msg

                pw.println(msg);//msg sent to server side by printwriter pw
                pw.flush();// refreshing PrintWriter's buffer
                //socket.close();
	}
   }

  