package duckutil.stoathorde;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class FruitBatServer
{
  public static final int buffer_size = 8192;  

  public static void main(String args[]) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Syntax FruitBatServer <port>");
      return;
    }

    int port = Integer.parseInt(args[0]);
    new FruitBatServer(port);

  }

  public FruitBatServer(int port) throws Exception
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
      System.out.println("New link: " + sock);
    }

    public void run()
    {
      try
      {

        byte[] buff = new byte[buffer_size];
        while(true)
        {
          int r = sock.getInputStream().read(buff);
          if (r > 0)
          {
            sock.getOutputStream().write(buff,0,r);
          }
        }
      }
      catch(java.io.IOException e)
      {
        try { sock.close(); } catch(Throwable t) { t.printStackTrace(); }
      }

    }
  }

}
