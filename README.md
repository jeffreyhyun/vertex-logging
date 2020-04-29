### Running locally
```
// jar
$ mvn package
$ java -DENV=local -DLOG4J_CONFIGURATION_FILE=log/log4j2-local.xml -jar vertex-logging/target/logging-0.0.1.jar

// intelliJ
Main Class: com.poc.Application
Environment Variables: ENV=local;LOG4J_CONFIGURATION_FILE=log/log4j2-local.xml
```

### Issue
The documentation to replace logging with `log4j2` is very brief. I had it successfully setup to use `slf4j` using the `SLF4JLogDelegateFactory` but switching over to `Log4j2LogDelegateFactory` gives me some messages using `JULLogger`. An example of this are messages from the `vertx-mongo-client`.

Is this normal? If not, can you provide some insight as to what I'm doing wrong?

```java
public class Application {
  static {
    System.setProperty(
      "vertx.logger-delegate-factory-class-name",
      "io.vertx.core.logging.Log4j2LogDelegateFactory"
    );
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("com.....MyClass");
  }
}

@Log4j2
public class MyClass extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);

  private void startSomething() {
    log.info("yooooooooo");
  }

  private void startDb() {
    db = MongoClient.createShared(vertx, dbConfig);
  }

  @Override
  public void start() throws Exception {
    startDb();
    startSomething();
  }
}
```
slf4j
```
2020-04-28 19:12:50:678 -0400 [vert.x-eventloop-thread-0] INFO org.mongodb.driver.cluster - Cluster created with settings {hosts=[localhost:27017], mode=SINGLE, requiredClusterType=UNKNOWN, serverSelectionTimeout='30000 ms', maxWaitQueueSize=500}
2020-04-28 19:12:50:867 -0400 [cluster-ClusterId{value='5ea8b8722c77093a40223f7c', description='null'}-localhost:27017] INFO org.mongodb.driver.connection - Opened connection [connectionId{localValue:1, serverValue:230}] to localhost:27017
2020-04-28 19:12:50:874 -0400 [cluster-ClusterId{value='5ea8b8722c77093a40223f7c', description='null'}-localhost:27017] INFO org.mongodb.driver.cluster - Monitor thread successfully connected to server with description ServerDescription{address=localhost:27017, type=STANDALONE, state=CONNECTED, ok=true, version=ServerVersion{versionList=[4, 2, 1]}, minWireVersion=0, maxWireVersion=8, maxDocumentSize=16777216, logicalSessionTimeoutMinutes=30, roundTripTimeNanos=5510019}
2020-04-28 19:12:51:144 -0400 [vert.x-eventloop-thread-0] INFO com.blah.blah.blah.Blah - Server started on port: 8080
```
log4j2
```
Apr 28, 2020 7:01:40 PM com.mongodb.diagnostics.logging.JULLogger log
INFO: Cluster created with settings {hosts=[localhost:27017], mode=SINGLE, requiredClusterType=UNKNOWN, serverSelectionTimeout='30000 ms', maxWaitQueueSize=500}
2020-04-28 19:01:40.233082  INFO [vert.x-eventloop-thread-0] c.l.l.i.s.Blah: yooooooooo
Apr 28, 2020 7:01:40 PM com.mongodb.diagnostics.logging.JULLogger log
INFO: Opened connection [connectionId{localValue:1, serverValue:229}] to localhost:27017
Apr 28, 2020 7:01:40 PM com.mongodb.diagnostics.logging.JULLogger log
INFO: Monitor thread successfully connected to server with description ServerDescription{address=localhost:27017, type=STANDALONE, state=CONNECTED, ok=true, version=ServerVersion{versionList=[4, 2, 1]}, minWireVersion=0, maxWireVersion=8, maxDocumentSize=16777216, logicalSessionTimeoutMinutes=30, roundTripTimeNanos=4734654}
2020-04-28 19:01:40.381426  INFO [vert.x-eventloop-thread-0] c.l.l.i.s.Blah: Server started on port: 8080
```