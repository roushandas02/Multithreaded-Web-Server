import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadBalancer {
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


    private final String[] backendHosts;
    private final int[] backendPorts;
    private int currentIndex = 0;//tracks which backend to select next(round robin)
    private final ExecutorService threadPool;//a threadPool variable of type ExecutionService

    public LoadBalancer(String[] backendHosts, int[] backendPorts, int poolSize) {
        this.backendHosts = backendHosts;
        this.backendPorts = backendPorts;
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    // Select backend server in round-robin fashion
    //synchronized signifies only one thread updates currentIndex at a time
    private synchronized int getNextBackendIndex() {
        int index = currentIndex;
        currentIndex = (currentIndex + 1) % backendPorts.length;
        return index;
    }

    //listening starts on port: listenPort
    public void start(int listenPort) throws IOException {
        //try-with-resources: Ensures serverSocket is automatically closed.
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println(ANSI_YELLOW+"Load Balancer is listening on port " + listenPort+ANSI_RESET );
            while (true) {
                //waits here until a connection from client is received
                Socket clientSocket = serverSocket.accept();
                //getting the current server(by round robin) to which the client request will be re-routed 
                int backendIndex = getNextBackendIndex();
                String backendHost = backendHosts[backendIndex];
                int backendPort = backendPorts[backendIndex];
                System.out.println("Forwarding client to backend " + backendHost + ":" + backendPort);

                // Handle connection forwarding in the thread pool
                threadPool.execute(() -> handleConnection(clientSocket, backendHost, backendPort));
            }
        }
    }

    //Handles connecting the client to the backend server and piping data between them.
    private void handleConnection(Socket clientSocket, String backendHost, int backendPort) {
        //try-with-resources: Ensures backend socket closes when done.
        //this will be treated as client socket by backend both servers
        try (Socket backendSocket = new Socket(backendHost, backendPort)) {
            // Create threads to forward data in both directions
            Thread clientToBackend = new Thread(() -> forwardData(clientSocket, backendSocket));
            Thread backendToClient = new Thread(() -> forwardData(backendSocket, clientSocket));

            clientToBackend.start();
            backendToClient.start();

            // Wait for both directions to finish
            clientToBackend.join();
            backendToClient.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }

    // Copies data from inputSocket to outputSocket
    //Reads data from in into buffer. bytesRead: Number of bytes read. Writes those bytes to out. Flushes output stream to ensure data is sent immediately.
    private void forwardData(Socket inputSocket, Socket outputSocket) {
        try (
            InputStream in = inputSocket.getInputStream();
            OutputStream out = outputSocket.getOutputStream()
        ) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            //Loop continues until input is closed (read() returns -1).
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException ignored) {
            // Connections close naturally when one side ends
        }
    }

    public static void main(String[] args) throws IOException {
        String[] backendHosts = {"localhost", "localhost"};
        int[] backendPorts = {8011, 8012};
        int poolSize = 20;

        LoadBalancer lb = new LoadBalancer(backendHosts, backendPorts, poolSize);
        lb.start(8010);
    }
}
