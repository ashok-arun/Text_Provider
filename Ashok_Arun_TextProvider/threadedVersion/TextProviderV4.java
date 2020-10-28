import java.net.*;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class TextProviderV4 implements Runnable {
  // port to listen connection
  static final int PORT = 8080;

  // Client Connection via Socket Class
  private Socket client;

  public TextProviderV4(Socket c) {
    client = c;
  }

  public static void main(String[] args) throws IOException {
    ServerSocket server = new ServerSocket(PORT);
    System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
    // we listen until user halts server execution
    while (true) {
      TextProviderV4 myServer = new TextProviderV4(server.accept());

      // create dedicated thread to manage the client connection
      Thread thread = new Thread(myServer);
      thread.start();
    }
  }

  // Thread which runs when the myServer gets a value from server.accept()
  @Override
  public void run() {
    try {
      // Print to client stream
      PrintWriter out = new PrintWriter(client.getOutputStream(), true);
      // read from client stream
      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      // get first line of the request from the client
      String input = in.readLine();
      // parse the request with a string tokenizer
      try {
        StringTokenizer parse = new StringTokenizer(input);
        parse.nextToken();
        // ext=["","get" or "shutdown", "line number"]
        String[] ext = parse.nextToken().toLowerCase().split("/");
        // must the second element must be either "get" or "shutdown"
        if (ext.length < 2) {
          out.print("invalid call. Use <curl localhost:8080/get/<n>> where n is the line number you want");
          out.println(" or <curl localhost:8080/shutdown> to kill the server.");
          out.flush();
        } else if (ext[1].equals("get")) {
          // line number requested
          String lineNum = ext[2];
          // try and get file
          try (BufferedReader br = new BufferedReader(new FileReader("lines/" + lineNum + ".txt"))) {
            out.println(br.readLine());
            out.flush();
            System.out.println("Printing line from file to client.");
          } catch (Exception e) {
            out.println("HTTP 404");
            out.flush();
          }
        }
        // incorrect command from client.
        else {
          out.print("invalid call. Use <curl localhost:8080/get/<n>> where n is the line number you want");
          out.println(" or <curl localhost:8080/shutdown> to kill the server.");
          out.flush();
        }
      } catch (NoSuchElementException e) {
        out.print("invalid call. Use <curl localhost:8080/get/<n>> where n is the line number you want");
        out.println(" or <curl localhost:8080/shutdown> to kill the server.");
        out.flush();

      }
      // close all connection but the server
      in.close();
      out.close();
      client.close();
    } catch (IOException e) {
    }
  }

}