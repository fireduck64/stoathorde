# Overview

This is a super simple network transit test between two points.
You configure the amount of data to send, the number of connections to use.

It uses random data, build the data is created once per connection and then repeated
to avoid CPU strain.  However, if the connection goes via a VPN with a sufficently large compression
window (32mb) it might actually manage to compress it via the repeats.  This seems unlikely.

Data flows from server to client.  The server just sends data to connecting clients until they go away.

# Building

To build:
* Install bazel - https://docs.bazel.build/versions/master/install.html
* Run:
```
  bazel build :all :StoatClient_deploy.jar :StoatServer_deploy.jar
```

If you don't want to build to mess with bazel, there are built jar files attached to the release on github.

# Running

There are multiple options.  If you have bazel, you can run it from the build directory like:

```
  bazel-bin/StoatServer <port>
  bazel-bin/StoatClient <host> <port> <connections> <gb_to_transfer>
```
Or if using the deploy jar files:

```
  java -jar StoatServer_deploy.jar <port>
  java -jar StoatClient <host> <port> <connections> <gb_to_transfer>
```

# Examples:

## On my local 1gb network
On one machine:

```
bazel-bin/StoatServer 11111
```

On another machine:
```
 java -jar StoatClient_deploy.jar ogog.1209k.com 11111 10 10
 Total rate: 10737418240 bytes transfered in 109.219 seconds - 98.311 MB/sec
```

## A machine talking to itself

Same server as before
```
 bazel-bin/StoatClient localhost 11111 20 20
 Total rate: 21474836480 bytes transfered in 4.737 seconds - 4.533 GB/sec
```

## Google Cloud Platform

Between two n1-standad-1 instances in same zone

```
java -jar StoatClient_deploy.jar 10.128.0.8 11111 20 20
Total rate: 21474836480 bytes transfered in 90.539 seconds - 237.189 MB/sec
```



# Future Features

If anyone actually wants me to, I could turn this into a way more cool tool.
I'm imagining a fabric saturation tool that uses a combo of specifided hosts
and multicast to find peers.  Then it connects to peers and tries to push as much data
as possible around the network.  Then using those same channels statistics could be exchanged
to show to total bandwidth the tool was able to use.  Or each node could send logs to something for collection.

Either way, it would be a way to use whatever machines were availible to stress test a network.

