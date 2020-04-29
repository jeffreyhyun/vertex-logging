package com.poc;

import io.vertx.core.Vertx;

public class Application {
  static {
    System.setProperty(
      "vertx.logger-delegate-factory-class-name",
      "io.vertx.core.logging.Log4j2LogDelegateFactory"
    );
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("com.poc.LoggingEx");
  }
}