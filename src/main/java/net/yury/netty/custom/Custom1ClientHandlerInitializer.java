package net.yury.netty.custom;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class Custom1ClientHandlerInitializer extends ChannelInitializer<SocketChannel> {
    public final static Custom1ClientHandlerInitializer instance = new Custom1ClientHandlerInitializer();

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
//                .addLast(new DelimiterBasedFrameDecoder(8192, Unpooled.wrappedBuffer(new byte[] {'\0'})))
//                .addLast(Custom1Codec.instance)
                .addLast(Custom1Decoder.instance)
                .addLast(Custom1Encoder.instance)
                .addLast(Custom1ClientHandler.instance);
    }
}
