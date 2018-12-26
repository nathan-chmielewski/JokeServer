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

import java.net.*;
import java.io.*;

public class JokeClientAdmin {

  public static void main(String[] args) {

    // Variables to hold server names and port numberes to be able to
    // connect to multiple servers and switch between servers on command.
    String serverOne = null;
    String serverTwo = null;
    int portOne = 5050;
    int portTwo = 5051;

    // If server name is not provided as the first argument when
    // JokeClientAdmin is launched in console, set the server name to "localhost".
    try {
      if (args.length < 1) {
        serverOne = "localhost";
      }
      // If one or two arguments are provided when JokeClientAdmin is launched
      // in the console, set the server names to the corresponding variables.
      else {
        // Assign the first console argument to serverOne variable
        serverOne = args[0];
      }
      // Perform InetAddress lookup based on server name.
      System.out.println("Server one: " + InetAddress.getByName(serverOne) + ", port " + portOne);
      if (args.length >= 2) {
        serverTwo = args[1];
        System.out.println("Server two: " + InetAddress.getByName(serverTwo) + ", port " + portTwo);
      }
    } catch(UnknownHostException ex) {
      // If server lookup of hostname or IP address provided by user/client
      // fails, display error message in admin console.
      System.out.println("Failed in attempt to look up server.");
    }

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    // Wait for user input command to connect to JokeServer to toggle mode, or
    // switch the server that JokeClientAdmin is connected to.
    try {
      String mode;
      String server = serverOne;
      int port = portOne;
      do {
        System.out.print("Press enter to toggle mode, type (s) to switch servers, (quit) to end: ");
        System.out.flush();
        mode = in.readLine();
        // If user presses enter with no other input, toggle Joke/Proverb mode
        // on the server the admin is connected to
        if (mode.isEmpty())
        toggleMode(server, port);
        // If user inputs 's' command, check if JokeClientAdmin has a secondary
        // JokeServer to connect to. If it does, switch connection to that
        // server.
        if (mode.equals("s")) {
          if(serverTwo == null) {
            System.out.println("No secondary server being used.");
          }
          else {
            if(port == portOne) {
              server = serverTwo;
              port = portTwo;
              System.out.println("Now communicating with: " + InetAddress.getByName(serverOne) + ", port " + portTwo);
            }
            else {
              server = serverOne;
              port = portOne;
              System.out.println("Now communicating with: " + InetAddress.getByName(serverTwo) + ", port " + portOne);
            }
          }
        }
        // While loop to continue to run do block to prompt user for a command
        // until the user enters 'quit' command.
      } while(mode.indexOf("quit") < 0);
      // When the user enters the quit command, display exit message and
      // client application ends.
      System.out.println("Cancelled by user request.");
    } catch (UnknownHostException uhe) { System.out.println("Failed attempt to look up server.");
  } catch (IOException ioe) { ioe.printStackTrace(); }
}

// Method to connect to JokeServer to toggle mode between Joke and Proverb
static void toggleMode(String server, int port) {
  Socket socket;

  try {
    socket = new Socket(server, port);
    socket.close();
  }
  catch(IOException x) {
    System.out.println("Socket error.");
    x.printStackTrace();
  }
}
}
