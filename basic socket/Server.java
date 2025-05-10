/*import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; */

import java.net.*;
import java.io.*;



public class Server{
  public static void main(String [] args) throws Exception{
	ServerSocket serverSocket = new ServerSocket(2001);
//serverSocket is initiated as server side socket with port number 2001 
//if 2001 port is already busy by other process then exception occurs
	System.out.println("Server is listening...");
        // msg of server process starting

	Socket socket = serverSocket.accept();	
// socket object created when a client's connection request is accepted by serverSocket
	System.out.println("Connected... :)");
// And cliernt and server connected

	BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
// br gets input from socket which the socket got from client side
	PrintWriter pw = new PrintWriter(socket.getOutputStream());
     // pw holds output of server side socket
	
	
        
    pw.println("Hello Client...");//pw print the message to client
	pw.flush();// pw's buffer cleared
      
                                                    
       
	String line = br.readLine();// line gets string from br i.e from client
	System.out.println("client says: " + line);// line's value printed

      


    String msg1=br.readLine();//again msg1 gets string from br i.e from client
	System.out.println("client says: " + msg1);// the client's message printed

       
	//socket.close();
	//serverSocket.close();
		
  }
}

