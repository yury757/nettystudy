package net.yury.netty.custom;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Custom1Codec extends ByteToMessageCodec<String> {
    public final static Custom1Decoder instance = new Custom1Decoder();

    public final static byte[] HEAD = new byte[] {8, 8, 8, 8};
    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        System.out.println(msg);
        out.writeBytes(HEAD);
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] head = new byte[4];
        in.readBytes(head);
        if (!Arrays.equals(head, HEAD)) {
            return;
        }
        int length = in.readInt();
        String request = in.readCharSequence(length, StandardCharsets.UTF_8).toString();
        System.out.println(request);
        out.add(request);
    }
}
