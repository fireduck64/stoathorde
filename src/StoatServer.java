package duckutil.stoathorde;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class StoatServer
{
  public static final int send_buffer_size = 32 * 1048576;  

  public static void main(String args[]) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Syntax StoatServer <port>");
      return;
    }

    int port = Integer.parseInt(args[0]);
    new StoatServer(port);

  }

  public StoatServer(int port) throws Exception
  {
    ServerSocket ss = new ServerSocket(port, 1024);
    ss.setReuseAddress(true);

    while(true)
    {
      Socket sock = ss.accept();
      new ServerThread(sock).start();
    }
  }

  public class ServerThread extends Thread
  {
    private Socket sock;
    public ServerThread(Socket sock)
    {
      this.sock = sock;
    }

    public void run()
    {
      try
      {
        Random rnd = new Random();
        byte[] buff = new byte[send_buffer_size];
        rnd.nextBytes(buff);

        while(true)
        {
          sock.getOutputStream().write(buff);
        }

      }
      catch(java.io.IOException e)
      {
        try { sock.close(); } catch(Throwable t) { t.printStackTrace(); }
      }

    }
  }

}
