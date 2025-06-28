# Java
    Java is a high-level, object-oriented programming language that provides built-in support for multithreading(provides both blocking and non-blocking APIs), enabling a program to execute multiple threads (independent paths of execution) concurrently within a single process.Blocking means a thread stops execution and waits for some operation to complete (e.g., reading data, acquiring a lock).

    Java supports multithreading through:
    ✅ The Thread class and the Runnable interface
    ✅ Synchronization mechanisms (synchronized keyword, locks) to coordinate access to shared data
    ✅ High-level concurrency utilities in the java.util.concurrent package (Executors, thread pools, semaphores, etc.)

---

## TCP Socket Server
    A TCP socket server is a program that listens for incoming TCP connections on a network port, establishes a connection (a socket) with a client, and then exchanges data reliably over that connection.

---

## 🛠 Technologies Used

- **Java SE** (Thread API, NIO, `java.util.concurrent`)
- **ExecutorService** for thread pool management
- **Selector** and **ServerSocketChannel** for non-blocking I/O
- **JMeter** for load testing

---

## 🚀 Server Types Explained

### 1️⃣  Single Threaded Server
     1. Import java.net.* and java.io.*

     2. Create ServerSocket
```java
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(20000);
```

     3. Accept connections (accept()) - Blocking Nature 
```java
        Socket acceptedConnection = socket.accept();
```

     4. Get input/output streams - Blocking Nature
```java
        PrintWriter toClient = new PrintWriter(acceptedConnection.getOutputStream(), true);
        BufferedReader fromClient = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
```
            
     5. Read/write data
```java
        String line = fromClient.readLine();
        System.out.println("Client: "+line);
        //First recieves a message from client and then send reply message to the client
        toClient.println("Hello World from the server");
```

    6. Close sockets
```java
        clientSocket.close();
        serverSocket.close();
```

### 2️⃣ Multi Threaded Server
    Whenever a client connection is accepted by the server, the server creates a new thread to handle that client’s request. This allows the server to handle multiple clients concurrently, making it faster and more responsive.

    However, this approach has a drawback. When there is a huge load (many client connections), creating a separate thread for each client consumes a lot of system resources. Each thread requires memory for its thread stack and other internal structures.

    As the number of threads grows, this can lead to high memory usage and increased CPU overhead for managing threads. Additionally, if too many threads are waiting for execution for a long time (due to thread scheduling delays or resource contention), they may become inactive or terminated by the system, leading to poor performance or failures in serving clients efficiently.

### 3️⃣ ThreadPool Server
    A thread pool is a collection of pre-created reusable threads that are managed by a framework or runtime. Instead of creating a new thread for every task or client connection, tasks are submitted to the pool, and an available thread executes them.A thread pool is like having a fixed team of workers ready to do jobs. You don’t hire a new worker every time (i.e., create a new thread).

    Java provides built-in thread pools via the ExecutorService interface.
**Example:**
```java
        ExecutorService pool = Executors.newFixedThreadPool(10);
```

    This creates a pool of 10 threads. (INCREASING POOL SIZE INCREASES PERFORMANCE)

    When you submit tasks (pool.submit() or pool.execute()), they are queued if all threads are busy, and handled as threads become free.


### 4️⃣ Non Blocking ThreadPool Socket Server
**Why implement non blocking nature?**
        - serverSocket.accept() blocks until a client connects
        - Each thread uses InputStream.read() and OutputStream.write(), which block while waiting for data.

        Due to this blocking nature, Threads can pile up under heavy load. So we implement this.

**How to implement this?**
        Java NIO (New I/O) is a collection of Java APIs introduced in Java 1.4 that provide high-performance, scalable I/O operations. NIO enables non-blocking I/O, buffer-oriented data handling, and advanced features like selectors and channels.

```java
    ServerSocket (blocking)                 ->  ServerSocketChannel (non-blocking)
    Socket.accept() (blocking)              ->  Selector to multiplex connections
    BufferedReader.readLine() (blocking)    ->  SocketChannel.read() (non-blocking)
```

**Working:**
        - Single selector thread waits for ready channels.
        - When a channel is ready to read, the selector is notified.
        - You dispatch processing to the thread pool (e.g., parsing the message, responding).
        - The selector thread remains free to monitor more channels.
    
    ✅ ServerSocketChannel is non-blocking
    ✅ Selector multiplexes connections
    ✅ When ready to read:

        - key.isReadable() fires.
        - We temporarily disable OP_READ (to avoid duplicate notifications).
        - Submit a task to the thread pool to process the data.
        - After processing, we re-enable OP_READ.

    ✅ This design allows:

        - Single selector thread to manage thousands of connections.
        - Thread pool to process data concurrently without blocking the selector.

    ✅ This is a classic Reactor + Worker Pool hybrid.


### 5️⃣ Load Balancer
    The load balancer listens for incoming client TCP connections, selects a backend server using a round-robin strategy, and transparently forwards bidirectional data between the client and the chosen backend server to distribute traffic evenly and improve scalability.

**Ports used:**
    | Component         | Port |
    |-------------------|------|
    | Load Balancer     | 8010 |
    | Backend Server 1  | 8011 | (ThreadPool Server)
    | Backend Server 2  | 8012 | (ThreadPool Server)

    The load balancer creates 2 sockets:
        - The load balancer uses a ServerSocket to accept incoming client connections and obtains a client Socket representing each connected client.
        - For each client connection, it creates a separate backend Socket to connect to the selected backend server and launches two threads to forward data bidirectionally between the client and backend sockets.


## Thread Pool vs Event Loop
**✅ Thread Pool**
    - Maintains a fixed number of threads to handle tasks.
    - Each thread typically blocks while reading or writing data for a connection.
    - Simpler to implement because you can use synchronous code.
    - Good for moderate concurrency, e.g., hundreds or a few thousand connections.
    - Per-connection memory overhead is higher (each thread consumes stack space).
    - Susceptible to performance degradation due to context switching under high load.

**✅ Event Loop**
    - Uses one or a small number of threads to monitor many connections simultaneously.
    - Relies on non-blocking I/O and readiness notifications (e.g., using Selector in Java NIO).
    - Scales very efficiently to tens of thousands of concurrent connections.
    - Requires more complex code (callbacks, state machines).
    - Minimal per-connection memory usage because it does not create a thread per connection.
    - Provides predictable latency even under heavy load.
    - Languages like JavaScript which uses Single Thread, uses Event Loop for handling operations


## ExecutorService
    A framework in Java for managing and controlling a pool of threads to run tasks asynchronously.

    **✅ ExecutorService – How It Works:**

        - Creates a thread pool of fixed size to handle concurrent tasks efficiently.
        - You submit tasks (Runnable or Callable) to the ExecutorService for execution.
        - Queues tasks automatically if all threads are busy.
        - Assigns tasks to available threads in the pool as soon as they become free.
        - Provides methods to shut down gracefully, such as shutdown() and shutdownNow(), to stop accepting new tasks and manage cleanup.















**Note:**

Client Socket Creation takes both IP address as mandatory field, whereas Server Socket Creation takes only Port as a mandatory field.
    When you create a server socket, you only need to specify the port (and optionally the IP/interface to bind to), because the server is listening for incoming connections, not connecting out to a specific address.
