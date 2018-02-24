package com.teclick.arch.http;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;

/**
 * Created by Nelson on 2018-02-04.
 */
public class RESTClient {

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setLogActivity(true)
                .setConnectTimeout(10000)
                .setDefaultHost("localhost")
                .setDefaultPort(8081);

        Vertx vertx = Vertx.vertx(options);
        HttpClient httpClient = vertx.createHttpClient(clientOptions);
        httpClient.request(HttpMethod.GET, "/artifactory/api/storage/libs-release-local/com/teclick/framework/app-config/3.0.0/app-config-3.0.0.jar").handler(response -> {
            response.bodyHandler(body -> {
                FileInfo fileInfo = body.toJsonObject().mapTo(FileInfo.class);
                System.out.println(fileInfo);
                httpClient.close();
                //vertx.close();
            }).exceptionHandler(error -> {
                httpClient.close();
                //vertx.close();
            });
        }).exceptionHandler(error -> {

        }).end();

    }
}
