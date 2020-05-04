# Overview

This is a super simple network transit test between two points.
You configure the amount of data to send, the number of connections to use.

# Building

To build:
* Install bazel - https://docs.bazel.build/versions/master/install.html
* Run:
```
  bazel build :all :StoatClient_deploy.jar :StoatServer_deploy.jar
```

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

On one machine:

```
bazel-bin/StoatServer 11111
```

On another machine:
```
java -jar StoatClient_deploy.jar ogog.1209k.com 11111 10 10
Total rate: 10737418240 bytes transfered in 109.219 seconds - 98.311 MB/sec
```


