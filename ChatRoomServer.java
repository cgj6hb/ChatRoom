/*
    Chris Jansson
    12399710
    CGJ6HB

    CMP_SC 4850 Lab #3
    ChatRoom Version1
    December 4th, 2017

    Program Description: This is the server program for the ChatRoom application.
    It is responsible for accepting a request from a client and handling any commands
    that are sent to it.

    There are 5 commands that can be used:

    login       This will log in a user from the users.txt file. A user must be logged
                in before they can send any messages.

    newuser     This will create a new user account and append that user information
                to users.txt. Username and password requirements are enforced.

    send        This allows a user to send a message to the server. The server will
                preface the message with the username of the user who sent it, and
                send it back to the client.

    logout      This will log out the current user, if one is logged in.

    exit        This will close the client and restart the server so another
                client can connect.
*/

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
    // These are initialized with users and passwords from the users.txt file
    static ArrayList<String> users;
    static ArrayList<String> passwords;

    // These keep track of whether or not someone is logged in
    static boolean loggedIn = false;
    static String currentUser = "";

    // Connection utilities
    static ServerSocket serverSocket = null;
    static Socket clientSocket = null;
    static BufferedReader inputStream;
    static PrintStream outputStream;

    // User command utilities
    static String line;
    static String command;
    static String lineArray[];
    static String loginArray[];

    public static void main(String args[]) {
        System.out.println("Server running...");
        startServer();
    }

    public static void startServer() {
        // Load the users and passwords into ArrayLists from the users.txt file
        users = new ArrayList<>();
        passwords = new ArrayList<>();
        refreshUsers();

        loggedIn = false;
        currentUser = "";

        // This will create the socket on the server side to accept requests from clients
        initServerSocket();

        // This is the bulk of the program which will parse client commands and handle them appropriately
        listen();
    }

    public static void stopServer() {
        // Free any allocated resources
        try {
            outputStream.close();
            inputStream.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void refreshUsers() {
        // Clear the lists beforehand to avoid duplicate data
        users.clear();
        passwords.clear();

        String filename = "users.txt";
        String line;
        String lineArray[];

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));

            // Read each line and parse the username and password
            // Then add them to the corresponding arrays
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
            // accept a client request and initialize the input and output streams
            clientSocket = serverSocket.accept();
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());

            while (true) {
                line = inputStream.readLine();

                // First, figure out if the command is made up of multiple words or not
                // If it is multiple words, split it. The first word is a command keyword, and the
                // rest of the line is data to be fed into that command
                if (line.contains(" ")) {
                    // First, separate the command from the rest of the line
                    lineArray = line.split(" ", 2);
                    command = lineArray[0];

                    // Now pass off the command to the appropriate method
                    if (command.equals("login")) {
                        handleLogin();
                    } else if (command.equals("send")) {
                        handleSend();
                    } else if (command.equals("newuser")) {
                        handleNewUser();
                    } else if (command.equals("logout")) {
                        handleLogout();
                    } else if (command.equals("exit")) {
                        handleExit();
                    } else {
                        outputStream.println("> Server: Invalid command");
                    }
                // In this case, it was a one word command that didn't require any arguments
                // Hand off the command to the appropriate method
                } else if (line.equals("newuser")) {
                    handleNewUser();
                } else if (line.equals("logout")) {
                    handleLogout();
                } else if (line.equals("exit")) {
                    handleExit();
                } else {
                    outputStream.println("> Server: Invalid command");
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
        // If someone else is already logged in, reject htis request
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
                // Initialize
                String userInput = loginArray[0];
                String passwordInput = loginArray[1];

                // Search the array of users to check for a match
                int index = users.indexOf(userInput);

                // If the username exists and the password is correct, log in that user
                // Otherwise, print an error message to the client console
                if (index != -1 && passwords.get(index).equals(passwordInput)) {
                    // Set these so we know someone is logged in
                    loggedIn = true;
                    currentUser = userInput;
                    outputStream.println("> Server: " + userInput + " joins");
                } else {
                    outputStream.println("> Server: Invalid username and/or password");
                }
            }
        }
        // This means there weren't exactly two arguments (name and password) provided
        else {
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
        while (newUserName.length() < 1 || newUserName.length() > 31 || newUserName.contains(" ") || users.contains(newUserName)) {
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

        // Append new user information to users.txt
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
        } else {
            outputStream.println("> Server: No user is logged in");
        }
    }

    public static void handleExit() {
        // Reset these for the next time a client connects
        if (loggedIn == true) {
            loggedIn = false;
            currentUser = "";
        }

        // Send the client an exit signal so it knows to close
        outputStream.println("EXIT_SIGNAL");

        stopServer();
        startServer();
    }
}
