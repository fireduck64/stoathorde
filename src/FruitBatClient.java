package duckutil.stoathorde;

import java.util.concurrent.Semaphore;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Random;

import duckutil.RateLimit;
import duckutil.StatData;
import java.util.concurrent.LinkedBlockingQueue;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.io.DataInputStream;


public class FruitBatClient
{
  public static void main(String args[]) throws Exception
  {
    if (args.length != 4)
    {
      System.out.println("Syntax: <host> <port> <speed> <minutes_to_test>");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);
    double speed_mbps = Double.parseDouble(args[2]);
    int minutes = Integer.parseInt(args[3]);

    new FruitBatClient(host, port, speed_mbps, minutes);
  }

  private final String host;
  private final int port;
  private final RateLimit limit;
  private final int duration_minutes;
  private final Socket sock;
  private final long start_time;
  private final long end_time;
  private final StatData stats;
  private final LinkedBlockingQueue<ChunkInfo> queue;

  
  private volatile long data_xfer = 0L;
  private volatile long last_data_time = System.currentTimeMillis();


  public FruitBatClient(String host, int port, double speed_mbps, int minutes)
    throws Exception
  {
    this.host = host;
    this.port = port;

    limit = new RateLimit(speed_mbps * 1e6 / 8, 1.0);

    this.duration_minutes = minutes;
    sock = new Socket(host, port);
    start_time = System.currentTimeMillis();
    end_time = start_time + duration_minutes * 60L * 1000L;
    System.out.println(String.format("Starting test run of %f mbps for %d minutes", speed_mbps, minutes));
    stats = new StatData();
    queue = new LinkedBlockingQueue<ChunkInfo>();

    new SendThread().start();
    new RecvThread().start();

    DecimalFormat df = new DecimalFormat("0.0");
    while((System.currentTimeMillis() < end_time) || (queue.size() > 0))
    {
      Thread.sleep(10000);
      stats.print("delay(microsec)", df);
      System.out.println(getRate(data_xfer, last_data_time - start_time));

    }
    Thread.sleep(5000);
    stats.print("delay(microsec)", df);
    System.out.println(getRate(data_xfer, last_data_time - start_time));


  }

  public static String getRate(long bytes, long ms)
  {
    double sec = ms / 1000.0;
    double rate = bytes * 8 / sec;
    DecimalFormat df = new DecimalFormat("0.000");

    String unit="B";
    if (rate > 1000.0){unit="Kbit"; rate/=1000.0; }
    if (rate > 1000.0){unit="Mbit"; rate/=1000.0; }
    if (rate > 1000.0){unit="Gbit"; rate/=1000.0; }
    if (rate > 1000.0){unit="Tbit"; rate/=1000.0; }

    return String.format("%d bytes transfered in %s seconds - %s %s/sec",
      bytes, df.format(sec), df.format(rate), unit);
  }

  public class SendThread extends Thread
  {
    public void run()
    {
      try
      {

        byte[] buffer = new byte[ FruitBatServer.buffer_size ];
        Random rnd = new Random();

        while(System.currentTimeMillis() < end_time)
        {
          rnd.nextBytes(buffer);
          limit.waitForRate( buffer.length );
          ChunkInfo ci = new ChunkInfo(buffer);
          queue.add(ci);
          sock.getOutputStream().write(buffer);
        }
      }
      catch(Throwable e)
      {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }
  public class RecvThread extends Thread
  {
    public void run()
    {
      try
      {
        byte[] buffer = new byte[ FruitBatServer.buffer_size ];
        DataInputStream d_in = new DataInputStream( sock.getInputStream() );

        while((System.currentTimeMillis() < end_time) || (queue.size() > 0))
        {
          ChunkInfo ci = queue.poll(1, TimeUnit.SECONDS); 
          if (ci != null)
          {
            d_in.readFully(buffer);
            long recv_time = System.nanoTime();
            long delay = (recv_time - ci.send_time)/1000;
            ci.check(buffer);

            data_xfer += buffer.length;
            last_data_time = System.currentTimeMillis();
            stats.addDataPoint(delay);
          }
        }
      }
      catch(Throwable e)
      {
        e.printStackTrace();
        System.exit(1);
      }


    }

  }


  public class ChunkInfo
  {
    public final long send_time;
    public final String hash;

    public ChunkInfo(byte[] buffer)
      throws Exception
    {
      this.send_time = System.nanoTime();
      this.hash = hashBytes(buffer);

    }
    public void check(byte[] buffer)
      throws Exception
    {
      String found = hashBytes(buffer);
      if (!found.equals(hash))
      {
        throw new Exception("Expected data mismatch");
      }
    }

  }

  public static String hashBytes(byte[] buff)
    throws Exception
  {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    
    return Base64.getEncoder().encodeToString(md.digest(buff));


  }



}
