import java.net.*;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class TextProviderV1 {
  // port to listen connection
  static final int PORT = 8080;
  // File which you want line from

  public static void main(String[] args) throws IOException {
    // start looking for request to port
    String FILE = args[0];
    ServerSocket server = new ServerSocket(PORT);
    System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

    // keep server open until shutdown
    boolean done = false;
    while (!done) {
      // create the client socket
      Socket client = server.accept();
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
        } else if (ext[1].equals("shutdown")) {
          out.println("Server has been killed");
          out.flush();
          done = true;
        } else if (ext[1].equals("get")) {
          // line number requested
          long lineNum = Long.parseLong(ext[2]);
          // line number less than 1
          if (lineNum < 1) {
            out.println("HTTP 404");
            out.flush();
          } else {
            // go through file line by line and stop when you either get to the
            // line or go through the entire file
            BufferedReader br = new BufferedReader(new FileReader(FILE));
            long lineCount = 0;
            lineNum--;
            while (lineCount < lineNum && br.readLine() != null) {
              lineCount++;
            }
            String line = br.readLine();
            // line number greater than length of file
            if (lineCount != lineNum || line == null) {
              out.println("HTTP 404");
              out.flush();
            } else {
              out.println(line);
              out.flush();
              System.out.println("Printing line from file to client.");
            }
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
    }
    // close the server.
    System.out.println("Server closed.");
    server.close();
  }
}