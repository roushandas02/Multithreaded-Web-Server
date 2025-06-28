import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
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
    
    public void run() throws UnknownHostException, IOException{
        int port = 8010;
        //Running on local computer so getting its IP address
        InetAddress address = InetAddress.getByName("localhost");
        //Creating a new client socket using IP address and Port
        Socket socket = new Socket(address, port);
        //Sends OutputStream data over socket by PrintWriter class. This OutputStream is the raw stream of bytes you can write to, to send data to the Server.
        //autoFlush true - Whenever you call println() or printf(), the writer immediately sends the data over the network without waiting for you to flush manually.
        PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true);
        //Recieve InputStream data from Server by BufferedReader. 
            //getInputStream() gives you the raw data pipe.
            //InputStreamReader turns the bytes in the pipe into readable letters.
            //BufferedReader gives you an easy way to read the letters line by line.
        BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(ANSI_CYAN+"Connection Established"+ANSI_RESET);
        System.out.println();
        //Sending First Message to Server
        toSocket.println("Hello World from socket "+socket.getLocalSocketAddress());
        //After Sending, receive message from Server
        String line = fromSocket.readLine();
        System.out.println("Server: "+line);


        //Ending Connection with server
        System.out.println();
        System.out.println(ANSI_RED+"Connection Ended"+ANSI_RESET);
        toSocket.close();
        fromSocket.close();
        socket.close();
    }
    
    public static void main(String[] args) {
        Client singleThreadedWebServer_Client = new Client();
        try{
            singleThreadedWebServer_Client.run();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}