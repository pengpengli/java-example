package com.teclick.arch.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Created by pengli on 2017-11-03.
 */
public class HttpMappedServer {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpMappedServer.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        //options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        options.setFileResolverCachingEnabled(false);
        Vertx.vertx(options).deployVerticle(new HttpMappedServerVerticle());
    }

    public static class HttpMappedServerVerticle extends AbstractVerticle {

        private static final int PORT = 8080;
        private static final int TARGET_PORT = 8081;
        private static final String TARGET_HOST = "localhost";

        @Override
        public void start() {
            vertx.getOrCreateContext().exceptionHandler(err -> logger.error("Vertx context exception", err));

            vertx.exceptionHandler(err -> logger.error("Vertx exception", err));

            HttpServerOptions serverOptions = new HttpServerOptions()
//                    .setLogActivity(true)
                    .setIdleTimeout(60);
            HttpServer httpServer = vertx.createHttpServer(serverOptions);
            httpServer.exceptionHandler(err -> logger.error("Server met exception", err));

            HttpClientOptions clientOptions = new HttpClientOptions()
//                    .setLogActivity(true)
                    .setDefaultHost(TARGET_HOST)
                    .setDefaultPort(TARGET_PORT);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpServer.requestHandler(request -> {
                HttpServerResponse serverResponse = request.response();
                HttpClientRequest localRequest = httpClient.request(request.method(), request.uri(), response -> {
                    serverResponse.setStatusCode(response.statusCode());
                    serverResponse.setStatusMessage(response.statusMessage());

                    serverResponse.headers().clear();
                    response.headers().forEach((entry) -> {
                        String key = entry.getKey();
                        if ("Location".equalsIgnoreCase(key)) {
                            String value = entry.getValue().replace(TARGET_HOST + ":" + TARGET_PORT, request.headers().get("HOST"));
                            serverResponse.putHeader(key, value);
                        } else {
                            serverResponse.putHeader(key, entry.getValue());
                        }
                    });

                    String chunked = response.getHeader("Transfer-Encoding");
                    serverResponse.setChunked((chunked != null) && ("chunked".equalsIgnoreCase(chunked)));

                    response.endHandler(handler -> serverResponse.end());
                    response.handler(serverResponse::write);
                }).exceptionHandler(error -> {
                    //io.vertx.core.VertxException
                    //io.netty.channel.AbstractChannel$AnnotatedConnectException
                    serverResponse.setStatusCode(500);
                    serverResponse.end();
                    logger.error("Local http request exception", error);
                }).setTimeout(300 * 1000);

                localRequest.connectionHandler(connection -> connection.exceptionHandler(error -> {
                    logger.error("Local http client connection exception", error);
                }));

                localRequest.headers().clear().addAll(request.headers());

                request.exceptionHandler(handler -> request.connection().close());

                request.endHandler(handler -> localRequest.end());
                request.handler(localRequest::write);

            }).exceptionHandler(err -> logger.error("Http Server", err)).listen(PORT, listenResult -> {
                if (listenResult.succeeded()) {
                    logger.info("http mapped server start up.");
                } else {
                    vertx.close();
                    logger.error("http mapped exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
                    System.exit(1);
                }
            });
        }
    }

}
