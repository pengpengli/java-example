package com.teclick.arch.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by pengli on 2018-01-08.
 */
public class ChineseProverbClient {

    private Logger logger = LoggerFactory.getLogger(ChineseProverbClient.class);

    private void run(int port) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)//允许广播
                    .handler(new ChineseProverbClientHandler());//设置消息处理器
            Channel ch = b.bind(0).sync().channel();
            //向网段内的所有机器广播UDP消息。
            ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语字典查询？", CharsetUtil.UTF_8), new InetSocketAddress("255.255.255.255", port))).sync();
            if (!ch.closeFuture().await(15000)) {
                logger.warn("查询超时！");
            }

            group.shutdownGracefully();
        } catch (Exception e) {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new ChineseProverbClient().run(port);
    }
}
