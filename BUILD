package(default_visibility = ["//visibility:public"])

java_library(
  name = "stoathordelib",
  srcs = glob(["src/**/*.java", "src/*.java"]),
  deps = [
    "@duckutil//:basic_lib",
  ],
)

java_binary(
  name = "StoatClient",
  main_class = "duckutil.stoathorde.StoatClient",
  runtime_deps = [
    ":stoathordelib",
  ],
)

java_binary(
  name = "StoatServer",
  main_class = "duckutil.stoathorde.StoatServer",
  runtime_deps = [
    ":stoathordelib",
  ],
)

java_binary(
  name = "FruitBatServer",
  main_class = "duckutil.stoathorde.FruitBatServer",
  runtime_deps = [
    ":stoathordelib",
  ],
)

java_binary(
  name = "FruitBatClient",
  main_class = "duckutil.stoathorde.FruitBatClient",
  runtime_deps = [
    ":stoathordelib",
  ],
)






