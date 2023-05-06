package net.yury.netty.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Custom1ServerHandler extends ChannelInboundHandlerAdapter {
    public final static Custom1ServerHandler instance = new Custom1ServerHandler();
    public final static ObjectMapper mapper = new ObjectMapper();
    public final static Random random = new Random();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("new Connection!");
        ctx.write("Welcome to Custom1Server!");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long time1 = System.currentTimeMillis();
        String request = (String) msg;
        System.out.println("[client handler] msg: " + request);
        if ("bye".equals(request)) {
            ChannelFuture channelFuture = ctx.writeAndFlush("have a good day!");
            channelFuture.addListener(ChannelFutureListener.CLOSE);
            return;
        }
        TimeUnit.MILLISECONDS.sleep(200L);
        ObjectNode requestNode = (ObjectNode)mapper.readTree(request);
        String traceId = requestNode.get("trace_id").asText();
        ArrayNode codes = (ArrayNode)requestNode.get("codes");
        Map<String, Object> res = new HashMap<>();
        res.put("trace_id", traceId);
        for (JsonNode code : codes) {
            String c = code.asText();
            res.put(c, random.nextLong());
        }
        String r = mapper.writeValueAsString(res);
        ChannelFuture f = ctx.writeAndFlush(r);
        f.sync();
        long time2 = System.currentTimeMillis();
        System.out.println("[client handler] msg: trace_id: " + traceId + ", cost time: " + (time2 - time1) + "ms");
        f.addListener(ChannelFutureListener.CLOSE);
    }
}
