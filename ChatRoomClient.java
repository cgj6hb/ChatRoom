/*
    Chris Jansson
    12399710
    CGJ6HB

    CMP_SC 4850 Lab #3
    ChatRoom Version1
    December 4th, 2017

    Program Description: This is the client program for the ChatRoom application.
    It is responsible for creating a socket connection with the server (localhost)
    and handling user commands.

    Most commands go straight to the server, with the exception of the "exit" command.
    If the user chooses to exit, this client application will close the connection
    and end. The server will continue running, however, so the client can be
    started again and instantly reconnect.
*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.SecurityException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

public class ChatRoomClient {
    // Connection utilities
    static Socket socket = null;
    static BufferedReader inputStream = null;
    static PrintStream outputStream = null;
    static BufferedReader inputLine = null;

    // User input utilities
    String line = null;
    String lineArray[] = null;
    String command = null;

    public static void main(String[] args) {
        System.out.println("My chat room client. Version One. Type \"exit\" to quit");

        // This will create the socket connection and initialize the input and
        // output streams.
        startServer();

        // This is the bulk of the program, and it listens for user commands
        // and sends them to the server.
        listen();

        // This will close the connection and free any resources
        cleanUp();
     }

     public static void startServer() {
         try {
             // Connet to localhost
             socket = new Socket("localhost", 19710);

             // Initialize input and output streams
             inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             outputStream = new PrintStream(socket.getOutputStream());

             // This input stream will collect user input
             inputLine = new BufferedReader(new InputStreamReader(System.in));
         } catch (UnknownHostException uhe) {
             System.err.println(uhe);
         } catch (SecurityException se) {
             System.err.println(se);
         } catch (IllegalArgumentException iae) {
             System.err.println(iae);
         } catch (IOException ioe) {
             System.err.println(ioe);
         }
     }

     public static void listen() {
         // Make sure all necessary resources are allocated and initialized
         if (socket != null && inputStream != null && outputStream != null && inputLine != null) {

             try {
                 String responseLine;
                 String message;

                 while (true) {
                     // Prompt
                     System.out.printf("> ");

                     // Gather user input
                     message = inputLine.readLine();

                     // Send the command to the server
                     outputStream.println(message);

                     // Get and print the server's response
                     responseLine = inputStream.readLine();

                     // If the user wants to exit, the server will send back an 
                     // EXIT_SIGNAL, which notifies the client to exit
                     if (responseLine.equals("EXIT_SIGNAL")) {
                         break;
                     } else {
                         System.out.println(responseLine);
                     }
                 }
             } catch (NoSuchElementException nsee) {
                 System.err.println(nsee);
             } catch (IllegalStateException ise) {
                 System.err.println(ise);
             } catch (IOException ioe) {
                 System.err.println(ioe);
             }
         }
     }

     public static void cleanUp() {
         // Free up any allocated resources
         try {
             inputLine.close();
             outputStream.close();
             inputStream.close();
             socket.close();
         } catch (IOException ioe) {
             System.err.println(ioe);
         }
     }
}
