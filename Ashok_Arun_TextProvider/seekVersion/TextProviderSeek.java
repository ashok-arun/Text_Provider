import java.net.*;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class TextProviderSeek implements Runnable {
  // port to listen connection
  static final int PORT = 8080;
  // path to file system
  static String FILE = "/Users/aarun/Documents/code/starburstData/trial.txt";
  static String PATH = "/Users/aarun/Documents/code/starburstData/linebytes.txt";
  static String[] LINES = null;

  // Client Connection via Socket Class
  private Socket client;

  public TextProviderSeek(Socket c) {
    client = c;
  }

  public static void main(String[] args) throws IOException {
    ServerSocket server = new ServerSocket(PORT);
    System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

    BufferedReader br = new BufferedReader(new FileReader(PATH));
    LINES = br.readLine().split(" ");

    // we listen until user halts server execution
    while (true) {
      TextProviderSeek myServer = new TextProviderSeek(server.accept());

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
          out.print("invalid call");
          out.flush();
        } else if (ext[1].equals("get")) {
          // line number requested
          int lineNum = Integer.parseInt(ext[2]);
          // try and get file
          RandomAccessFile get = new RandomAccessFile(FILE, "r");
          get.seek(Integer.parseInt(LINES[lineNum - 1]));
          out.println(get.readLine());
          out.flush();
          System.out.println("printing to curl");

        }
        // incorrect command from client.
        else {
          out.print("invalid call");
          out.flush();
        }
      } catch (NoSuchElementException e) {
        out.print("invalid call");
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