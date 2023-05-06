package net.yury.netty.custom;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

public class Custom1ClientHandler extends SimpleChannelInboundHandler<String> {
    public static final List<String> list = new ArrayList<>();
    public static final Custom1ClientHandler instance = new Custom1ClientHandler();

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println(msg);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        list.add(msg);
        System.out.println("[client handler] msg: " + msg);
    }
}
