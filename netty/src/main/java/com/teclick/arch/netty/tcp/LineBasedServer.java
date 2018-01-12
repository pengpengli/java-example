package com.teclick.arch.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by 581854 on 2018-01-08 10:18.
 */
public class LineBasedServer {

    private int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2;

    private EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    private EventLoopGroup workerGroup = new NioEventLoopGroup(BIZGROUPSIZE);

    private void bind(int port) throws Exception {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LineBasedFrameDecoder(1024));
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());

                            p.addLast(new LineServerHandler());
                        }
                    });

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        LineBasedServer server = new LineBasedServer();
        server.bind(8080);
        server.shutdown();
    }
}
