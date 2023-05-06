package net.yury.netty.custom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义TCP报文协议解码器，接收请求时使用
 * 前四个字节代表int型长度，表示该请求的报文长度（不包含int4个字节本身），后面字节都是UTF-8型的字符串
 */
public class Custom1Decoder extends ByteToMessageDecoder {
    public final static Custom1Decoder instance = new Custom1Decoder();

    public final static byte[] HEAD = new byte[] {8, 8, 8, 8};
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] head = new byte[4];
        byteBuf.readBytes(head);
        if (!Arrays.equals(head, HEAD)) {
            return;
        }
        int length = byteBuf.readInt();
        String request = byteBuf.readCharSequence(length, StandardCharsets.UTF_8).toString();
        System.out.println("[decoder] msg: " + request);
        list.add(request);
    }
}
