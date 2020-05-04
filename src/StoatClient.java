package duckutil.stoathorde;

import java.util.concurrent.Semaphore;
import java.net.Socket;
import java.text.DecimalFormat;


public class StoatClient
{
  public static void main(String args[]) throws Exception
  {
    if (args.length != 4)
    {
      System.out.println("Syntax: <host> <port> <connection_count> <gb_to_xfer>");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);
    int conn_count = Integer.parseInt(args[2]);
    long gb = Long.parseLong(args[3]);

    new StoatClient(host, port, conn_count, gb);
  }

  private final String host;
  private final int port;
  private final long bytes_per_client;
  private final Semaphore complete_sem;
  

  public StoatClient(String host, int port, int conn_count, long total_gb)
    throws Exception
  {
    this.host = host;
    this.port = port;

    complete_sem = new Semaphore(0);

    bytes_per_client = total_gb * 1024*1024*1024 / conn_count;


    long tm_start = System.currentTimeMillis();
    for(int i=0;i<conn_count; i++)
    {
      new ClientThread().start();
    }
    
    complete_sem.acquire(conn_count);
    long tm_end = System.currentTimeMillis();

    System.out.println("Total rate: " + getRate( total_gb * 1024L * 1024L * 1024L, tm_end - tm_start));



  }

  public static String getRate(long bytes, long ms)
  {
    double sec = ms / 1000.0;
    double rate = bytes / sec;
    DecimalFormat df = new DecimalFormat("0.000");

    String unit="B";
    if (rate > 1000.0){unit="KB"; rate/=1000.0; }
    if (rate > 1000.0){unit="MB"; rate/=1000.0; }
    if (rate > 1000.0){unit="GB"; rate/=1000.0; }
    if (rate > 1000.0){unit="TB"; rate/=1000.0; }

    return String.format("%d bytes transfered in %s seconds - %s %s/sec",
      bytes, df.format(sec), df.format(rate), unit);

  }

  public class ClientThread extends Thread
  {
    public void run()
    {
      try
      {
        Socket sock = new Socket(host, port);
        byte[] buff = new byte[StoatServer.send_buffer_size];

        long read = 0;
        while(read < bytes_per_client)
        {
          long buff_len = buff.length;
          long to_read_l = Math.min( buff_len, bytes_per_client - read);
          int to_read = (int) to_read_l;

          long r = sock.getInputStream().read(buff,0, to_read);
          if (r<0) throw new Exception("Unexpected EOF");
          read += r;
        }

        complete_sem.release(1);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }


    }
  }



}
