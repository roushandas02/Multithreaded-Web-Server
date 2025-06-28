import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*;

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
    
    public void run() throws IOException, UnknownHostException{
        int port = 8010;
        //Created a new Socket and set timeout of 20 sec
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(20000);

        
        while(true){
            System.out.println(ANSI_YELLOW+"Server is listening on port: "+port+ANSI_RESET);
            System.out.println();
            //Will wait on this line till any client connection is received - after 20 sec if no connection, then close socket
            Socket acceptedConnection = socket.accept();
            //If any client connection is accepted, print its details
            System.out.println(ANSI_CYAN+"Connected to "+acceptedConnection.getRemoteSocketAddress()+ANSI_RESET);
            System.out.println();
            //Sends OutputStream data over socket by PrintWriter class. This OutputStream is the raw stream of bytes you can write to, to send data to the client.
            //autoFlush true - Whenever you call println() or printf(), the writer immediately sends the data over the network without waiting for you to flush manually.
            PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
            //Recieve InputStream data from client by BufferedReader. 
            //getInputStream() gives you the raw data pipe.
            //InputStreamReader turns the bytes in the pipe into readable letters.
            //BufferedReader gives you an easy way to read the letters line by line.
            BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
            String line = fromClient.readLine();
            System.out.println("Client: "+line);
            //First recieves a message from client and then send reply message to the client
            toClient.println("Hello World from the server");


            //End of while loop means end of 1 connection
            System.out.println();
            System.out.println(ANSI_RED+"Connection Ended by Client"+ANSI_RESET);
        }
    }

    public static void main(String[] args){
        Server server = new Server();
        try{
            server.run();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}