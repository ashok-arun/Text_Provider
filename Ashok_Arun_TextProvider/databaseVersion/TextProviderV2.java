import java.net.*;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

// how to compile javac -cp postgresql-42.2.14.jar: TextProviderV2.java
// how to run java -cp postgresql-42.2.14.jar: TextProviderV2
public class TextProviderV2 {
  // port to listen connection
  static final int PORT = 8080;

  public static void main(String[] args) throws IOException {
    // start looking for request to port
    InetAddress addr = InetAddress.getByName("0.0.0.0");
    ServerSocket server = new ServerSocket(PORT,100,addr);
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
          String lineNum = ext[2];
          // try and get file
          Connection c = null;
          Statement stmt = null;
          try {
            Class.forName("org.postgresql.Driver");
            // connect to database
            c = DriverManager.getConnection("jdbc:postgresql://52.201.186.77:5432/starburst", "postgres", "starburst");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            // get info from table
            ResultSet rs = stmt.executeQuery("SELECT * FROM lines WHERE lineNum = " + lineNum + ";");
            String lineText = null;
            while (rs.next()) {
              // only if line is in table
              lineText = rs.getString("lineText");
              System.out.println("Printing line from file to client.");
              out.println(lineText);
              out.flush();
            }
            // line not in table
            if (lineText == null) {
              out.println("HTTP 404");
              out.flush();
            }
            rs.close();
            stmt.close();
            c.close();
          } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
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
