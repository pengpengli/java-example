package com.teclick.arch.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Map;

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
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        options.setFileResolverCachingEnabled(false);
        Vertx.vertx(options).deployVerticle(new HttpMappedServerVerticle());
    }

    public static class HttpMappedServerVerticle extends AbstractVerticle {

        private final int port = 8081;
        private final int targetPort = 8080;
        private final String targetHost = "localhost";

        @Override
        public void start() {
            HttpServerOptions serverOptions = new HttpServerOptions()
//                    .setLogActivity(true)
                    .setIdleTimeout(60);
            HttpServer httpServer = vertx.createHttpServer(serverOptions);
            httpServer.exceptionHandler(throwable -> logger.error("Server met exception", throwable));

            HttpClientOptions clientOptions = new HttpClientOptions()
//                    .setLogActivity(true)
                    .setDefaultHost(targetHost)
                    .setDefaultPort(targetPort);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpServer.requestHandler(request -> {
                HttpClientRequest localRequest = httpClient.request(request.method(), request.uri(), response -> {
                    HttpServerResponse serverResponse = request.response();
                    serverResponse.setStatusCode(response.statusCode());
                    serverResponse.setStatusMessage(response.statusMessage());
                    serverResponse.headers().clear();
                    for (Map.Entry<String, String> entry : response.headers()) {
                        String key = entry.getKey();
                        if ("Location".equalsIgnoreCase(key)) {
                            String value = entry.getValue().replace(targetHost, request.headers().get("HOST"));
                            serverResponse.putHeader(key, value);
                        } else {
                            serverResponse.putHeader(key, entry.getValue());
                        }
                    }

                    String chunked = response.getHeader("Transfer-Encoding");
                    serverResponse.setChunked((chunked != null) && ("chunked".equalsIgnoreCase(chunked)));

                    response.handler(serverResponse::write);
                    response.endHandler(handler -> serverResponse.end());
                });

                localRequest.headers().clear();
                for (Map.Entry<String, String> entry : request.headers()) {
                    String key = entry.getKey();
                    if ("HOST".equalsIgnoreCase(key)) {
                        localRequest.headers().add(key, targetHost);
                    } else {
                        localRequest.headers().add(key, entry.getValue());
                    }
                }

                localRequest.exceptionHandler(handler -> {
                    if (null != localRequest.connection()) {
                        localRequest.connection().close();
                    }
                    request.connection().close();
                });

                request.exceptionHandler(handler -> {
                    request.connection().close();
                    if (null != localRequest.connection()) {
                        localRequest.connection().close();
                    }
                });

                request.endHandler(handler -> localRequest.end());

                switch (request.method()) {
                    case GET: {
                        String path = request.path().toLowerCase();
                        if (path.endsWith(".zip") || path.endsWith(".war")) {
                            System.out.println("zip or war");
                        }
                    }
                    case HEAD:
                    case DELETE: {
                        break;
                    }
                    case PUT: {
                        System.out.println("PUT");
                    }
                    case POST:
                    default: {
                        request.handler(localRequest::write);
                    }
                }

            }).listen(port, listenResult -> {
                if (listenResult.succeeded()) {
                    logger.info("http mapped server start up.");
                } else {
                    logger.error("http mapped exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
                    System.exit(1);
                }
            });
        }
    }

}
