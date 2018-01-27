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

        private final int port = 80;
        private final int targetPort = 8081;
        private final String targetHost = "localhost";

        @Override
        public void start() {
            HttpServerOptions serverOptions = new HttpServerOptions().setLogActivity(false);//.setIdleTimeout(1);
            HttpServer httpServer = vertx.createHttpServer(serverOptions);
            httpServer.exceptionHandler(throwable -> logger.error("Server met exception", throwable));

            HttpClientOptions clientOptions = new HttpClientOptions().setLogActivity(false).setKeepAlive(false);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpServer.requestHandler(request -> {

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setHost(targetHost);
                requestOptions.setPort(targetPort);
                requestOptions.setURI(request.uri());

                HttpClientRequest localRequest = httpClient.request(request.method(), requestOptions, response -> {
                    request.response().setStatusCode(response.statusCode());
                    request.response().setStatusMessage(response.statusMessage());
                    request.response().headers().clear().addAll(response.headers());
                    request.response().setChunked(true);

                    response.endHandler(handler -> request.response().end());
                    response.handler(handler -> request.response().write(handler));
                });

                localRequest.headers().clear();
                for (Map.Entry<String, String> entry: request.headers()) {
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

                request.exceptionHandler(handler ->{
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
                            // spec file can't download
                        }
                    }
                    case HEAD:
                    case DELETE: {
                        break;
                    }
                    case PUT: {
                        // Call verify service to check
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
