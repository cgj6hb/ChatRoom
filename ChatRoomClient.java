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

    static Socket socket = null;
    static BufferedReader inputStream = null;
    static PrintStream outputStream = null;
    static BufferedReader inputLine = null;

    String line = null;
    String lineArray[] = null;
    String command = null;

    public static void main(String[] args) {
        System.out.println("My chat room client. Version One.");

        startServer();

        listen();

        cleanUp();
     }

     public static void startServer() {

         try {
             socket = new Socket("localhost", 19710);
             inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             outputStream = new PrintStream(socket.getOutputStream());
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

         if (socket != null && inputStream != null && outputStream != null && inputLine != null) {

             try {
                 String responseLine;

                 while (true) {
                     // Prompt
                     System.out.printf("> ");

                     // Send user input to server
                     outputStream.println(inputLine.readLine());

                     responseLine = inputStream.readLine();
                     System.out.println(responseLine);
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
