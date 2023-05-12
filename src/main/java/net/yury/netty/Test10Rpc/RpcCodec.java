package net.yury.netty.Test10Rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author yury757
 * 以Message为消息体的编解码
 */
public class RpcCodec extends ByteToMessageCodec<Message> {
    private boolean isServer;
    private ObjectMapper mapper = new ObjectMapper();

    public RpcCodec() {
        this(true);
    }

    public RpcCodec(boolean isServer) {
        super();
        this.isServer = isServer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] bytes = mapper.writeValueAsString(msg).getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        CharSequence cs = in.readCharSequence(length, StandardCharsets.UTF_8);
        // 如果是服务端则解析成请求消息，如果是客户端则解析成响应消息
        Class<?> clazz = isServer? RpcRequestMessage.class: RpcResponseMessage.class;
        Object message = mapper.readValue(cs.toString(), clazz);
        out.add(message);
    }
}
