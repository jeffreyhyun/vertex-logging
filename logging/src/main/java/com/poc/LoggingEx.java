package com.poc;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public class LoggingEx extends AbstractVerticle {
  private MongoClient db;
  private RedisClient cache;

  private void startServer() {
    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.get("/").handler(routingContext -> {
      log.info("test route");
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain");
      response.end("Hello World from Vert.x-Web!");
    });

    Integer serverPort = config().getJsonObject("server").getInteger("port");

    vertx.createHttpServer().requestHandler(router).listen(serverPort, r -> {
      if (r.succeeded()) {
        log.info("Server started on port: " + serverPort);
      } else if (r.failed()) {
        log.error("Unable to start server: " + r.cause().getMessage());
      }else {
        log.error("Unable to start server");
      }
    });
  }

  private void startDb() {
    JsonObject dbConfigObj = config().getJsonObject("db");
    String dbName = dbConfigObj.getString("name");
    String dbHost = dbConfigObj.getString("host");
    Integer dbPort = dbConfigObj.getInteger("port");

    JsonObject dbConfig = new JsonObject()
      .put("db_name", dbName)
      .put("host", dbHost)
      .put("port", dbPort);

    db = MongoClient.createShared(vertx, dbConfig);
  }

  private void startCache() {
    String redisHost = config().getJsonObject("redis").getString("host");
    Integer redisPort = config().getJsonObject("redis").getInteger("port");

    RedisOptions cacheConfig = new RedisOptions()
      .setHost(redisHost)
      .setPort(redisPort);

    cache = RedisClient.create(vertx, cacheConfig);
  }

  @Override
  public void start() throws Exception {
    String env = System.getenv("ENV") != null
      ? System.getenv("ENV")
      : "local";

    InputStream envConfigFilestream = this.getClass().getClassLoader().getResourceAsStream("app/application-" + env + ".json");
    byte[] envConfigbytes = IOUtil.toByteArray(envConfigFilestream);
    JsonObject configObj = new JsonObject(new String(envConfigbytes, StandardCharsets.UTF_8));

    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("json")
      .setConfig(configObj)
      .setOptional(false);

    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(fileStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

    retriever.getConfig(r -> {
      if (r.failed()) {
        log.error("failed to retrieve config.");
      } else {
        log.info("configuration:\n" + r.result().encodePrettily());

        config().mergeIn(r.result());

        startCache();
        startDb();
        startServer();
      }
    });
  }
}
