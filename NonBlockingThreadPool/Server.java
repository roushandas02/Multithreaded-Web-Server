import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    //threadPool variable of type ExecutorService
    private final ExecutorService threadPool;
    //created a threadpool of fixed size
    public Server(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    public static void main(String[] args) throws IOException {
        int port = 8010;
        int poolSize = 10;
        Server server = new Server(poolSize);
        server.start(port);
    }

    public void start(int port) throws IOException {
        //Server Socket Channel created - non blocking - and bind to the port 
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        //A selector created for server which will constantly track ready channels for either new connection (OP_ACCEPT) or ready to read from client channel/IO operation (OP_READ)
        Selector selector = Selector.open();
        //Registering a server channel as OP_ACCEPT 
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        //Upto here selector.selectedKeys() will contain only one SelectionKey instance - <serverChannel, selector, OP_ACCEPT, No Attachment(as it is a new connection operation)>

        System.out.println(ANSI_YELLOW + "Non-blocking server listening on port " + port + ANSI_RESET);

        //SELECTION KEY
        //A SelectionKey is a token that represents the registration of a channel with a selector.
        //Components:
            //1. The SocketChannel or ServerSocketChannel you registered.
                //✅ key.channel() returns the channel.
            //2. The Selector that monitors this key.
                //✅ key.selector() returns the selector.
            //3. What you want to be notified about (interested operation) - OP_ACCEPT & OP_READ
                //✅ key.interestOps() returns the interest set.
            //4. An arbitrary object you attach when registering - buffer here
                //✅ key.attachment() returns the buffer attached.

        //This while loop constantly checks for ready channels
        while (true) {
            //blocks until 1 channel is ready - for first time, it will be the serverChannel which will be selected
            selector.select();
            //What does selector.selectedKeys() contain?
            //👉 It contains all the SelectionKey instances that are ready for some operation.
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            //iterate over this set
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                //key has 1 event - either accept (make new connection) or read (read data from client)
                SelectionKey key = iter.next();
                iter.remove();

                //making new connection
                if (key.isAcceptable()) {
                    //new client socket creation
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    //Register for read events(creating more SelectionKey instances), attach a buffer to store incoming data(a separate buffer is added to each such read request).
                    clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println(ANSI_CYAN + "Accepted connection from " + clientChannel.getRemoteAddress() + ANSI_RESET);

                    //Send a greeting message to client immediately
                    ByteBuffer buffer = ByteBuffer.wrap(("Hello " + clientChannel.getRemoteAddress() + "\n").getBytes());
                    clientChannel.write(buffer);

                } 
                //channel ready to read data from client
                else if (key.isReadable()) {
                    //getting client socket channel from key
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    //getting buffer data
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    //prepare buffer for new data
                    buffer.clear();

                    //stores the number of bytes read from the channel to the buffer
                    int bytesRead;
                    try {
                        //from the channel, data is read (put) in the buffer alloted and it returns the number of bytes read
                        bytesRead = clientChannel.read(buffer);
                        //if bytesRead=0, no data
                        //if bytesRead=-1, client gracefully closed the connection
                        if (bytesRead == -1) {
                            clientChannel.close();
                            System.out.println(ANSI_RED + "Connection closed by client" + ANSI_RESET);
                            continue;
                        }
                    } catch (IOException e) {
                        clientChannel.close();
                        System.out.println(ANSI_RED + "Error reading from client: " + e.getMessage() + ANSI_RESET);
                        continue;
                    }

                    //ByteBuffer has a position pointer and a limit.
                    //When you write data into the buffer (e.g., read()), the position advances.
                    //Before you can read that data back out, you must reset the position to the beginning and set the limit to where the data ends.
                    //flip() does exactly that
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    String message = new String(data).trim();

                    threadPool.submit(() -> processMessage(clientChannel, message));
                }
            }
        }
    }

    //to process the recieved message from client (here we give a number representing client id - hardcoded as 1 for each)
    private void processMessage(SocketChannel clientChannel, String message) {
        try {
            int number = Integer.parseInt(message);
            System.out.println(ANSI_RED + "Connection Ended by Client " + number + ANSI_RESET);
            //if successfully number processed, send acknowledgement to client else catch block
            ByteBuffer response = ByteBuffer.wrap(("Received number: " + number + "\n").getBytes());
            clientChannel.write(response);
            clientChannel.close();
        } catch (NumberFormatException | IOException e) {
            try {
                clientChannel.write(ByteBuffer.wrap("Invalid number\n".getBytes()));
                clientChannel.close();
            } catch (IOException ex) {
                // Ignore cleanup errors
            }
        }
    }
}
