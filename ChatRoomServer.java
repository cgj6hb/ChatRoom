import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;


public class ChatRoomServer {

    static ArrayList<String> users;
    static ArrayList<String> passwords;

    static boolean loggedIn = false;
    static String currentUser = "";

    static ServerSocket serverSocket = null;
    static Socket clientSocket = null;
    static BufferedReader inputStream;
    static PrintStream outputStream;
    static String line;
    static String command;

    static String lineArray[];
    static String loginArray[];

    public static void main(String args[]) {
        startServer();
    }

    public static void startServer() {
        // Load the users and passwords into ArrayLists from the users.txt file
        users = new ArrayList<>();
        passwords = new ArrayList<>();
        refreshUsers();

        initServerSocket();
        System.out.println("Server running...");

        listen();
    }

    public static void refreshUsers() {
        users.clear();
        passwords.clear();

        String filename = "users.txt";
        String line;
        String lineArray[];

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));

            while ((line = bufferedReader.readLine()) != null) {
                lineArray = line.split(":", 2);
                users.add(lineArray[0]);
                passwords.add(lineArray[1]);
            }

            bufferedReader.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void initServerSocket() {

        try {
            serverSocket = new ServerSocket(19710);
        } catch (SecurityException se) {
            System.err.println(se);
        } catch (IllegalArgumentException iae) {
            System.err.println(iae);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public static void listen() {

        try {
            clientSocket = serverSocket.accept();
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                line = inputStream.readLine();

                if (line.contains(" ")) {
                    // First, separate the command from the rest of the line
                    lineArray = line.split(" ", 2);
                    command = lineArray[0];

                    if (command.equals("login")) {
                        handleLogin();
                    } else if (command.equals("newuser")) {
                        handleNewUser();
                    } else if (command.equals("send")) {
                        handleSend();
                    } else if (command.equals("logout")) {
                        handleLogout();
                    } else {
                        outputStream.println("> Server: Invalid command");
                    }
                }
            }
        } catch (SocketTimeoutException ste) {
            System.err.println(ste);
        } catch (IllegalBlockingModeException ibme) {
            System.err.println(ibme);
        } catch (SecurityException se) {
            System.err.println(se);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public static void handleLogin() {

        if (loggedIn == true) {
            outputStream.println("> Server: Another user is already logged in");
        }

        // Check that there is exactly one space in the remaining string.
        // This space should be the delimiter between username and password
        else if (lineArray.length == 2 && lineArray[1].matches("\\w+ \\w+")) {
            loginArray = lineArray[1].split(" ");

            if (loginArray.length != 2) {
                outputStream.println("> Server: Login operation requires 2 arguments");
            } else {
                String userInput = loginArray[0];
                String passwordInput = loginArray[1];

                // Search the array of users to check for a match
                int index = users.indexOf(userInput);

                // If the username exists and the password is correct, log in that user
                // Otherwise, print an error message to the client console
                if (index != -1 && passwords.get(index).equals(passwordInput)) {
                    loggedIn = true;
                    currentUser = userInput;
                    outputStream.println("> Server: " + userInput + " joins");
                } else {
                    outputStream.println("> Server: Invalid username and/or password");
                }
            }
        } else {
            outputStream.println("> Server: Improperly formatted command");
        }
    }

    public static void handleNewUser() {
        String newUserName = "";
        String newUserPassword = "";

        // Start by getting the new user's username
        outputStream.println("> Server: Enter a unique username (31 characters max, no spaces): ");

        try {
            newUserName = inputStream.readLine();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

        // Loop until a valid username is entered
        while (newUserName.length() > 31 || newUserName.contains(" ") || users.contains(newUserName)) {
            outputStream.println("> Server: Invalid username. Enter a unique username (31 characters max, no spaces): ");

            try {
                newUserName = inputStream.readLine();
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }

        // Now get the user's password
        outputStream.println("> Server: Enter a password (between 4 and 8 characters, no spaces): ");

        try {
            newUserPassword = inputStream.readLine();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

        // Loop until a valid password is entered
        while (newUserPassword.length() < 4 || newUserPassword.length() > 8 || newUserPassword.contains(" ")) {
            outputStream.println("> Server: Invalid password. Enter a password (between 4 and 8 characters, no spaces): ");

            try {
                newUserPassword = inputStream.readLine();
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }

        // Now append info to file users.txt
        String filename = "users.txt";
        BufferedWriter bufferedWriter;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(filename, true));
            bufferedWriter.write(newUserName + ":" + newUserPassword);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

        // Finally, refresh the ArrayLists of users and passwords
        refreshUsers();
        outputStream.println("> Server: New user added");
    }

    public static void handleSend() {
        // make sure user is logged in
        if (loggedIn == true) {
            outputStream.println("> " + currentUser + ": " + lineArray[1]);
        } else {
            outputStream.println("> Server: Denied. Please log in first");
        }
    }

    public static void handleLogout() {
        if (loggedIn == true) {
            outputStream.println("> Server: " + currentUser + " left");
            loggedIn = false;
            currentUser = "";

            try {
                outputStream.close();
                inputStream.close();
                clientSocket.close();
                serverSocket.close();
            } catch (IOException ioe) {
                System.err.println(ioe);
            }

            System.out.println("Server stopped.");
            startServer();
        } 
    }
}
