package com.teclick.arch.netty.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 581854 on 2018-01-08 11:48.
 */
public class ChineseProverbClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private Logger logger = LoggerFactory.getLogger(ChineseProverbServerHandler.class);

    /**
     * DatagramPacket的详细介绍，看服务器的代码注释，这里不重复了。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String response = msg.content().toString(CharsetUtil.UTF_8);
        if (response.startsWith("谚语查询结果：")) {
            ctx.close();
            logger.info(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.warn("exceptionCaught", cause);
    }

}
