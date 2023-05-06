package net.yury.netty.custom;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Demo4Custom1Client {
//    private Channel channel;

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(Custom1ClientHandlerInitializer.instance);
            final Channel channel = client.connect("localhost", 8001).sync().channel();
            List<Thread> list = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                final int ii = i;
                list.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long time1 = System.currentTimeMillis();
                        String request = "{\"trace_id\":\"123" + ii + "\",\"codes\":[\"000001.SH@" + ii + "\"]}";
                        ChannelFuture lastFuture = channel.writeAndFlush(request);
//                DefaultChannelPromise promise = (DefaultChannelPromise)lastFuture;
//                Void value = promise.get();
//                        lastFuture.sync();
                        long time2 = System.currentTimeMillis();
                        System.out.println("cost time: " + (time2 - time1) + "ms");
                    }
                }));
            }
            for (Thread thread : list) {
                thread.start();
            }
            TimeUnit.MILLISECONDS.sleep(4000L);
            System.out.println(Custom1ClientHandler.list);
            // channel.writeAndFlush("bye").sync();
            channel.closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        Demo4Custom1Client client = new Demo4Custom1Client();
//        for (int i = 0; i < 20; i++) {
//            long time1 = System.currentTimeMillis();
//            String request = "{\"trace_id\":\"123" + i + "\",\"codes\":[\"000001.SH@" + i + "\"]}";
//            ChannelFuture lastFuture = client.channel.writeAndFlush(request);
//            lastFuture.sync();
//            long time2 = System.currentTimeMillis();
//            System.out.println("cost time: " + (time2 - time1) + "ms");
//        }
//        client.channel.writeAndFlush("bye").sync();
//    }

//    public String send(String request) throws InterruptedException {
//        ChannelFuture sync = channel.writeAndFlush(request).sync();
//        ChannelPromise promise = clientHandler.sendMessage(message);
//        promise.await(3, TimeUnit.SECONDS);
//        return clientHandler.getResponse();
//    }
//
//    public static void main(String[] args) {
//        for (int i = 0; i < 20; i++) {
//            String request = "{\"trace_id\":\"123" + i + "\",\"codes\":[\"000001.SH@" + i + "\"]}";
//            ChannelFuture lastFuture = channel.writeAndFlush(request);
//            lastFuture.sync();
//        }
//        channel.writeAndFlush("bye").sync();
//        channel.closeFuture().sync();
//
//    }
}
