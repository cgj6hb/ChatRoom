import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

public class ChatRoomServer {

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader inputStream = null;
        PrintStream outputStream = null;
        String inputString;

        try {
            serverSocket = new ServerSocket(19710);
        } catch (SecurityException se) {
            System.err.println(se.getMessage());
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        System.out.println("Server is up...");

        try {
            clientSocket = serverSocket.accept();
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                inputString = inputStream.readLine();
                outputStream.println("From server: " + inputString);
            }
        } catch (SocketTimeoutException ste) {
            System.err.println(ste.getMessage());
        } catch (IllegalBlockingModeException ibme) {
            System.err.println(ibme.getMessage());
        } catch (SecurityException se) {
            System.err.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
