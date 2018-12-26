/*--------------------------------------------------------

1. Name / Date:
Nathan Chmielewski / September 22, 2018

2. Java version used, if not the official version for the class:
java version "1.8.0_162"


3. Precise command-line compilation examples / instructions:
Compile java files using command-line instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:
In separate shell windows, run the compiled files using command-line instructions
below. Launch the files in any order.
JOKESERVER:
- For localhost, no arguments when running compiled file
> java JokeServer
- For secondary, add 'secondary' as argument
> java JokeServer secondary
JOKECLIENT:
- For localhost, no arguments or one argument ('localhost' OR 127.0.0.1) to run
> java JokeClient
> java JokeClient localhost
- For two servers, two IP addresses as arguments
> java JokeClient localhost localhost
JOKECLIENTADMIN:
- For one server, no arguments or one argument
> java JokeClientAdmin
> java JokeClientAdmin localhost

This was not tested across machines. It was tested for multiple servers that
were both local. Therefore, it may run across machines when the Joke Client is
passed the IP address.

5. List of files needed for running the program.
- JokeServer.java
- JokeClient.java
- JokeClientAdmin.java

5. Notes:
- The client uuid is not really a universally unique identifier, I use a random
int generator for the uuids in JokeClient. This could create a bug if two
clients end up with the same uuid, though the chances are very low that this
occurs.
- The program is limited to 1 or 2 JokeServers running at the same time.
It was not tested across Internet connections, only locally. Therefore, it
may not perform correctly when passed remote IP addresses as arguments.
- When JokeClient or JokeClientAdmin switch servers, the server name and IP
address are displayed,
e.g. 'Now communicating with: localhost/127.0.0.1, port 5051'
- JokeServer runs continuously until shell is terminated.

----------------------------------------------------------*/

import java.io.*;
import java.net.*;
import java.lang.Math;
import java.util.*;

public class JokeClient {
  public static void main(String args[]) {

    // Variables to hold server names and InetAddress objects to be able to
    // connect to multiple servers and switch between servers on command.
    String serverOne = null;
    String serverTwo = null;
    int portOne = 4545;
    int portTwo = 4546;

    // If server name is not provided as the first argument when JokeClient is
    // launched in console, set the server name to "localhost".

    try {
      if (args.length < 1) {
        serverOne = "localhost";
      }
      // If one or two arguments are provided when JokeClient is launched in the
      // console, set the server names to the corresponding variables.
      else {
        // Assign the first argument to serverOne variable, and perform
        // InetAddress lookup based on the server name input to display IP addresses.
        serverOne = args[0];
      }
      System.out.println("Server one: " + InetAddress.getByName(serverOne) + ", port " + portOne);
      if (args.length >= 2) {
        // Assign the second argument to serverTwo variable, and perform
        // InetAddress lookup based on the server name input.
        // try {
        serverTwo = args[1];
        System.out.println("Server two: " + InetAddress.getByName(serverTwo) + ", port " + portTwo);
        // } catch(UnknownHostException ex) {
        // If server lookup of hostname or IP address provided by user/client
        // fails, display error message in client console.
        // System.out.println("Failed in attempt to look up " + name);
        // }
      }
    } catch(UnknownHostException ex) {
      // If server lookup of hostname or IP address provided by user/client
      // fails, display error message in client console.
      System.out.println("Failed in attempt to look up server.");
    }

    // Prompt the user to enter a name.
    String name = "";
    System.out.print("Enter your name: ");
    System.out.flush();
    // Generate pseudo-random int value as UUID, store it in a variable.
    // Send this UUID to the server everytime the client connects to the server.
    Random random = new Random();
    int uuid = random.nextInt(2147483646);

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    try {
      // Read console input from user, assign to name variable.
      name = in.readLine();
    } catch(IOException ioe) { ioe.printStackTrace(); }
    String input;
    String server = serverOne;
    int port = portOne;
    try {
      do {
        System.out.print("Press enter to receive a joke or proverb, (s) to switch servers, (quit) to end: ");
        input = in.readLine();
        // If user presses enter with no input, client calls getJokeOrProverb and
        // passes in uuid, name, server and port to communicate with JokeServer
        // and receive a joke or a proverb.
        if(input.isEmpty()) {
          getJokeOrProverb(uuid, name, server, port);
        }
        // If user inputs 's', JokeClient checks for secondary server. If there
        // is a secondary server, JokeClient switches connection.
        if (input.equals("s")) {
          if(serverTwo == null) {
            System.out.println("No secondary server being used.");
          }
          else {
            if(port == portOne) {
              server = serverTwo;
              port = portTwo;
              System.out.println("Now communicating with: " + InetAddress.getByName(serverTwo) + ", port " + portTwo);
            }
            // If JokeClient was connected to secondary server, connects back
            // to first server.
            else {
              server = serverOne;
              port = portOne;
              System.out.println("Now communicating with: " + InetAddress.getByName(serverOne) + ", port " + portOne);
            }
          }
        }
      }
      // While loop to continue to run try block to prompt user for a hostname
      // or an IP address until the user enters 'quit' command.
      while(input.indexOf("quit") < 0);

      // When the user enters the quit command, display exit message and
      // client application ends.
      System.out.println("Cancelled by user request.");
    } catch (UnknownHostException uhe) { System.out.println("Failed attempt to look up server.");
    } catch (IOException ioe) { ioe.printStackTrace(); }
  }

  // Method to make connection with JokeServer, send data to the server to store
  // the client's state, and receives a joke or proverb to display to the console.
  static void getJokeOrProverb(int uuid, String name, String server, int port) {
    Socket socket;
    BufferedReader fromServer;
    PrintStream toServer;
    String[] textFromServer = new String[2];

    try {
      /* Opens the connection to the server through a socket, using the server
      name passed in as parameter, serverName, and port number (hardcoded) */
      // Display server name and port number.
      socket = new Socket(InetAddress.getByName(server), port);

      // Assigns a BufferedReader variable, 'fromServer', to read/buffer
      // character input data from the server through the socket.
      fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      // Assigns PrintStream variable, 'toServer', to send data to the server
      // through the socket
      toServer = new PrintStream(socket.getOutputStream());

      // Writes client UUID and name entered to the PrintStream to send data
      // through the socket.
      toServer.println(uuid);
      toServer.println(name);

      toServer.flush();

      // For loop reads and displays JOKE OR PROVERB from the server that was read
      // into fromServer variable through the socket input stream.

      for(int i = 0; i < 2; i++) {
        // Reads line of text/data from the server through the socket.
        textFromServer[i] = fromServer.readLine();
      }
      System.out.println(textFromServer[0] + " " + name + ": " + textFromServer[1]);


      // Closes this socket's connection
      socket.close();
    } catch(IOException x) {
      System.out.println("Socket error.");
      x.printStackTrace();
    }
  }
}
