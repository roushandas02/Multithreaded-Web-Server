import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

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


    //This is a handler that just takes a Client Socket and greets it and closes the connection
    //Consumer<Socket> is a functional interface from java.util.function, it represents an operation that takes a Socket and does something with it, but returns no value.
    public Consumer<Socket> getConsumer() {
        return (clientSocket) -> {
            try (PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true)) {
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line = fromClient.readLine();
                int number=Integer.parseInt(line);
                toSocket.println("Hello " + clientSocket.getInetAddress());

                //End of while loop means end of 1 connection
                System.out.println();
                System.out.println(ANSI_RED+"Connection Ended by Client "+(number)+ANSI_RESET);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }
    //WITHOUT LAMBDA FUNCTION
    // public Consumer<Socket> getConsumer() {
    //     return new Consumer<Socket>() {
    //         @Override
    //         public void accept(Socket clientSocket) {
    //             try (PrintWriter toSocket = new PrintWriter(clientSocket.getOutputStream(), true)) {
    //                 toSocket.println("Hello from server " + clientSocket.getInetAddress());
    //             } catch (IOException ex) {
    //                 ex.printStackTrace();
    //             }
    //         }
    //     };
    // }
    

    public static void main(String[] args) {
        int port = 8010;
        Server server = new Server();
        
        try {
            //Created a new Server Socket with only port
            ServerSocket serverSocket = new ServerSocket(port);
            //Server Closes in 7 Seconds if no clients connect
            serverSocket.setSoTimeout(20000);

            System.out.println(ANSI_YELLOW+"Server is listening on port " + port+ANSI_RESET);
            System.out.println();

            while (true) {
                //Server Waits here till it recieve a connection from client
                Socket clientSocket = serverSocket.accept();
                
                
                // Create and start a new thread for each client request
                //by calling getConsumer() we get an object/handler which can handle (perform a certain task) a client socket
                Thread thread = new Thread(() -> server.getConsumer().accept(clientSocket));
                thread.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}