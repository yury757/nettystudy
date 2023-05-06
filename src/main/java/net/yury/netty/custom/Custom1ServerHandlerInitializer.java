package net.yury.netty.custom;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class Custom1ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    public static final Custom1ServerHandlerInitializer instance = new Custom1ServerHandlerInitializer();

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
//                .addLast(new DelimiterBasedFrameDecoder(8192, Unpooled.wrappedBuffer(new byte[] {'\0'})))
//                .addLast(Custom1Codec.instance)
                .addLast(Custom1Decoder.instance)
                .addLast(Custom1Encoder.instance)
                .addLast(Custom1ServerHandler.instance);
    }
}
