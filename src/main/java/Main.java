import java.io.*;


public class Main {
  public static void main(String[] args) {
      // You can use print statements as follows for debugging, they'll be visible when running tests.
      System.out.println("Logs from your program will appear here!");
      final HttpServer httpServer = new HttpServer(5);
      httpServer.run();
  }
}
