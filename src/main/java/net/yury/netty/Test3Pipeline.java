package net.yury.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Test3Pipeline {
    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        ServerBootstrap bs = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        bs.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new StringDecoder())
                                // 添加处理器：head -> h1 -> h2 -> h3 -> h4 -> tail
                                // 注意：
                                // 1、pipeline工作时，inbound是按照代码addLast加入的顺序执行的，而outbound是倒序执行的。
                                // 2、handler分入站处理器和出站处理器，netty对handler的执行逻辑是：
                                // （1）从head开始按顺序执行入站处理器，直到某个处理器调用了 writeAndFlush()
                                // （2）若是channel调用的 writeAndFlush()，则从tail开始反方向调用出站处理器；若是ctx调用的 writeAndFlush()，则从当前节点位置反方向调用出站处理器
                                .addLast("inbound1", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("inbound1 receive msg: " + msg.toString());
                                        String s = (String) msg;
                                        Student student = new Student(s);
                                        // 注意：要将对应数据传到pipeline的下一个inbound handler时
                                        // 必须在结束时调用 {@link ChannelInboundHandlerAdapter#channelRead(ChannelHandlerContext, Object)}
                                        // 否则pipeline会断掉了
                                        super.channelRead(ctx, student);
                                    }
                                })
                                .addLast("inbound2", new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.debug("inbound2 receive msg: " + msg.toString());
                                        Student student = (Student) msg;
                                        student.setName(student.name + "-inbound2");
                                        // 注意：
                                        // 1、最后一个inbound handler如果写了数据，才会调用后面的outbound handler
                                        // 写入的数据对应outbound handler中的msg参数；否则即使有outbound handler也不会调用
                                        // 2、ctx.channel().writeAndFlush() 和 ctx.writeAndFlush() 方法的区别在于
                                        // 前者是让整个channel开始出站，因此会离开read链条，进入write链条
                                        // 而后者是在当前链条所在节点开始出站，出站是倒序的，因此会在当前链条往前找的outbound handler，因此后面的handler都会被跳过了
                                        ctx.channel().writeAndFlush(student);
                                        // ctx.writeAndFlush(student);
                                    }
                                })
                                .addLast("outbound1", new ChannelOutboundHandlerAdapter() {
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.debug("outbound1 receive msg: " + msg.toString());
                                        Student student = (Student)msg;
                                        student.setName(student.name + "-outbound1");
                                        // 注意：同样，出站处理器需要调用 ctx.write(msg, promise); 否则链条会断掉了
                                        super.write(ctx, student, promise);
                                    }
                                })
                                .addLast("outbound2", new ChannelOutboundHandlerAdapter() {
                                    @Override
                                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                        log.debug("outbound2 receive msg: " + msg.toString());
                                        Student student = (Student)msg;
                                        student.setName(student.name + "-outbound2");
                                        super.write(ctx, msg, promise);
                                    }
                                })
                                .addLast(new StringEncoder());
                    }
                })
                .bind(new InetSocketAddress("0.0.0.0", port)).sync().channel().closeFuture().sync();
    }

    static class Student {
        private String name;
        public Student(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
