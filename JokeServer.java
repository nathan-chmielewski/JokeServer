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
import java.util.*;
import java.lang.Math;

// ClientData data structure stores client uuid and a list of the labels of
//  what jokes and proverbs the client has not been sent
class ClientData {
  final int uuid; // unique identifier to identify client on each request
  LinkedList<String> jokeLabelLinkedList; // List holds labels of jokes have not been sent
  LinkedList<String> proverbLabelLinkedList; // List holds labels of proverbs have not been sent

  ClientData(int uuid) {
    this.uuid = uuid;
    jokeLabelLinkedList = new LinkedList<String>();
    proverbLabelLinkedList = new LinkedList<String>();
    shuffleJokeLabels();
    shuffleProverbLabels();
  }

  /* When ClientData is first constructed, shuffleJokesLabels method adds
  joke labels to a linked list and shuffles the order of the labels.
  When the client makes a request while in JOKE mode, a label is popped from
  the linked list. When the list is empty, the method adds labels back to the
  linked list and shuffles the order again. */
  void shuffleJokeLabels() {
    jokeLabelLinkedList.add("JA");
    jokeLabelLinkedList.add("JB");
    jokeLabelLinkedList.add("JC");
    jokeLabelLinkedList.add("JD");
    Collections.shuffle(jokeLabelLinkedList);
  }

  /* When ClientData is first constructed, shuffleProverbLabels method adds
  proverb labels to a linked list and shuffles the order of the labels.
  When the client makes a request while in PROVERB mode, a label is popped from
  the linked list. When the list is empty, the method adds labels back to the
  linked list and shuffles the order again. */
  void shuffleProverbLabels() {
    proverbLabelLinkedList.add("PA");
    proverbLabelLinkedList.add("PB");
    proverbLabelLinkedList.add("PC");
    proverbLabelLinkedList.add("PD");
    Collections.shuffle(proverbLabelLinkedList);
  }
}

// Worker class definition, extends Thread to create a new thread of execution
// when Worker object is created, run.
class Worker extends Thread {
  Socket socket;                      // socket connection to client
  ArrayList<ClientData> clientList;   // List to hold client data for lookup
  static HashMap jokesMap;            // Map to look up joke text using label
  static HashMap proverbsMap;         // Map to look up proverb text using label
  boolean secondary;                  // boolean indicates secondary server

  Worker (Socket sock, ArrayList<ClientData> cd, HashMap<String, String> jm,
  HashMap<String, String> pm, boolean s) {
    socket = sock;
    clientList = cd;
    jokesMap = jm;
    proverbsMap = pm;
    secondary = s;
  }

  public void run() {

    // Initialize local variables to send data through and read from the socket.
    PrintStream out = null;
    BufferedReader in = null;

    try {
      // Instantiate 'in' to read input from the client through the socket.
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // Instantiate 'out' to write to the PrintStream to send JOKE or PROVERB
      // through the socket to the client.
      out = new PrintStream(socket.getOutputStream());

      try {
        // Initializes local variable to hold client's uuid, name to store in ClientData
        int uuid;
        int index = 0;
        uuid = Integer.parseInt(in.readLine()); // parse int uuid passed from JokeClient

        // Add client to clientData array list if list is empty
        if(clientList.isEmpty()) {
          clientList.add(new ClientData(uuid));
        }
        // if client list is not empty, check to see if client already added to list
        else {
          boolean contains = false;
          // Iterate through client list to identify client data
          for (int i = 0; i < clientList.size(); i++) {
            if(uuid == clientList.get(i).uuid) {
              contains = true;
              index = i;
            }
          }
          if(!contains) { // Client list is not empty and client is not in the list
            // Add client to list
            clientList.add(new ClientData(uuid));
            index = clientList.size() - 1;
          }
        }

        // If JokeServer is in JOKE mode, call method to send joke to JokeClient
        if(JokeServer.mode == JokeServer.Mode.JOKE) {
          // Call printJoke method to send data through socket to the client
          printJoke(out, clientList.get(index), secondary);
        }
        // If JokeServer is in Proverb mode, call method to send proverb to JokeClient
        else {
          // Call printProverb method to send data through socket to the client
          printProverb(out, clientList.get(index), secondary);
        }

      } catch(IOException x) {
        System.out.println("Server read error.");
        x.printStackTrace();
      }
      // Closes this socket connection.
      socket.close();
    } catch(IOException ioe) { System.out.println(ioe); }
  }

  // Writes joke to the PrintStream to send data/text to the client through the socket.
  // This text will be read by the client, printed/displayed in the client console
  static void printJoke(PrintStream out, ClientData client, boolean secondary) {
    try {

      // Pop joke label from client's joke label list
      String jokeLabel = client.jokeLabelLinkedList.pop();

      // Use joke label to look up corresponding joke text in joke map and
      // write output in correct format to output stream. Add <S2> if this
      // JokeServer is marked as secondary
      if(secondary)
        out.println("<S2> " + jokeLabel);
      else
        out.println(jokeLabel);
      out.println(jokesMap.get(jokeLabel));


      // If the client's joke label list is empty, print Cycle Completed message,
      // call ClientData method to add all joke labels back to the linked list
      // and shuffle the list.
      if(client.jokeLabelLinkedList.isEmpty()) {
        client.shuffleJokeLabels();
        System.out.println("JOKE CYCLE COMPLETED");
      }

    } catch(Exception e) {
      // If server lookup of joke, display error message in client console.
      out.println("Failed in attempt to send JOKE.");
    }
  }

  // Writes to the PrintStream to send data/text to the client through the socket.
  // This text will be read by the client, printed/displayed in the client console
  static void printProverb(PrintStream out, ClientData client, boolean secondary) {
    try {

      // Pop joke label from client's joke label list
      String proverbLabel = client.proverbLabelLinkedList.pop();

      // Use joke label to look up corresponding proverb text in proverb map and
      // write output in correct format to output stream. Add <S2> if this
      // JokeServer is marked as secondary
      if(secondary)
        out.println("<S2> " + proverbLabel);
      else
      out.println(proverbLabel);
      out.println(proverbsMap.get(proverbLabel));

      // If the client's joke label list is empty, print Cycle Completed message,
      // call ClientData method to add all joke labels back to the linked list
      // and shuffle the list.
      if(client.proverbLabelLinkedList.isEmpty()) {
        client.shuffleProverbLabels();
        System.out.println("PROVERB CYCLE COMPLETED");
      }

    } catch(Exception e) {
      // If server lookup of proverb, display error message in client console.
      out.println("Failed in attempt to send PROVERB.");
    }
  }
}

// ModeWorker thread runs when user in JokeClientAdmin presses enter
// to change the server mode from Joke to Proverb. When two servers are running,
// ModeWorker only changes mode of the server that JokeClientAdmin is
// communicating with.
class ModeWorker extends Thread {
  Socket socket;
  ModeWorker (Socket s) { socket = s; }

  public void run() {
    BufferedReader in = null;
    // When user presses enter in JokeClientAdmin, try block checks which mode
    // the server is currently in, and toggles the mode.
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      if(JokeServer.mode == JokeServer.Mode.JOKE) {
        JokeServer.mode = JokeServer.Mode.PROVERB;
        System.out.println("Mode toggled to PROVERB.");
      }
      else {
        JokeServer.mode = JokeServer.Mode.JOKE;
        System.out.println("Mode toggled to JOKE.");
      }

      // Closes this socket connection.
      socket.close();
    } catch(IOException ioe) { System.out.println(ioe); }
  }
}

// ModeServer runs asynchronously, waits for JokeClientAdmin to connect to
// the server. When the JokeClientAdmin user presses enter, the ModeWorker
// thread runs to toggle the server's mode from Joke to Proverb, or from
// Proverb to Joke.
class ModeServer implements Runnable {
  public static boolean adminControlSwitch = true;
  int port;

  ModeServer(int p) {
    port = p;
  }
  public void run() {
    // Number of requests for OS to queue
    int queueLength = 6;
    // Set port number for socket connection to Admin client
    Socket socket;

    // Create server socket (using port, queue length variables) to handle
    // requests from admin client.
    try {
      ServerSocket serversocket = new ServerSocket(port, queueLength);
      while(adminControlSwitch) {
        // Socket accepts admin client connection and runs ModeWorker thread
        // to toggle modes between Joke and Proverb for all clients connected
        // to the server
        socket = serversocket.accept();
        new ModeWorker(socket).start();
      }
    } catch (IOException ioe) { System.out.println(ioe); }
  }
}

// Main class of JokeServer. Iniitalizes variables to store client data, and
// map joke and proverb labels to full text. Launches asynchronous ModeServer
// thread to connect to JokeClientAdmin, and listens for JokeClient socket
// connection to run Worker thread. JokeServer can be launched with no argument
// and with 'secondary' argument to run a second server.
public class JokeServer {
  public static void main(String args[]) throws IOException {
    // Array list to hold client data so that it can grow as more new
    // clients connect to the servers
    ArrayList<ClientData> clientData = new ArrayList<ClientData>();
    // Map objects that hold joke and proverb labels as keys and full text
    // of corresponding jokes and proverbs as values for lookup
    HashMap<String, String> jokesMap = new HashMap<String, String>();
    HashMap<String, String> proverbsMap = new HashMap<String, String>();

    String JokeA = "What happens to a frog's car when it breaks down? It gets toad away.";
    String JokeB = "Why did the picture go to jail? Because it was framed.";
    String JokeC = "What did the tie say to the hat? You go on ahead and I'll hang around!";
    String JokeD = "Why do birds fly south for the winter? It's easier than walking!";
    jokesMap.put("JA", JokeA);
    jokesMap.put("JB", JokeB);
    jokesMap.put("JC", JokeC);
    jokesMap.put("JD", JokeD);

    String ProverbA = "Comparison is the thief of joy.";
    String ProverbB = "The best way out is always through.";
    String ProverbC = "Better to light a candle than to curse the darkness.";
    String ProverbD = "Fortune favors the brave.";
    proverbsMap.put("PA", ProverbA);
    proverbsMap.put("PB", ProverbB);
    proverbsMap.put("PC", ProverbC);
    proverbsMap.put("PD", ProverbD);

    // Number of requests for OS to queue
    int queueLength = 6;
    // Set port number for socket connection
    int port = 4545;
    // Variable to hold default server name, used when no argument is provided
    // in the command line
    String serverName = "localhost";
    Socket socket;
    InetAddress inetAddress = InetAddress.getByName(serverName);

    if (args.length > 0)  {
      if(args[0].equals("secondary")) {
        ModeServer modeServer = new ModeServer(5051);
        Thread thread = new Thread(modeServer);
        thread.start();
        port = 4546;
        System.out.println
        ("Server two: " + serverName + ", port " + port);
        // Create server socket (using queue length, port number, server name) to handle
        // requests from client.
        ServerSocket serversocket = new ServerSocket(port, queueLength, inetAddress);
        // While loop runs continuously until terminating the console.
        while(true) {
          socket = serversocket.accept();
          /* Create and executes a new Worker thread; passes in socket that accepted
          a connection as the parameter. Worker thread will return JOKE or PROVERB
          Results from lookup will be written to the socket, read by the client,
          and displayed in client console. */
          new Worker(socket, clientData, jokesMap, proverbsMap, true).start();
        }
      }
    }
    else {
      System.out.println
      ("Server one: " + serverName + ", port " + port);

      // "Create a Mode thread and send it off, asynchronously, to get MODE instructions"
      ModeServer modeServer = new ModeServer(5050);
      Thread thread = new Thread(modeServer);
      thread.start();


      // Create server socket (using queue length, port number, server name) to handle
      // requests from client.
      ServerSocket serversocket = new ServerSocket(port, queueLength, inetAddress);

      // While loop runs continuously until terminating the console.
      while(true) {
        // Listens for a connection to the socket, 'sock', and accepts the connection
        socket = serversocket.accept();
        /* Create and executes a new Worker thread; passes in socket that accepted
        a connection as the parameter. Worker thread will return JOKE or PROVERB
        Results from lookup will be written to the socket, read by the client,
        and displayed in client console. */
        new Worker(socket, clientData, jokesMap, proverbsMap, false).start();
      }
    }
  }

  // ENUM to hold JOKE or PROVERB mode
  public enum Mode {
    JOKE,
    PROVERB
  }

  public static Mode mode = Mode.JOKE;

}
