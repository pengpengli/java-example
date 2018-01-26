package com.teclick.arch.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 581854 on 2017-11-03 15:03.
 */
public class HttpMappedServer {

    //mvn deploy:deploy-file -Durl=http://127.0.0.1:8081/artifactory/libs-release-local -DrepositoryId=central -Dfile=security-db-driver-mysql-3.0.11.master.jar -DgroupId=com.sfbest.arch.jdbc -DartifactId=security-db-driver -Dversion=3.0.11.master

    private static final Logger logger = LoggerFactory.getLogger(HttpMappedServer.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        Vertx.vertx(options).deployVerticle(new HttpMappedServerVerticle());
    }

    public static class HttpMappedServerVerticle extends AbstractVerticle {
        private final int port = 8081;
        private final int targetPort = 8080;
        private final String targetHost = "127.0.0.1";//"10.102.32.88";

        @Override
        public void start() throws Exception {
            HttpServerOptions serverOptions = new HttpServerOptions().setLogActivity(false);
            HttpServer httpServer = vertx.createHttpServer(serverOptions);

            HttpClientOptions clientOptions = new HttpClientOptions().setLogActivity(true);
            HttpClient httpClient = vertx.createHttpClient(clientOptions);

            httpServer.requestHandler(request -> {

                request.connection().closeHandler(handler -> {
                    request.connection().close();
                });

                request.connection().exceptionHandler(e -> {
                    logger.debug("Client closed ......");
                    request.connection().close();
                });

                if ("PUT".equals(request.method().name())) {
                    logger.debug("Request URI: " + request.uri());
                }

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

                request.endHandler(handler -> {
                    localRequest.end();
                });

                request.bodyHandler(buffer -> {
                    localRequest.write(buffer);
                });

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
