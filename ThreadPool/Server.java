import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    //To colour terminal text
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    // Thread pool to manage a fixed number of worker threads for handling clients
    private final ExecutorService threadPool;

    // Constructor initializes the thread pool with a given size
    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    // Logic to handle an individual client connection
    public void handleClient(Socket clientSocket) {
        try (PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // Send a greeting message to the connected client
            toSocket.println("Hello " + clientSocket.getInetAddress());
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line = fromClient.readLine();
            int number=Integer.parseInt(line);

            //End of while loop means end of 1 connection
            System.out.println();
            System.out.println(ANSI_RED+"Connection Ended by Client "+(number)+ANSI_RESET);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Main method starts the server and listens for incoming connections
    public static void main(String[] args) {
        int port = 8010; // Port number where the server will listen
        int poolSize = 10; // Maximum number of concurrent client handler threads
        Server server = new Server(poolSize); // Create the server with thread pool

        try {
            ServerSocket serverSocket = new ServerSocket(port); // Create a listening socket on the specified port
            serverSocket.setSoTimeout(20000); // Set a timeout for accepting connections (70 seconds)
            System.out.println("Server is listening on port " + port);

            while (true) {
                // Wait for a client to connect (blocks until connection arrives or timeout occurs)
                Socket clientSocket = serverSocket.accept();

                // Submit the client handling task to the thread pool for execution
                server.threadPool.execute(() -> server.handleClient(clientSocket));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Shutdown the thread pool gracefully when the server stops
            server.threadPool.shutdown();
        }
    }
}
