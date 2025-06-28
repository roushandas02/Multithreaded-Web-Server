
import java.io.BufferedReader;
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

    // public int number=0;
    
    //Runnable is an interface which has only one function 'run()'' and it runs that
    public Runnable getRunnable(int number) throws UnknownHostException, IOException {
        return new Runnable() {
            @Override
            public void run() {
                int port = 8010;
                try {
                    //Getting IP Address of local machine
                    InetAddress address = InetAddress.getByName("localhost");
                    //Creating a Client Socket
                    Socket socket = new Socket(address, port);
                    try (
                        //PrintWriter and BufferedReader class to handle OutputStream and InputStream in socket pipeline respectively
                        PrintWriter toSocket = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    ) {
                        System.out.println(ANSI_CYAN+"Connection Established by Client "+(number)+ANSI_RESET);
                        System.out.println();
                        //Send First message to Server
                        // toSocket.println("Hello " + socket.getLocalSocketAddress());
                        toSocket.println(number);
                        //Then print server response
                        String line = fromSocket.readLine();
                        System.out.println("Server: " + line);
                        System.out.println();
                        //Ending Connection with server
                        System.out.println();
                        System.out.println(ANSI_RED+"Connection Ended by Client "+(number) +ANSI_RESET);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // The socket will be closed automatically when leaving the try-with-resources block
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        };
    }
    
    public static void main(String[] args){
        Client client = new Client();
        //100 threads created simultaneously to populate 100 client request simultaneously
        for(int i=0; i<100; i++){
            try{
                //A runnable interface is called in each thread which creates a client socket and sends connection request to Server socket Simultaneously 
                //This runnable interface contains absolutely nothing except a run() function which is overridden
                Thread thread = new Thread(client.getRunnable(i+1));
                thread.start();
            }catch(Exception ex){
                return;
            }
        }
        return;
    }
}