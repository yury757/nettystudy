package net.yury.netty.Test10Rpc.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import net.yury.netty.Test10Rpc.ComplicateClass;
import net.yury.netty.Test10Rpc.RpcRequestMessage;
import net.yury.netty.Test10Rpc.service.HelloService;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RpcClient {
    private static Channel channel = null;
    private static final AtomicLong REQUEST_ID = new AtomicLong(0);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public RpcClient() throws InterruptedException {
        Bootstrap bs = new Bootstrap();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        channel = bs.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcClientHandlerInitializer())
                .connect(new InetSocketAddress("localhost", 8080))
                .sync()
                .channel();
        channel.closeFuture().addListener(future -> workerGroup.shutdownGracefully());
    }

    public static void main(String[] args) throws InterruptedException {
        RpcClient client = new RpcClient();
        HelloService proxyService = client.getProxyService(HelloService.class);
        try {
            String res1 = proxyService.sayHello("yury757-1");
            String res2 = proxyService.sayHello("yury757-2");
            String res3 = proxyService.sayHello("yury757-3");
            String res4 = proxyService.sayHello("yury757-4");
            String res5 = proxyService.sayHello("yury757-5");
            ComplicateClass testComplicateReturnType = proxyService.testComplicateReturnType();
            System.out.println(res1);
            System.out.println(res2);
            System.out.println(res3);
            System.out.println(res4);
            System.out.println(res5);
            System.out.println(testComplicateReturnType);
            System.out.println(proxyService.addNewMethod());
            proxyService.testError();
        }finally {
            client.close();
        }
    }

    public <T> T getProxyService(Class<T> clazz) {
        long id = REQUEST_ID.getAndIncrement();
        @SuppressWarnings("unchecked")
        T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            RpcRequestMessage request = new RpcRequestMessage(
                    id,
                    clazz.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);
            Promise<Object> promise = send(request);
            promise.await();
            Object res;
            if (promise.isSuccess()) {
                Object o = promise.getNow();
                Class<?> returnType = method.getReturnType();
                if (o instanceof String) {
                    if (String.class.equals(returnType)) {
                        return o;
                    }else {
                        res = MAPPER.readValue((String)o, returnType);
                    }
                }else if (o instanceof JsonNode) {
                    JsonNode node = (JsonNode) o;
                    res = MAPPER.treeToValue(node, returnType);
                }else if (o instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>)o;
                    ObjectNode node = MAPPER.valueToTree(map);
                    res = MAPPER.treeToValue(node, returnType);
                }else {
                    throw new RuntimeException("unrecognized type " + o.getClass());
                }
                return res;
            }else {
                throw new RuntimeException(promise.cause());
            }
        });
        return t;
    }

    public Promise<Object> send(RpcRequestMessage request) {
        try {
            channel.writeAndFlush(request).addListener(future -> {
                if (!future.isSuccess()) {
                    throw new RuntimeException(future.cause());
                }
            }).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Promise<Object> promise = new DefaultPromise<>(channel.eventLoop());
        RpcClientHandlerInitializer.PROMISE_MAP.put(request.getSequenceID(), promise);
        return promise;
    }

    public void close() {
        try {
            channel.close().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
