package com.teclick.arch.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Created by 581854 on 2017-11-03 15:03.
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
        Vertx.vertx(options).deployVerticle(new HttpMappedServerVerticle());
    }

    public static class HttpMappedServerVerticle extends AbstractVerticle {
        private final int port = 8080;
        private final int targetPort = 8081;
        private final String targetHost = "127.0.0.1";//"10.102.32.88";

        @Override
        public void start() {
            HttpServerOptions serverOptions = new HttpServerOptions().setLogActivity(false);
            HttpServer httpServer = vertx.createHttpServer(serverOptions);

            HttpClientOptions clientOptions = new HttpClientOptions().setLogActivity(false);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpServer.requestHandler(request -> {

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.setHost(targetHost);
                requestOptions.setPort(targetPort);
                requestOptions.setURI(request.uri());

                HttpClientRequest localRequest = httpClient.request(request.method(), requestOptions, response -> {
                    request.response().setStatusCode(response.statusCode());
                    request.response().headers().clear().addAll(response.headers());
                    response.bodyHandler(bodyBuffer -> request.response().end(bodyBuffer));
                });

                localRequest.headers().addAll(request.headers());
/*
                request.connection().closeHandler(handler -> {
                    localRequest.connection().close();
                });

                request.connection().exceptionHandler(e -> {
                    request.connection().close();
                });
*/
                localRequest.exceptionHandler(handler -> {
                    localRequest.connection().close();
                    request.connection().close();
                });

                request.endHandler(handler -> localRequest.end());

                switch (request.method()) {
                    case PUT: {
                        //call service to check
                    }
                    case POST: {
                        request.bodyHandler(localRequest::write);
                        break;
                    }
                    case GET: {
                        String path = request.uri().toLowerCase();
                        if (path.endsWith(".zip") || path.endsWith(".war")) {
                            // spec file can't download
                        }
                    }
                    default: {
                        // Other no request body of http method
                    }
                }

            }).listen(port, listenResult -> { //代理服务器的监听端口
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
