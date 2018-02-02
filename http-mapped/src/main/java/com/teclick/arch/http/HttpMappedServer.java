package com.teclick.arch.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static io.vertx.core.impl.FileResolver.DISABLE_CP_RESOLVING_PROP_NAME;
import static io.vertx.core.impl.FileResolver.DISABLE_FILE_CACHING_PROP_NAME;

/**
 * Created by pengli on 2017-11-03.
 */
public class HttpMappedServer {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        System.setProperty(DISABLE_FILE_CACHING_PROP_NAME, "false");
        System.setProperty(DISABLE_CP_RESOLVING_PROP_NAME, "false");
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpMappedServer.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        Vertx.vertx(options).deployVerticle(new HttpMappedServerVerticle());
    }

    public static class HttpMappedServerVerticle extends AbstractVerticle {

        private static final int PORT = 8081;
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
                    .setConnectTimeout(10000)
                    .setDefaultHost(TARGET_HOST)
                    .setDefaultPort(TARGET_PORT);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpClient.redirectHandler(response -> null);

            httpServer.requestHandler(request -> {

                HttpServerResponse serverResponse = request.response();

                request.exceptionHandler(error -> {
                    serverResponse.setStatusCode(400).end();
                    serverResponse.close();
                    logger.info("Client request issue, close response");
                }).connection().exceptionHandler(error -> {
                    serverResponse.close();
                    logger.info("Client connection issue, close response. " + error.getClass().getSimpleName());
                });

                HttpClientRequest localRequest = httpClient.request(request.method(), request.uri(), response -> {
                    serverResponse.setStatusCode(response.statusCode());
                    serverResponse.setStatusMessage(response.statusMessage());
                    serverResponse.headers().clear().addAll(response.headers());

                    String chunked = response.getHeader("Transfer-Encoding");
                    serverResponse.setChunked((chunked != null) && ("chunked".equalsIgnoreCase(chunked)));

                    response.endHandler(handler -> serverResponse.end());
                    response.handler(serverResponse::write);
                }).exceptionHandler(error -> {
                    //io.vertx.core.VertxException
                    //io.netty.channel.AbstractChannel$AnnotatedConnectException
                    serverResponse.setStatusCode(500).end();
                    serverResponse.close();
                }).setTimeout(300 * 1000).setFollowRedirects(true).connectionHandler(connection -> connection.exceptionHandler(error -> {
                    logger.error("Local http client connection exception", error);
                }));

                localRequest.headers().clear().addAll(request.headers());

                request.endHandler(handler -> localRequest.end());
                request.handler(localRequest::write);

            }).exceptionHandler(err -> logger.error("Http Server", err)).listen(PORT, "local.sfbest.com", listenResult -> {
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
