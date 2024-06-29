import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final ExecutorService executorService;
    private String directory;
    public HttpServer(final int concurrencyLevel,String directory){
        this.executorService = Executors.newFixedThreadPool(concurrencyLevel);
        this.directory = directory;
    }
    public void run() {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors

            serverSocket.setReuseAddress(true);
            while(true) {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                this.executorService.submit(() -> {
                    try {
                        handleRequest(clientSocket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private void handleRequest(Socket clientSocket) throws IOException {
        System.out.println("accepted new connection");
        InputStream input = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String requestLine = reader.readLine();
        String[] HttpRequest = requestLine.split(" ");

        Map<String,String> requestHeaders = new HashMap<String,String>();
        String header = null;
        while((header = reader.readLine()) != null && !header.isEmpty()){
            String[] keyVal = header.split(":",2);
            if(keyVal.length == 2){
                requestHeaders.put(keyVal[0],keyVal[1].trim());
            }
        }

        OutputStream output = clientSocket.getOutputStream();


        if (HttpRequest[1].matches("/echo/(.*)")) {
            String responseString = HttpRequest[1].substring(6);
            output.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + responseString.length()
                    + "\r\n\r\n" + responseString).getBytes());
        } else if (HttpRequest[1].equals("/user-agent")) {
            String userAgent = requestHeaders.get("User-Agent");
            output.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.length()
                    + "\r\n\r\n" + userAgent).getBytes());

        } else if (HttpRequest[1].equals("/")) {
            output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        } else if(HttpRequest[1].matches("/files/(.*)")){
            String fileName = HttpRequest[1].substring(7);
            File file = new File(this.directory,fileName);
            if(HttpRequest[0].equals("POST")){
                if(file.createNewFile()){
                    char[] bodyChars = new char[Integer.parseInt(requestHeaders.get("Content-Length"))];
                    reader.read(bodyChars,0,Integer.parseInt(requestHeaders.get("Content-Length")));
                    String bodyData = new String(bodyChars);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(bodyData);
                    fileWriter.close();
                    output.write(("HTTP/1.1 201 Created\r\n").getBytes());
                } else {
                    output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
            } else {
                if(file.exists()){
                    BufferedReader fileReader = new BufferedReader(new FileReader(file));
                    String content = fileReader.readLine();
                    output.write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " + content.length()
                            + "\r\n\r\n" + content).getBytes());
                } else {
                    output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
            }
        }else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }
        clientSocket.close();
    }
}
