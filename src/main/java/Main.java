import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
     ServerSocket serverSocket = null;
     Socket clientSocket = null;

     try {
       serverSocket = new ServerSocket(4221);
       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors

       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");
       InputStream input = clientSocket.getInputStream();
       BufferedReader reader = new BufferedReader(new InputStreamReader(input));
       String line = reader.readLine();
       String[] HttpRequest = line.split(" ");
       OutputStream  output = clientSocket.getOutputStream();
       if(HttpRequest[1].matches("/echo/(.*)")){
           String responceString = HttpRequest[1].substring(6);
           output.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + responceString.length()
                   + "\r\n\r\n" + responceString).getBytes());
       } else if(HttpRequest[1].equals("/user-agent")){
           reader.readLine();
           String userAgent = reader.readLine().split(" ")[1];
           output.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + userAgent.length()
                   + "\r\n\r\n" + userAgent).getBytes());

       }else if(HttpRequest[1].equals("/")) {
           output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
       } else {
           output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
       }
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
