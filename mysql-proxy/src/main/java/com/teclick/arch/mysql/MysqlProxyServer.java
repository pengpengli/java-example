package com.teclick.arch.mysql;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Created by pengli on 2017-11-03.
 */
public class MysqlProxyServer {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static final Logger logger = LoggerFactory.getLogger(MysqlProxyServer.class);

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();
        options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        options.setFileResolverCachingEnabled(false);
        Vertx.vertx(options).deployVerticle(new MysqlProxyServerVerticle());
    }

    public static class MysqlProxyServerVerticle extends AbstractVerticle {
        private final int port = 3306;
        private final String mysqlHost = "127.0.0.1";

        @Override
        public void start() throws Exception {
            NetServer netServer = vertx.createNetServer();//创建代理服务器
            NetClient netClient = vertx.createNetClient();//创建连接mysql客户端

            netServer.connectHandler(socket -> netClient.connect(port, mysqlHost, result -> {
                //响应来自客户端的连接请求，成功之后，在建立一个与目标mysql服务器的连接
                if (result.succeeded()) {
                    //与目标mysql服务器成功连接连接之后，创造一个MysqlProxyConnection对象,并执行代理方法

                    //socket.write("haha");
                    /*
                    socket.handler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {

                        }
                    });
                    */
                    new MysqlProxyConnection(socket, result.result()).proxy();
                } else {
                    logger.error(result.cause().getMessage(), result.cause());
                    socket.close();
                }
            })).listen(3307, listenResult -> {//代理服务器的监听端口
                if (listenResult.succeeded()) {
                    //成功启动代理服务器
                    logger.info("Mysql proxy server start up.");
                } else {
                    //启动代理服务器失败
                    logger.error("Mysql proxy exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
                    System.exit(1);
                }
            });
        }
    }

    static class MysqlProxyConnection {
        private final NetSocket clientSocket;
        private final NetSocket serverSocket;

        MysqlProxyConnection(NetSocket clientSocket, NetSocket serverSocket) {
            this.clientSocket = clientSocket;
            this.serverSocket = serverSocket;
        }

        private void proxy() {
            //当代理与mysql服务器连接关闭时，关闭client与代理的连接
            serverSocket.closeHandler(v -> clientSocket.close());
            //反之亦然
            clientSocket.closeHandler(v -> serverSocket.close());

            //不管那端的连接出现异常时，关闭两端的连接
            serverSocket.exceptionHandler(e -> {
                logger.error(e.getMessage(), e);
                close();
            });
            clientSocket.exceptionHandler(e -> {
                logger.error(e.getMessage(), e);
                close();
            });

            //当收到来自客户端的数据包时，转发给mysql目标服务器
            clientSocket.handler(serverSocket::write);
            //当收到来自mysql目标服务器的数据包时，转发给客户端
            serverSocket.handler(clientSocket::write);
        }

        private void close() {
            clientSocket.close();
            serverSocket.close();
        }
    }
}
