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

    public static void main(String[] args) {

        Socket socket = null;
        BufferedReader inputStream = null;
        PrintStream outputStream = null;
        BufferedReader inputLine = null;

        try {
            socket = new Socket("localhost", 19710);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintStream(socket.getOutputStream());
            inputLine = new BufferedReader(new InputStreamReader(System.in));
        } catch (UnknownHostException uhe) {
            System.err.println(uhe.getMessage());
        } catch (SecurityException se) {
            System.err.println(se.getMessage());
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        if (socket != null && inputStream != null && outputStream != null && inputLine != null) {

            System.out.println("Client is up...");

            try {
                String inputString;
                outputStream.println("1: " + inputLine.readLine());

                while((inputString = inputLine.readLine()) != null) {
                    System.out.println("2: " + inputString);
                    outputStream.println("3: " + inputLine.readLine());
                }
            } catch (NoSuchElementException nsee) {
                System.err.println(nsee.getMessage());
            } catch (IllegalStateException ise) {
                System.err.println(ise.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }

            try {
                outputStream.close();
                inputStream.close();
                inputLine.close();
                socket.close();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }
}
